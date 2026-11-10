package dev.tr7zw.entityculling.versionless.occlusionculling.cache;

import dev.tr7zw.entityculling.versionless.occlusionculling.util.*;

public interface OcclusionCache {

    void resetCache();

    void setVisible(int x, int y, int z);

    void setHidden(int x, int y, int z);

    int getState(int x, int y, int z);

    void setLastHidden();

    void setLastVisible();

    // -1 = invalid location, 0 = not checked yet, 1 = visible, 2 = occluding
    int getCacheValue(int[] cameraPos, int x, int y, int z);

    void cacheResult(int[] cameraPos, int x, int y, int z, boolean result);

    void cacheResult(int[] cameraPos, Vec3d vector, boolean result);
}
