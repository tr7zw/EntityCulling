package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.Config;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import dev.tr7zw.entityculling.EntityCullingMod;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow public abstract EntityRenderer get(Entity entity);

    @Inject(at = @At("HEAD"), method = "method_1920", cancellable = true)
    private void renderEntity(Entity entity, double e, double f, double g, float h, float par6, CallbackInfo ci) {
        if (Config.Fields.disableEntityCulling) {
            return;
        }
        Cullable cullable = (Cullable) entity;
        if (!cullable.isForcedVisible() && cullable.isCulled()) {
            EntityCullingMod.instance.skippedEntities++;
            if (entity instanceof LivingEntity livingEntity && get(livingEntity) instanceof LivingEntityRenderer livingEntityRenderer) {
                ((LivingEntityRendererAccessor) livingEntityRenderer).invokeMethod_821(livingEntity, e, f, g);
            }
            ci.cancel();
            return;
        }
        EntityCullingMod.instance.renderedEntities++;
        cullable.setOutOfCamera(false);
    }

}
