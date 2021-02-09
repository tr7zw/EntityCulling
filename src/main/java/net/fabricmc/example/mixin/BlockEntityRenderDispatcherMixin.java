package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.occlusionculling.AxisAlignedBB;
import net.fabricmc.example.ExampleMod;
import net.fabricmc.example.access.Cullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

	private MinecraftClient client = MinecraftClient.getInstance();
	private AxisAlignedBB blockAABB = new AxisAlignedBB(0d, 0d, 0d, 1d, 1d, 1d);
	
	public BlockEntityRenderDispatcherMixin() {
		System.out.println("Started tile culling!");
	}
	
	@Inject(method = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
	public <E extends BlockEntity> void render(E blockEntity, float tickDelta, MatrixStack matrix,
			VertexConsumerProvider vertexConsumerProvider, CallbackInfo info) {
		if(((Cullable)blockEntity).forceVisible())return;
		if (!ExampleMod.instance.culling.isAABBVisible(
				new Vec3d(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ()),
				blockAABB, client.player.getCameraPosVec(tickDelta), false)) {
			
			info.cancel();
		}
		((Cullable)blockEntity).setVisible();
	}


}
