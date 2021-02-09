package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.occlusionculling.OcclusionCullingInstance;
import net.fabricmc.example.ExampleMod;
import net.fabricmc.example.access.Cullable;
import net.fabricmc.example.access.EntityRendererInter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V", ordinal = 0))
	public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
			GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {

	}
	
	@Shadow
	private EntityRenderDispatcher entityRenderDispatcher;
	
	@Inject(at = @At("HEAD"), method = "renderEntity", cancellable = true)
	private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
		Cullable cullable = (Cullable) entity;
		if(cullable.isForcedVisible()) {
			return;
		}
		if(cullable.isCulled()) {
			if(ExampleMod.instance.debug) {
				renderDebugPoints(matrices, vertexConsumers);
			}
			@SuppressWarnings("unchecked")
			EntityRenderer<Entity> entityRenderer = (EntityRenderer<Entity>) entityRenderDispatcher.getRenderer(entity);
			@SuppressWarnings("unchecked")
			EntityRendererInter<Entity> entityRendererInter = (EntityRendererInter<Entity>) entityRenderer;
			if(matrices != null && vertexConsumers != null && entityRendererInter.shadowHasLabel(entity)) {
				double x = MathHelper.lerp((double) tickDelta, (double) entity.lastRenderX, (double) entity.getX()) - cameraX;
				double y = MathHelper.lerp((double) tickDelta, (double) entity.lastRenderY, (double) entity.getY()) - cameraY;
				double z = MathHelper.lerp((double) tickDelta, (double) entity.lastRenderZ, (double) entity.getZ()) - cameraZ;
				Vec3d vec3d = entityRenderer.getPositionOffset(entity, tickDelta);
				double d = x + vec3d.getX();
				double e = y + vec3d.getY();
				double f = z + vec3d.getZ();
				matrices.push();
				matrices.translate(d, e, f);
				entityRendererInter.shadowRenderLabelIfPresent(entity, entity.getDisplayName(), matrices, vertexConsumers, this.entityRenderDispatcher.getLight(entity, tickDelta));
				matrices.pop();
			}
			info.cancel();
			return;
		}
		renderDebugPoints(matrices, vertexConsumers);
	}
	
	private void renderDebugPoints(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		if(ExampleMod.instance.debug) {
			for(Vec3d pos : OcclusionCullingInstance.targets) {
				drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), new Box(pos.x, pos.y, pos.z, pos.x+0.1, pos.y+0.1, pos.z+0.1), 1, 1, 1);
			}
		}
	}
	
	private void drawBox(MatrixStack matrix, VertexConsumer vertices, Box box, float red, float green,
			float blue) {
		WorldRenderer.drawBox((MatrixStack) matrix, (VertexConsumer) vertices, (Box) box, (float) red, (float) green,
				(float) blue, (float) 1.0f);
	}
	
	
}
