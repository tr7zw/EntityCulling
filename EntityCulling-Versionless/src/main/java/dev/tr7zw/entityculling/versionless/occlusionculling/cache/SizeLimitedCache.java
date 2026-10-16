package dev.tr7zw.entityculling.versionless.occlusionculling.cache;

import dev.tr7zw.entityculling.versionless.occlusionculling.util.*;

public abstract class SizeLimitedCache implements OcclusionCache {

    private final int reach;

    public SizeLimitedCache(int reach) {
        this.reach = reach;
    }

    @Override
    public int getCacheValue(int[] cameraPos, int x, int y, int z) {
        x -= cameraPos[0];
        y -= cameraPos[1];
        z -= cameraPos[2];
        if (Math.abs(x) > reach - 2 || Math.abs(y) > reach - 2 || Math.abs(z) > reach - 2) {
            return -1;
        }

        // check if target is already known
        return getState(x + reach, y + reach, z + reach);
    }

    @Override
    public void cacheResult(int[] cameraPos, int x, int y, int z, boolean result) {
        int cx = x - cameraPos[0] + reach;
        int cy = y - cameraPos[1] + reach;
        int cz = z - cameraPos[2] + reach;
        if (result) {
            setVisible(cx, cy, cz);
        } else {
            setHidden(cx, cy, cz);
        }
    }

    @Override
    public void cacheResult(int[] cameraPos, Vec3d vector, boolean result) {
        int cx = MathUtilities.floor(vector.x) - cameraPos[0] + reach;
        int cy = MathUtilities.floor(vector.y) - cameraPos[1] + reach;
        int cz = MathUtilities.floor(vector.z) - cameraPos[2] + reach;
        if (result) {
            setVisible(cx, cy, cz);
        } else {
            setHidden(cx, cy, cz);
        }
    }

}
