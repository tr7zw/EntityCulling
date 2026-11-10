package dev.tr7zw.entityculling.versionless.occlusionculling.cache;

import java.util.*;

public class FastBrickedCache extends SizeLimitedCache {

    private final byte[] cache;
    private final int tileYShift;
    private final int tileZShift;
    private int entry;
    private int offset;

    public FastBrickedCache(int reach) {
        super(reach);
        int width = reach * 2;
        // Round width up to next power of 2 for shift-based math
        int p2Width = Integer.highestOneBit(Math.max(width - 1, 1)) << 1;
        int tilesPerAxis = p2Width >> 3; // 8 blocks per tile

        this.tileYShift = Integer.numberOfTrailingZeros(tilesPerAxis);
        this.tileZShift = tileYShift * 2;

        int totalTiles = tilesPerAxis * tilesPerAxis * tilesPerAxis;
        // 512 blocks per tile, 2 bits per block = 128 bytes per tile
        this.cache = new byte[totalTiles * 128];
    }

    @Override
    public void resetCache() {
        Arrays.fill(cache, (byte) 0);
    }

    private int computeIndex(int x, int y, int z) {
        // Tile index (outer grid)
        int tileIdx = (x >> 3) | ((y >> 3) << tileYShift) | ((z >> 3) << tileZShift);
        // Local index inside 8x8x8 tile (0..511)
        int localIdx = (x & 7) | ((y & 7) << 3) | ((z & 7) << 6);
        return (tileIdx << 9) | localIdx;
    }

    @Override
    public void setVisible(int x, int y, int z) {
        int idx = computeIndex(x, y, z);
        cache[idx >> 2] |= (byte) (1 << ((idx & 3) << 1));
    }

    @Override
    public void setHidden(int x, int y, int z) {
        int idx = computeIndex(x, y, z);
        cache[idx >> 2] |= (byte) (1 << (((idx & 3) << 1) + 1));
    }

    @Override
    public int getState(int x, int y, int z) {
        // Inlined computeIndex to eliminate method call overhead:
        int tileIdx = (x >> 3) | ((y >> 3) << tileYShift) | ((z >> 3) << tileZShift);
        int localIdx = (x & 7) | ((y & 7) << 3) | ((z & 7) << 6);
        int idx = (tileIdx << 9) | localIdx;

        this.entry = idx >> 2;
        this.offset = (idx & 3) << 1;
        return (cache[entry] >> offset) & 3;
    }

    @Override
    public void setLastVisible() {
        cache[entry] |= (byte) (1 << offset);
    }

    @Override
    public void setLastHidden() {
        cache[entry] |= (byte) (1 << (offset + 1));
    }

}