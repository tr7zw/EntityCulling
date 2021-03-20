package dev.tr7zw.entityculling;

import java.util.HashSet;
import java.util.Set;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.options.KeyBinding;

public class EntityCullingMod implements ModInitializer {

	public static EntityCullingMod instance;
	public final OcclusionCullingInstance culling = new OcclusionCullingInstance(128, new FabricProvider());
	public Set<BlockEntityType<?>> unCullable = new HashSet<>();
	public boolean nametags = true;
	public boolean debug = false;
	public boolean debugHitboxes = false;
	public static boolean enabled = true; //public static to make it faster for the jvm
	private CullTask cullTask = new CullTask(culling, unCullable);
	private Thread cullThread;
	private KeyBinding keybind = new KeyBinding("key.entityculling.toggle", -1, "EntityCulling");
	private boolean pressed = false;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		instance = this;
		ClientTickEvents.START_WORLD_TICK.register((event) -> {
			cullTask.requestCull = true;
		});
	    ClientTickEvents.START_CLIENT_TICK.register(e ->
	    {
	        if(keybind.isPressed()) {
	        	if(pressed)return;
	        	pressed = true;
	        	enabled = !enabled;
	        }else {
	        	pressed = false;
	        }
	    });
		unCullable.add(BlockEntityType.BEACON);// TODO: Move to config
		cullThread = new Thread(cullTask, "CullThread");
		cullThread.setUncaughtExceptionHandler((thread, ex) -> {
			System.out.println("The CullingThread has crashed! Please report the following stacktrace!");
			ex.printStackTrace();
		});
		cullThread.start();
		KeyBindingHelper.registerKeyBinding(keybind);
	}
}
