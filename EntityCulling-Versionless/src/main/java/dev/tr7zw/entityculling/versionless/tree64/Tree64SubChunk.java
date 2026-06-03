package dev.tr7zw.entityculling.versionless.tree64;

/**
 * Two-level 64-tree for a single Minecraft subchunk (16x16x16).
 *
 * Root: 64 children covering 4x4x4 blocks each. Leaf: 64-bit occupancy for
 * blocks inside each 4x4x4 child.
 */
public final class Tree64SubChunk {

    private static final Tree64SubChunk UNAVAILABLE = new Tree64SubChunk(false, 0L, new long[64]);

    private final boolean available;
    private long rootMask;
    private final long[] leafMasks;

    private Tree64SubChunk(boolean available, long rootMask, long[] leafMasks) {
        this.available = available;
        this.rootMask = rootMask;
        this.leafMasks = leafMasks;
    }

    public static Tree64SubChunk unavailable() {
        return UNAVAILABLE;
    }

    public static Tree64SubChunk of(long rootMask, long[] leafMasks) {
        return new Tree64SubChunk(true, rootMask, leafMasks);
    }

    public static Tree64SubChunk createEmpty() {
        return new Tree64SubChunk(true, 0L, new long[64]);
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isOpaqueLocal(int localX, int localY, int localZ) {
        int childIndex = childIndex(localX, localY, localZ);
        long childBit = 1L << childIndex;
        if ((rootMask & childBit) == 0L) {
            return false;
        }
        int leafIndex = leafIndex(localX, localY, localZ);
        return (leafMasks[childIndex] & (1L << leafIndex)) != 0L;
    }

    public boolean isChildEmptyLocal(int localX, int localY, int localZ) {
        int childIndex = childIndex(localX, localY, localZ);
        return (rootMask & (1L << childIndex)) == 0L;
    }

    public boolean isEmpty() {
        return rootMask == 0L;
    }

    public void setOpaqueLocal(int localX, int localY, int localZ, boolean opaque) {
        int childIndex = childIndex(localX, localY, localZ);
        int leafIndex = leafIndex(localX, localY, localZ);
        long leafBit = 1L << leafIndex;

        if (opaque) {
            leafMasks[childIndex] |= leafBit;
            rootMask |= 1L << childIndex;
            return;
        }

        leafMasks[childIndex] &= ~leafBit;
        if (leafMasks[childIndex] == 0L) {
            rootMask &= ~(1L << childIndex);
        }
    }

    public static int childIndex(int localX, int localY, int localZ) {
        int x = (localX >> 2) & 3;
        int y = (localY >> 2) & 3;
        int z = (localZ >> 2) & 3;
        // x + z * 4 + y * 16, matching Tree64 indexing from the reference.
        return x | (z << 2) | (y << 4);
    }

    public static int leafIndex(int localX, int localY, int localZ) {
        int x = localX & 3;
        int y = localY & 3;
        int z = localZ & 3;
        return x | (z << 2) | (y << 4);
    }

}