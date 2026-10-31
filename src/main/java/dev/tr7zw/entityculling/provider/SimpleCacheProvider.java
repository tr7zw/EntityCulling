package dev.tr7zw.entityculling.provider;

import com.ibm.icu.impl.*;
import dev.tr7zw.entityculling.*;
import dev.tr7zw.entityculling.access.*;
import dev.tr7zw.entityculling.util.*;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.chunk.*;

public class SimpleCacheProvider implements DataProvider {
    private final Minecraft client = Minecraft.getInstance();
    private ClientLevel world = null;
    private SimpleCache cache = null;
    private int curX = Integer.MIN_VALUE;
    private int curY = Integer.MIN_VALUE;
    private int curZ = Integer.MIN_VALUE;

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
                cache = access.ec$getProviderData(SimpleCache.class);
                if (cache == null) {
                    rebuild(access);
                    cache = access.ec$getProviderData(SimpleCache.class);
                }
                return cache != null;
            }
        }
        cache = null;
        return false;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        if (cache == null) {
            return false;
        }
        return cache.isOpaque(x & 0xF, y & 0xF, z & 0xF);
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
    public void onChange(ChunkSectionProvider chunk, int x, int y, int z, boolean isOpaque) {
        if (chunk instanceof ChunkSectionAccess access) {
            SimpleCache localCache = access.ec$getProviderData(SimpleCache.class);
            if (localCache != null) {
                localCache.setOpaque(x & 0xF, y & 0xF, z & 0xF, isOpaque);
            } else {
                rebuild(access);
            }
        }
    }

    @Override
    public void recalcBlockCounts(ChunkSectionProvider chunk) {

    }

    private void rebuild(ChunkSectionAccess access) {
        SimpleCache localCache = access.ec$getProviderData(SimpleCache.class);
        if (localCache == null) {
            localCache = new SimpleCache();
            access.ec$setProviderData(localCache);
        }
        var states = access.entityCulling$getStates();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    localCache.setOpaque(x, y, z, ECUtil.isOpaqueFullCube(states.get(x, y, z)));
                }
            }
        }
        access.ec$setDirty(false);
    }

    private static class SimpleCache {
        private final long[] cache = new long[64];

        public void setOpaque(int x, int y, int z, boolean opaque) {
            int index = (x & 0xF) | ((y & 0xF) << 4) | ((z & 0xF) << 8);
            int arrayIndex = index >>> 6;
            long bitMask = 1L << (index & 63);
            if (opaque) {
                cache[arrayIndex] |= bitMask;
            } else {
                cache[arrayIndex] &= ~bitMask;
            }
        }

        public boolean isOpaque(int x, int y, int z) {
            int index = (x & 0xF) | ((y & 0xF) << 4) | ((z & 0xF) << 8);
            return (cache[index >>> 6] & (1L << (index & 63))) != 0;
        }
    }

}
