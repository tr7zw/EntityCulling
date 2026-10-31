package dev.tr7zw.entityculling.util.debug;

import dev.tr7zw.entityculling.versionless.occlusionculling.cache.*;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.*;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.*;

public class JoinedCache implements OcclusionCache {

    private final OcclusionCache[] caches;

    public JoinedCache(OcclusionCache... caches) {
        this.caches = caches;
    }

    @Override
    public void resetCache() {
        for (OcclusionCache cache : caches) {
            cache.resetCache();
        }
    }

    @Override
    public void setVisible(int x, int y, int z) {
        for (OcclusionCache cache : caches) {
            cache.setVisible(x, y, z);
        }
    }

    @Override
    public void setHidden(int x, int y, int z) {
        for (OcclusionCache cache : caches) {
            cache.setHidden(x, y, z);
        }
    }

    @Override
    public int getState(int x, int y, int z) {
        return 0;
    }

    @Override
    public void setLastHidden() {

    }

    @Override
    public void setLastVisible() {

    }

    @Override
    public int getCacheValue(int[] cameraPos, int x, int y, int z) {
        return 0;
    }

    @Override
    public void cacheResult(int[] cameraPos, int x, int y, int z, boolean result) {

    }

    @Override
    public void cacheResult(int[] cameraPos, Vec3d vector, boolean result) {

    }
}
