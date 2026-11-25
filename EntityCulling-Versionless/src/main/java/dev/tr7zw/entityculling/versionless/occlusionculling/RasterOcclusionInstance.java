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

    // 16K entries to prevent cache thrashing with 9,000+ entities
    private static final int LOCAL_CACHE_MASK = 16383;
    private final long[] localCacheKeys = new long[LOCAL_CACHE_MASK + 1];
    private final byte[] localCacheValues = new byte[LOCAL_CACHE_MASK + 1];

    // Reusable slice buffer for 2D greedy meshing per layer
    private final long[] layerBitmask = new long[128];

    private double lastCamX = Double.NaN;
    private double lastCamY = Double.NaN;
    private double lastCamZ = Double.NaN;
    private boolean needsRebake = true;

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider) {
        this(maxDistance, provider, 0.0);
    }

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider, OcclusionCache cache) {
        this(maxDistance, provider, 0.0);
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

        for (int d = 0; d <= maxDist; d++) {
            if (fullRows == RES) break;

            switch (face) {
                case 0 -> fullRows += scanLayer(cam, face, camX + d, camY - d - 1, camY + d + 1, camZ - d - 1, camZ + d + 1, faceOffset);
                case 1 -> fullRows += scanLayer(cam, face, camX - d, camY - d - 1, camY + d + 1, camZ - d - 1, camZ + d + 1, faceOffset);
                case 2 -> fullRows += scanLayer(cam, face, camY + d, camX - d - 1, camX + d + 1, camZ - d - 1, camZ + d + 1, faceOffset);
                case 3 -> fullRows += scanLayer(cam, face, camY - d, camX - d - 1, camX + d + 1, camZ - d - 1, camZ + d + 1, faceOffset);
                case 4 -> fullRows += scanLayer(cam, face, camZ + d, camX - d - 1, camX + d + 1, camY - d - 1, camY + d + 1, faceOffset);
                case 5 -> fullRows += scanLayer(cam, face, camZ - d, camX - d - 1, camX + d + 1, camY - d - 1, camY + d + 1, faceOffset);
            }
        }
    }

    /**
     * Extracts a 2D slice of solid blocks at layer distance d, greedily merges them
     * into 2D rectangles, and projects them with zero internal seams.
     */
    private int scanLayer(Vec3d cam, int face, int normalCoord, int minT1, int maxT1, int minT2, int maxT2, int faceOffset) {
        int dim1 = maxT1 - minT1 + 1;
        int dim2 = maxT2 - minT2 + 1;
        if (dim1 <= 0 || dim2 <= 0 || dim1 > 128) return 0;

        // Clear bitmask for the 2D layer slice
        Arrays.fill(layerBitmask, 0, dim1, 0L);

        int startT1 = (minT1 >> 2) << 2;
        int endT1 = (maxT1 >> 2) << 2;
        int startT2 = (minT2 >> 2) << 2;
        int endT2 = (maxT2 >> 2) << 2;

        for (int b1 = startT1; b1 <= endT1; b1 += 4) {
            for (int b2 = startT2; b2 <= endT2; b2 += 4) {
                int bx = (face <= 1) ? normalCoord : b1;
                int by = (face == 2 || face == 3) ? normalCoord : ((face <= 1) ? b1 : b2);
                int bz = (face >= 4) ? normalCoord : b2;

                provider.prepareChunk(bx >> 4, by >> 4, bz >> 4);
                if (provider.getVoxelBitmask() == null || provider.isBrickEmpty(bx, by, bz)) {
                    continue;
                }

                int init1 = Math.max(b1, minT1);
                int lim1 = Math.min(b1 + 3, maxT1);
                int init2 = Math.max(b2, minT2);
                int lim2 = Math.min(b2 + 3, maxT2);

                for (int sub1 = init1; sub1 <= lim1; sub1++) {
                    int idx1 = sub1 - minT1;
                    for (int sub2 = init2; sub2 <= lim2; sub2++) {
                        int vx = (face <= 1) ? normalCoord : sub1;
                        int vy = (face == 2 || face == 3) ? normalCoord : ((face <= 1) ? sub1 : sub2);
                        int vz = (face >= 4) ? normalCoord : sub2;

                        if (provider.isOpaqueFullCube(vx, vy, vz)) {
                            int bit2 = sub2 - minT2;
                            if (bit2 >= 0 && bit2 < 64) {
                                layerBitmask[idx1] |= (1L << bit2);
                            }
                        }
                    }
                }
            }
        }

        // 2D Greedy Meshing: find maximal rectangles of set bits
        int newlyFull = 0;
        for (int r = 0; r < dim1; r++) {
            while (layerBitmask[r] != 0L) {
                int cStart = Long.numberOfTrailingZeros(layerBitmask[r]);
                long mask = layerBitmask[r] >>> cStart;
                int width = Long.numberOfTrailingZeros(~mask); // width of continuous 1s

                long runMask = (width == 64) ? -1L : ((1L << width) - 1L);
                long fullRunMask = runMask << cStart;

                // Expand vertically to find height
                int height = 1;
                while (r + height < dim1 && (layerBitmask[r + height] & fullRunMask) == fullRunMask) {
                    layerBitmask[r + height] &= ~fullRunMask;
                    height++;
                }
                layerBitmask[r] &= ~fullRunMask;

                // World coordinates of merged rectangle
                int t1Start = minT1 + r;
                int t1End = t1Start + height;
                int t2Start = minT2 + cStart;
                int t2End = t2Start + width;

                newlyFull += rasterizeMergedRect(cam, face, normalCoord, t1Start, t1End, t2Start, t2End, faceOffset);
            }
        }

        return newlyFull;
    }

    private int rasterizeMergedRect(Vec3d cam, int face, int normalCoord, int t1_0, int t1_1, int t2_0, int t2_1, int faceOffset) {
        double bx1 = (face <= 1) ? normalCoord : t1_0;
        double bx2 = (face <= 1) ? (normalCoord + 1.0) : t1_1;
        double by1 = (face == 2 || face == 3) ? normalCoord : ((face <= 1) ? t1_0 : t2_0);
        double by2 = (face == 2 || face == 3) ? (normalCoord + 1.0) : ((face <= 1) ? t1_1 : t2_1);
        double bz1 = (face >= 4) ? normalCoord : t2_0;
        double bz2 = (face >= 4) ? (normalCoord + 1.0) : t2_1;

        double rx1 = bx1 - cam.x, rx2 = bx2 - cam.x;
        double ry1 = by1 - cam.y, ry2 = by2 - cam.y;
        double rz1 = bz1 - cam.z, rz2 = bz2 - cam.z;

        switch (face) {
            case 0 -> { // +X face
                if (rx2 <= 0.05) return 0;
                double inv1 = 1.0 / Math.max(0.05, rx1);
                return fillQuad(faceOffset, face, -rz2 * inv1, -rz1 * inv1, ry1 * inv1, ry2 * inv1, (float) rx1);
            }
            case 1 -> { // -X face
                double x1 = -rx2, x2 = -rx1;
                if (x2 <= 0.05) return 0;
                double inv1 = 1.0 / Math.max(0.05, x1);
                return fillQuad(faceOffset, face, rz1 * inv1, rz2 * inv1, ry1 * inv1, ry2 * inv1, (float) x1);
            }
            case 2 -> { // +Y face
                if (ry2 <= 0.05) return 0;
                double inv1 = 1.0 / Math.max(0.05, ry1);
                return fillQuad(faceOffset, face, rx1 * inv1, rx2 * inv1, rz1 * inv1, rz2 * inv1, (float) ry1);
            }
            case 3 -> { // -Y face
                double y1 = -ry2, y2 = -ry1;
                if (y2 <= 0.05) return 0;
                double inv1 = 1.0 / Math.max(0.05, y1);
                return fillQuad(faceOffset, face, rx1 * inv1, rx2 * inv1, -rz2 * inv1, -rz1 * inv1, (float) y1);
            }
            case 4 -> { // +Z face
                if (rz2 <= 0.05) return 0;
                double inv1 = 1.0 / Math.max(0.05, rz1);
                return fillQuad(faceOffset, face, rx1 * inv1, rx2 * inv1, ry1 * inv1, ry2 * inv1, (float) rz1);
            }
            default -> { // -Z face
                double z1 = -rz2, z2 = -rz1;
                if (z2 <= 0.05) return 0;
                double inv1 = 1.0 / Math.max(0.05, z1);
                return fillQuad(faceOffset, face, -rx2 * inv1, -rx1 * inv1, ry1 * inv1, ry2 * inv1, (float) z1);
            }
        }
    }

    private int fillQuad(int faceOffset, int face, double minU, double maxU, double minV, double maxV, float depth) {
        if (maxU <= -1.0 || minU >= 1.0 || maxV <= -1.0 || minV >= 1.0) {
            return 0;
        }

        double sMinX = 32.0 * (Math.max(-1.0, minU) + 1.0);
        double sMaxX = 32.0 * (Math.min(1.0, maxU) + 1.0);
        double sMinY = 32.0 * (1.0 - Math.min(1.0, maxV));
        double sMaxY = 32.0 * (1.0 - Math.max(-1.0, minV));

        // Symmetric pixel-center coverage on merged surfaces (guarantees contiguous boundaries)
        int minPx = (int) Math.round(sMinX);
        int maxPx = (int) Math.round(sMaxX) - 1;
        int minPy = (int) Math.round(sMinY);
        int maxPy = (int) Math.round(sMaxY) - 1;

        if (minPx > maxPx || minPy > maxPy) return 0;

        minPx = Math.max(0, Math.min(RES - 1, minPx));
        maxPx = Math.max(0, Math.min(RES - 1, maxPx));
        minPy = Math.max(0, Math.min(RES - 1, minPy));
        maxPy = Math.max(0, Math.min(RES - 1, maxPy));

        int width = maxPx - minPx + 1;
        long rowMask = (width == 64) ? -1L : (((1L << width) - 1L) << minPx);

        int newlyFull = 0;
        int depthFaceOffset = face * RES * RES;

        for (int py = minPy; py <= maxPy; py++) {
            int maskIdx = faceOffset + py;
            long current = coverageMask[maskIdx];

            long newlyCovered = rowMask & ~current;
            if (newlyCovered != 0L) {
                coverageMask[maskIdx] = current | rowMask;
                if (current != -1L && (current | rowMask) == -1L) {
                    newlyFull++;
                }
            }

            int depthRowOffset = depthFaceOffset + (py << 6);
            for (int px = minPx; px <= maxPx; px++) {
                int pIdx = depthRowOffset + px;
                if (depth < depthBuffer[pIdx]) {
                    depthBuffer[pIdx] = depth;
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

        // Primary face early-out
        double cx = (x1 + x2) * 0.5, cy = (y1 + y2) * 0.5, cz = (z1 + z2) * 0.5;
        double absX = Math.abs(cx), absY = Math.abs(cy), absZ = Math.abs(cz);

        int primaryFace;
        if (absX >= absY && absX >= absZ) {
            primaryFace = (cx > 0) ? 0 : 1;
        } else if (absY >= absX && absY >= absZ) {
            primaryFace = (cy > 0) ? 2 : 3;
        } else {
            primaryFace = (cz > 0) ? 4 : 5;
        }

        int res = testFaceVisibility(primaryFace, x1, x2, y1, y2, z1, z2);
        if (res == 1) {
            cacheResult(idx, key, (byte) 1);
            return true;
        }

        boolean sampledAny = (res == 2);
        for (int face = 0; face < 6; face++) {
            if (face == primaryFace) continue;

            if (face == 0 && x2 <= 0.05) continue;
            if (face == 1 && x1 >= -0.05) continue;
            if (face == 2 && y2 <= 0.05) continue;
            if (face == 3 && y1 >= -0.05) continue;
            if (face == 4 && z2 <= 0.05) continue;
            if (face == 5 && z1 >= -0.05) continue;

            res = testFaceVisibility(face, x1, x2, y1, y2, z1, z2);
            if (res == 1) {
                cacheResult(idx, key, (byte) 1);
                return true;
            }
            if (res == 2) sampledAny = true;
        }

        if (!sampledAny) {
            cacheResult(idx, key, (byte) 1);
            return true;
        }

        cacheResult(idx, key, (byte) 2);
        return false;
    }

    private int testFaceVisibility(int face, double x1, double x2, double y1, double y2, double z1, double z2) {
        double minU, maxU, minV, maxV, faceDist;

        switch (face) {
            case 0 -> {
                if (x2 <= 0.05) return 0;
                faceDist = Math.max(0.05, x1);
                double nx = faceDist, fx = Math.max(0.05, x2);
                minU = Math.min(-z2 / nx, -z2 / fx); maxU = Math.max(-z1 / nx, -z1 / fx);
                minV = Math.min( y1 / fx,  y1 / nx); maxV = Math.max( y2 / fx,  y2 / nx);
            }
            case 1 -> {
                if (x1 >= -0.05) return 0;
                double nx1 = -x2, nx2 = -x1;
                faceDist = Math.max(0.05, nx1);
                double nx = faceDist, fx = Math.max(0.05, nx2);
                minU = Math.min( z1 / fx,  z1 / nx); maxU = Math.max( z2 / fx,  z2 / nx);
                minV = Math.min( y1 / fx,  y1 / nx); maxV = Math.max( y2 / fx,  y2 / nx);
            }
            case 2 -> {
                if (y2 <= 0.05) return 0;
                faceDist = Math.max(0.05, y1);
                double ny = faceDist, fy = Math.max(0.05, y2);
                minU = Math.min( x1 / fy,  x1 / ny); maxU = Math.max( x2 / fy,  x2 / ny);
                minV = Math.min( z1 / fy,  z1 / ny); maxV = Math.max( z2 / fy,  z2 / ny);
            }
            case 3 -> {
                if (y1 >= -0.05) return 0;
                double ny1 = -y2, ny2 = -y1;
                faceDist = Math.max(0.05, ny1);
                double ny = faceDist, fy = Math.max(0.05, ny2);
                minU = Math.min( x1 / fy,  x1 / ny); maxU = Math.max( x2 / fy,  x2 / ny);
                minV = Math.min(-z2 / ny, -z2 / fy); maxV = Math.max(-z1 / ny, -z1 / fy);
            }
            case 4 -> {
                if (z2 <= 0.05) return 0;
                faceDist = Math.max(0.05, z1);
                double nz = faceDist, fz = Math.max(0.05, z2);
                minU = Math.min( x1 / fz,  x1 / nz); maxU = Math.max( x2 / fz,  x2 / nz);
                minV = Math.min( y1 / fz,  y1 / nz); maxV = Math.max( y2 / fz,  y2 / nz);
            }
            default -> {
                if (z1 >= -0.05) return 0;
                double nz1 = -z2, nz2 = -z1;
                faceDist = Math.max(0.05, nz1);
                double nz = faceDist, fz = Math.max(0.05, nz2);
                minU = Math.min(-x2 / nz, -x2 / fz); maxU = Math.max(-x1 / nz, -x1 / fz);
                minV = Math.min( y1 / fz,  y1 / nz); maxV = Math.max( y2 / fz,  y2 / nz);
            }
        }

        if (maxU < -1.0 || minU > 1.0 || maxV < -1.0 || minV > 1.0) {
            return 0;
        }

        double sMinX = (Math.max(-1.0, minU) + 1.0) * 32.0;
        double sMaxX = (Math.min(1.0, maxU) + 1.0) * 32.0;
        double sMinY = (1.0 - Math.min(1.0, maxV)) * 32.0;
        double sMaxY = (1.0 - Math.max(-1.0, minV)) * 32.0;

        int minPx = (int) Math.round(sMinX);
        int maxPx = (int) Math.round(sMaxX) - 1;
        int minPy = (int) Math.round(sMinY);
        int maxPy = (int) Math.round(sMaxY) - 1;

        // Sub-pixel projection opening cannot be proven occluded -> conservatively visible
        if (minPx > maxPx || minPy > maxPy) {
            return 1;
        }

        minPx = Math.max(0, Math.min(RES - 1, minPx));
        maxPx = Math.max(0, Math.min(RES - 1, maxPx));
        minPy = Math.max(0, Math.min(RES - 1, minPy));
        maxPy = Math.max(0, Math.min(RES - 1, maxPy));

        int width = maxPx - minPx + 1;
        long queryMask = (width == 64) ? -1L : (((1L << width) - 1L) << minPx);

        int faceOffset = face * RES;
        int depthFaceOffset = face * RES * RES;

        for (int py = minPy; py <= maxPy; py++) {
            long rowCoverage = coverageMask[faceOffset + py];
            if ((rowCoverage & queryMask) != queryMask) {
                return 1; // Air opening detected
            }

            int depthRowOffset = depthFaceOffset + (py << 6);
            for (int px = minPx; px <= maxPx; px++) {
                if (faceDist < depthBuffer[depthRowOffset + px]) {
                    return 1; // Entity is closer than occluder
                }
            }
        }
        return 2; // Occluded
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