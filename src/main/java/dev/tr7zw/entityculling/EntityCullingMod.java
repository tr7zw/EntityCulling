package dev.tr7zw.entityculling;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class EntityCullingMod extends EntityCullingModBase implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        super.onInitialize();
    }

    @Override
    public void initModloader() {
        ClientTickEvents.START_WORLD_TICK.register((event) -> {
            this.worldTick();
        });
        ClientTickEvents.START_CLIENT_TICK.register(e -> {
            this.clientTick();
        });
        KeyBindingHelper.registerKeyBinding(keybind);
    }

    @Override
    public AABB setupAABB(BlockEntity entity, BlockPos pos) {
        return new AABB(pos);
    }

}
