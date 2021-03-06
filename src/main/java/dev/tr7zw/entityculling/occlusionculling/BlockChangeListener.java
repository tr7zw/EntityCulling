package dev.tr7zw.entityculling.occlusionculling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.EnchantingTable;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import dev.tr7zw.entityculling.CullingPlugin;

public class BlockChangeListener implements Listener {

	public final Map<ChunkCoords, ChunkSnapshot> cachedChunkSnapshots = new HashMap<ChunkCoords, ChunkSnapshot>();
	public final Map<ChunkCoords, BlockState[]> cachedChunkTiles = new HashMap<ChunkCoords, BlockState[]>();
	public final Map<ChunkCoords, Entity[]> cachedChunkEntities = new HashMap<ChunkCoords, Entity[]>();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final WriteLock writeLock = lock.writeLock();
	private final ReadLock readLock = lock.readLock();

	public BlockChangeListener() {
		for (World world : Bukkit.getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				handleChunkLoadSync(chunk);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		Chunk chunk = e.getBlock().getChunk();
		updateCachedChunkSync(new ChunkCoords(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), chunk);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		Chunk chunk = e.getBlock().getChunk();
		updateCachedChunkSync(new ChunkCoords(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), chunk);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent e) {
		handleChunkLoadSync(e.getChunk());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent e) {
		Chunk chunk = e.getChunk();
		updateCachedChunkSync(new ChunkCoords(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent e) {
		handleExplosionSync(e.blockList());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		handleExplosionSync(e.blockList());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSpawn(EntitySpawnEvent event) {
		Chunk chunk = event.getEntity().getLocation().getChunk();
		updateCachedChunkEntitiesSync(new ChunkCoords(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), chunk);
	}

	public void handleChunkLoadSync(Chunk loadedChunk) {
		updateCachedChunkSync(new ChunkCoords(loadedChunk.getWorld().getName(), loadedChunk.getX(), loadedChunk.getZ()),
				loadedChunk);
	}

	public void handleExplosionSync(List<Block> blockList) {
		Set<ChunkCoords> chunks = new HashSet<>();
		for (Block block : blockList) {
			int chunkX = (int) Math.floor(block.getX() / 16d);
			int chunkZ = (int) Math.floor(block.getZ() / 16d);
			chunks.add(new ChunkCoords(block.getWorld().getName(), chunkX, chunkZ));
		}
		for (ChunkCoords cc : chunks) {
			updateCachedChunkSync(cc, cc.getRealChunkSync());
		}
	}

	public void updateCachedChunkSync(final ChunkCoords cc, final Chunk chunk) {

		if (chunk == null) {
			try {
				writeLock.lock();
				cachedChunkSnapshots.remove(cc);
				cachedChunkTiles.remove(cc);
				cachedChunkEntities.remove(cc);
				return;
			} finally {
				writeLock.unlock();
			}
		}
		CullingPlugin.runTask(new Runnable() {

			@Override
			public void run() {
				try {
					writeLock.lock();
					cachedChunkSnapshots.put(cc, chunk.getChunkSnapshot());
					cachedChunkTiles.put(cc, filterTiles(chunk.getTileEntities()));
					cachedChunkEntities.put(cc, chunk.getEntities());
				} finally {
					writeLock.unlock();
				}
			}
		});
	}

	private BlockState[] filterTiles(BlockState[] tiles) {
		if (tiles.length == 0)
			return tiles;
		List<BlockState> list = new ArrayList(Arrays.asList(tiles)); // the arrays as list is not modifyable
		ListIterator<BlockState> it = list.listIterator();
		while (it.hasNext()) {
			BlockState state = it.next();
			if (!(state instanceof Chest || state instanceof Shulker || state instanceof CreatureSpawner
					|| state instanceof EnchantingTable || state instanceof Banner || state instanceof Skull)) {
				it.remove();
			}
		}
		return list.toArray(new BlockState[0]);
	}

	public void updateCachedChunkEntitiesSync(final ChunkCoords cc, final Chunk chunk) {
		if (chunk == null) {
			try {
				writeLock.lock();
				cachedChunkSnapshots.remove(cc);
				cachedChunkTiles.remove(cc);
				cachedChunkEntities.remove(cc);
				return;
			} finally {
				writeLock.unlock();
			}
		}
		CullingPlugin.runTask(new Runnable() {

			@Override
			public void run() {
				try {
					writeLock.lock();
					cachedChunkEntities.put(cc, chunk.getEntities());
				} finally {
					writeLock.unlock();
				}
			}
		});
	}

	public void updateCachedChunkEntitiesSync(final Set<ChunkCoords> chunks) {
		CullingPlugin.runTask(new Runnable() {

			@Override
			public void run() {
				try {
					writeLock.lock();
					for (ChunkCoords cc : chunks) {
						cachedChunkEntities.put(cc, cc.getRealChunkSync().getEntities());
					}
				} finally {
					writeLock.unlock();
				}
			}
		});
	}

	public ChunkCoords getChunkCoords(Location loc) {
		int chunkX = (int) Math.floor(loc.getBlockX() / 16d);
		int chunkZ = (int) Math.floor(loc.getBlockZ() / 16d);
		return new ChunkCoords(loc.getWorld().getName(), chunkX, chunkZ);
	}

	@Deprecated
	public boolean isInLoadedChunk(Location loc) {
		return isInLoadedChunk(getChunkCoords(loc));
	}

	public boolean isInLoadedChunk(ChunkCoords cc) {
		try {
			readLock.lock();
			return cachedChunkSnapshots.containsKey(cc);
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			readLock.unlock();
		}
		return false;
	}

	@Deprecated
	public ChunkSnapshot getChunk(Location loc) {
		return getChunk(getChunkCoords(loc));
	}

	public ChunkSnapshot getChunk(ChunkCoords cc) {
		try {
			readLock.lock();
			return cachedChunkSnapshots.get(cc);
		} finally {
			readLock.unlock();
		}
	}

	@Deprecated
	public BlockState[] getChunkTiles(Location loc) {
		return getChunkTiles(getChunkCoords(loc));
	}

	public BlockState[] getChunkTiles(ChunkCoords cc) {
		try {
			readLock.lock();
			return cachedChunkTiles.get(cc);
		} finally {
			readLock.unlock();
		}
	}

	@Deprecated
	public Entity[] getChunkEntities(Location loc) {
		return getChunkEntities(getChunkCoords(loc));
	}

	public Entity[] getChunkEntities(ChunkCoords cc) {
		try {
			readLock.lock();
			return cachedChunkEntities.get(cc);
		} finally {
			readLock.unlock();
		}
	}

	public static class ChunkCoords {
		public String worldName;
		public int chunkX;
		public int chunkZ;

		public ChunkCoords(String worldName, int chunkX, int chunkZ) {
			this.worldName = worldName;
			this.chunkX = chunkX;
			this.chunkZ = chunkZ;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + chunkX;
			result = prime * result + chunkZ;
			result = prime * result + ((worldName == null) ? 0 : worldName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChunkCoords other = (ChunkCoords) obj;
			if (chunkX != other.chunkX)
				return false;
			if (chunkZ != other.chunkZ)
				return false;
			if (worldName == null) {
				if (other.worldName != null)
					return false;
			} else if (!worldName.equals(other.worldName))
				return false;
			return true;
		}

		public Chunk getRealChunkSync() {
			World world = Bukkit.getWorld(worldName);
			return world.getChunkAt(chunkX, chunkZ);
		}

	}

}
