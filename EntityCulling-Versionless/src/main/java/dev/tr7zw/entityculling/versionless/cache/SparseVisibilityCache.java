package dev.tr7zw.entityculling.versionless.cache;

import java.util.Arrays;

/**
 * Unbounded sparse cache keyed by absolute block position.
 *
 * States: 0 = unknown, 1 = visible, 2 = hidden.
 */
public class SparseVisibilityCache {

    private static final float LOAD_FACTOR = 0.65f;

    private long[] keys;
    private byte[] values;
    private int[] generations;
    private int currentGeneration = 1;
    private int size = 0;
    private int threshold;

    private long hotKey1;
    private byte hotValue1;
    private boolean hotValid1;
    private long hotKey2;
    private byte hotValue2;
    private boolean hotValid2;

    public SparseVisibilityCache() {
        int capacity = 1 << 14;
        keys = new long[capacity];
        values = new byte[capacity];
        generations = new int[capacity];
        threshold = (int) (capacity * LOAD_FACTOR);
    }

    public int getState(int x, int y, int z) {
        long key = packKey(x, y, z);

        if (hotValid1 && hotKey1 == key) {
            return hotValue1;
        }
        if (hotValid2 && hotKey2 == key) {
            // Promote second-most-recent entry to front.
            long k = hotKey1;
            byte v = hotValue1;
            boolean valid = hotValid1;
            hotKey1 = hotKey2;
            hotValue1 = hotValue2;
            hotValid1 = hotValid2;
            hotKey2 = k;
            hotValue2 = v;
            hotValid2 = valid;
            return hotValue1;
        }

        int mask = keys.length - 1;
        int idx = mix64To32(key) & mask;

        while (generations[idx] == currentGeneration) {
            if (keys[idx] == key) {
                rememberHot(key, values[idx]);
                return values[idx];
            }
            idx = (idx + 1) & mask;
        }
        return 0;
    }

    public void setVisible(int x, int y, int z) {
        putState(packKey(x, y, z), (byte) 1);
    }

    public void setHidden(int x, int y, int z) {
        putState(packKey(x, y, z), (byte) 2);
    }

    public void clear() {
        size = 0;
        hotValid1 = false;
        hotValid2 = false;
        currentGeneration++;
        if (currentGeneration == 0) {
            Arrays.fill(generations, 0);
            currentGeneration = 1;
        }
    }

    private void putState(long key, byte state) {
        if (size >= threshold) {
            rehash(keys.length << 1);
        }

        int mask = keys.length - 1;
        int idx = mix64To32(key) & mask;

        while (generations[idx] == currentGeneration) {
            if (keys[idx] == key) {
                values[idx] = state;
                return;
            }
            idx = (idx + 1) & mask;
        }

        generations[idx] = currentGeneration;
        keys[idx] = key;
        values[idx] = state;
        rememberHot(key, state);
        size++;
    }

    private void rehash(int newCapacity) {
        long[] oldKeys = keys;
        byte[] oldValues = values;
        int[] oldGenerations = generations;
        int oldGeneration = currentGeneration;

        keys = new long[newCapacity];
        values = new byte[newCapacity];
        generations = new int[newCapacity];
        currentGeneration = 1;
        size = 0;
        threshold = (int) (newCapacity * LOAD_FACTOR);

        int mask = newCapacity - 1;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldGenerations[i] != oldGeneration) {
                continue;
            }
            long key = oldKeys[i];
            byte value = oldValues[i];

            int idx = mix64To32(key) & mask;
            while (generations[idx] == currentGeneration) {
                idx = (idx + 1) & mask;
            }
            generations[idx] = currentGeneration;
            keys[idx] = key;
            values[idx] = value;
            size++;
        }

        hotValid1 = false;
        hotValid2 = false;
    }

    private void rememberHot(long key, byte value) {
        if (hotValid1 && hotKey1 == key) {
            hotValue1 = value;
            return;
        }
        hotKey2 = hotKey1;
        hotValue2 = hotValue1;
        hotValid2 = hotValid1;
        hotKey1 = key;
        hotValue1 = value;
        hotValid1 = true;
    }

    private static int mix64To32(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        z = z ^ (z >>> 33);
        return (int) z;
    }

    private static long packKey(int x, int y, int z) {
        // X/Z use signed 26-bit world bounds (~+-33.5M), Y uses signed 12-bit.
        long px = ((long) x + 33_554_432L) & 0x3FFFFFFL;
        long py = ((long) y + 2_048L) & 0xFFFL;
        long pz = ((long) z + 33_554_432L) & 0x3FFFFFFL;
        return (px << 38) | (py << 26) | pz;
    }

}
