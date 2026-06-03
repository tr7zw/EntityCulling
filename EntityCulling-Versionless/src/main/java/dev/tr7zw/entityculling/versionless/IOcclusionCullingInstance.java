package dev.tr7zw.entityculling.versionless;

import dev.tr7zw.entityculling.versionless.util.Vec3d;

public interface IOcclusionCullingInstance {

    boolean isAABBVisible(Vec3d aabbMin, Vec3d aabbMax, Vec3d viewerPosition);

    void resetCache();

    String getImplementationName();

    boolean isUsingTree64();

    void setUseTree64(boolean useTree64);

    default void toggleImplementation() {
        setUseTree64(!isUsingTree64());
    }

}