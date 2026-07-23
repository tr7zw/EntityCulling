package dev.tr7zw.entityculling;

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
}
