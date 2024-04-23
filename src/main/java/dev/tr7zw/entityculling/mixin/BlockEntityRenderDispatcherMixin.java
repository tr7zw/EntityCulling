package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.versionless.access.Cullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"), cancellable = true)
    public <E extends BlockEntity> void render(E blockEntity, float f, PoseStack poseStack,
            MultiBufferSource multiBufferSource, CallbackInfo info) {
        if (EntityCullingModBase.instance.config.skipBlockEntityCulling) {
            return;
        }
        BlockEntityRenderer<E> blockEntityRenderer = getRenderer(blockEntity);
        // respect the "shouldRenderOffScreen" method
        if (blockEntityRenderer != null && blockEntityRenderer.shouldRenderOffScreen(blockEntity)) {
            EntityCullingModBase.instance.renderedBlockEntities++;
            return;
        }

        if (!((Cullable) blockEntity).isForcedVisible() && ((Cullable) blockEntity).isCulled()) {
            EntityCullingModBase.instance.skippedBlockEntities++;
            info.cancel();
            return;
        }
        EntityCullingModBase.instance.renderedBlockEntities++;
    }

    @Shadow
    public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockEntity);

}
