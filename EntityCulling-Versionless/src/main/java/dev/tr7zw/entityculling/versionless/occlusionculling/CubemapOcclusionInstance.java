package dev.tr7zw.entityculling.versionless.occlusionculling;

import dev.tr7zw.entityculling.versionless.occlusionculling.cache.OcclusionCache;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.BrickProvider;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.MathUtilities;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.Vec3d;

import java.util.Arrays;

public class CubemapOcclusionInstance implements OcclusionCulling {

    private static final int RES = 32;
    private static final int TOTAL_PIXELS = 6 * RES * RES;

    private final double[] preDirX = new double[TOTAL_PIXELS];
    private final double[] preDirY = new double[TOTAL_PIXELS];
    private final double[] preDirZ = new double[TOTAL_PIXELS];

    private final double[] preDimFracX = new double[TOTAL_PIXELS];
    private final double[] preDimFracY = new double[TOTAL_PIXELS];
    private final double[] preDimFracZ = new double[TOTAL_PIXELS];
    private final int[] preIncX = new int[TOTAL_PIXELS];
    private final int[] preIncY = new int[TOTAL_PIXELS];
    private final int[] preIncZ = new int[TOTAL_PIXELS];

    private final int reach;
    private final double aabbExpansion;
    private final BrickProvider provider;

    private final float[] depthBuffer = new float[TOTAL_PIXELS];
    private final int[] camBlockPos = new int[3];

    // Zero-allocation entity result cache
    private static final int LOCAL_CACHE_MASK = 1023; // 1,024 slots (~10 KB)
    private final long[] localCacheKeys = new long[LOCAL_CACHE_MASK + 1];
    private final byte[] localCacheValues = new byte[LOCAL_CACHE_MASK + 1]; // 0=empty, 1=visible, 2=hidden

    private double lastCamX = Double.NaN;
    private double lastCamY = Double.NaN;
    private double lastCamZ = Double.NaN;
    private boolean needsRebake = true;

    public CubemapOcclusionInstance(int maxDistance, BrickProvider provider) {
        this(maxDistance, provider, 0.5);
    }

    public CubemapOcclusionInstance(int maxDistance, BrickProvider provider, OcclusionCache cache) {
        this(maxDistance, provider, 0.5);
    }

    public CubemapOcclusionInstance(int maxDistance, BrickProvider provider, OcclusionCache cache, double aabbExpansion) {
        this(maxDistance, provider, aabbExpansion);
    }

    public CubemapOcclusionInstance(int maxDistance, BrickProvider provider, double aabbExpansion) {
        this.reach = maxDistance;
        this.provider = provider;
        this.aabbExpansion = aabbExpansion;
        Arrays.fill(localCacheKeys, Long.MIN_VALUE);
        initRayDirections();
    }

