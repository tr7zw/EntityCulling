package dev.tr7zw.entityculling;

import com.logisticscraft.occlusionculling.DataProvider;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class FabricProvider implements DataProvider {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private ClientWorld world = null;
    
    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.world;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return world.getBlockState(pos).isOpaqueFullCube(world, pos);
    }

    @Override
    public void cleanup() {
        world = null;
    }

}
