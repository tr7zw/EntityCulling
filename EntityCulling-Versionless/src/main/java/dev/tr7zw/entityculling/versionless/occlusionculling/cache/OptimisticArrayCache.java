package dev.tr7zw.entityculling.versionless.occlusionculling.cache;

import java.util.Arrays;

public class OptimisticArrayCache extends SizeLimitedCache {

    // Past this many touched entries a reset clears the whole array instead
    private static final int MAX_DIRTY = 8192;

    private final int reachX2;
    private final byte[] cache;
    private final int[] dirty = new int[MAX_DIRTY];
    private int dirtyCount;
    private boolean dirtyOverflow;
    private int positionKey;
    private int entry;
    private int offset;

    public OptimisticArrayCache(int reach) {
        super(reach);
        this.reachX2 = reach * 2;
        this.cache = new byte[(reachX2 * reachX2 * reachX2) / 4];
    }

    @Override
    public void resetCache() {
        if (dirtyOverflow) {
            Arrays.fill(cache, (byte) 0);
        } else {
            for (int i = 0; i < dirtyCount; i++) {
                cache[dirty[i]] = 0;
            }
        }
        dirtyCount = 0;
        dirtyOverflow = false;
    }

    @Override
    public void setVisible(int x, int y, int z) {
        positionKey = x + y * reachX2 + z * reachX2 * reachX2;
        entry = positionKey / 4;
        offset = (positionKey % 4) * 2;
        markDirty();
        cache[entry] |= 1 << offset;
    }

    @Override
    public void setHidden(int x, int y, int z) {
        positionKey = x + y * reachX2 + z * reachX2 * reachX2;
        entry = positionKey / 4;
        offset = (positionKey % 4) * 2;
        markDirty();
        cache[entry] |= 1 << offset + 1;
    }

    @Override
    public int getState(int x, int y, int z) {
        positionKey = x + y * reachX2 + z * reachX2 * reachX2;
        entry = positionKey / 4;
        offset = (positionKey % 4) * 2;
        return cache[entry] >> offset & 3;
    }

    @Override
    public void setLastVisible() {
        markDirty();
        cache[entry] |= 1 << offset;
    }

    @Override
    public void setLastHidden() {
        markDirty();
        cache[entry] |= 1 << offset + 1;
    }

    // Remembers the current entry for the next reset if it is still zero
    private void markDirty() {
        if (cache[entry] != 0 || dirtyOverflow) {
            return;
        }
        if (dirtyCount == MAX_DIRTY) {
            dirtyOverflow = true;
            return;
        }
        dirty[dirtyCount++] = entry;
    }

}