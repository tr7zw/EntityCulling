package net.fabricmc.example;

import java.util.Map.Entry;

import dev.tr7zw.entityculling.occlusionculling.AxisAlignedBB;
import dev.tr7zw.entityculling.occlusionculling.OcclusionCullingInstance;
import net.fabricmc.example.access.Cullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class CullTask implements Runnable {

	public boolean requestCull = false;

	private final OcclusionCullingInstance culling = new OcclusionCullingInstance();
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final AxisAlignedBB blockAABB = new AxisAlignedBB(0d, 0d, 0d, 1d, 1d, 1d);
	private final int sleepDelay = 10;

	@Override
	public void run() {
		while (client.isRunning()) {
			try {
				Thread.sleep(sleepDelay);

				if (client.world != null && requestCull) {
					requestCull = false;
					culling.resetCache();
					Vec3d camera = client.player.getCameraPosVec(client.getTickDelta()); // upate later to work with f3,
																							// currently for debugging
					for (int x = -3; x <= 3; x++) {
						for (int z = -3; z <= 3; z++) {
							WorldChunk chunk = client.world.getChunk(client.player.chunkX + x,
									client.player.chunkZ + z);
							for (Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
								Cullable cullable = (Cullable) entry.getValue();
								if (!cullable.isForcedVisible()) {
									BlockPos pos = entry.getKey();
									boolean visible = culling.isAABBVisible(
											new Vec3d(pos.getX(), pos.getY(), pos.getZ()), blockAABB, camera, false);
									cullable.setCulled(!visible);
								}
							}
							for (Entity entity : client.world.getEntities()) {
								Cullable cullable = (Cullable) entity;
								if (!cullable.isForcedVisible()) {
									Box boundingBox = entity.getVisibilityBoundingBox();
									boolean visible = ExampleMod.instance.culling.isAABBVisible(
											new Vec3d(entity.getPos().getX(), entity.getPos().getY(),
													entity.getPos().getZ()),
											new AxisAlignedBB(boundingBox.minX - 0.05, boundingBox.minY,
													boundingBox.minZ - 0.05, boundingBox.maxX + 0.05, boundingBox.maxY,
													boundingBox.maxZ + 0.05),
											client.player.getCameraPosVec(client.getTickDelta()), true);
									cullable.setCulled(!visible);
								}
							}

						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
