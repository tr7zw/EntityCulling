package dev.tr7zw.entityculling.versionless.occlusionculling;

import dev.tr7zw.entityculling.versionless.occlusionculling.cache.OcclusionCache;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.BrickProvider;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.MathUtilities;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.Vec3d;

import java.util.Arrays;

public class RasterOcclusionInstance implements OcclusionCulling {

    private static final int RES = 64;
    private static final int TOTAL_PIXELS = 6 * RES * RES;

    private final int reach;
    private final double aabbExpansion;
    private final BrickProvider provider;

    private final float[] depthBuffer = new float[TOTAL_PIXELS];
    private final long[] coverageMask = new long[6 * RES];

    private static final int LOCAL_CACHE_MASK = 1023;
    private final long[] localCacheKeys = new long[LOCAL_CACHE_MASK + 1];
    private final byte[] localCacheValues = new byte[LOCAL_CACHE_MASK + 1];

    private double lastCamX = Double.NaN;
    private double lastCamY = Double.NaN;
    private double lastCamZ = Double.NaN;
    private boolean needsRebake = true;

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider) {
        this(maxDistance, provider, 0.15); // Tighter expansion to avoid over-rendering
    }

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider, OcclusionCache cache) {
        this(maxDistance, provider, 0.15);
    }

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider, OcclusionCache cache, double aabbExpansion) {
        this(maxDistance, provider, aabbExpansion);
    }

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider, double aabbExpansion) {
        this.reach = maxDistance;
        this.provider = provider;
        this.aabbExpansion = aabbExpansion;
        Arrays.fill(localCacheKeys, Long.MIN_VALUE);
    }

    public void resetCache() {
        this.needsRebake = true;
        this.lastCamX = Double.NaN;
        this.lastCamY = Double.NaN;
        this.lastCamZ = Double.NaN;
        Arrays.fill(localCacheKeys, Long.MIN_VALUE);
    }

    public void prepareFrame(Vec3d cameraPos) {
        this.lastCamX = cameraPos.x;
        this.lastCamY = cameraPos.y;
        this.lastCamZ = cameraPos.z;
        this.needsRebake = false;

        Arrays.fill(depthBuffer, (float) reach);
        Arrays.fill(coverageMask, 0L);
        Arrays.fill(localCacheKeys, Long.MIN_VALUE);

        for (int face = 0; face < 6; face++) {
            rasterizeFace(cameraPos, face);
        }
    }

    private void rasterizeFace(Vec3d cam, int face) {
        int camX = MathUtilities.floor(cam.x);
        int camY = MathUtilities.floor(cam.y);
        int camZ = MathUtilities.floor(cam.z);

        int faceOffset = face * RES;
        int fullRows = 0;
        int maxDist = Math.min(reach, 48);

        for (int d = 1; d <= maxDist; d++) {
            if (fullRows == RES) {
                break;
            }

            switch (face) {
                case 0 -> fullRows += scanLayer(cam, 0, camX + d, camY - d, camY + d, camZ - d, camZ + d, faceOffset);
                case 1 -> fullRows += scanLayer(cam, 1, camX - d, camY - d, camY + d, camZ - d, camZ + d, faceOffset);
                case 2 -> fullRows += scanLayer(cam, 2, camY + d, camX - d, camX + d, camZ - d, camZ + d, faceOffset);
                case 3 -> fullRows += scanLayer(cam, 3, camY - d, camX - d, camX + d, camZ - d, camZ + d, faceOffset);
                case 4 -> fullRows += scanLayer(cam, 4, camZ + d, camX - d, camX + d, camY - d, camY + d, faceOffset);
                case 5 -> fullRows += scanLayer(cam, 5, camZ - d, camX - d, camX + d, camY - d, camY + d, faceOffset);
            }
        }
    }

    private int scanLayer(Vec3d cam, int face, int normalCoord, int minT1, int maxT1, int minT2, int maxT2, int faceOffset) {
        int newlyFullRows = 0;

        // Align coordinates to 4-block brick boundaries
        int startT1 = (minT1 >> 2) << 2;
        int endT1 = (maxT1 >> 2) << 2;
        int startT2 = (minT2 >> 2) << 2;
        int endT2 = (maxT2 >> 2) << 2;

        for (int b1 = startT1; b1 <= endT1; b1 += 4) {
            for (int b2 = startT2; b2 <= endT2; b2 += 4) {
                int bx = (face <= 1) ? normalCoord : ((face <= 3) ? b1 : b1);
                int by = (face == 2 || face == 3) ? normalCoord : ((face <= 1) ? b1 : b2);
                int bz = (face >= 4) ? normalCoord : b2;

                provider.prepareChunk(bx >> 4, by >> 4, bz >> 4);
                long[] bitmask = provider.getVoxelBitmask();
                if (bitmask == null || provider.isBrickEmpty(bx, by, bz)) {
                    continue;
                }

                int lim1 = Math.min(b1 + 3, maxT1);
                int lim2 = Math.min(b2 + 3, maxT2);
                int init1 = Math.max(b1, minT1);
                int init2 = Math.max(b2, minT2);

                for (int sub1 = init1; sub1 <= lim1; sub1++) {
                    for (int sub2 = init2; sub2 <= lim2; sub2++) {
                        int vx = (face <= 1) ? normalCoord : ((face <= 3) ? sub1 : sub1);
                        int vy = (face == 2 || face == 3) ? normalCoord : ((face <= 1) ? sub1 : sub2);
                        int vz = (face >= 4) ? normalCoord : sub2;

                        int blockIdx = (vx & 15) | ((vy & 15) << 4) | ((vz & 15) << 8);
                        if ((bitmask[blockIdx >>> 6] & (1L << (blockIdx & 63))) != 0) {
                            newlyFullRows += rasterizeBlock(cam, face, vx, vy, vz, faceOffset);
                        }
                    }
                }
            }
        }
        return newlyFullRows;
    }

    private int rasterizeBlock(Vec3d cam, int face, int bx, int by, int bz, int faceOffset) {
        double x1 = bx - cam.x, x2 = x1 + 1.0;
        double y1 = by - cam.y, y2 = y1 + 1.0;
        double z1 = bz - cam.z, z2 = z1 + 1.0;

        double minU, maxU, minV, maxV;
        double depth;

        switch (face) {
            case 0 -> { // +X
                if (x1 <= 0.05) return 0;
                depth = x1;
                double inv = 1.0 / depth;
                minU = -z2 * inv; maxU = -z1 * inv;
                minV =  y1 * inv; maxV =  y2 * inv;
            }
            case 1 -> { // -X
                if (x2 >= -0.05) return 0;
                depth = -x2;
                double inv = 1.0 / depth;
                minU =  z1 * inv; maxU =  z2 * inv;
                minV =  y1 * inv; maxV =  y2 * inv;
            }
            case 2 -> { // +Y
                if (y1 <= 0.05) return 0;
                depth = y1;
                double inv = 1.0 / depth;
                minU =  x1 * inv; maxU =  x2 * inv;
                minV =  z1 * inv; maxV =  z2 * inv;
            }
            case 3 -> { // -Y
                if (y2 >= -0.05) return 0;
                depth = -y2;
                double inv = 1.0 / depth;
                minU =  x1 * inv; maxU =  x2 * inv;
                minV = -z2 * inv; maxV = -z1 * inv;
            }
            case 4 -> { // +Z
                if (z1 <= 0.05) return 0;
                depth = z1;
                double inv = 1.0 / depth;
                minU =  x1 * inv; maxU =  x2 * inv;
                minV =  y1 * inv; maxV =  y2 * inv;
            }
            default -> { // -Z
                if (z2 >= -0.05) return 0;
                depth = -z2;
                double inv = 1.0 / depth;
                minU = -x2 * inv; maxU = -x1 * inv;
                minV =  y1 * inv; maxV =  y2 * inv;
            }
        }

        if (maxU <= -1.0 || minU >= 1.0 || maxV <= -1.0 || minV >= 1.0) {
            return 0;
        }

        // Convert normalized [-1, 1] to [0, 64] pixel space
        double sMinX = 32.0 * (minU + 1.0);
        double sMaxX = 32.0 * (maxU + 1.0);
        double sMinY = 32.0 * (1.0 - maxV);
        double sMaxY = 32.0 * (1.0 - minV);

        // Continuous pixel rounding: eliminates 1-pixel seams between adjacent blocks
        int minPx = (int) Math.round(sMinX);
        int maxPx = (int) Math.round(sMaxX) - 1;
        int minPy = (int) Math.round(sMinY);
        int maxPy = (int) Math.round(sMaxY) - 1;

        if (minPx > maxPx || minPy > maxPy) {
            return 0;
        }

        minPx = Math.max(0, minPx);
        maxPx = Math.min(RES - 1, maxPx);
        minPy = Math.max(0, minPy);
        maxPy = Math.min(RES - 1, maxPy);

        if (minPx > maxPx || minPy > maxPy) {
            return 0;
        }

        int width = maxPx - minPx + 1;
        long rowMask = (width == 64) ? -1L : (((1L << width) - 1L) << minPx);
        
        // Use true front-face depth with a small epsilon for precision
        float conservativeDepth = (float) (depth + 0.05);

        int newlyFull = 0;
        int depthFaceOffset = face * RES * RES;

        for (int py = minPy; py <= maxPy; py++) {
            int maskIdx = faceOffset + py;
            long current = coverageMask[maskIdx];

            long newlyCovered = rowMask & ~current;
            if (newlyCovered == 0L) {
                continue;
            }

            coverageMask[maskIdx] = current | rowMask;
            if (current != -1L && (current | rowMask) == -1L) {
                newlyFull++;
            }

            int depthRowOffset = depthFaceOffset + py * RES;
            for (int px = minPx; px <= maxPx; px++) {
                if ((newlyCovered & (1L << px)) != 0) {
                    depthBuffer[depthRowOffset + px] = conservativeDepth;
                }
            }
        }

        return newlyFull;
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

        long key = computeLocalKey(bMinX, bMinY, bMinZ, bMaxX - bMinX, bMaxY - bMinY, bMaxZ - bMinZ);
        int idx = (int) (key & LOCAL_CACHE_MASK);
        if (localCacheKeys[idx] == key) {
            byte val = localCacheValues[idx];
            if (val == 1) return true;
            if (val == 2) return false;
        }

        double x1 = minX - viewerPosition.x, x2 = maxX - viewerPosition.x;
        double y1 = minY - viewerPosition.y, y2 = maxY - viewerPosition.y;
        double z1 = minZ - viewerPosition.z, z2 = maxZ - viewerPosition.z;

        boolean sampledAny = false;

        for (int face = 0; face < 6; face++) {
            double minU, maxU, minV, maxV;
            double faceDist;

            switch (face) {
                case 0 -> { // +X
                    if (x2 <= 0.05) continue;
                    faceDist = Math.max(0.05, x1);
                    double nx = faceDist, fx = Math.max(0.05, x2);
                    minU = Math.min(-z2 / nx, -z2 / fx); maxU = Math.max(-z1 / nx, -z1 / fx);
                    minV = Math.min( y1 / fx,  y1 / nx); maxV = Math.max( y2 / fx,  y2 / nx);
                }
                case 1 -> { // -X
                    if (x1 >= -0.05) continue;
                    faceDist = Math.max(0.05, -x2);
                    double nx = faceDist, fx = Math.max(0.05, -x1);
                    minU = Math.min( z1 / fx,  z1 / nx); maxU = Math.max( z2 / fx,  z2 / nx);
                    minV = Math.min( y1 / fx,  y1 / nx); maxV = Math.max( y2 / fx,  y2 / nx);
                }
                case 2 -> { // +Y
                    if (y2 <= 0.05) continue;
                    faceDist = Math.max(0.05, y1);
                    double ny = faceDist, fy = Math.max(0.05, y2);
                    minU = Math.min( x1 / fy,  x1 / ny); maxU = Math.max( x2 / fy,  x2 / ny);
                    minV = Math.min( z1 / fy,  z1 / ny); maxV = Math.max( z2 / fy,  z2 / ny);
                }
                case 3 -> { // -Y
                    if (y1 >= -0.05) continue;
                    faceDist = Math.max(0.05, -y2);
                    double ny = faceDist, fy = Math.max(0.05, -y1);
                    minU = Math.min( x1 / fy,  x1 / ny); maxU = Math.max( x2 / fy,  x2 / ny);
                    minV = Math.min(-z2 / ny, -z2 / fy); maxV = Math.max(-z1 / ny, -z1 / fy);
                }
                case 4 -> { // +Z
                    if (z2 <= 0.05) continue;
                    faceDist = Math.max(0.05, z1);
                    double nz = faceDist, fz = Math.max(0.05, z2);
                    minU = Math.min( x1 / fz,  x1 / nz); maxU = Math.max( x2 / fz,  x2 / nz);
                    minV = Math.min( y1 / fz,  y1 / nz); maxV = Math.max( y2 / fz,  y2 / nz);
                }
                default -> { // -Z
                    if (z1 >= -0.05) continue;
                    faceDist = Math.max(0.05, -z2);
                    double nz = faceDist, fz = Math.max(0.05, -z1);
                    minU = Math.min(-x2 / nz, -x2 / fz); maxU = Math.max(-x1 / nz, -x1 / fz);
                    minV = Math.min( y1 / fz,  y1 / nz); maxV = Math.max( y2 / fz,  y2 / nz);
                }
            }

            if (maxU < -1.0 || minU > 1.0 || maxV < -1.0 || minV > 1.0) {
                continue;
            }

            double cMinU = Math.max(-1.0, minU);
            double cMaxU = Math.min(1.0, maxU);
            double cMinV = Math.max(-1.0, minV);
            double cMaxV = Math.min(1.0, maxV);

            int minPx = Math.max(0, (int) Math.floor((cMinU + 1.0) * 32.0));
            int maxPx = Math.min(RES - 1, (int) Math.ceil((cMaxU + 1.0) * 32.0 - 1.0));
            int minPy = Math.max(0, (int) Math.floor((1.0 - cMaxV) * 32.0));
            int maxPy = Math.min(RES - 1, (int) Math.ceil((1.0 - cMinV) * 32.0 - 1.0));

            int width = maxPx - minPx + 1;
            long queryMask = (width == 64) ? -1L : (((1L << width) - 1L) << minPx);

            int faceOffset = face * RES;
            int depthFaceOffset = face * RES * RES;

            for (int py = minPy; py <= maxPy; py++) {
                sampledAny = true;
                long rowCoverage = coverageMask[faceOffset + py];

                // Fast-path: if any pixel in the row is unoccluded air, it's visible immediately
                if ((rowCoverage & queryMask) != queryMask) {
                    cacheResult(idx, key, (byte) 1);
                    return true;
                }

                // Otherwise, compare depth
                int depthRowOffset = depthFaceOffset + py * RES;
                for (int px = minPx; px <= maxPx; px++) {
                    if (faceDist < depthBuffer[depthRowOffset + px]) {
                        cacheResult(idx, key, (byte) 1);
                        return true;
                    }
                }
            }
        }

        if (!sampledAny) {
            cacheResult(idx, key, (byte) 1);
            return true;
        }

        cacheResult(idx, key, (byte) 2);
        return false;
    }

    private static long computeLocalKey(int minX, int minY, int minZ, int dimX, int dimY, int dimZ) {
        return ((long) minX * 73856093L) ^ ((long) minY * 19349663L) ^ ((long) minZ * 83492791L)
             ^ ((long) dimX * 39916801L) ^ ((long) dimY * 479001599L) ^ ((long) dimZ * 2147483647L);
    }

    private void cacheResult(int idx, long key, byte value) {
        localCacheKeys[idx] = key;
        localCacheValues[idx] = value;
    }
}