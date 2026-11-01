package dev.tr7zw.entityculling.provider;

import dev.tr7zw.entityculling.access.ChunkSectionAccess;
import dev.tr7zw.entityculling.util.ECUtil;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.BrickProvider;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.ChunkSectionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Arrays;

public class BrickCacheProvider implements BrickProvider {

    private static final int GRID_SIZE = 16;
    private static final int GRID_MASK = GRID_SIZE - 1; // 15
    private static final int TOTAL_SECTIONS = GRID_SIZE * GRID_SIZE * GRID_SIZE; // 4096

    private final Minecraft client = Minecraft.getInstance();
    private ClientLevel world = null;

    private final long[][] sectionBitmasks = new long[TOTAL_SECTIONS][];
    private final long[] sectionBrickMasks = new long[TOTAL_SECTIONS];
    private final long[] sectionTags = new long[TOTAL_SECTIONS];

    // Reusable scratchpad array to prevent heap churn during chunk bakes
    private final long[] scratchMask = new long[64];

    private volatile int modificationCount = 0;

    public long[] activeBitmask = null;
    public long activeBrickMask = 0L;
    public int activeChunkX = Integer.MIN_VALUE;
    public int activeChunkY = Integer.MIN_VALUE;
    public int activeChunkZ = Integer.MIN_VALUE;

    public BrickCacheProvider() {
        Arrays.fill(sectionTags, Long.MIN_VALUE);
    }

    public int getModificationCount() {
        return modificationCount;
    }

    private static int getSlot(int cx, int cy, int cz) {
        return (cx & GRID_MASK) | ((cy & GRID_MASK) << 4) | ((cz & GRID_MASK) << 8);
    }

    private static long packTag(int cx, int cy, int cz) {
        return (((long) cx & 0x3FFFFFF) << 38) | (((long) cz & 0x3FFFFFF) << 12) | ((long) cy & 0xFFF);
    }

    @Override
    public long[] getVoxelBitmask() {
        return activeBitmask;
    }

    @Override
    public boolean prepareChunk(int chunkX, int chunkY, int chunkZ) {
        if (chunkX == activeChunkX && chunkY == activeChunkY && chunkZ == activeChunkZ) {
            return true;
        }

        activeChunkX = chunkX;
        activeChunkY = chunkY;
        activeChunkZ = chunkZ;

        int slot = getSlot(chunkX, chunkY, chunkZ);
        long tag = packTag(chunkX, chunkY, chunkZ);

        if (sectionTags[slot] == tag) {
            activeBitmask = sectionBitmasks[slot];
            activeBrickMask = sectionBrickMasks[slot];
            return true;
        }

        world = client.level;
        if (world == null) {
            activeBitmask = null;
            activeBrickMask = 0L;
            return false;
        }

        ChunkSectionAccess access = ECUtil.resolveSection(world, chunkX, chunkY, chunkZ);
        if (access == null) {
            // Pure air chunk
            sectionTags[slot] = tag;
            sectionBitmasks[slot] = null;
            sectionBrickMasks[slot] = 0L;
            activeBitmask = null;
            activeBrickMask = 0L;
            return true;
        }

        // Bake chunk bitmasks
        Arrays.fill(scratchMask, 0L);
        long brickMask = 0L;
        var states = access.entityCulling$getStates();

        for (int x = 0; x < 16; x++) {
            int bx = x >> 2;
            for (int y = 0; y < 16; y++) {
                int by = y >> 2;
                int brickYShift = bx | (by << 2);
                for (int z = 0; z < 16; z++) {
                    if (ECUtil.isOpaqueFullCube(states.get(x, y, z))) {
                        int idx = x | (y << 4) | (z << 8);
                        scratchMask[idx >>> 6] |= (1L << (idx & 63));

                        int bz = z >> 2;
                        brickMask |= (1L << (brickYShift | (bz << 4)));
                    }
                }
            }
        }

        sectionTags[slot] = tag;
        sectionBitmasks[slot] = brickMask == 0L ? null : scratchMask.clone();
        sectionBrickMasks[slot] = brickMask;
        activeBitmask = sectionBitmasks[slot];
        activeBrickMask = brickMask;

        return true;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        if (activeBitmask == null) {
            return false;
        }
        int blockIdx = (x & 15) | ((y & 15) << 4) | ((z & 15) << 8);
        return (activeBitmask[blockIdx >>> 6] & (1L << (blockIdx & 63))) != 0;
    }

    @Override
    public boolean isBrickEmpty(int lx, int ly, int lz) {
        int brickIdx = ((lx & 15) >> 2) | (((ly & 15) >> 2) << 2) | (((lz & 15) >> 2) << 4);
        return (activeBrickMask & (1L << brickIdx)) == 0;
    }

    @Override
    public void onChange(ChunkSectionProvider chunk, int x, int y, int z, boolean isOpaque) {
        int cx = x >> 4, cy = y >> 4, cz = z >> 4;
        int slot = getSlot(cx, cy, cz);
        if (sectionTags[slot] == packTag(cx, cy, cz)) {
            sectionTags[slot] = Long.MIN_VALUE;
        }
        modificationCount++;
    }

    @Override
    public void postRun() {
        world = null;
        activeBitmask = null;
        activeBrickMask = 0L;
        activeChunkX = Integer.MIN_VALUE;
        activeChunkY = Integer.MIN_VALUE;
        activeChunkZ = Integer.MIN_VALUE;
    }

    @Override
    public void recalcBlockCounts(ChunkSectionProvider chunk) {}
}