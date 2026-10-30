package dev.tr7zw.entityculling.versionless.occlusionculling;

import dev.tr7zw.entityculling.versionless.occlusionculling.cache.OcclusionCache;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.DataProvider;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.MathUtilities;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.Vec3d;

import java.util.Arrays;

public class CubemapOcclusionInstance implements OcclusionCulling {

    private static final int RES = 32;
    private static final int TOTAL_PIXELS = 6 * RES * RES;

    private final double[] preDimFracX = new double[TOTAL_PIXELS];
    private final double[] preDimFracY = new double[TOTAL_PIXELS];
    private final double[] preDimFracZ = new double[TOTAL_PIXELS];
    private final int[] preIncX = new int[TOTAL_PIXELS];
    private final int[] preIncY = new int[TOTAL_PIXELS];
    private final int[] preIncZ = new int[TOTAL_PIXELS];

    private final double[] preInvX = new double[TOTAL_PIXELS];
    private final double[] preInvY = new double[TOTAL_PIXELS];
    private final double[] preInvZ = new double[TOTAL_PIXELS];

    private final int reach;
    private final double aabbExpansion;
    private final DataProvider provider;
    private final OcclusionCache externalCache;

    private final float[] depthBuffer = new float[TOTAL_PIXELS];
    private final Vec3d[] rayDirs = new Vec3d[TOTAL_PIXELS];
    private final int[] camBlockPos = new int[3];

    // --- Internal zero-allocation entity result cache (used when externalCache is null) ---
    private static final int LOCAL_CACHE_MASK = 1023; // 1,024 slots (~10 KB)
    private final long[] localCacheKeys = new long[LOCAL_CACHE_MASK + 1];
    private final byte[] localCacheValues = new byte[LOCAL_CACHE_MASK + 1]; // 0=empty, 1=visible, 2=hidden

    private double lastCamX = Double.NaN;
    private double lastCamY = Double.NaN;
    private double lastCamZ = Double.NaN;
    private boolean needsRebake = true;

    public CubemapOcclusionInstance(int maxDistance, DataProvider provider) {
        this(maxDistance, provider, null, 0.5);
    }

    public CubemapOcclusionInstance(int maxDistance, DataProvider provider, OcclusionCache cache) {
        this(maxDistance, provider, cache, 0.5);
    }

