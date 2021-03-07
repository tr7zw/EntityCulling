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

	private Vec3d[] targets = new Vec3d[8];
	private MinecraftClient client = MinecraftClient.getInstance();

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
			int maxX = (int) Math.ceil(aabbBlock.x + aabb.maxx + 0.25);
			int maxY = (int) Math.ceil(aabbBlock.y + aabb.maxy + 0.25);
			int maxZ = (int) Math.ceil(aabbBlock.z + aabb.maxz + 0.25);
			int minX = (int) Math.floor(aabbBlock.x + aabb.minx - 0.25);
			int minY = (int) Math.floor(aabbBlock.y + aabb.miny - 0.25);
			int minZ = (int) Math.floor(aabbBlock.z + aabb.minz - 0.25);
			
			Relative relX = Relative.from(minX, maxX);
			Relative relY = Relative.from(minY, maxY);
			Relative relZ = Relative.from(minZ + 1, maxZ + 1);
			if (minX <= 0 && maxX > 0 && minY <= 0 && maxY >= 0 && minZ < 0 && maxZ >= 0) {
				return true; // We are inside of the AABB, don't cull
			}

			Vec3d[] blocks = new Vec3d[(maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1)];
			boolean[][] faceEdgeData = new boolean[(maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1)][];
			int slot = 0;
			
			boolean[] onFaceEdge = new boolean[6];
			for (int x = minX; x < maxX; x++) {
				onFaceEdge[0] = (relX == Relative.POSITIVE && x == minX);
				onFaceEdge[1] = (relX == Relative.NEGATIVE && x == maxX - 1);
				for (int y = minY; y < maxY; y++) {
					onFaceEdge[2] = (relY == Relative.POSITIVE && y == minY);
					onFaceEdge[3] = (relY == Relative.NEGATIVE && y == maxY - 1);
					for (int z = minZ; z < maxZ; z++) {
						int cVal = getCacheValue(x, y, z);
						if(cVal == 1) {
							return true;
						}
						if(cVal == 0) {
							onFaceEdge[4] = (relZ == Relative.POSITIVE && z == minZ);
							onFaceEdge[5] = (relZ == Relative.NEGATIVE && z == maxZ - 1);
							if (onFaceEdge[0] || onFaceEdge[1] || onFaceEdge[2] || onFaceEdge[3] || onFaceEdge[4] || onFaceEdge[5]) {
								blocks[slot] = new Vec3d(x, y, z);
								faceEdgeData[slot] = Arrays.copyOf(onFaceEdge, 6);
								slot++;
							}
						}
					}
				}
			}
			for(int i = 0; i < slot; i++) {
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
		int maxX = (int) Math.abs(x);
		int maxY = (int) Math.abs(y);
		int maxZ = (int) Math.abs(z);

		if (maxX > reach - 2 || maxY > reach - 2 || maxZ > reach - 2)
			return -1;
		
		{// check if target is already known
			int cx = (int) Math.floor(x + reach);
			int cy = (int) Math.floor(y + reach);
			int cz = (int) Math.floor(z + reach);
			int keyPos = cx + cy * (reach * 2) + cz * (reach * 2) * (reach * 2);
			int entry = keyPos / 4;
			int offset = (keyPos % 4) * 2;
			int cVal = cache[entry] >> offset & 3;
			return cVal;
		}
	}

	private boolean isVoxelVisible(Vec3d playerLoc, Vec3d position, boolean[] faceEdgeData, boolean showDebug) {
		int targetSize = 0;
		boolean onMinX = faceEdgeData[0];
		boolean onMaxX = faceEdgeData[1];
		boolean onMinY = faceEdgeData[2];
		boolean onMaxY = faceEdgeData[3];
		boolean onMinZ = faceEdgeData[4];
		boolean onMaxZ = faceEdgeData[5];
		if(onMinX || onMinZ) {
			targets[targetSize++] = position;
		}
		if(onMaxX) {
			targets[targetSize++] = position.add(0.95, 0, 0);
		}
		if(onMaxZ) {
			targets[targetSize++] = position.add(0, 0, 0.95);
		}
		if(onMinX && onMaxZ) {
			targets[targetSize++] = position.add(0, 0.95, 0);
		}
		if(onMinX && onMaxY) {
			targets[targetSize++] = position.add(0, 0.95, 0);
		}
		if(onMaxX && onMaxY && onMaxZ) {
			targets[targetSize++] = position.add(0.95, 0.95, 0.95);
		}
		/*
		targets[0] = position;
		targets[1] = position.add(0.95, 0, 0);
		targets[2] = position.add(0, 0.95, 0);
		targets[3] = position.add(0.95, 0.95, 0);
		targets[4] = position.add(0, 0, 0.95);
		targets[5] = position.add(0.95, 0, 0.95);
		targets[6] = position.add(0, 0.95, 0.95);
		targets[7] = position.add(0.95, 0.95, 0.95);
		*/
		if (showDebug) {
			for (int i = 0; i < targetSize; i++) {
				Vec3d target = targets[i];
				client.world.addImportantParticle(ParticleTypes.HAPPY_VILLAGER, true, ((int) playerLoc.x) + target.x,
						((int) playerLoc.y) + target.y, ((int) playerLoc.z) + target.z, 0, 0, 0);
			}
		}
		return isVisible(playerLoc, targets, targetSize);
	}

	private final int reach = 64;
	private final byte[] cache = new byte[((reach * 2) * (reach * 2) * (reach * 2)) / 4];

	public void resetCache() {
		Arrays.fill(cache, (byte) 0);
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
			Vec3d target = targets[v];
			// coordinates of start and target point
			double x0 = start.getX();
			double y0 = start.getY();
			double z0 = start.getZ();
			double x1 = x0 + target.getX();
			double y1 = y0 + target.getY();
			double z1 = z0 + target.getZ();

			// horizontal and vertical cell amount spanned
			double dx = Math.abs(x1 - x0);
			double dy = Math.abs(y1 - y0);
			double dz = Math.abs(z1 - z0);

			// start cell coordinate
			int x = (int) Math.floor(x0);
			int y = (int) Math.floor(y0);
			int z = (int) Math.floor(z0);

			// distance between horizontal intersection points with cell border as a
			// fraction of the total Vec3d length
			double dt_dx = 1f / dx;
			// distance between vertical intersection points with cell border as a fraction
			// of the total Vec3d length
			double dt_dy = 1f / dy;
			double dt_dz = 1f / dz;

			// total amount of intersected cells
			int n = 1;

			// 1, 0 or -1
			// determines the direction of the next cell (horizontally / vertically)
			int x_inc, y_inc, z_inc;
			// the distance to the next horizontal / vertical intersection point with a cell
			// border as a fraction of the total Vec3d length
			double t_next_y, t_next_x, t_next_z;

			if (dx == 0f) {
				x_inc = 0;
				t_next_x = dt_dx; // don't increment horizontally because the Vec3d is perfectly vertical
			} else if (x1 > x0) {
				x_inc = 1; // target point is horizontally greater than starting point so increment every
							// step by 1
				n += (int) Math.floor(x1) - x; // increment total amount of intersecting cells
				t_next_x = (float) ((Math.floor(x0) + 1 - x0) * dt_dx); // calculate the next horizontal intersection
																		// point based on the position inside
																		// the first cell
			} else {
				x_inc = -1; // target point is horizontally smaller than starting point so reduce every step
							// by 1
				n += x - (int) Math.floor(x1); // increment total amount of intersecting cells
				t_next_x = (float) ((x0 - Math.floor(x0)) * dt_dx); // calculate the next horizontal intersection point
																	// based on the position inside
																	// the first cell
			}

			if (dy == 0f) {
				y_inc = 0;
				t_next_y = dt_dy; // don't increment vertically because the Vec3d is perfectly horizontal
			} else if (y1 > y0) {
				y_inc = 1; // target point is vertically greater than starting point so increment every
							// step by 1
				n += (int) Math.floor(y1) - y; // increment total amount of intersecting cells
				t_next_y = (float) ((Math.floor(y0) + 1 - y0) * dt_dy); // calculate the next vertical intersection
																		// point based on the position inside
																		// the first cell
			} else {
				y_inc = -1; // target point is vertically smaller than starting point so reduce every step
							// by 1
				n += y - (int) Math.floor(y1); // increment total amount of intersecting cells
				t_next_y = (float) ((y0 - Math.floor(y0)) * dt_dy); // calculate the next vertical intersection point
																	// based on the position inside
																	// the first cell
			}

			if (dz == 0f) {
				z_inc = 0;
				t_next_z = dt_dz; // don't increment vertically because the Vec3d is perfectly horizontal
			} else if (z1 > z0) {
				z_inc = 1; // target point is vertically greater than starting point so increment every
							// step by 1
				n += (int) Math.floor(z1) - z; // increment total amount of intersecting cells
				t_next_z = (float) ((Math.floor(z0) + 1 - z0) * dt_dz); // calculate the next vertical intersection
																		// point based on the position inside
																		// the first cell
			} else {
				z_inc = -1; // target point is vertically smaller than starting point so reduce every step
							// by 1
				n += z - (int) Math.floor(z1); // increment total amount of intersecting cells
				t_next_z = (float) ((z0 - Math.floor(z0)) * dt_dz); // calculate the next vertical intersection point
																	// based on the position inside
																	// the first cell
			}

			boolean finished = stepRay(start, x0, y0, z0, x, y, z, dt_dx, dt_dy, dt_dz, n, x_inc, y_inc, z_inc,
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
		int cx = (int) Math.floor(vector.x + reach);
		int cy = (int) Math.floor(vector.y + reach);
		int cz = (int) Math.floor(vector.z + reach);
		int keyPos = cx + cy * (reach * 2) + cz * (reach * 2) * (reach * 2);
		int entry = keyPos / 4;
		int offset = (keyPos % 4) * 2;
		if (result) {
			cache[entry] |= 1 << offset;
		} else {
			cache[entry] |= 1 << offset + 1;
		}
	}

	private boolean stepRay(Vec3d start, double x0, double y0, double z0, int x, int y, int z, double dt_dx,
			double dt_dy, double dt_dz, int n, int x_inc, int y_inc, int z_inc, double t_next_y, double t_next_x,
			double t_next_z) {
		int chunkX = 0;
		int chunkZ = 0;
		WorldChunk snapshot = null;
		ClientWorld world = client.world;

		// iterate through all intersecting cells (n times)
		for (; n > 1; n--) { // n-1 times because we don't want to check the last block
			int cx = (int) Math.floor((x - x0) + reach);
			int cy = (int) Math.floor((y - y0) + reach);
			int cz = (int) Math.floor((z - z0) + reach);

			int keyPos = cx + cy * (reach * 2) + cz * (reach * 2) * (reach * 2);
			int entry = keyPos / 4;
			int offset = (keyPos % 4) * 2;
			int cVal = cache[entry] >> offset & 3;
			if (cVal == 2) {
				return false;
			}
			if (cVal == 0) {
				// save current cell
				int tchunkX = (int) Math.floor(x / 16d);
				int tchunkZ = (int) Math.floor(z / 16d);
				if (snapshot == null || chunkX != tchunkX || chunkZ != tchunkZ) {
					chunkX = tchunkX;
					chunkZ = tchunkZ;
					snapshot = world.getChunk(chunkX, chunkZ);
					if (snapshot == null) {
						return false;
					}
				}

				int relativeX = x % 16;
				if (relativeX < 0) {
					relativeX = 16 + relativeX;
				}
				int relativeZ = z % 16;
				if (relativeZ < 0) {
					relativeZ = 16 + relativeZ;
				}
				if (relativeX < 0) {
					cache[entry] |= 1 << offset + 1;
					return false;
				}
				if (relativeZ < 0) {
					cache[entry] |= 1 << offset + 1;
					return false;
				}
				if (y < 0 || y > 255) {
					cache[entry] |= 1 << offset + 1;
					return false;
				}
				BlockPos pos = new BlockPos(x, y, z);
				BlockState state = snapshot.getBlockState(pos);
				if (state.isOpaqueFullCube(world, pos)) {
					cache[entry] |= 1 << offset + 1;
					return false;
				}
				cache[entry] |= 1 << offset;
			}

			if (t_next_y < t_next_x && t_next_y < t_next_z) { // next cell is upwards/downwards because the distance to
																// the next vertical
				// intersection point is smaller than to the next horizontal intersection point
				y += y_inc; // move up/down
				t_next_y += dt_dy; // update next vertical intersection point
			} else if (t_next_x < t_next_y && t_next_x < t_next_z) { // next cell is right/left
				x += x_inc; // move right/left
				t_next_x += dt_dx; // update next horizontal intersection point
			} else {
				z += z_inc; // move right/left
				t_next_z += dt_dz; // update next horizontal intersection point
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

}
