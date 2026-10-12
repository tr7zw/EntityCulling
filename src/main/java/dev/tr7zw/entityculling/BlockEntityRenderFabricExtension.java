package dev.tr7zw.entityculling;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;

/*
Copy from NeoForge, allowing mods to have the same functionality as NeoForge's BlockEntityRenderExtension on Fabric.
See https://github.com/tr7zw/EntityCulling/issues/313 / https://github.com/cc-tweaked/CC-Tweaked/commit/f2314178a5e5a3e6c0f6d7400c467ad12b1f6650 for more information and example usage.
 */
public interface BlockEntityRenderFabricExtension<T extends BlockEntity> {

    default AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }

    // This only calculates this once per class, so we're not going to waste as much time with the lookup
    // like we normally would with reflection.
    ClassValue<Optional<Method>> RENDER_BOUNDING_BOX_GETTER = new ClassValue<>() {
        @Override
        protected Optional<Method> computeValue(Class<?> type) {
            try {
                Method method = type.getMethod("getRenderBoundingBox", BlockEntity.class);
                int modifiers = method.getModifiers();

                if (!Modifier.isStatic(modifiers) && method.getReturnType() == AABB.class) {
                    return Optional.of(method);
                }
            } catch (NoSuchMethodException ignored) {
            }

            return Optional.empty();
        }
    };

    static AABB tryGetRenderBoundingBox(BlockEntityRenderer renderer, BlockEntity blockEntity) {
        // There shouldn't be a performance impact for the value below, but just in case, let's bypass it.
        if (renderer instanceof BlockEntityRenderFabricExtension extension) {
            return extension.getRenderBoundingBox(blockEntity);
        }

        // Reflection time! Fortunately, this shouldn't be horrible on performance.
        Optional<Method> method = RENDER_BOUNDING_BOX_GETTER.get(renderer.getClass());
        if (method.isPresent()) {
            try {
                return (AABB) method.orElseThrow().invoke(renderer, blockEntity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return new AABB(blockEntity.getBlockPos());
    }
}
