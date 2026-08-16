package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.RenderHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.ducks.CullableExt;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

@Mixin(RenderManager.class)
public abstract class WorldRendererMixin {

    @Shadow
    public abstract <T extends Entity> Render<T> getEntityRenderObject(Entity entityIn);

    @Inject(at = @At("HEAD"), method = "doRenderEntity", cancellable = true)
    public void doRenderEntity(Entity entity, double x, double y, double z,
            float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> info) {
        CullableExt cullable = (CullableExt) entity;
        if (!cullable.entityCulling$isForcedVisible() && cullable.entityCulling$isCulled()) {
            if (EntityCullingMod.instance.config.renderNametagsThroughWalls) {
                RenderHook.handle(getEntityRenderObject(entity), entity, x, y, z);
            }
            EntityCullingMod.instance.skippedEntities++;
            info.cancel();
            return;
        }
        EntityCullingMod.instance.renderedEntities++;
        cullable.entityCulling$setOutOfCamera(false);
    }

}
