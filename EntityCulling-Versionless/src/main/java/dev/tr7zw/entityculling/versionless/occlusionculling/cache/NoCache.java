package dev.tr7zw.entityculling.versionless.occlusionculling.cache;

import dev.tr7zw.entityculling.versionless.occlusionculling.util.*;

public class NoCache implements OcclusionCache {
    @Override
    public void resetCache() {
    }

    @Override
    public void setVisible(int x, int y, int z) {
    }

    @Override
    public void setHidden(int x, int y, int z) {
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
