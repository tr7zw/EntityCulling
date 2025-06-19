package dev.tr7zw.entityculling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import dev.tr7zw.entityculling.config.ConfigScreenProvider;
import dev.tr7zw.transition.loader.ModLoaderEventUtil;
import dev.tr7zw.transition.loader.ModLoaderUtil;

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
    }

    @Override
    public AABB setupAABB(BlockEntity entity, BlockPos pos) {
        //#if FABRIC || NEOFORGE
        return new AABB(pos);
        //#else
        //$$       return entity.getRenderBoundingBox();
        //#endif
    }
}
