package dev.tr7zw.entityculling.versionless.api;

import dev.tr7zw.entityculling.versionless.occlusionculling.util.*;

public interface CullingProvider {
    boolean isAABBVisible(Vec3d aabbMin, Vec3d aabbMax);
}
