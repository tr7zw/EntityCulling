package dev.tr7zw.entityculling;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class EntityCullingMod extends EntityCullingModBase implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        super.onInitialize();
    }

    /*@Override
    public void initModloader() {
        ClientTickEvents.START_WORLD_TICK.register((event) -> {
            this.worldTick();
        });
        ClientTickEvents.START_CLIENT_TICK.register(e -> {
            this.clientTick();
        });
        KeyBindingHelper.registerKeyBinding(keybind);
    }*/

    @Override
    public Box setupBox(BlockEntity entity, BlockPos pos) {
        Box boundingBox = entity.getBlock().getBoundingBox(entity.world, entity.x, entity.y, entity.z);
        if (entity instanceof SignBlockEntity) System.out.println(boundingBox);
        return boundingBox;
    }

}
