package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.Config;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import dev.tr7zw.entityculling.EntityCullingMod;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow public abstract EntityRenderer getRenderer(Entity entity);

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/Entity;DDDFF)V", cancellable = true)
    private void renderEntity(Entity entity, double e, double f, double g, float h, float par6, CallbackInfo ci) {
        if (EntityCullingMod.instance.config.disableEntityCulling) {
            return;
        }
        Cullable cullable = (Cullable) entity;
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
            EntityCullingMod.instance.skippedEntities++;
            if (entity instanceof LivingEntity && getRenderer(entity) instanceof LivingEntityRenderer) {
                LivingEntityRenderer livingEntityRenderer = (LivingEntityRenderer) getRenderer(entity);
                LivingEntity livingEntity = (LivingEntity) entity;
                ((LivingEntityRendererAccessor) livingEntityRenderer).invokeRenderNameTag(livingEntity, e, f, g);
            }
            ci.cancel();
            return;
        }
        EntityCullingMod.instance.renderedEntities++;
        cullable.setOutOfCamera(false);
    }

}
