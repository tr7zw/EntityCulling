package dev.tr7zw.entityculling;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


public class EntityCullingMod extends EntityCullingModBase {
    
    public EntityCullingMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        onInitialize();
    }


    @Override
    public void initModloader() {
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, keybind);
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
    
    private void doWorldTick(LevelTickEvent event) {
        this.worldTick();
    }

    @Override
    public AABB setupAABB(BlockEntity entity, BlockPos pos) {
        return entity.getRenderBoundingBox();
    }
    
}
