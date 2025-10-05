package dev.tr7zw.entityculling.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.logisticscraft.occlusionculling.util.MathUtilities;
//#if MC < 12105
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//#endif
import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.NMSCullingHelper;
import dev.tr7zw.entityculling.versionless.access.Cullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    private EntityRenderDispatcher entityCulling$entityRenderDispatcher = Minecraft.getInstance()
            .getEntityRenderDispatcher();
    private List<Runnable> lateRenders = new ArrayList<Runnable>();

    private double aabbExpansion = 0.5;

    //#if MC >= 12109
    @Inject(at = @At("HEAD"), method = "extractEntity", cancellable = true)
    private void extractEntityRedir(Entity entity, float partialTick,
            CallbackInfoReturnable<net.minecraft.client.renderer.entity.state.EntityRenderState> ci) {
        if (EntityCullingModBase.instance.config.skipEntityCulling) {
            return;
        }
        Cullable cullable = (Cullable) entity;
        if (!cullable.isForcedVisible() && cullable.isCulled() /* && !NMSCullingHelper.ignoresCulling(entity) */) {
            EntityCullingModBase.instance.skippedEntities++;
            var state = new net.minecraft.client.renderer.entity.state.ArmorStandRenderState();
            state.entityType = EntityType.ARMOR_STAND;
            state.x = entity.getX();
            state.y = entity.getY();
            state.z = entity.getZ();
            state.isInvisible = true;
            if (entity.shouldShowName()) {
                state.nameTag = entity.getDisplayName();
                state.nameTagAttachment = entity.getAttachments().getNullable(
                        net.minecraft.world.entity.EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTick));
            }
            ci.setReturnValue(state);
            return;
        }
        EntityCullingModBase.instance.renderedEntities++;
        cullable.setOutOfCamera(false);
    }

    @Inject(at = @At("HEAD"), method = "extractVisibleEntities")
    private void extractVisibleEntities(net.minecraft.client.Camera camera,
            net.minecraft.client.renderer.culling.Frustum frustum, net.minecraft.client.DeltaTracker deltaTracker,
            net.minecraft.client.renderer.state.LevelRenderState levelRenderState, CallbackInfo ci) {
        EntityCullingModBase.instance.frustum = frustum;
    }

    //#endif

    //#if MC < 12109
    //$$ @Inject(at = @At("HEAD"), method = "renderEntity", cancellable = true)
    //$$private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
    //$$      PoseStack matrices, MultiBufferSource vertexConsumers, org.spongepowered.asm.mixin.injection.callback.CallbackInfo info) {
    //$$  if (EntityCullingModBase.instance.config.skipEntityCulling) {
    //$$      return;
    //$$  }
    //$$ Cullable cullable = (Cullable) entity;
    //$$ if (!cullable.isForcedVisible() && cullable.isCulled() && !NMSCullingHelper.ignoresCulling(entity)) {
    //$$    if (this.entityCulling$entityRenderDispatcher.getRenderer(entity) instanceof net.minecraft.client.renderer.entity.EntityRenderer entityRenderer
    //$$            && entityRenderer instanceof dev.tr7zw.entityculling.access.EntityRendererInter entityRendererInter) {
    //$$        if (EntityCullingModBase.instance.config.renderNametagsThroughWalls && matrices != null
    //$$                && vertexConsumers != null && entityRendererInter.shadowShouldShowName(entity)) {
    //$$           double x = net.minecraft.util.Mth.lerp((double) tickDelta, (double) entity.xOld, (double) entity.getX()) - cameraX;
    //$$            double y = net.minecraft.util.Mth.lerp((double) tickDelta, (double) entity.yOld, (double) entity.getY()) - cameraY;
    //$$            double z = net.minecraft.util.Mth.lerp((double) tickDelta, (double) entity.zOld, (double) entity.getZ()) - cameraZ;
    //$$            var vec3d = NMSCullingHelper.getRenderOffset(entityRenderer, entity, tickDelta);
    //$$            double d = x + vec3d.x;
    //$$            double e = y + vec3d.y;
    //$$            double f = z + vec3d.z;
    //$$            matrices.pushPose();
    //$$            matrices.translate(d, e, f);
    //$$            entityRendererInter.shadowRenderNameTag(entity, entity.getDisplayName(), matrices, vertexConsumers,
    //$$                    this.entityCulling$entityRenderDispatcher.getPackedLightCoords(entity, tickDelta),
    //$$                     tickDelta);
    //$$              matrices.popPose();
    //$$         }
    //#if MC >= 12104
    //$$           if (EntityCullingModBase.instance.debugHitboxes) {
    //$$               lateRenders.add(() -> {
    //$$                   renderDebugBox(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers, false);
    //$$                });
    //$$            }
    //#endif
    //$$            EntityCullingModBase.instance.skippedEntities++;
    //$$            info.cancel();
    //$$           return;
    //$$       }
    //$$    }
    //$$     EntityCullingModBase.instance.renderedEntities++;
    //$$    cullable.setOutOfCamera(false);
    //#if MC >= 12104
    //$$   if (EntityCullingModBase.instance.debugHitboxes) {
    //$$        lateRenders.add(() -> {
    //$$            renderDebugBox(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers, true);
    //$$        });
    //$$    }
    //#endif
    //$$}
    //#endif

    //#if MC >= 12104

    //    @Inject(at = @At("RETURN"), method = "renderEntities")
    //    private void renderEntities(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource, net.minecraft.client.Camera camera,
    //            net.minecraft.client.DeltaTracker deltaTracker, List<Entity> list, CallbackInfo info) {
    //        if (!lateRenders.isEmpty()) {
    //            //#if MC < 12105
    //            //$$ RenderSystem.disableDepthTest();
    //            //#endif
    //            for (Runnable r : lateRenders) {
    //                r.run();
    //            }
    //            //#if MC < 12105
    //            //$$ RenderSystem.enableDepthTest();
    //            //#endif
    //            lateRenders.clear();
    //        }
    //    }

    private void renderDebugBox(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
            PoseStack matrices, MultiBufferSource vertexConsumers, boolean visible) {
        AABB boundingBox = NMSCullingHelper.getCullingBox(entity);
        double maxX = MathUtilities.ceil(boundingBox.maxX + aabbExpansion) - cameraX;
        double maxY = MathUtilities.ceil(boundingBox.maxY + aabbExpansion) - cameraY;
        double maxZ = MathUtilities.ceil(boundingBox.maxZ + aabbExpansion) - cameraZ;
        double minX = MathUtilities.floor(boundingBox.minX - aabbExpansion) - cameraX;
        double minY = MathUtilities.floor(boundingBox.minY - aabbExpansion) - cameraY;
        double minZ = MathUtilities.floor(boundingBox.minZ - aabbExpansion) - cameraZ;

        DebugRenderer.renderFilledBox(matrices, vertexConsumers, new AABB(maxX, maxY, maxZ, minX, minY, minZ),
                visible ? 0f : 1f, visible ? 1f : 0f, 0f, 0.25f);
    }

    //#endif

}
