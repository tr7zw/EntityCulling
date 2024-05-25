package dev.tr7zw.entityculling;

import dev.tr7zw.util.ModLoaderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
//spotless:off 
//#if FABRIC
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//#elseif NEOFORGE
//#if MC >= 12005
//$$ import net.neoforged.neoforge.client.event.ClientTickEvent;
//$$ import net.neoforged.neoforge.event.tick.LevelTickEvent;
//#else
//$$ import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
//$$ import net.neoforged.neoforge.event.TickEvent.LevelTickEvent;
//#endif
//$$ import net.neoforged.fml.IExtensionPoint;
//$$ import net.neoforged.fml.ModLoadingContext;
//$$ import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
//$$ import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
//$$ import net.neoforged.neoforge.common.NeoForge;
//#else
//$$ import net.minecraftforge.common.MinecraftForge;
//$$ import net.minecraftforge.event.TickEvent.ClientTickEvent;
//$$ import net.minecraftforge.event.TickEvent.LevelTickEvent;
//$$ import net.minecraftforge.fml.IExtensionPoint;
//$$ import net.minecraftforge.fml.ModLoadingContext;
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//#endif
//spotless:on

//spotless:off 
//#if FABRIC
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
        ModLoaderUtil.registerKeybind(keybind);
    }

    //#else
  //$$  public class EntityCullingMod extends EntityCullingModBase {
  //$$
  //$$        public EntityCullingMod() {
  //$$           FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
  //$$       }
  //$$
  //$$      private void setup(final FMLCommonSetupEvent event) {
  //$$          onInitialize();
  //$$      }
  //$$
  //$$      @Override
  //$$      public void initModloader() {
  //$$         ModLoaderUtil.registerKeybind(keybind);
  //$$         ModLoaderUtil.registerForgeEvent(this::doClientTick);
  //$$         ModLoaderUtil.registerForgeEvent(this::doWorldTick);
    //#if FORGE
  //$$         ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
  //$$                 () -> new IExtensionPoint.DisplayTest(
  //$$                        () -> ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString(),
  //$$                         (remote, isServer) -> true));
    //#endif
  //$$    }
  //$$
    //#if NEOFORGE && MC >= 12005
  //$$    private void doClientTick(ClientTickEvent.Pre event) {
    //#else
  //$$    private void doClientTick(ClientTickEvent event) {
    //#endif
  //$$       this.clientTick();
  //$$    }
  //$$
  //#if NEOFORGE && MC >= 12005
  //$$   private void doWorldTick(LevelTickEvent.Pre event) {
  //#else
  //$$   private void doWorldTick(LevelTickEvent event) {
  //#endif
  //$$       this.worldTick();
  //$$   }
  //$$
    //#endif
    
    @Override
    public AABB setupAABB(BlockEntity entity, BlockPos pos) {
    //#if FABRIC || NEOFORGE
        return new AABB(pos);
    //#else
    //$$       return entity.getRenderBoundingBox();
    //#endif
    }
    //spotless:on
}