    private void initRayDirections() {
        double invRes = 2.0 / RES;
        int idx = 0;

        for (int face = 0; face < 6; face++) {
            for (int py = 0; py < RES; py++) {
                double ny = 1.0 - (py + 0.5) * invRes;
                for (int px = 0; px < RES; px++) {
                    double nx = (px + 0.5) * invRes - 1.0;

                    double x = 0, y = 0, z = 0;
                    switch (face) {
                        case 0 -> { x =  1.0; y =  ny;  z = -nx; } // +X
                        case 1 -> { x = -1.0; y =  ny;  z =  nx; } // -X
                        case 2 -> { x =  nx;  y =  1.0; z =  ny; } // +Y
                        case 3 -> { x =  nx;  y = -1.0; z = -ny; } // -Y
                        case 4 -> { x =  nx;  y =  ny;  z =  1.0; } // +Z
                        case 5 -> { x = -nx;  y =  ny;  z = -1.0; } // -Z
                    }

                    double len = Math.sqrt(x * x + y * y + z * z);
                    double dx = x / len;
                    double dy = y / len;
                    double dz = z / len;

                    preDirX[idx] = dx;
                    preDirY[idx] = dy;
                    preDirZ[idx] = dz;

                    preDimFracX[idx] = dx == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dx);
                    preDimFracY[idx] = dy == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dy);
                    preDimFracZ[idx] = dz == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dz);

                    preIncX[idx] = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
                    preIncY[idx] = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
                    preIncZ[idx] = dz > 0 ? 1 : (dz < 0 ? -1 : 0);

                    idx++;
                }
            }
        }
    }

    public void resetCache() {
        this.needsRebake = true;
        Arrays.fill(localCacheKeys, Long.MIN_VALUE);
    }

    public void prepareFrame(Vec3d cameraPos) {
        this.lastCamX = cameraPos.x;
        this.lastCamY = cameraPos.y;
        this.lastCamZ = cameraPos.z;
        this.needsRebake = false;

        this.camBlockPos[0] = MathUtilities.floor(cameraPos.x);
        this.camBlockPos[1] = MathUtilities.floor(cameraPos.y);
        this.camBlockPos[2] = MathUtilities.floor(cameraPos.z);

        Arrays.fill(depthBuffer, (float) reach);
        Arrays.fill(localCacheKeys, Long.MIN_VALUE);

        for (int i = 0; i < TOTAL_PIXELS; i++) {
            depthBuffer[i] = (float) traceSingleRay(i, cameraPos, camBlockPos[0], camBlockPos[1], camBlockPos[2]);
        }
    }

    private double traceSingleRay(int rayIdx, Vec3d start, int x, int y, int z) {
        double dirX = preDirX[rayIdx];
        double dirY = preDirY[rayIdx];
        double dirZ = preDirZ[rayIdx];

        double dimFracX = preDimFracX[rayIdx];
        double dimFracY = preDimFracY[rayIdx];
        double dimFracZ = preDimFracZ[rayIdx];

        int x_inc = preIncX[rayIdx];
        int y_inc = preIncY[rayIdx];
        int z_inc = preIncZ[rayIdx];

        double t_next_x = x_inc > 0 ? (x + 1 - start.x) * dimFracX : (x_inc < 0 ? (start.x - x) * dimFracX : Double.MAX_VALUE);
        double t_next_y = y_inc > 0 ? (y + 1 - start.y) * dimFracY : (y_inc < 0 ? (start.y - y) * dimFracY : Double.MAX_VALUE);
        double t_next_z = z_inc > 0 ? (z + 1 - start.z) * dimFracZ : (z_inc < 0 ? (start.z - z) * dimFracZ : Double.MAX_VALUE);

        int curChunkX = x >> 4;
        int curChunkY = y >> 4;
        int curChunkZ = z >> 4;
        boolean chunkReady = provider.prepareChunk(curChunkX, curChunkY, curChunkZ);

        double t = 0.0;
        while (t < reach) {
            int chX = x >> 4;
            int chY = y >> 4;
            int chZ = z >> 4;
            if (chX != curChunkX || chY != curChunkY || chZ != curChunkZ) {
                curChunkX = chX;
                curChunkY = chY;
                curChunkZ = chZ;
                chunkReady = provider.prepareChunk(chX, chY, chZ);
            }

            if (!chunkReady) {
                return reach;
            }

            // --- LEVEL 2: 16x16x16 Chunk Skip ---
            if (provider.getVoxelBitmask() == null) {
                int remX = x_inc > 0 ? (16 - (x & 15)) : ((x & 15) + 1);
                int remY = y_inc > 0 ? (16 - (y & 15)) : ((y & 15) + 1);
                int remZ = z_inc > 0 ? (16 - (z & 15)) : ((z & 15) + 1);

                double tExitX = x_inc != 0 ? t_next_x + (remX - 1) * dimFracX : Double.MAX_VALUE;
                double tExitY = y_inc != 0 ? t_next_y + (remY - 1) * dimFracY : Double.MAX_VALUE;
                double tExitZ = z_inc != 0 ? t_next_z + (remZ - 1) * dimFracZ : Double.MAX_VALUE;

                double tExit = Math.min(tExitX, Math.min(tExitY, tExitZ));
                if (tExit >= reach) {
                    return reach;
                }

                // Synchronize all dimensions across the boundary face
                double tAdv = tExit + 1e-6;
                x = MathUtilities.floor(start.x + dirX * tAdv);
                y = MathUtilities.floor(start.y + dirY * tAdv);
                z = MathUtilities.floor(start.z + dirZ * tAdv);

                t_next_x = x_inc > 0 ? (x + 1 - start.x) * dimFracX : (x_inc < 0 ? (start.x - x) * dimFracX : Double.MAX_VALUE);
                t_next_y = y_inc > 0 ? (y + 1 - start.y) * dimFracY : (y_inc < 0 ? (start.y - y) * dimFracY : Double.MAX_VALUE);
                t_next_z = z_inc > 0 ? (z + 1 - start.z) * dimFracZ : (z_inc < 0 ? (start.z - z) * dimFracZ : Double.MAX_VALUE);

                t = tExit;
                continue;
            }

            // --- LEVEL 1: 4x4x4 Brick Skip ---
            if (provider.isBrickEmpty(x, y, z)) {
                int remX = x_inc > 0 ? (4 - (x & 3)) : ((x & 3) + 1);
                int remY = y_inc > 0 ? (4 - (y & 3)) : ((y & 3) + 1);
                int remZ = z_inc > 0 ? (4 - (z & 3)) : ((z & 3) + 1);

                double tExitX = x_inc != 0 ? t_next_x + (remX - 1) * dimFracX : Double.MAX_VALUE;
                double tExitY = y_inc != 0 ? t_next_y + (remY - 1) * dimFracY : Double.MAX_VALUE;
                double tExitZ = z_inc != 0 ? t_next_z + (remZ - 1) * dimFracZ : Double.MAX_VALUE;

                double tExit = Math.min(tExitX, Math.min(tExitY, tExitZ));
                if (tExit >= reach) {
                    return reach;
                }

                // Synchronize all dimensions across the brick boundary
                double tAdv = tExit + 1e-6;
                x = MathUtilities.floor(start.x + dirX * tAdv);
                y = MathUtilities.floor(start.y + dirY * tAdv);
                z = MathUtilities.floor(start.z + dirZ * tAdv);

                t_next_x = x_inc > 0 ? (x + 1 - start.x) * dimFracX : (x_inc < 0 ? (start.x - x) * dimFracX : Double.MAX_VALUE);
                t_next_y = y_inc > 0 ? (y + 1 - start.y) * dimFracY : (y_inc < 0 ? (start.y - y) * dimFracY : Double.MAX_VALUE);
                t_next_z = z_inc > 0 ? (z + 1 - start.z) * dimFracZ : (z_inc < 0 ? (start.z - z) * dimFracZ : Double.MAX_VALUE);

                t = tExit;
                continue;
            }

            // --- LEVEL 0: 1x1x1 Exact Voxel Test ---
            if (provider.isOpaqueFullCube(x, y, z)) {
                return t;
            }

            // Standard 1-block DDA step
            if (t_next_x <= t_next_y && t_next_x <= t_next_z) {
                x += x_inc;
                t = t_next_x;
                t_next_x += dimFracX;
            } else if (t_next_y <= t_next_z) {
                y += y_inc;
                t = t_next_y;
                t_next_y += dimFracY;
            } else {
                z += z_inc;
                t = t_next_z;
                t_next_z += dimFracZ;
            }
        }
        return reach;
    }

    @Override
    public boolean isAABBVisible(Vec3d aabbMin, Vec3d aabbMax, Vec3d viewerPosition) {
        if (needsRebake || viewerPosition.x != lastCamX || viewerPosition.y != lastCamY || viewerPosition.z != lastCamZ) {
            prepareFrame(viewerPosition);
        }

        double minX = aabbMin.x - aabbExpansion;
        double minY = aabbMin.y - aabbExpansion;
        double minZ = aabbMin.z - aabbExpansion;
        double maxX = aabbMax.x + aabbExpansion;
        double maxY = aabbMax.y + aabbExpansion;
        double maxZ = aabbMax.z + aabbExpansion;

        if (viewerPosition.x >= minX && viewerPosition.x <= maxX &&
            viewerPosition.y >= minY && viewerPosition.y <= maxY &&
            viewerPosition.z >= minZ && viewerPosition.z <= maxZ) {
            return true;
        }

        int bMinX = MathUtilities.floor(minX);
        int bMinY = MathUtilities.floor(minY);
        int bMinZ = MathUtilities.floor(minZ);
        int bMaxX = MathUtilities.floor(maxX);
        int bMaxY = MathUtilities.floor(maxY);
        int bMaxZ = MathUtilities.floor(maxZ);

        int midX = (bMinX + bMaxX) >> 1;
        int midY = (bMinY + bMaxY) >> 1;
        int midZ = (bMinZ + bMaxZ) >> 1;
        int dimX = bMaxX - bMinX;
        int dimY = bMaxY - bMinY;
        int dimZ = bMaxZ - bMinZ;

        long key = computeLocalKey(midX, midY, midZ, dimX, dimY, dimZ);
        int idx = (int) (key & LOCAL_CACHE_MASK);
        if (localCacheKeys[idx] == key) {
            byte val = localCacheValues[idx];
            if (val == 1) return true;
            if (val == 2) return false;
        }

        double nearX = Math.max(minX, Math.min(viewerPosition.x, maxX)) - viewerPosition.x;
        double nearY = Math.max(minY, Math.min(viewerPosition.y, maxY)) - viewerPosition.y;
        double nearZ = Math.max(minZ, Math.min(viewerPosition.z, maxZ)) - viewerPosition.z;
        double minDist = Math.sqrt(nearX * nearX + nearY * nearY + nearZ * nearZ);

        double x1 = minX - viewerPosition.x, x2 = maxX - viewerPosition.x;
        double y1 = minY - viewerPosition.y, y2 = maxY - viewerPosition.y;
        double z1 = minZ - viewerPosition.z, z2 = maxZ - viewerPosition.z;

        boolean sampledAnyRay = false;
        double invRes = 2.0 / RES;

        for (int face = 0; face < 6; face++) {
            double minU, maxU, minV, maxV;

            switch (face) {
                case 0 -> { // +X
                    if (x2 <= 0.05) continue;
                    double nx = Math.max(0.05, x1), fx = Math.max(0.05, x2);
                    minU = Math.min(Math.min(-z1 / nx, -z1 / fx), Math.min(-z2 / nx, -z2 / fx));
                    maxU = Math.max(Math.max(-z1 / nx, -z1 / fx), Math.max(-z2 / nx, -z2 / fx));
                    minV = Math.min(Math.min( y1 / nx,  y1 / fx), Math.min( y2 / nx,  y2 / fx));
                    maxV = Math.max(Math.max( y1 / nx,  y1 / fx), Math.max( y2 / nx,  y2 / fx));
                }
                case 1 -> { // -X
                    if (x1 >= -0.05) continue;
                    double nx = Math.max(0.05, -x2), fx = Math.max(0.05, -x1);
                    minU = Math.min(Math.min( z1 / nx,  z1 / fx), Math.min( z2 / nx,  z2 / fx));
                    maxU = Math.max(Math.max( z1 / nx,  z1 / fx), Math.max( z2 / nx,  z2 / fx));
                    minV = Math.min(Math.min( y1 / nx,  y1 / fx), Math.min( y2 / nx,  y2 / fx));
                    maxV = Math.max(Math.max( y1 / nx,  y1 / fx), Math.max( y2 / nx,  y2 / fx));
                }
                case 2 -> { // +Y
                    if (y2 <= 0.05) continue;
                    double ny = Math.max(0.05, y1), fy = Math.max(0.05, y2);
                    minU = Math.min(Math.min( x1 / ny,  x1 / fy), Math.min( x2 / ny,  x2 / fy));
                    maxU = Math.max(Math.max( x1 / ny,  x1 / fy), Math.max( x2 / ny,  x2 / fy));
                    minV = Math.min(Math.min( z1 / ny,  z1 / fy), Math.min( z2 / ny,  z2 / fy));
                    maxV = Math.max(Math.max( z1 / ny,  z1 / fy), Math.max( z2 / ny,  z2 / fy));
                }
                case 3 -> { // -Y
                    if (y1 >= -0.05) continue;
                    double ny = Math.max(0.05, -y2), fy = Math.max(0.05, -y1);
                    minU = Math.min(Math.min( x1 / ny,  x1 / fy), Math.min( x2 / ny,  x2 / fy));
                    maxU = Math.max(Math.max( x1 / ny,  x1 / fy), Math.max( x2 / ny,  x2 / fy));
                    minV = Math.min(Math.min(-z1 / ny, -z1 / fy), Math.min(-z2 / ny, -z2 / fy));
                    maxV = Math.max(Math.max(-z1 / ny, -z1 / fy), Math.max(-z2 / ny, -z2 / fy));
                }
                case 4 -> { // +Z
                    if (z2 <= 0.05) continue;
                    double nz = Math.max(0.05, z1), fz = Math.max(0.05, z2);
                    minU = Math.min(Math.min( x1 / nz,  x1 / fz), Math.min( x2 / nz,  x2 / fz));
                    maxU = Math.max(Math.max( x1 / nz,  x1 / fz), Math.max( x2 / nz,  x2 / fz));
                    minV = Math.min(Math.min( y1 / nz,  y1 / fz), Math.min( y2 / nz,  y2 / fz));
                    maxV = Math.max(Math.max( y1 / nz,  y1 / fz), Math.max( y2 / nz,  y2 / fz));
                }
                default -> { // -Z
                    if (z1 >= -0.05) continue;
                    double nz = Math.max(0.05, -z2), fz = Math.max(0.05, -z1);
                    minU = Math.min(Math.min(-x1 / nz, -x1 / fz), Math.min(-x2 / nz, -x2 / fz));
                    maxU = Math.max(Math.max(-x1 / nz, -x1 / fz), Math.max(-x2 / nz, -x2 / fz));
                    minV = Math.min(Math.min( y1 / nz,  y1 / fz), Math.min( y2 / nz,  y2 / fz));
                    maxV = Math.max(Math.max( y1 / nz,  y1 / fz), Math.max( y2 / nz,  y2 / fz));
                }
            }

            if (maxU < -1.0 || minU > 1.0 || maxV < -1.0 || minV > 1.0) {
                continue;
            }

            double cMinU = Math.max(-1.0, minU);
            double cMaxU = Math.min(1.0, maxU);
            double cMinV = Math.max(-1.0, minV);
            double cMaxV = Math.min(1.0, maxV);

            int minPx = Math.max(0, (int) Math.floor((cMinU + 1.0) * 0.5 * RES));
            int maxPx = Math.min(RES - 1, (int) Math.floor((cMaxU + 1.0) * 0.5 * RES));
            int minPy = Math.max(0, (int) Math.floor((1.0 - cMaxV) * 0.5 * RES));
            int maxPy = Math.min(RES - 1, (int) Math.floor((1.0 - cMinV) * 0.5 * RES));

            int faceOffset = face * RES * RES;

            for (int py = minPy; py <= maxPy; py++) {
                double rayV = 1.0 - (py + 0.5) * invRes;
                if (rayV < cMinV || rayV > cMaxV) continue;

                int rowOffset = faceOffset + py * RES;
                for (int px = minPx; px <= maxPx; px++) {
                    double rayU = (px + 0.5) * invRes - 1.0;
                    if (rayU < cMinU || rayU > cMaxU) continue;

                    sampledAnyRay = true;
                    int rayIdx = rowOffset + px;

                    if (minDist < depthBuffer[rayIdx] + 0.1) {
                        cacheResult(idx, key, (byte) 1);
                        return true;
                    }
                }
            }
        }

        // Sub-pixel conservative guarantee: if no ray centers fell inside the projection, don't cull
        if (!sampledAnyRay) {
            cacheResult(idx, key, (byte) 1);
            return true;
        }

        cacheResult(idx, key, (byte) 2);
        return false;
    }

    private static long computeLocalKey(int midX, int midY, int midZ, int dimX, int dimY, int dimZ) {
        return ((long) midX * 73856093L) ^ ((long) midY * 19349663L) ^ ((long) midZ * 83492791L)
             ^ ((long) dimX * 39916801L) ^ ((long) dimY * 479001599L) ^ ((long) dimZ * 2147483647L);
    }

    private void cacheResult(int idx, long key, byte value) {
        localCacheKeys[idx] = key;
        localCacheValues[idx] = value;
    }
}