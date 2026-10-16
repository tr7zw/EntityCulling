package dev.tr7zw.entityculling.versionless.occlusionculling;

import dev.tr7zw.entityculling.versionless.occlusionculling.util.*;

public interface OcclusionCulling {
    boolean isAABBVisible(Vec3d aabbMin, Vec3d aabbMax, Vec3d viewerPosition);
}
