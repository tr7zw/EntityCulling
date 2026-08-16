package dev.tr7zw.entityculling.mixin;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Render.class)
public interface RenderAccessor<T extends Entity> {
    @Invoker
    void callRenderName(T entity, double x, double y, double z);
}
