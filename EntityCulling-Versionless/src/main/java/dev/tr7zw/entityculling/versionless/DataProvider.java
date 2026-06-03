package dev.tr7zw.entityculling.versionless;

import dev.tr7zw.entityculling.versionless.tree64.Tree64SubChunk;
import dev.tr7zw.entityculling.versionless.util.Vec3d;

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
    boolean prepareChunk(int chunkX, int chunkZ, int y);

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

    default void checkingPosition(Vec3d[] targetPoints, int size, Vec3d viewerPosition) {
    }

    /**
     * Returns prebuilt Tree64 data for a subchunk (16x16x16), or null if it is not
     * currently available.
     */
    default Tree64SubChunk getTree64SubChunk(int subChunkX, int subChunkY, int subChunkZ) {
        return null;
    }

    /**
     * Enqueue a subchunk build/update request that will be handled on the main
     * thread.
     */
    default void requestTree64SubChunk(int subChunkX, int subChunkY, int subChunkZ) {
    }

    /**
     * Process queued Tree64 build/update requests on the main thread.
     */
    default void processTree64Requests(int maxRequests) {
    }

}
