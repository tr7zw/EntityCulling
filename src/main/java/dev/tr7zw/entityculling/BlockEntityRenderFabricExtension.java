package dev.tr7zw.entityculling;

import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;

/*
Copy from NeoForge, allowing mods to have the same functionality as NeoForge's BlockEntityRenderExtension on Fabric.
 */
public interface BlockEntityRenderFabricExtension<T extends BlockEntity> {

    default AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }
}
