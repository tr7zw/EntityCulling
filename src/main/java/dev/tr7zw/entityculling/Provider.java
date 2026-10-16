package dev.tr7zw.entityculling;

import dev.tr7zw.entityculling.versionless.occlusionculling.DataProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getInstance();
    private ClientLevel world = null;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.level;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        var state = world.getBlockState(pos.set(x, y, z));
        if (EntityCullingModBase.instance.config.solidLeaves && state.getBlock() instanceof LeavesBlock) {
            return true;
        }
        //? if <= 1.21.1 {
        /*
         return state.isSolidRender(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        *///? } else {

        return state.isSolidRender();
        //? }
    }

    @Override
    public void cleanup() {
        world = null;
    }

}
