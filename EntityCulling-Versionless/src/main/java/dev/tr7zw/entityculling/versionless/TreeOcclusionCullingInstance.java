package dev.tr7zw.entityculling.versionless;

import java.util.Arrays;

import dev.tr7zw.entityculling.versionless.access.*;
import dev.tr7zw.entityculling.versionless.cache.SparseVisibilityCache;
import dev.tr7zw.entityculling.versionless.tree64.Tree64RayMarcher;
import dev.tr7zw.entityculling.versionless.tree64.Tree64World;
import dev.tr7zw.entityculling.versionless.util.MathUtilities;
import dev.tr7zw.entityculling.versionless.util.Vec3d;

public class TreeOcclusionCullingInstance implements IOcclusionCullingInstance {

    private static final int ON_MIN_X = 0x01;
    private static final int ON_MAX_X = 0x02;
    private static final int ON_MIN_Y = 0x04;
    private static final int ON_MAX_Y = 0x08;
    private static final int ON_MIN_Z = 0x10;
    private static final int ON_MAX_Z = 0x20;
    private static final int BLOCK_MEMO_SIZE = 6;

    private final int reach;
    private final double aabbExpansion;
    private final DataProvider provider;
    private final SparseVisibilityCache visibilityCache = new SparseVisibilityCache();
    private final Tree64World tree64World;
    private final Tree64RayMarcher tree64RayMarcher;
    private long nextProfilePrintNs = 0L;

    private final Vec3d[] targetPoints = new Vec3d[15];
    private final Vec3d targetPos = new Vec3d(0, 0, 0);
    private final int[] cameraPos = new int[3];
    private final boolean[] dotselectors = new boolean[14];
    private final int[] lastHitBlock = new int[3];
    private final int[][] blockedMemo = new int[BLOCK_MEMO_SIZE][3];
    private int blockedMemoCount = 0;
    private int blockedMemoWriteIndex = 0;
    private final Vec3d tempRay = new Vec3d(0, 0, 0);
    private boolean allowRayChecks = false;