    public CubemapOcclusionInstance(int maxDistance, DataProvider provider, OcclusionCache cache, double aabbExpansion) {
        this.reach = maxDistance;
        this.provider = provider;
        this.externalCache = cache;
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

                    rayDirs[idx] = new Vec3d(dx, dy, dz);

                    preDimFracX[idx] = dx == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dx);
                    preDimFracY[idx] = dy == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dy);
                    preDimFracZ[idx] = dz == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dz);

                    preIncX[idx] = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
                    preIncY[idx] = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
                    preIncZ[idx] = dz > 0 ? 1 : (dz < 0 ? -1 : 0);

                    preInvX[idx] = dx == 0.0 ? 1e9 : 1.0 / dx;
                    preInvY[idx] = dy == 0.0 ? 1e9 : 1.0 / dy;
                    preInvZ[idx] = dz == 0.0 ? 1e9 : 1.0 / dz;

                    idx++;
                }
            }
        }
    }

    public void resetCache() {
        this.needsRebake = true;
        if (this.externalCache != null) {
            this.externalCache.resetCache();
        }
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
        double dimFracX = preDimFracX[rayIdx];
        double dimFracY = preDimFracY[rayIdx];
        double dimFracZ = preDimFracZ[rayIdx];

        int x_inc = preIncX[rayIdx];
        int y_inc = preIncY[rayIdx];
        int z_inc = preIncZ[rayIdx];

        double t_next_x = x_inc > 0 ? (x + 1 - start.x) * dimFracX : (start.x - x) * dimFracX;
        double t_next_y = y_inc > 0 ? (y + 1 - start.y) * dimFracY : (start.y - y) * dimFracY;
        double t_next_z = z_inc > 0 ? (z + 1 - start.z) * dimFracZ : (start.z - z) * dimFracZ;

        // Current chunk tracking - only prepareChunk when crossing a 16-block border!
        int curChunkX = x >> 4;
        int curChunkY = y >> 4;
        int curChunkZ = z >> 4;
        boolean chunkReady = provider.prepareChunk(curChunkX, curChunkY, curChunkZ);

        double t = 0.0;
        for (int step = 0; step < reach; step++) {
            // Did we cross into a new chunk section?
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
                return reach; // Unloaded chunk: stop ray
            }

            // Pure bitwise operation in SimpleCache!
            if (provider.isOpaqueFullCube(x, y, z)) {
                return t;
            }

            if (t_next_y < t_next_x && t_next_y < t_next_z) {
                y += y_inc;
                t = t_next_y;
                t_next_y += dimFracY;
            } else if (t_next_x < t_next_y && t_next_x < t_next_z) {
                x += x_inc;
                t = t_next_x;
                t_next_x += dimFracX;
            } else {
                z += z_inc;
                t = t_next_z;
                t_next_z += dimFracZ;
            }

            if (t > reach) break;
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

        int midX = (bMinX + bMaxX) / 2;
        int midY = (bMinY + bMaxY) / 2;
        int midZ = (bMinZ + bMaxZ) / 2;

        // Check external cache if active
        if (externalCache != null) {
            boolean allBlocksHidden = true;
            for (int bx = bMinX; bx <= bMaxX; bx++) {
                for (int by = bMinY; by <= bMaxY; by++) {
                    for (int bz = bMinZ; bz <= bMaxZ; bz++) {
                        int cv = externalCache.getCacheValue(camBlockPos, bx, by, bz);
                        if (cv == 1) return true;
                        if (cv != 2) allBlocksHidden = false;
                    }
                }
            }
            if (allBlocksHidden) return false;
        } else {
            // Check standalone local cache
            long key = (((long) midX & 0x3FFFFFF) << 38) | (((long) midZ & 0x3FFFFFF) << 12) | ((long) midY & 0xFFF);
            int idx = (int) (key & LOCAL_CACHE_MASK);
            if (localCacheKeys[idx] == key) {
                if (localCacheValues[idx] == 1) return true;
                if (localCacheValues[idx] == 2) return false;
            }
        }

        double x1 = minX - viewerPosition.x, x2 = maxX - viewerPosition.x;
        double y1 = minY - viewerPosition.y, y2 = maxY - viewerPosition.y;
        double z1 = minZ - viewerPosition.z, z2 = maxZ - viewerPosition.z;

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

            if (maxU < -1.0 || minU > 1.0 || minV < -1.0 || maxV > 1.0) {
                continue;
            }

            double cMinU = Math.max(-1.0, minU);
            double cMaxU = Math.min(1.0, maxU);
            double cMinV = Math.max(-1.0, minV);
            double cMaxV = Math.min(1.0, maxV);

            int minPx = Math.max(0, (int) ((cMinU + 1.0) * 0.5 * RES));
            int maxPx = Math.min(RES - 1, (int) ((cMaxU + 1.0) * 0.5 * RES));
            int minPy = Math.max(0, (int) ((1.0 - cMaxV) * 0.5 * RES));
            int maxPy = Math.min(RES - 1, (int) ((1.0 - cMinV) * 0.5 * RES));

            int faceOffset = face * RES * RES;

            for (int py = minPy; py <= maxPy; py++) {
                int rowOffset = faceOffset + py * RES;
                for (int px = minPx; px <= maxPx; px++) {
                    int rayIdx = rowOffset + px;

                    double tx1 = x1 * preInvX[rayIdx];
                    double tx2 = x2 * preInvX[rayIdx];
                    double tminX = Math.min(tx1, tx2);
                    double tmaxX = Math.max(tx1, tx2);

                    double ty1 = y1 * preInvY[rayIdx];
                    double ty2 = y2 * preInvY[rayIdx];
                    double tminY = Math.min(ty1, ty2);
                    double tmaxY = Math.max(ty1, ty2);

                    double tz1 = z1 * preInvZ[rayIdx];
                    double tz2 = z2 * preInvZ[rayIdx];
                    double tminZ = Math.min(tz1, tz2);
                    double tmaxZ = Math.max(tz1, tz2);

                    double tEnter = Math.max(Math.max(tminX, tminY), tminZ);
                    double tExit  = Math.min(Math.min(tmaxX, tmaxY), tmaxZ);

                    if (tEnter <= tExit && tExit > 0) {
                        double distToBox = tEnter > 0 ? tEnter : 0;
                        if (distToBox < depthBuffer[rayIdx] + 0.1) {
                            cacheVisible(midX, midY, midZ);
                            return true;
                        }
                    }
                }
            }
        }

        cacheHidden(midX, midY, midZ);
        return false;
    }

    private void cacheVisible(int x, int y, int z) {
        if (externalCache != null) {
            externalCache.cacheResult(camBlockPos, x, y, z, true);
        } else {
            long key = (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | ((long) y & 0xFFF);
            int idx = (int) (key & LOCAL_CACHE_MASK);
            localCacheKeys[idx] = key;
            localCacheValues[idx] = 1;
        }
    }

    private void cacheHidden(int x, int y, int z) {
        if (externalCache != null) {
            externalCache.cacheResult(camBlockPos, x, y, z, false);
        } else {
            long key = (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | ((long) y & 0xFFF);
            int idx = (int) (key & LOCAL_CACHE_MASK);
            localCacheKeys[idx] = key;
            localCacheValues[idx] = 2;
        }
    }
}