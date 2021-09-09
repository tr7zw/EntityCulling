package dev.tr7zw.entityculling;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

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
        ClientTickEvents.START_CLIENT_TICK.register(e ->
        {
            this.clientTick();
        });
        KeyBindingHelper.registerKeyBinding(keybind);
    }

}
