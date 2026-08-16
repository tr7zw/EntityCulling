package dev.tr7zw.entityculling;

import dev.tr7zw.entityculling.mixin.RenderAccessor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

public final class RenderHook {
    private RenderHook() {
    }

    // Has to be in a separate class otherwise the mixin won't work
    // (seems to be a bug in Mixin 0.7.11)
    public static <E extends Entity> void handle(Render<E> render, E entity, double x, double y, double z) {
        @SuppressWarnings("unchecked")
        RenderAccessor<E> accessor = ((RenderAccessor<E>) render);
        accessor.callRenderName(entity, x, y, z);
    }
}
