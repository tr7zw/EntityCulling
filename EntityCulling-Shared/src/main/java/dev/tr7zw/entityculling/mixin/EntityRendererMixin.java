package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

@Mixin(Render.class)
public abstract class EntityRendererMixin<T extends Entity> extends Render<T> implements EntityRendererInter<T>  {

    protected EntityRendererMixin(RenderManager p_i46179_1_) {
        super(p_i46179_1_);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean shadowShouldShowName(T entity) {
        return true;
    }

    @Override
    public void shadowRenderNameTag(T p_renderName_1_, double p_renderName_2_, double d1, double d2) {
        renderName(p_renderName_1_, p_renderName_2_, d1, d2);
    }

}
