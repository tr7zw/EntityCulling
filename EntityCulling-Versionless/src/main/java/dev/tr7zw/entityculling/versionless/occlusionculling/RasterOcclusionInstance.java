package dev.tr7zw.entityculling.versionless.occlusionculling;

import dev.tr7zw.entityculling.versionless.occlusionculling.cache.OcclusionCache;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.BrickProvider;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.MathUtilities;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.Vec3d;

import java.util.Arrays;

public class RasterOcclusionInstance implements OcclusionCulling {

    // 64x64 resolution: each row is exactly one 64-bit long (single CPU cycle mask ops)
    private static final int RES = 64;
    private static final int TOTAL_PIXELS = 6 * RES * RES;

    private final int reach;
    private final double aabbExpansion;
    private final BrickProvider provider;

    // Depth buffer storing max occluder distance per pixel
    private final float[] depthBuffer = new float[TOTAL_PIXELS];

    // 6 faces x 64 rows of 64-bit coverage masks
    private final long[] coverageMask = new long[6 * RES];

    // Local result cache
    private static final int LOCAL_CACHE_MASK = 1023;
    private final long[] localCacheKeys = new long[LOCAL_CACHE_MASK + 1];
    private final byte[] localCacheValues = new byte[LOCAL_CACHE_MASK + 1];

