package dev.tr7zw.entityculling.util.debug;

import dev.tr7zw.entityculling.versionless.occlusionculling.*;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.*;
import dev.tr7zw.transition.mc.*;
import net.minecraft.*;

public class ComparingOcclusionCullingInstance implements OcclusionCulling {

    private final OcclusionCulling instance1;
    private final OcclusionCulling instance2;

    public ComparingOcclusionCullingInstance(OcclusionCulling instance1, OcclusionCulling instance2) {
        this.instance1 = instance1;
        this.instance2 = instance2;
    }

    @Override
    public boolean isAABBVisible(Vec3d aabbMin, Vec3d aabbMax, Vec3d viewerPosition) {
        boolean result1 = instance1.isAABBVisible(aabbMin, aabbMax, viewerPosition);
        boolean result2 = instance2.isAABBVisible(aabbMin, aabbMax, viewerPosition);
        if (result1 != result2 && viewerPosition.squaredDistanceTo(aabbMin) < 60*60 && viewerPosition.squaredDistanceTo(aabbMax) < 60*60) {
            ClientUtil.sendChatMessage(ComponentProvider.literal("Missmatch at: " + aabbMin + ", distance: " + Math.sqrt(viewerPosition.squaredDistanceTo(aabbMin)) + ", result1: " + result1 + ", result2: " + result2).withStyle(ChatFormatting.RED));
        }
        return result1 != result2;
    }
}
