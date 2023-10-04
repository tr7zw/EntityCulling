package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.access.Cullable;
import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

@Mixin(RenderManager.class)
public abstract class WorldRendererMixin {

    //@Shadow
    //private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    public abstract Render getEntityRenderObject(Entity p_78713_1_);

    @Inject(at = @At("HEAD"), method = "func_147939_a", cancellable = true)
    public void doRenderEntity(Entity entity, double p_doRenderEntity_2_, double d1, double d2,
            float tickDelta, float p_doRenderEntity_9_, boolean p_doRenderEntity_10_, CallbackInfoReturnable<Boolean> info) {
        Cullable cullable = (Cullable) entity;
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
            EntityRendererInter<Entity> entityRenderer = (EntityRendererInter) getEntityRenderObject(entity);
            if (EntityCullingModBase.instance.config.renderNametagsThroughWalls && entityRenderer.shadowShouldShowName(entity)) {
                entityRenderer.shadowRenderNameTag(entity, p_doRenderEntity_2_, d1, d2);
                //entityRenderer.doRender(entity, entity.posX, entity.posY, entity.posZ, tickDelta, tickDelta);
            }
            EntityCullingModBase.instance.skippedEntities++;
            info.cancel();
            return;
        }
        EntityCullingModBase.instance.renderedEntities++;
        cullable.setOutOfCamera(false);
    }

}