    public TreeOcclusionCullingInstance(int maxDistance, DataProvider provider, double aabbExpansion) {
        this.reach = maxDistance;
        this.provider = provider;
        this.tree64World = new Tree64World(provider);
        this.tree64RayMarcher = new Tree64RayMarcher(tree64World);
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

            int viewerX = MathUtilities.floor(viewerPosition.x);
            int viewerY = MathUtilities.floor(viewerPosition.y);
            int viewerZ = MathUtilities.floor(viewerPosition.z);

            cameraPos[0] = viewerX;
            cameraPos[1] = viewerY;
            cameraPos[2] = viewerZ;

            Relative relX = Relative.from(minX, maxX, viewerX);
            Relative relY = Relative.from(minY, maxY, viewerY);
            Relative relZ = Relative.from(minZ, maxZ, viewerZ);

            if (relX == Relative.INSIDE && relY == Relative.INSIDE && relZ == Relative.INSIDE) {
                return true;
            }

            allowRayChecks = false;

            for (int x = minX; x <= maxX; x++) {
                boolean onMinX = x == minX;
                boolean onMaxX = x == maxX;
                boolean visibleMinX = onMinX && relX == Relative.POSITIVE;
                boolean visibleMaxX = onMaxX && relX == Relative.NEGATIVE;

                byte faceEdgeDataX = 0;
                if (onMinX) {
                    faceEdgeDataX |= ON_MIN_X;
                }
                if (onMaxX) {
                    faceEdgeDataX |= ON_MAX_X;
                }

                byte visibleOnFaceX = 0;
                if (visibleMinX) {
                    visibleOnFaceX |= ON_MIN_X;
                }
                if (visibleMaxX) {
                    visibleOnFaceX |= ON_MAX_X;
                }

                for (int y = minY; y <= maxY; y++) {
                    boolean onMinY = y == minY;
                    boolean onMaxY = y == maxY;
                    boolean visibleMinY = onMinY && relY == Relative.POSITIVE;
                    boolean visibleMaxY = onMaxY && relY == Relative.NEGATIVE;

                    byte faceEdgeDataY = faceEdgeDataX;
                    if (onMinY) {
                        faceEdgeDataY |= ON_MIN_Y;
                    }
                    if (onMaxY) {
                        faceEdgeDataY |= ON_MAX_Y;
                    }

                    byte visibleOnFaceY = visibleOnFaceX;
                    if (visibleMinY) {
                        visibleOnFaceY |= ON_MIN_Y;
                    }
                    if (visibleMaxY) {
                        visibleOnFaceY |= ON_MAX_Y;
                    }

                    for (int z = minZ; z <= maxZ; z++) {
                        boolean onMinZ = z == minZ;
                        boolean onMaxZ = z == maxZ;
                        boolean visibleMinZ = onMinZ && relZ == Relative.POSITIVE;
                        boolean visibleMaxZ = onMaxZ && relZ == Relative.NEGATIVE;

                        if (!(visibleMinX || visibleMaxX || visibleMinY || visibleMaxY || visibleMinZ || visibleMaxZ)) {
                            continue;
                        }

                        int cachedValue = getCacheValue(x, y, z);
                        if (cachedValue == 1) {
                            return true;
                        }
                        if (cachedValue != 0) {
                            continue;
                        }

                        byte faceEdgeData = faceEdgeDataY;
                        if (onMinZ) {
                            faceEdgeData |= ON_MIN_Z;
                        }
                        if (onMaxZ) {
                            faceEdgeData |= ON_MAX_Z;
                        }

                        byte visibleOnFace = visibleOnFaceY;
                        if (visibleMinZ) {
                            visibleOnFace |= ON_MIN_Z;
                        }
                        if (visibleMaxZ) {
                            visibleOnFace |= ON_MAX_Z;
                        }

                        targetPos.set(x, y, z);
                        if (isVoxelVisible(viewerPosition, targetPos, faceEdgeData, visibleOnFace)) {
                            return true;
                        }
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
        for (int v = 0; v < size; v++) {
            Vec3d target = targets[v];

            if (allowRayChecks) {
                tempRay.set(start.x - target.getX(), start.y - target.getY(), start.z - target.getZ());
                tempRay.normalize();
                if (intersectsBlockedMemo(start, tempRay)) {
                    continue;
                }
            }

            if (tree64RayMarcher.isVisible(start, target, lastHitBlock)) {
                provider.cleanup();
                cacheResult(targets[0], true);
                return true;
            }

            rememberBlocked(lastHitBlock);

            allowRayChecks = true;
        }
        provider.cleanup();
        cacheResult(targets[0], false);
        return false;
    }

    private void cacheResult(Vec3d vector, boolean result) {
        int cx = MathUtilities.floor(vector.x);
        int cy = MathUtilities.floor(vector.y);
        int cz = MathUtilities.floor(vector.z);
        if (Math.abs(cx - cameraPos[0]) > reach - 2 || Math.abs(cy - cameraPos[1]) > reach - 2
                || Math.abs(cz - cameraPos[2]) > reach - 2) {
            return;
        }
        if (result) {
            visibilityCache.setVisible(cx, cy, cz);
        } else {
            visibilityCache.setHidden(cx, cy, cz);
        }
    }

    private int getCacheValue(int x, int y, int z) {
        if (Math.abs(x - cameraPos[0]) > reach - 2 || Math.abs(y - cameraPos[1]) > reach - 2
                || Math.abs(z - cameraPos[2]) > reach - 2) {
            return -1;
        }
        return visibilityCache.getState(x, y, z);
    }

    private void rememberBlocked(int[] block) {
        int[] slot = blockedMemo[blockedMemoWriteIndex];
        slot[0] = block[0];
        slot[1] = block[1];
        slot[2] = block[2];

        blockedMemoWriteIndex = (blockedMemoWriteIndex + 1) % BLOCK_MEMO_SIZE;
        if (blockedMemoCount < BLOCK_MEMO_SIZE) {
            blockedMemoCount++;
        }
    }

    private boolean intersectsBlockedMemo(Vec3d start, Vec3d normalizedRay) {
        for (int i = 0; i < blockedMemoCount; i++) {
            if (MathUtilities.rayIntersection(blockedMemo[i], start, normalizedRay)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void resetCache() {
        this.visibilityCache.clear();
        this.tree64World.clear();
    }

    @Override
    public String getImplementationName() {
        return "Tree64";
    }

    @Override
    public boolean isUsingTree64() {
        return true;
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
