package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.access.EntityRendererInter;

@Mixin(EntityRenderDispatcher.class)
public abstract class WorldRendererMixin {

    @Shadow public abstract EntityRenderer get(Entity entity);

    @Inject(at = @At("HEAD"), method = "method_1920", cancellable = true)
    private void renderEntity(Entity entity, double e, double f, double g, float h, float par6, CallbackInfo ci) {
        /*if (EntityCullingModBase.instance.config.skipEntityCulling) {
            return;
        }*/
        Cullable cullable = (Cullable) entity;
        if (!cullable.isForcedVisible() && cullable.isCulled() /*&& !entity.noCulling*/) {
            EntityRenderer entityRenderer = get(entity);
            @SuppressWarnings("unchecked")
            EntityRendererInter<Entity> entityRendererInter = (EntityRendererInter<Entity>) entityRenderer;
           /* if (*//*EntityCullingModBase.instance.config.renderNametagsThroughWalls && (true)*//* matrices != null
                    && vertexConsumers != null && entityRendererInter.shadowShouldShowName(entity)) {*/
            // No render offset probably?
               /* Vec3 vec3d = entityRenderer.getRenderOffset(entity, tickDelta);*/
                /*double d = x + vec3d.x;
                double e = y + vec3d.y;
                double f = z + vec3d.z;*/
                /*matrices.pushPose();
                matrices.translate(e, f, g);
                entityRendererInter.shadowRenderNameTag(entity, entity.getDisplayName(), matrices, vertexConsumers,
                        this.entityRenderDispatcher.getPackedLightCoords(entity, tickDelta));
                matrices.popPose();*/
            //}
            EntityCullingModBase.instance.skippedEntities++;
            ci.cancel();
            return;
        }
        EntityCullingModBase.instance.renderedEntities++;
        cullable.setOutOfCamera(false);
    }

}
