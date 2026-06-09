package dev.tr7zw.entityculling.versionless.tree64;

import dev.tr7zw.entityculling.versionless.util.MathUtilities;
import dev.tr7zw.entityculling.versionless.util.Vec3d;

public final class Tree64RayMarcher {

    private static final double EPSILON = 1e-7;
    private static final int MAX_STEPS = 384;
    private static final double NEAR_CAMERA_CONSERVATIVE_T = 0.08;
    private static final double BOUNDARY_NUDGE = 1.0;

    private final Tree64World world;

    public Tree64RayMarcher(Tree64World world) {
        this.world = world;
    }

    public boolean isVisible(Vec3d start, Vec3d target, int[] blockedVoxelOut) {
        double dirX = target.x - start.x;
        double dirY = target.y - start.y;
        double dirZ = target.z - start.z;

        double lengthSq = dirX * dirX + dirY * dirY + dirZ * dirZ;
        if (lengthSq <= EPSILON) {
            return true;
        }

        double t = 0.0;
        boolean allowWallClipping = true;

        for (int i = 0; i < MAX_STEPS && t <= 1.0; i++) {
            if (t >= 1.0 - EPSILON) {
                return true;
            }

            double posX = start.x + dirX * t;
            double posY = start.y + dirY * t;
            double posZ = start.z + dirZ * t;

            int voxelX = MathUtilities.floor(posX);
            int voxelY = MathUtilities.floor(posY);
            int voxelZ = MathUtilities.floor(posZ);

            int sample = world.sample(voxelX, voxelY, voxelZ);

            boolean opaque = sample == Tree64World.SAMPLE_OPAQUE;
            if (opaque) {
                if (!allowWallClipping) {
                    if (blockedVoxelOut != null) {
                        blockedVoxelOut[0] = voxelX;
                        blockedVoxelOut[1] = voxelY;
                        blockedVoxelOut[2] = voxelZ;
                    }
                    return false;
                }
            } else {
                allowWallClipping = false;
            }

            int stepSize;
            if (t < NEAR_CAMERA_CONSERVATIVE_T) {
                stepSize = 1;
            } else if (sample == Tree64World.SAMPLE_AIR_STEP16) {
                stepSize = 16;
            } else if (sample == Tree64World.SAMPLE_AIR_STEP4) {
                stepSize = 4;
            } else {
                stepSize = 1;
            }

            int cellMinX = (stepSize == 1) ? voxelX : Math.floorDiv(voxelX, stepSize) * stepSize;
            int cellMinY = (stepSize == 1) ? voxelY : Math.floorDiv(voxelY, stepSize) * stepSize;
            int cellMinZ = (stepSize == 1) ? voxelZ : Math.floorDiv(voxelZ, stepSize) * stepSize;
            int cellMaxX = cellMinX + stepSize;
            int cellMaxY = cellMinY + stepSize;
            int cellMaxZ = cellMinZ + stepSize;

            double tx = boundaryT(start.x, posX, dirX, cellMinX, cellMaxX);
            double ty = boundaryT(start.y, posY, dirY, cellMinY, cellMaxY);
            double tz = boundaryT(start.z, posZ, dirZ, cellMinZ, cellMaxZ);

            double nextT = Math.min(tx, Math.min(ty, tz));
            boolean tieX = Math.abs(tx - nextT) <= EPSILON;
            boolean tieY = Math.abs(ty - nextT) <= EPSILON;
            boolean tieZ = Math.abs(tz - nextT) <= EPSILON;
            int tieAxes = (tieX ? 1 : 0) + (tieY ? 1 : 0) + (tieZ ? 1 : 0);

            if (!allowWallClipping && tieAxes >= 2) {
                int tieResult = checkTieOccluders(voxelX, voxelY, voxelZ, dirX, dirY, dirZ, tieX, tieY, tieZ,
                        blockedVoxelOut);
                if (tieResult == Tree64World.SAMPLE_OPAQUE) {
                    return false;
                }
            }

            nextT += 1e-5;

            if (nextT <= t + EPSILON) {
                nextT = t + 1e-6;
            }
            t = nextT;
        }

        return true;
    }

    private int checkTieOccluders(int voxelX, int voxelY, int voxelZ, double dirX, double dirY, double dirZ,
            boolean tieX, boolean tieY, boolean tieZ, int[] blockedVoxelOut) {
        int sx = dirX >= 0.0 ? 1 : -1;
        int sy = dirY >= 0.0 ? 1 : -1;
        int sz = dirZ >= 0.0 ? 1 : -1;

        if (tieX) {
            int result = sampleTieVoxel(voxelX + sx, voxelY, voxelZ, blockedVoxelOut);
            if (result != Tree64World.SAMPLE_AIR) {
                return result;
            }
        }
        if (tieY) {
            int result = sampleTieVoxel(voxelX, voxelY + sy, voxelZ, blockedVoxelOut);
            if (result != Tree64World.SAMPLE_AIR) {
                return result;
            }
        }
        if (tieZ) {
            int result = sampleTieVoxel(voxelX, voxelY, voxelZ + sz, blockedVoxelOut);
            if (result != Tree64World.SAMPLE_AIR) {
                return result;
            }
        }

        if (tieX && tieY) {
            int result = sampleTieVoxel(voxelX + sx, voxelY + sy, voxelZ, blockedVoxelOut);
            if (result != Tree64World.SAMPLE_AIR) {
                return result;
            }
        }
        if (tieX && tieZ) {
            int result = sampleTieVoxel(voxelX + sx, voxelY, voxelZ + sz, blockedVoxelOut);
            if (result != Tree64World.SAMPLE_AIR) {
                return result;
            }
        }
        if (tieY && tieZ) {
            int result = sampleTieVoxel(voxelX, voxelY + sy, voxelZ + sz, blockedVoxelOut);
            if (result != Tree64World.SAMPLE_AIR) {
                return result;
            }
        }
        if (tieX && tieY && tieZ) {
            int result = sampleTieVoxel(voxelX + sx, voxelY + sy, voxelZ + sz, blockedVoxelOut);
            if (result != Tree64World.SAMPLE_AIR) {
                return result;
            }
        }

        return Tree64World.SAMPLE_AIR;
    }

    private int sampleTieVoxel(int x, int y, int z, int[] blockedVoxelOut) {
        int sample = world.sample(x, y, z);
        if (sample == Tree64World.SAMPLE_OPAQUE) {
            if (blockedVoxelOut != null) {
                blockedVoxelOut[0] = x;
                blockedVoxelOut[1] = y;
                blockedVoxelOut[2] = z;
            }
            return sample;
        }
        return Tree64World.SAMPLE_AIR;
    }

    private static double boundaryT(double startAxis, double posAxis, double dirAxis, int cellMin, int cellMax) {
        if (Math.abs(dirAxis) <= EPSILON) {
            return Double.POSITIVE_INFINITY;
        }

        double boundary = dirAxis > 0.0 ? cellMax : cellMin;
        if (Math.abs(boundary - posAxis) <= EPSILON) {
            boundary += Math.copySign(BOUNDARY_NUDGE, dirAxis);
        }

        return (boundary - startAxis) / dirAxis;
    }

}
