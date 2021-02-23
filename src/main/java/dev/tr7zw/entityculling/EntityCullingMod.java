package dev.tr7zw.entityculling;

import java.util.HashSet;
import java.util.Set;

import dev.tr7zw.entityculling.occlusionculling.OcclusionCullingInstance;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.entity.BlockEntityType;

public class EntityCullingMod implements ModInitializer {

	public static EntityCullingMod instance;
	public final OcclusionCullingInstance culling = new OcclusionCullingInstance();
	public Set<BlockEntityType<?>> unCullable = new HashSet<>();
	public boolean nametags = true;
	public boolean debug = false;
	private CullTask cullTask = new CullTask(culling, unCullable);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		instance = this;
		ClientTickEvents.START_WORLD_TICK.register((event) -> {
			culling.resetCache();
			cullTask.requestCull = true;
		});
		unCullable.add(BlockEntityType.BEACON);// TODO: Move to config
		new Thread(cullTask, "CullThread").start();
	}
}
