package dev.tr7zw.entityculling;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;

import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CullTask implements Runnable {

	public boolean requestCull = false;

	private final OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getInstance();
	private final int sleepDelay = EntityCullingMod.instance.config.sleepDelay;
	private final int hitboxLimit = EntityCullingMod.instance.config.hitboxLimit;
	private final Set<BlockEntityType<?>> unCullable;
	public long lastTime = 0;
	
	// reused preallocated vars
	private Vec3d lastPos = new Vec3d(0, 0, 0);
	private Vec3d aabbMin = new Vec3d(0, 0, 0);
	private Vec3d aabbMax = new Vec3d(0, 0, 0);

	public CullTask(OcclusionCullingInstance culling, Set<BlockEntityType<?>> unCullable) {
		this.culling = culling;
		this.unCullable = unCullable;
	}
	
	@Override
	public void run() {
		while (client.isRunning()) {
			try {
				Thread.sleep(sleepDelay);

				if (EntityCullingMod.enabled && client.level != null && client.player != null && client.player.tickCount > 10) {
				    Vec3 cameraMC = EntityCullingMod.instance.config.debugMode
                            ? client.player.getEyePosition(client.getDeltaFrameTime())
                            : client.gameRenderer.getMainCamera().getPosition();
					
					if (requestCull || !(cameraMC.x == lastPos.x && cameraMC.y == lastPos.y && cameraMC.z == lastPos.z)) {
						long start = System.currentTimeMillis();
						requestCull = false;
						lastPos.set(cameraMC.x, cameraMC.y, cameraMC.z);
						Vec3d camera = lastPos;
						culling.resetCache();
						boolean spectator = client.player.isSpectator();
						for (int x = -8; x <= 8; x++) {
							for (int z = -8; z <= 8; z++) {
							    LevelChunk chunk = client.level.getChunk(client.player.chunkPosition().x + x,
                                        client.player.chunkPosition().z + z);
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
										if(pos.closerThan(cameraMC, 64)) { // 64 is the fixed max tile view distance
										    aabbMin.set(pos.getX(), pos.getY(), pos.getZ());
										    aabbMax.set(pos.getX()+1d, pos.getY()+1d, pos.getZ()+1d);
    										boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
    										cullable.setCulled(!visible);
										}
									}
								}

							}
						}
						Entity entity = null;
						Iterator<Entity> iterable = client.level.entitiesForRendering().iterator();
						while (iterable.hasNext()) {
							try {
								entity = iterable.next();
							} catch (NullPointerException | ConcurrentModificationException ex) {
								break; // We are not synced to the main thread, so NPE's/CME are allowed here and way less
										// overhead probably than trying to sync stuff up for no really good reason
							}
							if(entity == null || !(entity instanceof Cullable)) {
							    continue; // Not sure how this could happen outside from mixin screwing up the inject into Entity
							}
							Cullable cullable = (Cullable) entity;
							if (!cullable.isForcedVisible()) {
								if (spectator || entity.isCurrentlyGlowing() || isSkippableArmorstand(entity)) {
									cullable.setCulled(false);
									continue;
								}
							    if(!entity.position().closerThan(cameraMC, EntityCullingMod.instance.config.tracingDistance)) {
							        cullable.setCulled(false); // If your entity view distance is larger than tracingDistance just render it
							        continue;
							    }
							    AABB boundingBox = entity.getBoundingBoxForCulling();
							    if(boundingBox.getXsize() > hitboxLimit || boundingBox.getYsize() > hitboxLimit || boundingBox.getZsize() > hitboxLimit) {
								    cullable.setCulled(false); // To big to bother to cull
								    continue;
								}
							    aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
							    aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
								boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
								cullable.setCulled(!visible);
							}
						}
						lastTime = (System.currentTimeMillis()-start);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Shutting down culling task!");
	}
	
	private boolean isSkippableArmorstand(Entity entity) {
	    if(!EntityCullingMod.instance.config.skipMarkerArmorStands)return false;
	    return entity instanceof ArmorStand && ((ArmorStand) entity).isMarker();
	}
}
