package dev.tr7zw.entityculling.versionless.occlusionculling.provider;

public interface DataProvider {

    /**
     * Prepares the requested chunk. Returns true if the chunk is ready, false when
     * not loaded. Should not reload the chunk when the x y z are the same as the
     * last request!
     *
     * @param chunkX
     * @param chunkY
     * @param chunkZ
     * @return
     */
    boolean prepareChunk(int chunkX, int chunkY, int chunkZ);

    /**
     * Location is inside the chunk.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    boolean isOpaqueFullCube(int x, int y, int z);

    default void onChange(ChunkSectionProvider chunk, int x, int y, int z, boolean isOpaque) {
    };

    default void recalcBlockCounts(ChunkSectionProvider chunk) {
    };

    default void postRay() {
    }

    default void postAABB() {
    }

    default void postRun() {
    }

}
