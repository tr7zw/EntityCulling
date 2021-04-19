package dev.tr7zw.entityculling;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public class EntityCullingMod implements ModInitializer {

	public static EntityCullingMod instance;
	public OcclusionCullingInstance culling;
	public Set<BlockEntityType<?>> unCullable = new HashSet<>();
	public boolean debugHitboxes = false;
	public static boolean enabled = true;
	public CullTask cullTask;
	private Thread cullThread;
	private KeyBinding keybind = new KeyBinding("key.entityculling.toggle", -1, "EntityCulling");
	private boolean pressed = false;
	
    public Config config;
    private final File settingsFile = new File("config", "entityculling.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	//stats
	public int renderedBlockEntities = 0;
	public int skippedBlockEntities = 0;
	public int renderedEntities = 0;
	public int skippedEntities = 0;

	@Override
	public void onInitialize() {
		instance = this;
        if (settingsFile.exists()) {
            try {
                config = gson.fromJson(new String(Files.readAllBytes(settingsFile.toPath()), StandardCharsets.UTF_8),
                        Config.class);
            } catch (Exception ex) {
                System.out.println("Error while loading config! Creating a new one!");
                ex.printStackTrace();
            }
        }
        if (config == null) {
            config = new Config();
            writeConfig();
        } else {
            if(ConfigUpgrader.upgradeConfig(config)) {
                writeConfig(); // Config got modified
            }
        }
        culling = new OcclusionCullingInstance(config.tracingDistance, new FabricProvider());
        cullTask = new CullTask(culling, unCullable);
		ClientTickEvents.START_WORLD_TICK.register((event) -> {
			cullTask.requestCull = true;
		});
	    ClientTickEvents.START_CLIENT_TICK.register(e ->
	    {
	        if(keybind.isPressed()) {
	        	if(pressed)return;
	        	pressed = true;
	        	enabled = !enabled;
	        	 ClientPlayerEntity player = MinecraftClient.getInstance().player;
	        	if(enabled) {
	        	    if (player != null) {
	                    player.sendSystemMessage(new LiteralText("Culling on").formatted(Formatting.GREEN),
	                            Util.NIL_UUID);
	                }
	        	} else {
                    if (player != null) {
                        player.sendSystemMessage(new LiteralText("Culling off").formatted(Formatting.RED),
                                Util.NIL_UUID);
                    }
	        	}
	        }else {
	        	pressed = false;
	        }
	    });
		for(String blockId : config.blockEntityWhitelist) {
		    Optional<BlockEntityType<?>> block = Registry.BLOCK_ENTITY_TYPE.getOrEmpty(new Identifier(blockId));
		    block.ifPresent(b -> {
		        unCullable.add(b);
		    });
		}
		cullThread = new Thread(cullTask, "CullThread");
		cullThread.setUncaughtExceptionHandler((thread, ex) -> {
			System.out.println("The CullingThread has crashed! Please report the following stacktrace!");
			ex.printStackTrace();
		});
		cullThread.start();
		KeyBindingHelper.registerKeyBinding(keybind);
	}
	
    public void writeConfig() {
        if (settingsFile.exists())
            settingsFile.delete();
        try {
            Files.write(settingsFile.toPath(), gson.toJson(config).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

	
}
