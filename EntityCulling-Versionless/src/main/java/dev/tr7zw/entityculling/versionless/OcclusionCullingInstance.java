package dev.tr7zw.entityculling.versionless;

import dev.tr7zw.entityculling.versionless.access.*;
import dev.tr7zw.entityculling.versionless.cache.ArrayOcclusionCache;
import dev.tr7zw.entityculling.versionless.cache.OcclusionCache;
import dev.tr7zw.entityculling.versionless.util.Vec3d;

public class OcclusionCullingInstance implements IOcclusionCullingInstance {

    private final IOcclusionCullingInstance legacy;
    private final IOcclusionCullingInstance tree64;
    private volatile IOcclusionCullingInstance active;

    public OcclusionCullingInstance(int maxDistance, DataProvider provider) {
        this(maxDistance, provider, new ArrayOcclusionCache(maxDistance), 0.5);
    }

    public OcclusionCullingInstance(int maxDistance, DataProvider provider, OcclusionCache cache,
                                    double aabbExpansion) {
        this.legacy = new LegacyOcclusionCullingInstance(maxDistance, provider, cache, aabbExpansion);
        this.tree64 = new TreeOcclusionCullingInstance(maxDistance, provider, aabbExpansion);
        this.active = this.tree64;
    }

    @Override
    public boolean isAABBVisible(Vec3d aabbMin, Vec3d aabbMax, Vec3d viewerPosition) {
        return active.isAABBVisible(aabbMin, aabbMax, viewerPosition);
    }

    @Override
    public void resetCache() {
        legacy.resetCache();
        tree64.resetCache();
    }

    @Override
    public String getImplementationName() {
        return active.getImplementationName();
    }

    @Override
    public boolean isUsingTree64() {
        return active.isUsingTree64();
    }

    @Override
    public synchronized void setUseTree64(boolean useTree64) {
        active = useTree64 ? tree64 : legacy;
        active.resetCache();
    }

    public IOcclusionCullingInstance getLegacyInstance() {
        return legacy;
    }

    public IOcclusionCullingInstance getTree64Instance() {
        return tree64;
    }

}
