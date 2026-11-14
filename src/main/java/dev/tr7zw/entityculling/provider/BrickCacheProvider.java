package dev.tr7zw.entityculling.provider;

import dev.tr7zw.entityculling.access.ChunkSectionAccess;
import dev.tr7zw.entityculling.util.ECUtil;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.BrickProvider;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.ChunkSectionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Arrays;

public class BrickCacheProvider implements BrickProvider {

    private final Minecraft client = Minecraft.getInstance();
    private ClientLevel world = null;

    private BrickCache cache = null;
    private int curX = Integer.MIN_VALUE;
    private int curY = Integer.MIN_VALUE;
    private int curZ = Integer.MIN_VALUE;

    @Override
    public long[] getVoxelBitmask() {
        return (cache != null && cache.brickMask != 0L) ? cache.bitmask : null;
    }

    @Override
    public boolean prepareChunk(int chunkX, int chunkY, int chunkZ) {
        if (curX == chunkX && curY == chunkY && curZ == chunkZ) {
            return cache != null;
        }

        curX = chunkX;
        curY = chunkY;
        curZ = chunkZ;

        world = client.level;
        if (world != null) {
            ChunkSectionAccess access = ECUtil.resolveSection(world, chunkX, chunkY, chunkZ);
            if (access != null) {
                cache = access.ec$getProviderData(BrickCache.class);
                if (cache == null) {
                    rebuild(access);
                    cache = access.ec$getProviderData(BrickCache.class);
                }
                return cache != null;
            }
        }

        cache = null;
        return false;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        if (cache == null || cache.brickMask == 0L) {
            return false;
        }
        return cache.isOpaque(x & 0xF, y & 0xF, z & 0xF);
    }

    @Override
    public boolean isBrickEmpty(int lx, int ly, int lz) {
        if (cache == null) {
            return true;
        }
        return cache.isBrickEmpty(lx & 0xF, ly & 0xF, lz & 0xF);
    }

    @Override
    public void onChange(ChunkSectionProvider chunk, int x, int y, int z, boolean isOpaque) {
        if (chunk instanceof ChunkSectionAccess access) {
            BrickCache localCache = access.ec$getProviderData(BrickCache.class);
            if (localCache != null) {
                localCache.setOpaque(x & 0xF, y & 0xF, z & 0xF, isOpaque);
            } else {
                rebuild(access);
            }
        }
    }

    @Override
    public void postRun() {
        world = null;
        cache = null;
        curX = Integer.MIN_VALUE;
        curY = Integer.MIN_VALUE;
        curZ = Integer.MIN_VALUE;
    }

    @Override
    public void recalcBlockCounts(ChunkSectionProvider chunk) {
    }

    private void rebuild(ChunkSectionAccess access) {
        BrickCache localCache = access.ec$getProviderData(BrickCache.class);
        if (localCache == null) {
            localCache = new BrickCache();
            access.ec$setProviderData(localCache);
        } else {
            localCache.reset();
        }

        var states = access.entityCulling$getStates();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (ECUtil.isOpaqueFullCube(states.get(x, y, z))) {
                        localCache.setOpaque(x, y, z, true);
                    }
                }
            }
        }
        access.ec$setDirty(false);
    }

    public static class BrickCache {
        public final long[] bitmask = new long[64];
        public final byte[] brickCounts = new byte[64];
        public long brickMask = 0L;

        public void reset() {
            Arrays.fill(bitmask, 0L);
            Arrays.fill(brickCounts, (byte) 0);
            brickMask = 0L;
        }

        public void setOpaque(int x, int y, int z, boolean opaque) {
            int blockIdx = x | (y << 4) | (z << 8);
            int arrayIdx = blockIdx >>> 6;
            long bit = 1L << (blockIdx & 63);
            boolean wasOpaque = (bitmask[arrayIdx] & bit) != 0;

            if (wasOpaque == opaque) {
                return;
            }

            int brickIdx = (x >> 2) | ((y >> 2) << 2) | ((z >> 2) << 4);

            if (opaque) {
                bitmask[arrayIdx] |= bit;
                if (brickCounts[brickIdx]++ == 0) {
                    brickMask |= (1L << brickIdx);
                }
            } else {
                bitmask[arrayIdx] &= ~bit;
                if (--brickCounts[brickIdx] == 0) {
                    brickMask &= ~(1L << brickIdx);
                }
            }
        }

        public boolean isOpaque(int x, int y, int z) {
            int blockIdx = x | (y << 4) | (z << 8);
            return (bitmask[blockIdx >>> 6] & (1L << (blockIdx & 63))) != 0;
        }

        public boolean isBrickEmpty(int lx, int ly, int lz) {
            int brickIdx = (lx >> 2) | ((ly >> 2) << 2) | ((lz >> 2) << 4);
            return (brickMask & (1L << brickIdx)) == 0;
        }
    }
}