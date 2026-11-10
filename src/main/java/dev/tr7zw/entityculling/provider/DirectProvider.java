package dev.tr7zw.entityculling.provider;

import dev.tr7zw.entityculling.*;
import dev.tr7zw.entityculling.util.*;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.DataProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;

public class DirectProvider implements DataProvider {

    private final Minecraft client = Minecraft.getInstance();
    private ClientLevel world = null;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    @Override
    public boolean prepareChunk(int chunkX, int chunkY, int chunkZ) {
        world = client.level;
        return world != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        var state = world.getBlockState(pos.set(x, y, z));
        return ECUtil.isOpaqueFullCube(state);
    }

    @Override
    public void postRun() {
        world = null;
    }

}
