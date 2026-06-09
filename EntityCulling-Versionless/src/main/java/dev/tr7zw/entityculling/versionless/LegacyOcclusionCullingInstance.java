package dev.tr7zw.entityculling.versionless;

import java.util.Arrays;
import java.util.BitSet;

import dev.tr7zw.entityculling.versionless.access.*;
import dev.tr7zw.entityculling.versionless.cache.ArrayOcclusionCache;
import dev.tr7zw.entityculling.versionless.cache.OcclusionCache;
import dev.tr7zw.entityculling.versionless.util.MathUtilities;
import dev.tr7zw.entityculling.versionless.util.Vec3d;

public class LegacyOcclusionCullingInstance implements IOcclusionCullingInstance {

    private static final int ON_MIN_X = 0x01;
    private static final int ON_MAX_X = 0x02;
    private static final int ON_MIN_Y = 0x04;
    private static final int ON_MAX_Y = 0x08;
    private static final int ON_MIN_Z = 0x10;
    private static final int ON_MAX_Z = 0x20;

    private final int reach;
    private final double aabbExpansion;
    private final DataProvider provider;
    private final OcclusionCache cache;

    private final BitSet skipList = new BitSet();
    private final Vec3d[] targetPoints = new Vec3d[15];
    private final Vec3d targetPos = new Vec3d(0, 0, 0);
    private final int[] cameraPos = new int[3];
    private final boolean[] dotselectors = new boolean[14];
    private boolean allowRayChecks = false;
    private final int[] lastHitBlock = new int[3];
    private boolean allowWallClipping = false;

    public LegacyOcclusionCullingInstance(int maxDistance, DataProvider provider) {
        this(maxDistance, provider, new ArrayOcclusionCache(maxDistance), 0.5);
    }

    public LegacyOcclusionCullingInstance(int maxDistance, DataProvider provider, OcclusionCache cache,
            double aabbExpansion) {
        this.reach = maxDistance;
        this.provider = provider;
        this.cache = cache;
        this.aabbExpansion = aabbExpansion;
        for (int i = 0; i < targetPoints.length; i++) {
            targetPoints[i] = new Vec3d(0, 0, 0);
        }
    }

