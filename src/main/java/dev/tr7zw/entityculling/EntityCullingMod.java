package dev.tr7zw.entityculling;


import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;

@Mod(modid = "entityculling", name = "EntityCulling", version = "@VER@"/*, clientSideOnly = true*/) //TODO Client side only?
public class EntityCullingMod extends EntityCullingModBase {

    //Forge only
    private boolean onServer = false;

    public EntityCullingMod() {
        try {
            Class clientClass = net.minecraft.client.Minecraft.class;
        }catch(Throwable ex) {
            System.out.println("EntityCulling Mod installed on a Server. Going to sleep.");
            onServer = true;
            return;
        }
        onInitialize();
    }

    @Override
    public void initModloader() {

   }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        ClientRegistry.registerKeyBinding(keybind);
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        cullTask.populateWhitelist(config.blockEntityWhitelist);
    }

    @SubscribeEvent
    public void doClientTick(TickEvent.ClientTickEvent event) {
        this.clientTick();
    }

    @SubscribeEvent
    public void doWorldTick(TickEvent.WorldTickEvent event) {
        this.worldTick();
    }

    @SubscribeEvent
    public void doRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo || !config.showDebugScreenInfo) return;
        ArrayList<String> left = event.left;
        left.add("[Culling] Last pass: " + EntityCullingModBase.instance.cullTask.lastTime + "ms");
        left.add("[Culling] Rendered Block Entities: " + EntityCullingModBase.instance.renderedBlockEntities + " Skipped: " + EntityCullingModBase.instance.skippedBlockEntities);
        left.add("[Culling] Rendered Entities: " + EntityCullingModBase.instance.renderedEntities + " Skipped: " + EntityCullingModBase.instance.skippedEntities);
        //list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);

        EntityCullingModBase.instance.renderedBlockEntities = 0;
        EntityCullingModBase.instance.skippedBlockEntities = 0;
        EntityCullingModBase.instance.renderedEntities = 0;
        EntityCullingModBase.instance.skippedEntities = 0;
    }

}