    private double lastCamX = Double.NaN;
    private double lastCamY = Double.NaN;
    private double lastCamZ = Double.NaN;
    private boolean needsRebake = true;

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider) {
        this(maxDistance, provider, 0.5);
    }

    public RasterOcclusionInstance(int maxDistance, BrickProvider provider, OcclusionCache cache) {
        this(maxDistance, provider, 0.5);
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

        // Rasterize occluders front-to-back for all 6 cubemap faces
        for (int face = 0; face < 6; face++) {
            rasterizeFace(cameraPos, face);
        }
    }

    /**
     * Traverses the voxel grid strictly front-to-back from the camera within the face pyramid.
     */
    private void rasterizeFace(Vec3d cam, int face) {
        int camX = MathUtilities.floor(cam.x);
        int camY = MathUtilities.floor(cam.y);
        int camZ = MathUtilities.floor(cam.z);

        int faceOffset = face * RES;
        int fullRows = 0;

        int maxDist = Math.min(reach, 48); // 48 blocks covers almost all direct occluders

        // Step front-to-back in depth layers
        for (int d = 1; d <= maxDist; d++) {
            // Early out: if all 64 rows of this face are 100% full, the whole pyramid is occluded!
            if (fullRows == RES) {
                break;
            }

            switch (face) {
                case 0 -> fullRows += scanLayer(cam, face, camX + d, camY - d, camY + d, camZ - d, camZ + d, 0, faceOffset); // +X
                case 1 -> fullRows += scanLayer(cam, face, camX - d, camY - d, camY + d, camZ - d, camZ + d, 1, faceOffset); // -X
                case 2 -> fullRows += scanLayer(cam, face, camY + d, camX - d, camX + d, camZ - d, camZ + d, 2, faceOffset); // +Y
                case 3 -> fullRows += scanLayer(cam, face, camY - d, camX - d, camX + d, camZ - d, camZ + d, 3, faceOffset); // -Y
                case 4 -> fullRows += scanLayer(cam, face, camZ + d, camX - d, camX + d, camY - d, camY + d, 4, faceOffset); // +Z
                case 5 -> fullRows += scanLayer(cam, face, camZ - d, camX - d, camX + d, camY - d, camY + d, 5, faceOffset); // -Z
            }
        }
    }

    private int scanLayer(Vec3d cam, int face, int normalCoord, int minT1, int maxT1, int minT2, int maxT2, int axis, int faceOffset) {
        int newlyFullRows = 0;

        // Traverse tangent 1 in steps of 4 (bricks)
        for (int t1 = minT1; t1 <= maxT1; t1 += 4) {
            for (int t2 = minT2; t2 <= maxT2; t2 += 4) {
                int bx = (axis == 0 || axis == 1) ? normalCoord : ((axis == 2 || axis == 3) ? t1 : t1);
                int by = (axis == 2 || axis == 3) ? normalCoord : ((axis == 0 || axis == 1) ? t1 : t2);
                int bz = (axis == 4 || axis == 5) ? normalCoord : ((axis == 0 || axis == 1) ? t2 : t2);

                int cx = bx >> 4, cy = by >> 4, cz = bz >> 4;
                provider.prepareChunk(cx, cy, cz);

                if (provider.getVoxelBitmask() == null) {
                    continue; // Pure air chunk
                }

                if (provider.isBrickEmpty(bx, by, bz)) {
                    continue; // 4x4x4 brick is pure air
                }

                // Rasterize solid blocks in this 4x4 brick
                int limT1 = Math.min(t1 + 3, maxT1);
                int limT2 = Math.min(t2 + 3, maxT2);

                for (int sub1 = t1; sub1 <= limT1; sub1++) {
                    for (int sub2 = t2; sub2 <= limT2; sub2++) {
                        int vx = (axis <= 1) ? normalCoord : ((axis <= 3) ? sub1 : sub1);
                        int vy = (axis == 2 || axis == 3) ? normalCoord : ((axis <= 1) ? sub1 : sub2);
                        int vz = (axis >= 4) ? normalCoord : ((axis <= 1) ? sub2 : sub2);

                        if (provider.isOpaqueFullCube(vx, vy, vz)) {
                            newlyFullRows += rasterizeBlock(cam, face, vx, vy, vz, faceOffset);
                        }
                    }
                }
            }
        }
        return newlyFullRows;
    }

    /**
     * Inner-conservative projection: fills ONLY pixels that are 100% inside the block projection.
     */
    private int rasterizeBlock(Vec3d cam, int face, int bx, int by, int bz, int faceOffset) {
        double x1 = bx - cam.x, x2 = bx + 1.0 - cam.x;
        double y1 = by - cam.y, y2 = by + 1.0 - cam.y;
        double z1 = bz - cam.z, z2 = bz + 1.0 - cam.z;

        double minU, maxU, minV, maxV;
        double nearDist;

        switch (face) {
            case 0 -> { // +X
                if (x1 <= 0.05) return 0;
                minU = -z2 / x1; maxU = -z1 / x2;
                minV =  y1 / x2; maxV =  y2 / x1;
                nearDist = x1;
            }
            case 1 -> { // -X
                if (x2 >= -0.05) return 0;
                double nx = -x2, fx = -x1;
                minU =  z1 / fx; maxU =  z2 / nx;
                minV =  y1 / fx; maxV =  y2 / nx;
                nearDist = nx;
            }
            case 2 -> { // +Y
                if (y1 <= 0.05) return 0;
                minU =  x1 / y2; maxU =  x2 / y1;
                minV =  z1 / y2; maxV =  z2 / y1;
                nearDist = y1;
            }
            case 3 -> { // -Y
                if (y2 >= -0.05) return 0;
                double ny = -y2, fy = -y1;
                minU =  x1 / fy; maxU =  x2 / ny;
                minV = -z2 / ny; maxV = -z1 / fy;
                nearDist = ny;
            }
            case 4 -> { // +Z
                if (z1 <= 0.05) return 0;
                minU =  x1 / z2; maxU =  x2 / z1;
                minV =  y1 / z2; maxV =  y2 / z1;
                nearDist = z1;
            }
            default -> { // -Z
                if (z2 >= -0.05) return 0;
                double nz = -z2, fz = -z1;
                minU = -x2 / nz; maxU = -x1 / fz;
                minV =  y1 / fz; maxV =  y2 / nz;
                nearDist = nz;
            }
        }

        // Clamp to face bounds
        if (maxU <= -1.0 || minU >= 1.0 || maxV <= -1.0 || minV >= 1.0) {
            return 0;
        }

        // STRICT INNER-CONSERVATIVE RASTERIZATION:
        // A pixel is filled IF AND ONLY IF it is 100% inside [minU, maxU] x [minV, maxV]
        int minPx = (int) Math.ceil(32.0 * (minU + 1.0));
        int maxPx = (int) Math.floor(32.0 * (maxU + 1.0) - 1.0);
        int minPy = (int) Math.ceil(32.0 * (1.0 - maxV));
        int maxPy = (int) Math.floor(32.0 * (1.0 - minV) - 1.0);

        // Clamping to screen bounds
        minPx = Math.max(0, minPx);
        maxPx = Math.min(RES - 1, maxPx);
        minPy = Math.max(0, minPy);
        maxPy = Math.min(RES - 1, maxPy);

        // If the block does not 100% cover any pixel, abort! The gap/slit is preserved!
        if (minPx > maxPx || minPy > maxPy) {
            return 0;
        }

        int width = maxPx - minPx + 1;
        long rowMask = (width == 64) ? -1L : (((1L << width) - 1L) << minPx);
        float conservativeDepth = (float) (nearDist + 1.732); // Max block diagonal

        int newlyFull = 0;
        int depthFaceOffset = face * RES * RES;

        for (int py = minPy; py <= maxPy; py++) {
            int maskIdx = faceOffset + py;
            long current = coverageMask[maskIdx];

            // Fast skip if all pixels in this row are already covered by closer occluders
            if ((current & rowMask) == rowMask) {
                continue;
            }

            coverageMask[maskIdx] = current | rowMask;
            if (current != -1L && (current | rowMask) == -1L) {
                newlyFull++;
            }

            int depthRowOffset = depthFaceOffset + py * RES;
            for (int px = minPx; px <= maxPx; px++) {
                int pIdx = depthRowOffset + px;
                if (conservativeDepth < depthBuffer[pIdx]) {
                    depthBuffer[pIdx] = conservativeDepth;
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

        boolean sampledAny = false;

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

            // OUTER-CONSERVATIVE TEST: Every pixel touched by the entity is queried
            int minPx = Math.max(0, (int) Math.floor((cMinU + 1.0) * 32.0));
            int maxPx = Math.min(RES - 1, (int) Math.ceil((cMaxU + 1.0) * 32.0 - 1.0));
            int minPy = Math.max(0, (int) Math.floor((1.0 - cMaxV) * 32.0));
            int maxPy = Math.min(RES - 1, (int) Math.ceil((1.0 - cMinV) * 32.0 - 1.0));

            int depthFaceOffset = face * RES * RES;

            for (int py = minPy; py <= maxPy; py++) {
                int depthRowOffset = depthFaceOffset + py * RES;
                for (int px = minPx; px <= maxPx; px++) {
                    sampledAny = true;
                    // If any pixel peeks through an unoccluded space or closer than occluder, it's visible!
                    if (minDist < depthBuffer[depthRowOffset + px]) {
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

        // All sampled pixels are 100% occluded by closer blocks
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