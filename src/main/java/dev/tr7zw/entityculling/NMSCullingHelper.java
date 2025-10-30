package dev.tr7zw.entityculling;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NMSCullingHelper {

    private final static Minecraft MC = Minecraft.getInstance();

    @SuppressWarnings("unchecked")
    public static boolean ignoresCulling(Entity entity) {
        //#if MC <= 12101
        //$$ return entity.noCulling;
        //#else
        return ((EntityRendererInter<Entity>) MC.getEntityRenderDispatcher().getRenderer(entity))
                .entityCullingIgnoresCulling(entity);
        //#endif
    }

    @SuppressWarnings("unchecked")
    public static AABB getCullingBox(Entity entity) {
        if (entity instanceof ArmorStand armorStand && armorStand.isMarker()) {
            // Marker armor stands have no bounding box by default, so we create the default one here
            return EntityType.ARMOR_STAND.getDimensions().makeBoundingBox(entity.position());
        }
        //#if MC <= 12101
        //$$ return entity.getBoundingBoxForCulling();
        //#else
        return ((EntityRendererInter<Entity>) MC.getEntityRenderDispatcher().getRenderer(entity))
                .entityCullingGetCullingBox(entity);
        //#endif
    }

    public static Vec3 getRenderOffset(EntityRenderer entityRenderer, Entity entity, float tickDelta) {
        //#if MC <= 12101
        //$$ return entityRenderer.getRenderOffset(entity, tickDelta);
        //#else
        return entityRenderer.getRenderOffset(entityRenderer.createRenderState(entity, tickDelta));
        //#endif
    }

}
