package dev.tr7zw.entityculling;


import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

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
    }

    @SubscribeEvent
    public void doClientTick(TickEvent.ClientTickEvent event) {
        this.clientTick();
    }

    @SubscribeEvent
    public void doWorldTick(TickEvent.WorldTickEvent event) {
        this.worldTick();
    }

}
