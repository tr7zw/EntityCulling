package dev.tr7zw.entityculling;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NMSCullingHelper {

    private final static Minecraft MC = Minecraft.getInstance();

    @SuppressWarnings("unchecked")
    public static boolean ignoresCulling(Entity entity) {
        // spotless:off
        //#if MC <= 12101
        //$$ return entity.noCulling;
        //#else
        return ((EntityRendererInter<Entity>) MC.getEntityRenderDispatcher().getRenderer(entity))
                .ignoresCulling(entity);
        //#endif
        //spotless:on
    }

    @SuppressWarnings("unchecked")
    public static AABB getCullingBox(Entity entity) {
        // spotless:off
        //#if MC <= 12101
        //$$ return entity.getBoundingBoxForCulling();
        //#else
        return ((EntityRendererInter<Entity>) MC.getEntityRenderDispatcher().getRenderer(entity)).getCullingBox(entity);
        //#endif
        //spotless:on
    }

    public static Vec3 getRenderOffset(EntityRenderer entityRenderer, Entity entity, float tickDelta) {
        // spotless:off
        //#if MC <= 12101
        //$$ return entityRenderer.getRenderOffset(entity, tickDelta);
        //#else
        return entityRenderer.getRenderOffset(entityRenderer.createRenderState(entity, tickDelta));
        //#endif
        //spotless:on
    }

    public static void sendChatMessage(Component message) {
        // spotless:off
        //#if MC <= 12101
        //$$ if (MC.player != null)
        //$$ MC.player.sendSystemMessage(message);
        //#else
        MC.getChatListener().handleSystemMessage(message, false);
        //#endif
        //spotless:on
    }

}
