package dev.tr7zw.entityculling.occlusionculling;

import java.util.Arrays;

import dev.tr7zw.entityculling.EntityCullingMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class OcclusionCullingInstance {

	//these can be final?
	private final Vec3d[] targets = new Vec3d[8];
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final int reach = 64;
	private final Cache cache = new ArrayCache(reach);

	public boolean isAABBVisible(Vec3d aabbBlock, AxisAlignedBB aabb, Vec3d playerLoc, boolean entity) {
		try {
			if (entity) {
				aabb.maxx -= aabbBlock.x;
				aabb.minx -= aabbBlock.x;
				aabb.maxy -= aabbBlock.y;
				aabb.miny -= aabbBlock.y;
				aabb.maxz -= aabbBlock.z;
				aabb.minz -= aabbBlock.z;
			}
			aabbBlock = aabbBlock.subtract(((int) playerLoc.x), ((int) playerLoc.y), ((int) playerLoc.z));
			int maxX = MathUtil.ceil(aabbBlock.x + aabb.maxx + 0.25);
			int maxY = MathUtil.ceil(aabbBlock.y + aabb.maxy + 0.25);
			int maxZ = MathUtil.ceil(aabbBlock.z + aabb.maxz + 0.25);
			int minX = MathUtil.fastFloor(aabbBlock.x + aabb.minx - 0.25);
			int minY = MathUtil.fastFloor(aabbBlock.y + aabb.miny - 0.25);
			int minZ = MathUtil.fastFloor(aabbBlock.z + aabb.minz - 0.25);

			Relative relX = Relative.from(minX, maxX);
			Relative relY = Relative.from(minY, maxY);
			Relative relZ = Relative.from(minZ + 1, maxZ + 1);
			if (minX <= 0 && maxX > 0 && minY <= 0 && maxY >= 0 && minZ < 0 && maxZ >= 0) {
				return true; // We are inside of the AABB, don't cull
			}
			int blockCount =(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
			Vec3d[] blocks = new Vec3d[blockCount];
			boolean[][] faceEdgeData = new boolean[blockCount][];
			int slot = 0;

			boolean[] onFaceEdge = new boolean[6];
			for (int x = minX; x < maxX; x++) {
				onFaceEdge[0] = x == minX;
				onFaceEdge[1] = x == maxX - 1;
				for (int y = minY; y < maxY; y++) {
					onFaceEdge[2] = y == minY;
					onFaceEdge[3] = y == maxY - 1;
					for (int z = minZ; z < maxZ; z++) {
						int cVal = getCacheValue(x, y, z);
						if (cVal == 1) {
							return true;
						}
						if (cVal == 0) {
							onFaceEdge[4] = z == minZ;
							onFaceEdge[5] = z == maxZ - 1;
							if ((onFaceEdge[0] && relX == Relative.POSITIVE)
									|| (onFaceEdge[1] && relX == Relative.NEGATIVE)
									|| (onFaceEdge[2] && relY == Relative.POSITIVE)
									|| (onFaceEdge[3] && relY == Relative.NEGATIVE)
									|| (onFaceEdge[4] && relZ == Relative.POSITIVE)
									|| (onFaceEdge[5] && relZ == Relative.NEGATIVE)) {
								blocks[slot] = new Vec3d(x, y, z);
								faceEdgeData[slot] = Arrays.copyOf(onFaceEdge, 6);
								slot++;
							}
						}
					}
				}
			}
			for (int i = 0; i < slot; i++) {
				if (isVoxelVisible(playerLoc, blocks[i], faceEdgeData[i],
						EntityCullingMod.instance.debugHitboxes && !entity)) {
					return true;
				}
			}
			return false;

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return true;
	}

	// -1 = invalid location, 0 = not checked yet, 1 = visible, 2 = blocked
	private int getCacheValue(int x, int y, int z) {
		if (Math.abs(x) > reach - 2 || Math.abs(y) > reach - 2 || Math.abs(z) > reach - 2)
			return -1;

		// check if target is already known
		int cx = MathUtil.fastFloor(x + reach);
		int cy = MathUtil.fastFloor(y + reach);
		int cz = MathUtil.fastFloor(z + reach);
		return cache.getState(cx, cy, cz);
		
	}

	private boolean isVoxelVisible(Vec3d playerLoc, Vec3d position, boolean[] faceEdgeData, boolean showDebug) {
		int targetSize = 0;
		// boolean onMinX = faceEdgeData[0];
		// boolean onMaxX = faceEdgeData[1];
		// boolean onMinY = faceEdgeData[2];
		// boolean onMaxY = faceEdgeData[3];
		// boolean onMinZ = faceEdgeData[4];
		// boolean onMaxZ = faceEdgeData[5];
		// main points for all faces
		position = position.add(0.05,0.05,0.05);
		if (faceEdgeData[0] || faceEdgeData[4] || faceEdgeData[2]) {
			targets[targetSize++] = position;
		}
		if (faceEdgeData[1]) {
			targets[targetSize++] = position.add(0.90, 0, 0);
		}
		if (faceEdgeData[3]) {
			targets[targetSize++] = position.add(0, 0.90, 0);
		}
		if (faceEdgeData[5]) {
			targets[targetSize++] = position.add(0, 0, 0.90);
		}
		// Extra corner points
		if ((faceEdgeData[4] && faceEdgeData[1] && faceEdgeData[3]) || (faceEdgeData[1] && faceEdgeData[3])) {
			targets[targetSize++] = position.add(0.90, 0.90, 0);
		}
		if ((faceEdgeData[0] && faceEdgeData[5] && faceEdgeData[3]) || (faceEdgeData[5] && faceEdgeData[3])) {
			targets[targetSize++] = position.add(0, 0.90, 0.90);
		}
		if (faceEdgeData[5] && faceEdgeData[1]) {
			targets[targetSize++] = position.add(0.90, 0, 0.90);
		}
		if (faceEdgeData[1] && faceEdgeData[3] && faceEdgeData[5]) {
			targets[targetSize++] = position.add(0.90, 0.90, 0.90);
		}

		if (showDebug) {
			for (int i = 0; i < targetSize; i++) {
				Vec3d target = targets[i];
				client.world.addImportantParticle(ParticleTypes.HAPPY_VILLAGER, true, ((int) playerLoc.x) + target.x,
						((int) playerLoc.y) + target.y, ((int) playerLoc.z) + target.z, 0, 0, 0);
			}
		}
		return isVisible(playerLoc, targets, targetSize);
	}



	/**
	 * returns the grid cells that intersect with this Vec3d<br>
	 * <a href=
	 * "http://playtechs.blogspot.de/2007/03/raytracing-on-grid.html">http://playtechs.blogspot.de/2007/03/raytracing-on-grid.html</a>
	 * 
	 * Caching assumes that all Vec3d's are inside the same block
	 */
	private boolean isVisible(Vec3d start, Vec3d[] targets, int size) {

		for (int v = 0; v < size; v++) {
			///raycasting target
			Vec3d target = targets[v];

			// coordinates of start and target point
			double startX = start.getX();
			double startY = start.getY();
			double startZ = start.getZ();

			double relativeX = startX + target.getX();
			double relativeY = startY + target.getY();
			double relativeZ = startZ + target.getZ();

			// horizontal and vertical cell amount spanned
			double dimensionX = Math.abs(relativeX - startX);
			double dimensionY = Math.abs(relativeY - startY);
			double dimensionZ = Math.abs(relativeZ - startZ);

			// start cell coordinate
			int x = MathUtil.floor(startX);
			int y = MathUtil.floor(startY);
			int z = MathUtil.floor(startZ);

			// distance between horizontal intersection points with cell border as a
			// fraction of the total Vec3d length
			double dimFracX = 1f / dimensionX;
			// distance between vertical intersection points with cell border as a fraction
			// of the total Vec3d length
			double dimFracY = 1f / dimensionY;
			double dimFracZ = 1f / dimensionZ;

			// total amount of intersected cells
			int intersectCount = 1;

			// 1, 0 or -1
			// determines the direction of the next cell (horizontally / vertically)
			int x_inc, y_inc, z_inc;

			// the distance to the next horizontal / vertical intersection point with a cell
			// border as a fraction of the total Vec3d length
			double t_next_y, t_next_x, t_next_z;

			if (dimensionX == 0f) {
				x_inc = 0;
				t_next_x = dimFracX; // don't increment horizontally because the Vec3d is perfectly vertical
			} else if (relativeX > startX) {
				x_inc = 1; // target point is horizontally greater than starting point so increment every
							// step by 1
				intersectCount += MathUtil.floor(relativeX) - x; // increment total amount of intersecting cells
				t_next_x = (float) ((MathUtil.floor(startX) + 1 - startX) * dimFracX); // calculate the next horizontal
																				// intersection
				// point based on the position inside
				// the first cell
			} else {
				x_inc = -1; // target point is horizontally smaller than starting point so reduce every step
							// by 1
				intersectCount += x - MathUtil.floor(relativeX); // increment total amount of intersecting cells
				t_next_x = (float) ((startX - MathUtil.floor(startX)) * dimFracX); // calculate the next horizontal
																			// intersection point
				// based on the position inside
				// the first cell
			}

			if (dimensionY == 0f) {
				y_inc = 0;
				t_next_y = dimFracY; // don't increment vertically because the Vec3d is perfectly horizontal
			} else if (relativeY > startY) {
				y_inc = 1; // target point is vertically greater than starting point so increment every
							// step by 1
				intersectCount += MathUtil.floor(relativeY) - y; // increment total amount of intersecting cells
				t_next_y = (float) ((MathUtil.floor(startY) + 1 - startY) * dimFracY); // calculate the next vertical
																				// intersection
				// point based on the position inside
				// the first cell
			} else {
				y_inc = -1; // target point is vertically smaller than starting point so reduce every step
							// by 1
				intersectCount += y - MathUtil.floor(relativeY); // increment total amount of intersecting cells
				t_next_y = (float) ((startY - MathUtil.floor(startY)) * dimFracY); // calculate the next vertical intersection
																			// point
				// based on the position inside
				// the first cell
			}

			if (dimensionZ == 0f) {
				z_inc = 0;
				t_next_z = dimFracZ; // don't increment vertically because the Vec3d is perfectly horizontal
			} else if (relativeZ > startZ) {
				z_inc = 1; // target point is vertically greater than starting point so increment every
							// step by 1
				intersectCount += MathUtil.floor(relativeZ) - z; // increment total amount of intersecting cells
				t_next_z = (float) ((MathUtil.floor(startZ) + 1 - startZ) * dimFracZ); // calculate the next vertical
																				// intersection
				// point based on the position inside
				// the first cell
			} else {
				z_inc = -1; // target point is vertically smaller than starting point so reduce every step
							// by 1
				intersectCount += z - MathUtil.floor(relativeZ); // increment total amount of intersecting cells
				t_next_z = (float) ((startZ - MathUtil.floor(startZ)) * dimFracZ); // calculate the next vertical intersection
																			// point
				// based on the position inside
				// the first cell
			}

			boolean finished = stepRay(start, startX, startY, startZ, x, y, z, dimFracX, dimFracY, dimFracZ, intersectCount, x_inc, y_inc, z_inc,
					t_next_y, t_next_x, t_next_z);
			if (finished) {
				cacheResult(targets[0], true);
				return true;
			}
		}
		cacheResult(targets[0], false);
		return false;
	}

	private void cacheResult(Vec3d vector, boolean result) {
		int cx = MathUtil.fastFloor(vector.x + reach);
		int cy = MathUtil.fastFloor(vector.y + reach);
		int cz = MathUtil.fastFloor(vector.z + reach);
		if (result) {
			cache.setVisible(cx, cy, cz);
		} else {
			cache.setHidden(cx, cy, cz);
		}
	}

	private boolean stepRay(Vec3d start, double startX, double startY, double startZ, int currentX, int currentY, int currentZ, double distInX,
			double distInY, double distInZ, int n, int x_inc, int y_inc, int z_inc, double t_next_y, double t_next_x,
			double t_next_z) {
		int chunkX = 0;
		int chunkZ = 0;
		WorldChunk snapshot = null;
		ClientWorld world = client.world;
		if(world == null)return false;

		// iterate through all intersecting cells (n times)
		for (; n > 1; n--) { // n-1 times because we don't want to check the last block
			// towards - where from
			int curToStartX = MathUtil.fastFloor((startX - currentX) + reach);
			int curToStartY = MathUtil.fastFloor((startY - currentY) + reach);
			int curToStartZ = MathUtil.fastFloor((startZ - currentZ) + reach);

			int cVal = cache.getState(curToStartX, curToStartY, curToStartZ);
			if (cVal == 2) {
				return false;
			}
			if (cVal == 0) {
				// save current cell
				int tchunkX = currentX >> 4;
				int tchunkZ = currentZ >> 4;
				if (snapshot == null || chunkX != tchunkX || chunkZ != tchunkZ) {
					chunkX = tchunkX;
					chunkZ = tchunkZ;
					snapshot = world.getChunk(chunkX, chunkZ);
					if (snapshot == null) {
						return false;
					}
				}

				int relativeX = currentX % 16;
				if (relativeX < 0) {
					relativeX = 16 + relativeX;
				}
				int relativeZ = currentZ % 16;
				if (relativeZ < 0) {
					relativeZ = 16 + relativeZ;
				}
				if (relativeX < 0) {
					cache.setLastHidden();
					return false;
				}
				if (relativeZ < 0) {
					cache.setLastHidden();
					return false;
				}
				if (currentY < 0 || currentY > snapshot.getHeight()) { // out of world // TODO fix for 1.17
					return true;
				}else{
					BlockPos pos = new BlockPos(currentX, currentY, currentZ);
					BlockState state = snapshot.getBlockState(pos);
					if (state.isOpaqueFullCube(world, pos)) {
						cache.setLastHidden();
						return false;
					}
				}
				cache.setLastVisible();
			}

			if (t_next_y < t_next_x && t_next_y < t_next_z) { // next cell is upwards/downwards because the distance to
																// the next vertical
				// intersection point is smaller than to the next horizontal intersection point
				currentY += y_inc; // move up/down
				t_next_y += distInY; // update next vertical intersection point
			} else if (t_next_x < t_next_y && t_next_x < t_next_z) { // next cell is right/left
				currentX += x_inc; // move right/left
				t_next_x += distInX; // update next horizontal intersection point
			} else {
				currentZ += z_inc; // move right/left
				t_next_z += distInZ; // update next horizontal intersection point
			}

		}
		return true;
	}

	private enum Relative {
		INSIDE, POSITIVE, NEGATIVE;

		public static Relative from(int min, int max) {
			if (max > 0 && min > 0) {
				return POSITIVE;
			} else if (min < 0 && max <= 0) {
				return NEGATIVE;
			}
			return INSIDE;
		}
	}

	public void resetCache() {
		this.cache.resetCache();
	}

}
