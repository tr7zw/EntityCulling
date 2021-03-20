package dev.tr7zw.entityculling;

import com.logisticscraft.occlusionculling.DataProvider;
import com.logisticscraft.occlusionculling.util.Vec3d;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
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

    @Override
    public void checkingPosition(Vec3d[] targets, int targetSize, Vec3d viewerPosition) {
        if(!EntityCullingMod.instance.debugHitboxes)return;
        for (int i = 0; i < targetSize; i++) {
            Vec3d target = targets[i];
            client.world.addImportantParticle(ParticleTypes.HAPPY_VILLAGER, true, ((int) viewerPosition.x) + target.x,
                    ((int) viewerPosition.y) + target.y, ((int) viewerPosition.z) + target.z, 0, 0, 0);
        }
    }

}
