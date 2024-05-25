package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> implements EntityRendererInter<T> {

    @Override
    public boolean shadowShouldShowName(T entity) {
        return shouldShowName(entity);
    }

    @Override
    public void shadowRenderNameTag(T entity, Component component, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int light, float f) {
        renderNameTag(entity, component, poseStack, multiBufferSource, light
        // spotless:off
                //#if MC >= 12005
                    , f);
                //#else
                //$$);
                //#endif
                //spotless:on
    }

    @Shadow
    public abstract boolean shouldShowName(T entity);

    @Shadow
    public abstract void renderNameTag(T entity, Component component, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int i
            // spotless:off
          //#if MC >= 12005
            , float f);
          //#else
          //$$);
          //#endif
          //spotless:on

}
