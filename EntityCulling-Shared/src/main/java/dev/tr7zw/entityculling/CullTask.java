package dev.tr7zw.entityculling;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;

import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;

public class CullTask implements Runnable {

	public boolean requestCull = false;

	private final OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getMinecraft();
	private final int sleepDelay = EntityCullingModBase.instance.config.sleepDelay;
	private final int hitboxLimit = EntityCullingModBase.instance.config.hitboxLimit;
	private final Set<String> unCullable;
	public long lastTime = 0;
	
	// reused preallocated vars
	private Vec3d lastPos = new Vec3d(0, 0, 0);
	private Vec3d aabbMin = new Vec3d(0, 0, 0);
	private Vec3d aabbMax = new Vec3d(0, 0, 0);

	public CullTask(OcclusionCullingInstance culling, Set<String> unCullable) {
		this.culling = culling;
		this.unCullable = unCullable;
	}
	
	@Override
	public void run() {
		while (client != null) { //FIXME
			try {
				Thread.sleep(sleepDelay);

				if (EntityCullingModBase.enabled && client.theWorld != null && client.thePlayer != null && client.thePlayer.ticksExisted > 10) {
				    Vec3 cameraMC = EntityCullingModBase.instance.config.debugMode
                            ? client.thePlayer.getPositionEyes(0)//FIXME?
                            : client.getRenderViewEntity().getPositionVector();
					
					if (requestCull || !(cameraMC.xCoord == lastPos.x && cameraMC.yCoord == lastPos.y && cameraMC.zCoord == lastPos.z)) {
						long start = System.currentTimeMillis();
						requestCull = false;
						lastPos.set(cameraMC.xCoord, cameraMC.yCoord, cameraMC.zCoord);
						Vec3d camera = lastPos;
						culling.resetCache();
						boolean spectator = client.thePlayer.isSpectator();
						for (int x = -8; x <= 8; x++) {
							for (int z = -8; z <= 8; z++) {
							    Chunk chunk = client.theWorld.getChunkFromChunkCoords((int)client.thePlayer.posX/16 + x, //FIXME
							            (int)client.thePlayer.posZ/16 + z);
								Iterator<Entry<BlockPos, TileEntity>> iterator = chunk.getTileEntityMap().entrySet().iterator();
								Entry<BlockPos, TileEntity> entry;
								while(iterator.hasNext()) {
									try {
										entry = iterator.next();
									}catch(NullPointerException | ConcurrentModificationException ex) {
										break; // We are not synced to the main thread, so NPE's/CME are allowed here and way less
										// overhead probably than trying to sync stuff up for no really good reason
									}
									if(unCullable.contains(entry.getValue().getBlockType().getUnlocalizedName())) { //FIXME?
										continue;
									}
									Cullable cullable = (Cullable) entry.getValue();
									if (!cullable.isForcedVisible()) {
										if (spectator) {
											cullable.setCulled(false);
											continue;
										}
										BlockPos pos = entry.getKey();
										if(pos.distanceSq(cameraMC.xCoord, cameraMC.yCoord, cameraMC.zCoord) < 64*64) { // 64 is the fixed max tile view distance
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
						Iterator<Entity> iterable = client.theWorld.getLoadedEntityList().iterator();
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
								if (spectator || isSkippableArmorstand(entity)) {
									cullable.setCulled(false);
									continue;
								}
							    if(entity.getPositionVector().squareDistanceTo(cameraMC) > EntityCullingModBase.instance.config.tracingDistance * EntityCullingModBase.instance.config.tracingDistance) {
							        cullable.setCulled(false); // If your entity view distance is larger than tracingDistance just render it
							        continue;
							    }
							    AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
							    /*if(boundingBox.x() > hitboxLimit || boundingBox.getYsize() > hitboxLimit || boundingBox.getZsize() > hitboxLimit) {
								    cullable.setCulled(false); // To big to bother to cull
								    continue;
								}*/
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
	    if(!EntityCullingModBase.instance.config.skipMarkerArmorStands)return false;
	    return entity instanceof EntityArmorStand && ((EntityArmorStand) entity).hasMarker();
	}
}
