package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntityRendererDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    
    @Inject(method = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V", at = @At("HEAD"), cancellable = true)
    public void renderTileEntityAt(TileEntity blockEntity, double x, double y, double z, float partialTicks, int destroyStage, float p_192854_10_, CallbackInfo info) {
        if (!((Cullable) blockEntity).isForcedVisible() && ((Cullable) blockEntity).isCulled()) {
            EntityCullingModBase.instance.skippedBlockEntities++;
            info.cancel();
            return;
        }
        EntityCullingModBase.instance.renderedBlockEntities++;
    }

}
