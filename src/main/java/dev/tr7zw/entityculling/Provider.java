package dev.tr7zw.entityculling;

import com.logisticscraft.occlusionculling.DataProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getMinecraft();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private WorldClient world = null;

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.theWorld;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        return world.getBlockState(pos.set(x, y, z)).getBlock().isOpaqueCube();
    }

    @Override
    public void cleanup() {
        world = null;
    }

}
