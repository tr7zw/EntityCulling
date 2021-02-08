package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.occlusionculling.AxisAlignedBB;
import net.fabricmc.example.ExampleMod;
import net.fabricmc.example.access.EntityRendererInter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	private MinecraftClient client = MinecraftClient.getInstance();
	private AxisAlignedBB entityAABB = new AxisAlignedBB(0d, 0d, 0d, 1d, 2d, 1d);
	
	private MatrixStack matrices;
	private VertexConsumerProvider vertexConsumers;
	
	@Inject(at = @At("RETURN"), method = "shouldRender", cancellable = true)
	public <T extends Entity> boolean shouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
		if(info.getReturnValue()) {
			if(!ExampleMod.instance.culling.isAABBVisible(new Vec3d(entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ()), entityAABB, client.player.getCameraPosVec(client.getTickDelta()), true)) {
				@SuppressWarnings("unchecked")
				EntityRendererInter<T> entityRenderer = (EntityRendererInter<T>) getRenderer(entity);
				if(matrices != null && vertexConsumers != null && entityRenderer.shadowHasLabel(entity)) {
					double d = MathHelper.lerp((double) client.getTickDelta(), (double) entity.lastRenderX, (double) entity.getX());
					double e = MathHelper.lerp((double) client.getTickDelta(), (double) entity.lastRenderY, (double) entity.getY());
					double f = MathHelper.lerp((double) client.getTickDelta(), (double) entity.lastRenderZ, (double) entity.getZ());
					float g = MathHelper.lerp((float) client.getTickDelta(), (float) entity.prevYaw, (float) entity.yaw);
					matrices.push();
					matrices.translate(d, e, f);
					entityRenderer.shadowRenderLabelIfPresent(entity, entity.getDisplayName(), matrices, vertexConsumers, 0);
					matrices.pop();
				}
				info.setReturnValue(false);
			}
		}
		return info.getReturnValue();
	}
	
	//smuggleing the matrices and vertex Consumer to the label renderer
	@Inject(at = @At("HEAD"), method = "render")
	public void render(Entity entity, double x, double y, double z, float yaw, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
		this.matrices = matrices;
		this.vertexConsumers = vertexConsumers;
	}
	
	@Shadow
	public abstract EntityRenderer<?> getRenderer(Entity entity);
	
}
