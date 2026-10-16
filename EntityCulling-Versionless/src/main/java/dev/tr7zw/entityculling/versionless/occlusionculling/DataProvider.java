package dev.tr7zw.entityculling.versionless.occlusionculling;

import dev.tr7zw.entityculling.versionless.occlusionculling.util.Vec3d;

public interface DataProvider {

    /**
     * Prepares the requested chunk. Returns true if the chunk is ready, false when
     * not loaded. Should not reload the chunk when the x and y are the same as the
     * last request!
     *
     * @param chunkX
     * @param chunkZ
     * @return
     */
    boolean prepareChunk(int chunkX, int chunkZ);

    /**
     * Location is inside the chunk.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    boolean isOpaqueFullCube(int x, int y, int z);

    default void cleanup() {
    }

}
