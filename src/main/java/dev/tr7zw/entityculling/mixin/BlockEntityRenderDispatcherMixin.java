package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.versionless.access.Cullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    //? if >= 1.21.9 {

    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    public void tryExtractRenderState(BlockEntity blockEntity, float f,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            CallbackInfoReturnable<net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> info) {
        if (EntityCullingModBase.instance.config.skipBlockEntityCulling) {
            return;
        }
        BlockEntityRenderer blockEntityRenderer = getRenderer(blockEntity);
        if (blockEntityRenderer == null) {
            return; // Not a block entity that has a renderer, skip all logic
        }
        var frustum = EntityCullingModBase.instance.frustum;
        if (blockEntityRenderer.shouldRenderOffScreen()) {
            EntityCullingModBase.instance.renderedBlockEntities++;
            return;
        } else if (EntityCullingModBase.instance.config.blockEntityFrustumCulling && frustum != null && !frustum
                .isVisible(EntityCullingModBase.instance.setupAABB(blockEntity, blockEntity.getBlockPos()))) {
            // Implement frustum culling like with entities
            EntityCullingModBase.instance.skippedBlockEntities++;
            info.setReturnValue(null);
            return;
        }
        if (blockEntity instanceof Cullable cullable) {
            if (!cullable.isForcedVisible() && cullable.isCulled()) {
                EntityCullingModBase.instance.skippedBlockEntities++;
                info.setReturnValue(null);
                return;
            }
            EntityCullingModBase.instance.renderedBlockEntities++;
            cullable.setOutOfCamera(false);
        }
    }
    //? if neoforge {
    /*
     @Inject(method = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;", at = @At("HEAD"), cancellable = true)
     public void tryExtractRenderState(BlockEntity blockEntity, float f,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, net.minecraft.client.renderer.culling.Frustum frustum,
            CallbackInfoReturnable<net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> info) {
        if (EntityCullingModBase.instance.config.skipBlockEntityCulling) {
            return;
       }
        BlockEntityRenderer blockEntityRenderer = getRenderer(blockEntity);
        if (blockEntityRenderer == null) {
            return; // Not a block entity that has a renderer, skip all logic
        }
        if (blockEntityRenderer.shouldRenderOffScreen()) {
            EntityCullingModBase.instance.renderedBlockEntities++;
            return;
        }
       if (blockEntity instanceof Cullable cullable) {
            if (!cullable.isForcedVisible() && cullable.isCulled()) {
                EntityCullingModBase.instance.skippedBlockEntities++;
               info.setReturnValue(null);
                return;
            }
           EntityCullingModBase.instance.renderedBlockEntities++;
           cullable.setOutOfCamera(false);
        }
     }
    *///? }

    //? } else {
    /*
     @Inject(method = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"), cancellable = true)
     public <E extends BlockEntity> void render(E blockEntity, float f, com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource multiBufferSource, org.spongepowered.asm.mixin.injection.callback.CallbackInfo info) {
      if (EntityCullingModBase.instance.config.skipBlockEntityCulling) {
          return;
      }
      BlockEntityRenderer blockEntityRenderer = getRenderer(blockEntity);
      if (blockEntityRenderer == null) {
         return; // Not a block entity that has a renderer, skip all logic
     }
     //? if >= 1.21.6 {
    
         if (blockEntityRenderer.shouldRenderOffScreen()) {
     //? } else {
    /^
      if(blockEntityRenderer.shouldRenderOffScreen(blockEntity)) {
     ^///? }
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
    *///? }

    @Shadow
    public abstract <E extends BlockEntity> BlockEntityRenderer getRenderer(E blockEntity);

}
