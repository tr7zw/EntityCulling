package dev.tr7zw.entityculling;

import com.logisticscraft.occlusionculling.DataProvider;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getMinecraft();
    private WorldClient world = null;
    
    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.world;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isOpaqueCube(state);
    }

    @Override
    public void cleanup() {
        world = null;
    }

}
