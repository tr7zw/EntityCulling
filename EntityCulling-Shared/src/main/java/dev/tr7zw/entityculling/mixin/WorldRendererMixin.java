package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.access.Cullable;
import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

@Mixin(RenderManager.class)
public abstract class WorldRendererMixin {

    //@Shadow
    //private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    public abstract <T extends Entity> Render<T> getEntityRenderObject(Entity p_getEntityRenderObject_1_);

    @Inject(at = @At("HEAD"), method = "renderEntity", cancellable = true)
    public void doRenderEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo info) {
        Cullable cullable = (Cullable) entity;
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
            EntityRendererInter<Entity> entityRenderer = (EntityRendererInter) getEntityRenderObject(entity);
            if (EntityCullingModBase.instance.config.renderNametagsThroughWalls && entityRenderer.shadowShouldShowName(entity)) {
                entityRenderer.shadowRenderNameTag(entity, x, y, z);
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
