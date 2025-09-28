package dev.tr7zw.entityculling;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import dev.tr7zw.entityculling.config.ConfigScreenProvider;
import dev.tr7zw.transition.loader.ModLoaderEventUtil;
import dev.tr7zw.transition.loader.ModLoaderUtil;
import dev.tr7zw.transition.mc.ComponentProvider;
import dev.tr7zw.transition.mc.GeneralUtil;
//#if MC >= 12109
import dev.tr7zw.entityculling.debugEntries.*;
//#endif

public class EntityCullingMod extends EntityCullingModBase
        //#if FABRIC
        implements net.fabricmc.api.ClientModInitializer
//#endif
{

    //#if FABRIC
    @Override
    //#endif
    public void onInitializeClient() {
        super.onInitialize();
    }

    @Override
    public void initModloader() {
        ModLoaderEventUtil.registerClientTickStartListener(this::clientTick);
        ModLoaderEventUtil.registerWorldTickStartListener(this::worldTick);
        ModLoaderUtil.registerKeybind(keybind);
        //#if MC >= 12104
        ModLoaderUtil.registerKeybind(keybindBoxes);
        //#endif
        ModLoaderUtil.registerConfigScreen(ConfigScreenProvider::createConfigScreen);
        ModLoaderUtil.disableDisplayTest();

        //#if MC >= 12109
        registerDebugLine("cull_timing", new CullTimingEntry());
        registerDebugLine("culled_entities", new CulledEntitiesEntry());
        registerDebugLine("culled_block_entities", new CulledBlockEntitiesEntry());
        registerDebugLine("tick_culling", new TickCullingEntry());
        //#endif
    }

    @Override
    public AABB setupAABB(BlockEntity entity, BlockPos pos) {
        //#if FABRIC || NEOFORGE
        return new AABB(pos);
        //#else
        //$$       return entity.getRenderBoundingBox();
        //#endif
    }

    //#if MC >= 12109
    public static final net.minecraft.client.gui.components.debug.DebugEntryCategory DEBUG_CATEGORY = new net.minecraft.client.gui.components.debug.DebugEntryCategory(
            ComponentProvider.translatable("text.entityculling.title"), 10F);
    public static final ResourceLocation DEBUG_CATEGORY_ID = GeneralUtil.getResourceLocation("entityculling", "debug");

    private static void registerDebugLine(String id, net.minecraft.client.gui.components.debug.DebugScreenEntry entry) {
        net.minecraft.client.gui.components.debug.DebugScreenEntries
                .register(GeneralUtil.getResourceLocation("entityculling", id), entry);
    }
    //#endif
}
