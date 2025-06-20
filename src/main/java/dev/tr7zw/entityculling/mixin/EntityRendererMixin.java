package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> implements EntityRendererInter<T> {

    @Override
    public boolean shadowShouldShowName(T entity) {
        //#if MC <= 12101
        //$$ return shouldShowName(entity);
        //#else
        return ((EntityRenderer) (Object) this).createRenderState(entity, 0).nameTag != null;
        //#endif
    }

    @Override
    public void shadowRenderNameTag(T entity, Component component, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int light, float delta) {
        //#if MC >= 12102
        renderNameTag(((EntityRenderer) (Object) this).createRenderState(entity, delta), component, poseStack,
                multiBufferSource, light);
        //#elseif MC >= 12005
        //$$ renderNameTag(entity, component, poseStack, multiBufferSource, light, delta);
        //#else
        //$$ renderNameTag(entity, component, poseStack, multiBufferSource, light);
        //#endif
    }

    //#if MC <= 12101
    //$$ @Shadow
    //$$ public abstract boolean shouldShowName(T entity);
    //#endif

    @Shadow
    //#if MC >= 12102
    public abstract void renderNameTag(net.minecraft.client.renderer.entity.state.EntityRenderState entityRenderState,
            Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i);
    //#elseif MC >= 12005
    //$$ public abstract void renderNameTag(T entity, Component component, PoseStack poseStack,
    //$$ MultiBufferSource multiBufferSource, int i, float f);
    //#else
    //$$    public abstract void renderNameTag(T entity, Component component, PoseStack poseStack,
    //$$ MultiBufferSource multiBufferSource, int i);
    //#endif

    @Override
    public boolean entityCullingIgnoresCulling(T entity) {
        //#if MC <= 12101
        //$$ return entity.noCulling;
        //#else
        return !affectedByCulling(entity);
        //#endif
    }

    @Override
    public AABB entityCullingGetCullingBox(T entity) {
        //#if MC <= 12101
        //$$ return entity.getBoundingBoxForCulling();
        //#else
        return getBoundingBoxForCulling(entity);
        //#endif
    }

    //#if MC >= 12102
    @Shadow
    abstract boolean affectedByCulling(T entity);

    @Shadow
    abstract AABB getBoundingBoxForCulling(T entity);
    //#endif

}
