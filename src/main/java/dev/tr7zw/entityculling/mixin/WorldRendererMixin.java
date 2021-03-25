package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.access.Cullable;
import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
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
		    EntityCullingMod.instance.renderedEntities++;
			return;
		}
		if(cullable.isCulled()) {
			@SuppressWarnings("unchecked")
			EntityRenderer<Entity> entityRenderer = (EntityRenderer<Entity>) entityRenderDispatcher.getRenderer(entity);
			@SuppressWarnings("unchecked")
			EntityRendererInter<Entity> entityRendererInter = (EntityRendererInter<Entity>) entityRenderer;
			if(EntityCullingMod.instance.nametags && matrices != null && vertexConsumers != null && entityRendererInter.shadowHasLabel(entity)) {
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
			EntityCullingMod.instance.skippedEntities++;
			info.cancel();
			return;
		}else {
		    EntityCullingMod.instance.renderedEntities++;
		}
	}
	
}
