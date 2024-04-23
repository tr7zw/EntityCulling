package dev.tr7zw.entityculling;

import com.logisticscraft.occlusionculling.DataProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getInstance();
    private ClientLevel world = null;

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        world = client.level;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return world.getBlockState(pos).isSolidRender(world, pos);
    }

    @Override
    public void cleanup() {
        world = null;
    }

}
