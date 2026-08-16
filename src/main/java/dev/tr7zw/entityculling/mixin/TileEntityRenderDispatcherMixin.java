package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.ducks.CullableExt;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRenderDispatcherMixin {

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At("HEAD"), cancellable = true)
    public void renderTileEntityAt(TileEntity blockEntity, double x, double y,
            double z, float partialTicks, int destroyStage, CallbackInfo info) {
        if (!((CullableExt) blockEntity).entityCulling$isForcedVisible() && ((CullableExt) blockEntity).entityCulling$isCulled()) {
            EntityCullingMod.instance.skippedBlockEntities++;
            info.cancel();
            return;
        }
        EntityCullingMod.instance.renderedBlockEntities++;
    }

}
