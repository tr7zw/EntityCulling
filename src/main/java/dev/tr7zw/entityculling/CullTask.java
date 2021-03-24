package dev.tr7zw.entityculling;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.logisticscraft.occlusionculling.util.AxisAlignedBB;
import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import dev.tr7zw.entityculling.access.Cullable;
import dev.tr7zw.entityculling.access.EntityAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import com.logisticscraft.occlusionculling.util.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class CullTask implements Runnable {

	public boolean requestCull = false;

	private final OcclusionCullingInstance culling;
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final int sleepDelay = 10;
	private final Set<BlockEntityType<?>> unCullable;
	private Vec3d lastPos = new Vec3d(0, 0, 0);
	private long lastTime = 0;

	public CullTask(OcclusionCullingInstance culling, Set<BlockEntityType<?>> unCullable) {
		this.culling = culling;
		this.unCullable = unCullable;
	}
	
	@Override
	public void run() {
		while (client.isRunning()) {
			try {
				Thread.sleep(sleepDelay);

				if (EntityCullingMod.enabled && client.world != null && client.player != null && client.player.age > 10) {
					net.minecraft.util.math.Vec3d cameraMC = EntityCullingMod.instance.debug
							? client.player.getCameraPosVec(client.getTickDelta())
							: client.gameRenderer.getCamera().getPos();
					Vec3d camera = new Vec3d(cameraMC.x, cameraMC.y, cameraMC.z);
					if (requestCull || !lastPos.equals(camera)) {
						long start = System.currentTimeMillis();
						requestCull = false;
						lastPos = camera;
						culling.resetCache();
						boolean spectator = client.player.isSpectator();
						for (int x = -8; x <= 8; x++) {
							for (int z = -8; z <= 8; z++) {
								WorldChunk chunk = client.world.getChunk(client.player.chunkX + x,
										client.player.chunkZ + z);
								Iterator<Entry<BlockPos, BlockEntity>> iterator = chunk.getBlockEntities().entrySet().iterator();
								Entry<BlockPos, BlockEntity> entry;
								while(iterator.hasNext()) {
									try {
										entry = iterator.next();
									}catch(NullPointerException | ConcurrentModificationException ex) {
										break; // We are not synced to the main thread, so NPE's/CME are allowed here and way less
										// overhead probably than trying to sync stuff up for no really good reason
									}
									if(unCullable.contains(entry.getValue().getType())) {
										continue;
									}
									Cullable cullable = (Cullable) entry.getValue();
									if (!cullable.isForcedVisible()) {
										if (spectator) {
											cullable.setCulled(false);
											continue;
										}
										BlockPos pos = entry.getKey();
										if(pos.isWithinDistance(cameraMC, 64)) { // 64 is the fixed max tile view distance
    										boolean visible = culling.isAABBVisible(
    										        new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1d, +pos.getY()+1d, pos.getZ()+1d), camera);
    										cullable.setCulled(!visible);
										}
									}
								}

							}
						}
						Entity entity = null;
						Iterator<Entity> iterable = client.world.getEntities().iterator();
						while (iterable.hasNext()) {
							try {
								entity = iterable.next();
							} catch (NullPointerException | ConcurrentModificationException ex) {
								break; // We are not synced to the main thread, so NPE's/CME are allowed here and way less
										// overhead probably than trying to sync stuff up for no really good reason
							}
							Cullable cullable = (Cullable) entity;
							if (!cullable.isForcedVisible()) {
								if (spectator || ((EntityAccessor)entity).isUnsafeGlowing()) {
									cullable.setCulled(false);
								} else {
								    if(entity.getPos().isInRange(cameraMC, 128)) { // Max supported range currently for this mod
    									Box boundingBox = entity.getVisibilityBoundingBox();
    									boolean visible = culling.isAABBVisible(
    											new AxisAlignedBB(boundingBox.minX, boundingBox.minY,
    													boundingBox.minZ, boundingBox.maxX, boundingBox.maxY,
    													boundingBox.maxZ),
    											camera);
    									cullable.setCulled(!visible);
								    } else {
								        cullable.setCulled(false); // If your entity view distance is larger than 128 blocks just render it
								    }
								}
							}
						}
						lastTime = (System.currentTimeMillis()-start);
					}
					if(!client.fpsDebugString.contains("CullTime"))
						client.fpsDebugString += " CullTime: " + lastTime + "ms"; // Bit hacky, but works for now :shrug:
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Shutting down culling task!");
	}
}
