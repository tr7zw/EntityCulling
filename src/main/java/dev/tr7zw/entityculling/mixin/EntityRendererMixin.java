package dev.tr7zw.entityculling.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import dev.tr7zw.entityculling.access.EntityRendererInter;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> implements EntityRendererInter<T> {

    @Override
    public boolean shadowShouldShowName(T entity) {
        return true;
    }

    @Override
    public void shadowRenderNameTag(T entity, String idk, int light) {
        //renderNameTag(entity, component, poseStack, multiBufferSource, light);
    }
    /*
    @Shadow
    public abstract boolean shouldShowName(T entity);

    @Shadow
    public abstract void renderNameTag(T entity, Component component, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int i);*/

}