    @Override
    public boolean isAABBVisible(Vec3d aabbMin, Vec3d aabbMax, Vec3d viewerPosition) {
        try {
            int maxX = MathUtilities.floor(aabbMax.x + aabbExpansion);
            int maxY = MathUtilities.floor(aabbMax.y + aabbExpansion);
            int maxZ = MathUtilities.floor(aabbMax.z + aabbExpansion);
            int minX = MathUtilities.floor(aabbMin.x - aabbExpansion);
            int minY = MathUtilities.floor(aabbMin.y - aabbExpansion);
            int minZ = MathUtilities.floor(aabbMin.z - aabbExpansion);

            cameraPos[0] = MathUtilities.floor(viewerPosition.x);
            cameraPos[1] = MathUtilities.floor(viewerPosition.y);
            cameraPos[2] = MathUtilities.floor(viewerPosition.z);

            Relative relX = Relative.from(minX, maxX, cameraPos[0]);
            Relative relY = Relative.from(minY, maxY, cameraPos[1]);
            Relative relZ = Relative.from(minZ, maxZ, cameraPos[2]);

            if (relX == Relative.INSIDE && relY == Relative.INSIDE && relZ == Relative.INSIDE) {
                return true;
            }

            skipList.clear();

            int id = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        int cachedValue = getCacheValue(x, y, z);

                        if (cachedValue == 1) {
                            return true;
                        }

                        if (cachedValue != 0) {
                            skipList.set(id);
                        }
                        id++;
                    }
                }
            }

            allowRayChecks = false;

            id = 0;
            for (int x = minX; x <= maxX; x++) {
                byte visibleOnFaceX = 0;
                byte faceEdgeDataX = 0;
                faceEdgeDataX |= (x == minX) ? ON_MIN_X : 0;
                faceEdgeDataX |= (x == maxX) ? ON_MAX_X : 0;
                visibleOnFaceX |= (x == minX && relX == Relative.POSITIVE) ? ON_MIN_X : 0;
                visibleOnFaceX |= (x == maxX && relX == Relative.NEGATIVE) ? ON_MAX_X : 0;
                for (int y = minY; y <= maxY; y++) {
                    byte faceEdgeDataY = faceEdgeDataX;
                    byte visibleOnFaceY = visibleOnFaceX;
                    faceEdgeDataY |= (y == minY) ? ON_MIN_Y : 0;
                    faceEdgeDataY |= (y == maxY) ? ON_MAX_Y : 0;
                    visibleOnFaceY |= (y == minY && relY == Relative.POSITIVE) ? ON_MIN_Y : 0;
                    visibleOnFaceY |= (y == maxY && relY == Relative.NEGATIVE) ? ON_MAX_Y : 0;
                    for (int z = minZ; z <= maxZ; z++) {
                        byte faceEdgeData = faceEdgeDataY;
                        byte visibleOnFace = visibleOnFaceY;
                        faceEdgeData |= (z == minZ) ? ON_MIN_Z : 0;
                        faceEdgeData |= (z == maxZ) ? ON_MAX_Z : 0;
                        visibleOnFace |= (z == minZ && relZ == Relative.POSITIVE) ? ON_MIN_Z : 0;
                        visibleOnFace |= (z == maxZ && relZ == Relative.NEGATIVE) ? ON_MAX_Z : 0;
                        if (skipList.get(id)) {
                            id++;
                            continue;
                        }

                        if (visibleOnFace != 0) {
                            targetPos.set(x, y, z);
                            if (isVoxelVisible(viewerPosition, targetPos, faceEdgeData, visibleOnFace)) {
                                return true;
                            }
                        }
                        id++;
                    }
                }
            }

            return false;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
    }

    private boolean isVoxelVisible(Vec3d viewerPosition, Vec3d position, byte faceData, byte visibleOnFace) {
        int targetSize = 0;
        Arrays.fill(dotselectors, false);
        if ((visibleOnFace & ON_MIN_X) == ON_MIN_X) {
            dotselectors[0] = true;
            if ((faceData & ~ON_MIN_X) != 0) {
                dotselectors[1] = true;
                dotselectors[4] = true;
                dotselectors[5] = true;
            }
            dotselectors[8] = true;
        }
        if ((visibleOnFace & ON_MIN_Y) == ON_MIN_Y) {
            dotselectors[0] = true;
            if ((faceData & ~ON_MIN_Y) != 0) {
                dotselectors[3] = true;
                dotselectors[4] = true;
                dotselectors[7] = true;
            }
            dotselectors[9] = true;
        }
        if ((visibleOnFace & ON_MIN_Z) == ON_MIN_Z) {
            dotselectors[0] = true;
            if ((faceData & ~ON_MIN_Z) != 0) {
                dotselectors[1] = true;
                dotselectors[4] = true;
                dotselectors[5] = true;
            }
            dotselectors[10] = true;
        }
        if ((visibleOnFace & ON_MAX_X) == ON_MAX_X) {
            dotselectors[4] = true;
            if ((faceData & ~ON_MAX_X) != 0) {
                dotselectors[5] = true;
                dotselectors[6] = true;
                dotselectors[7] = true;
            }
            dotselectors[11] = true;
        }
        if ((visibleOnFace & ON_MAX_Y) == ON_MAX_Y) {
            dotselectors[1] = true;
            if ((faceData & ~ON_MAX_Y) != 0) {
                dotselectors[2] = true;
                dotselectors[5] = true;
                dotselectors[6] = true;
            }
            dotselectors[12] = true;
        }
        if ((visibleOnFace & ON_MAX_Z) == ON_MAX_Z) {
            dotselectors[2] = true;
            if ((faceData & ~ON_MAX_Z) != 0) {
                dotselectors[3] = true;
                dotselectors[6] = true;
                dotselectors[7] = true;
            }
            dotselectors[13] = true;
        }

        if (dotselectors[0])
            targetPoints[targetSize++].setAdd(position, 0.05, 0.05, 0.05);
        if (dotselectors[1])
            targetPoints[targetSize++].setAdd(position, 0.05, 0.95, 0.05);
        if (dotselectors[2])
            targetPoints[targetSize++].setAdd(position, 0.05, 0.95, 0.95);
        if (dotselectors[3])
            targetPoints[targetSize++].setAdd(position, 0.05, 0.05, 0.95);
        if (dotselectors[4])
            targetPoints[targetSize++].setAdd(position, 0.95, 0.05, 0.05);
        if (dotselectors[5])
            targetPoints[targetSize++].setAdd(position, 0.95, 0.95, 0.05);
        if (dotselectors[6])
            targetPoints[targetSize++].setAdd(position, 0.95, 0.95, 0.95);
        if (dotselectors[7])
            targetPoints[targetSize++].setAdd(position, 0.95, 0.05, 0.95);
        if (dotselectors[8])
            targetPoints[targetSize++].setAdd(position, 0.05, 0.5, 0.5);
        if (dotselectors[9])
            targetPoints[targetSize++].setAdd(position, 0.5, 0.05, 0.5);
        if (dotselectors[10])
            targetPoints[targetSize++].setAdd(position, 0.5, 0.5, 0.05);
        if (dotselectors[11])
            targetPoints[targetSize++].setAdd(position, 0.95, 0.5, 0.5);
        if (dotselectors[12])
            targetPoints[targetSize++].setAdd(position, 0.5, 0.95, 0.5);
        if (dotselectors[13])
            targetPoints[targetSize++].setAdd(position, 0.5, 0.5, 0.95);

        return isVisible(viewerPosition, targetPoints, targetSize);
    }

    private boolean isVisible(Vec3d start, Vec3d[] targets, int size) {
        int x = cameraPos[0];
        int y = cameraPos[1];
        int z = cameraPos[2];

        for (int v = 0; v < size; v++) {
            Vec3d target = targets[v];

            double relativeX = start.x - target.getX();
            double relativeY = start.y - target.getY();
            double relativeZ = start.z - target.getZ();

            if (allowRayChecks && MathUtilities.rayIntersection(lastHitBlock, start,
                    new Vec3d(relativeX, relativeY, relativeZ).normalize())) {
                continue;
            }

            double dimensionX = Math.abs(relativeX);
            double dimensionY = Math.abs(relativeY);
            double dimensionZ = Math.abs(relativeZ);

            double dimFracX = 1f / dimensionX;
            double dimFracY = 1f / dimensionY;
            double dimFracZ = 1f / dimensionZ;

            int intersectCount = 1;

            int x_inc, y_inc, z_inc;
            double t_next_y, t_next_x, t_next_z;

            if (dimensionX == 0f) {
                x_inc = 0;
                t_next_x = dimFracX;
            } else if (target.x > start.x) {
                x_inc = 1;
                intersectCount += MathUtilities.floor(target.x) - x;
                t_next_x = (float) ((x + 1 - start.x) * dimFracX);
            } else {
                x_inc = -1;
                intersectCount += x - MathUtilities.floor(target.x);
                t_next_x = (float) ((start.x - x) * dimFracX);
            }

            if (dimensionY == 0f) {
                y_inc = 0;
                t_next_y = dimFracY;
            } else if (target.y > start.y) {
                y_inc = 1;
                intersectCount += MathUtilities.floor(target.y) - y;
                t_next_y = (float) ((y + 1 - start.y) * dimFracY);
            } else {
                y_inc = -1;
                intersectCount += y - MathUtilities.floor(target.y);
                t_next_y = (float) ((start.y - y) * dimFracY);
            }

            if (dimensionZ == 0f) {
                z_inc = 0;
                t_next_z = dimFracZ;
            } else if (target.z > start.z) {
                z_inc = 1;
                intersectCount += MathUtilities.floor(target.z) - z;
                t_next_z = (float) ((z + 1 - start.z) * dimFracZ);
            } else {
                z_inc = -1;
                intersectCount += z - MathUtilities.floor(target.z);
                t_next_z = (float) ((start.z - z) * dimFracZ);
            }

            boolean finished = stepRay(start, x, y, z, dimFracX, dimFracY, dimFracZ, intersectCount, x_inc, y_inc,
                    z_inc, t_next_y, t_next_x, t_next_z);
            provider.cleanup();
            if (finished) {
                cacheResult(targets[0], true);
                return true;
            } else {
                allowRayChecks = true;
            }
        }
        cacheResult(targets[0], false);
        return false;
    }

    private boolean stepRay(Vec3d start, int currentX, int currentY, int currentZ, double distInX, double distInY,
            double distInZ, int n, int x_inc, int y_inc, int z_inc, double t_next_y, double t_next_x, double t_next_z) {
        allowWallClipping = true;
        for (; n > 1; n--) {
            int cVal = getCacheValue(currentX, currentY, currentZ);

            if (cVal == 2 && !allowWallClipping) {
                lastHitBlock[0] = currentX;
                lastHitBlock[1] = currentY;
                lastHitBlock[2] = currentZ;
                return false;
            }

            if (cVal == 0) {
                int chunkX = currentX >> 4;
                int chunkZ = currentZ >> 4;
                if (!provider.prepareChunk(chunkX, chunkZ, currentY)) {
                    return false;
                }

                if (provider.isOpaqueFullCube(currentX, currentY, currentZ)) {
                    if (!allowWallClipping) {
                        cache.setLastHidden();
                        lastHitBlock[0] = currentX;
                        lastHitBlock[1] = currentY;
                        lastHitBlock[2] = currentZ;
                        return false;
                    }
                } else {
                    allowWallClipping = false;
                    cache.setLastVisible();
                }
            }

            if (cVal == 1) {
                allowWallClipping = false;
            }

            if (t_next_y < t_next_x && t_next_y < t_next_z) {
                currentY += y_inc;
                t_next_y += distInY;
            } else if (t_next_x < t_next_y && t_next_x < t_next_z) {
                currentX += x_inc;
                t_next_x += distInX;
            } else {
                currentZ += z_inc;
                t_next_z += distInZ;
            }

        }
        return true;
    }

    private int getCacheValue(int x, int y, int z) {
        x -= cameraPos[0];
        y -= cameraPos[1];
        z -= cameraPos[2];
        if (Math.abs(x) > reach - 2 || Math.abs(y) > reach - 2 || Math.abs(z) > reach - 2) {
            return -1;
        }

        return cache.getState(x + reach, y + reach, z + reach);
    }

    private void cacheResult(Vec3d vector, boolean result) {
        int cx = MathUtilities.floor(vector.x) - cameraPos[0] + reach;
        int cy = MathUtilities.floor(vector.y) - cameraPos[1] + reach;
        int cz = MathUtilities.floor(vector.z) - cameraPos[2] + reach;
        if (result) {
            cache.setVisible(cx, cy, cz);
        } else {
            cache.setHidden(cx, cy, cz);
        }
    }

    @Override
    public void resetCache() {
        this.cache.resetCache();
    }

    @Override
    public String getImplementationName() {
        return "Legacy";
    }

    @Override
    public boolean isUsingTree64() {
        return false;
    }

    @Override
    public void setUseTree64(boolean useTree64) {
        // Fixed implementation; switching is handled by the delegating wrapper.
    }

    private enum Relative {
        INSIDE, POSITIVE, NEGATIVE;

        public static Relative from(int min, int max, int pos) {
            if (max > pos && min > pos) {
                return POSITIVE;
            } else if (min < pos && max < pos) {
                return NEGATIVE;
            }
            return INSIDE;
        }
    }

}
