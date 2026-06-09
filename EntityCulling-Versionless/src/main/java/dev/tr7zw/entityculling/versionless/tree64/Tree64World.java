package dev.tr7zw.entityculling.versionless.tree64;

import java.util.HashSet;
import java.util.Set;

import dev.tr7zw.entityculling.versionless.access.DataProvider;

public final class Tree64World {

    public static final int SAMPLE_UNAVAILABLE = 0;
    public static final int SAMPLE_AIR = 1;
    public static final int SAMPLE_OPAQUE = 2;
    public static final int SAMPLE_AIR_STEP4 = 5;
    public static final int SAMPLE_AIR_STEP16 = 17;

    private static final float LOAD_FACTOR = 0.65f;

    private final DataProvider provider;
    private final Set<Long> requestedMissing = new HashSet<>();

    private long[] cacheKeys;
    private Tree64SubChunk[] cacheValues;
    private int[] cacheGenerations;
    private int cacheGeneration = 1;
    private int cacheSize = 0;
    private int cacheThreshold;

    public Tree64World(DataProvider provider) {
        this.provider = provider;
        int capacity = 1 << 16;
        this.cacheKeys = new long[capacity];
        this.cacheValues = new Tree64SubChunk[capacity];
        this.cacheGenerations = new int[capacity];
        this.cacheThreshold = (int) (capacity * LOAD_FACTOR);
    }

    public int sample(int worldX, int worldY, int worldZ) {
        Tree64SubChunk subChunk = getOrRequestSubChunk(worldX >> 4, worldY >> 4, worldZ >> 4);
        if (!subChunk.isAvailable()) {
            return SAMPLE_UNAVAILABLE;
        }

        int localX = worldX & 15;
        int localY = worldY & 15;
        int localZ = worldZ & 15;

        if (subChunk.isOpaqueLocal(localX, localY, localZ)) {
            return SAMPLE_OPAQUE;
        }

        if (subChunk.isEmpty()) {
            return SAMPLE_AIR_STEP16;
        }

        return subChunk.isChildEmptyLocal(localX, localY, localZ) ? SAMPLE_AIR_STEP4 : SAMPLE_AIR;
    }

    public void clear() {
        if (cacheSize > 131_072) {
            cacheSize = 0;
            cacheGeneration++;
            if (cacheGeneration == 0) {
                java.util.Arrays.fill(cacheGenerations, 0);
                cacheGeneration = 1;
            }
            requestedMissing.clear();
        }
    }

    private Tree64SubChunk getOrRequestSubChunk(int subChunkX, int subChunkY, int subChunkZ) {
        long key = packKey(subChunkX, subChunkY, subChunkZ);
        Tree64SubChunk cached = cacheGet(key);
        if (cached != null) {
            return cached;
        }

        Tree64SubChunk tree = provider.getTree64SubChunk(subChunkX, subChunkY, subChunkZ);
        if (tree == null) {
            if (requestedMissing.add(key)) {
                provider.requestTree64SubChunk(subChunkX, subChunkY, subChunkZ);
            }
            return Tree64SubChunk.unavailable();
        }
        requestedMissing.remove(key);
        cachePut(key, tree);
        return tree;
    }

    private Tree64SubChunk cacheGet(long key) {
        int mask = cacheKeys.length - 1;
        int idx = mix64To32(key) & mask;
        while (cacheGenerations[idx] == cacheGeneration) {
            if (cacheKeys[idx] == key) {
                return cacheValues[idx];
            }
            idx = (idx + 1) & mask;
        }
        return null;
    }

    private void cachePut(long key, Tree64SubChunk value) {
        if (cacheSize >= cacheThreshold) {
            rehash(cacheKeys.length << 1);
        }

        int mask = cacheKeys.length - 1;
        int idx = mix64To32(key) & mask;
        while (cacheGenerations[idx] == cacheGeneration) {
            if (cacheKeys[idx] == key) {
                cacheValues[idx] = value;
                return;
            }
            idx = (idx + 1) & mask;
        }
        cacheGenerations[idx] = cacheGeneration;
        cacheKeys[idx] = key;
        cacheValues[idx] = value;
        cacheSize++;
    }

    private void rehash(int newCapacity) {
        long[] oldKeys = cacheKeys;
        Tree64SubChunk[] oldValues = cacheValues;
        int[] oldGenerations = cacheGenerations;
        int oldGeneration = cacheGeneration;

        cacheKeys = new long[newCapacity];
        cacheValues = new Tree64SubChunk[newCapacity];
        cacheGenerations = new int[newCapacity];
        cacheGeneration = 1;
        cacheSize = 0;
        cacheThreshold = (int) (newCapacity * LOAD_FACTOR);

        int mask = newCapacity - 1;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldGenerations[i] != oldGeneration) {
                continue;
            }
            long key = oldKeys[i];
            Tree64SubChunk value = oldValues[i];

            int idx = mix64To32(key) & mask;
            while (cacheGenerations[idx] == cacheGeneration) {
                idx = (idx + 1) & mask;
            }
            cacheGenerations[idx] = cacheGeneration;
            cacheKeys[idx] = key;
            cacheValues[idx] = value;
            cacheSize++;
        }
    }

    private static int mix64To32(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        z = z ^ (z >>> 33);
        return (int) z;
    }

    private static long packKey(int x, int y, int z) {
        long lx = ((long) x) & 0x1FFFFFL;
        long ly = ((long) y) & 0x1FFFFFL;
        long lz = ((long) z) & 0x1FFFFFL;
        return lx | (ly << 21) | (lz << 42);
    }

}