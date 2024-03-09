package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

@Mixin(Render.class)
public abstract class EntityRendererMixin<T extends Entity> implements EntityRendererInter<T>  {

    @Override
    public boolean shadowShouldShowName(T entity) {
        return canRenderName(entity);
    }

    @Override
    public void shadowRenderNameTag(T p_renderName_1_,  double x, double y, double z) {
        renderName(p_renderName_1_, x, y, z);
    }

    @Shadow
    protected abstract void renderName(T p_renderName_1_, double x, double y, double z);
    
    @Shadow
    protected abstract boolean canRenderName(T p_canRenderName_1_);
    
}
