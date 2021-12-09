package dev.tr7zw.entityculling;

import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("entityculling")
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        if(onServer)return;
        onInitialize();
    }


    @Override
    public void initModloader() {
        ClientRegistry.registerKeyBinding(keybind);
        MinecraftForge.EVENT_BUS.addListener(this::doClientTick);
        MinecraftForge.EVENT_BUS.addListener(this::doWorldTick);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString(),
                        (remote, isServer) -> true));
    }
    
    private void doClientTick(ClientTickEvent event) {
        this.clientTick();
    }
    
    private void doWorldTick(WorldTickEvent event) {
        this.worldTick();
    }
    
}
