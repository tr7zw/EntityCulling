package dev.tr7zw.entityculling;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "entityculling", name = "EntityCulling", version = "1.6.2", clientSideOnly = true)
public class EntityCullingMod {

    public static EntityCullingMod instance = new EntityCullingMod();
    public OcclusionCullingInstance culling;
    public boolean debugHitboxes = false;
    public static boolean enabled = true; // public static to make it faster for the jvm
    public CullTask cullTask;
    private Thread cullThread;
    protected KeyBinding keybind = new KeyBinding("key.entityculling.toggle", Keyboard.KEY_NONE, "text.entityculling.title");

    public Config config;
    private final File settingsFile = new File("config", "entityculling.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //stats
    public int renderedBlockEntities = 0;
    public int skippedBlockEntities = 0;
    public int renderedEntities = 0;
    public int skippedEntities = 0;
    //public int tickedEntities = 0;
    //public int skippedEntityTicks = 0;

    // TODO: Should probably be using FMLPreInitializationEvent
    public EntityCullingMod() {
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
            if (ConfigUpgrader.upgradeConfig(config)) {
                writeConfig(); // Config got modified
            }
        }
        culling = new OcclusionCullingInstance(config.tracingDistance, new Provider());
        cullTask = new CullTask(culling, config.blockEntityWhitelist);

        cullThread = new Thread(cullTask, "CullThread");
        cullThread.setUncaughtExceptionHandler((thread, ex) -> {
            System.out.println("The CullingThread has crashed! Please report the following stacktrace!");
            ex.printStackTrace();
        });
        cullThread.start();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        ClientRegistry.registerKeyBinding(keybind);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void writeConfig() {
        if (settingsFile.exists()) {
            settingsFile.delete();
        }
        try {
            Files.write(settingsFile.toPath(), gson.toJson(config).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        cullTask.requestCull = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        cullTask.requestCull = true;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.showDebugInfo || mc.gameSettings.reducedDebugInfo || mc.thePlayer.hasReducedDebug()) {
            return;
        }

        event.left.add("[Culling] Last pass: " + cullTask.lastTime + "ms");
        event.left.add("[Culling] Rendered Block Entities: " + renderedBlockEntities + " Skipped: " + skippedBlockEntities);
        event.left.add("[Culling] Rendered Entities: " + renderedEntities + " Skipped: " + skippedEntities);
        //event.left.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);

        renderedBlockEntities = 0;
        skippedBlockEntities = 0;
        renderedEntities = 0;
        skippedEntities = 0;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        keyBindPressed();
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        keyBindPressed();
    }

    public void keyBindPressed() {
        if (keybind.isPressed()) {
            enabled = !enabled;
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            if (enabled) {
                if (player != null) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Culling on"));
                }
            } else {
                if (player != null) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Culling off"));
                }
            }
        }
    }
}
