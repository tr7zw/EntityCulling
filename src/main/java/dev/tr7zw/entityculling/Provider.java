package dev.tr7zw.entityculling;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.tr7zw.entityculling.versionless.access.DataProvider;
import dev.tr7zw.entityculling.versionless.tree64.Tree64SubChunk;

import dev.tr7zw.entityculling.access.LevelChunkSectionTree64Access;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getInstance();
    private final ConcurrentLinkedQueue<Long> pendingTree64Requests = new ConcurrentLinkedQueue<>();
    private final Set<Long> queuedTree64Requests = ConcurrentHashMap.newKeySet();

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ, int y) {
        return client.level != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        ClientLevel world = client.level;
        if (world == null) {
            return false;
        }
        BlockPos pos = new BlockPos(x, y, z);
        var state = world.getBlockState(pos);
        return isOpaqueState(state);
    }

    @Override
    public void cleanup() {
        // no-op, provider uses local world snapshots per call
    }

    @Override
    public Tree64SubChunk getTree64SubChunk(int subChunkX, int subChunkY, int subChunkZ) {
        LevelChunkSection section = resolveSection(subChunkX, subChunkY, subChunkZ);
        if (section == null) {
            return null;
        }
        LevelChunkSectionTree64Access access = (LevelChunkSectionTree64Access) section;
        if (access.entityCulling$isTreeDirty()) {
            return null;
        }
        return access.entityCulling$getTree64();
    }

    @Override
    public void requestTree64SubChunk(int subChunkX, int subChunkY, int subChunkZ) {
        long key = packSubChunkKey(subChunkX, subChunkY, subChunkZ);
        if (queuedTree64Requests.add(key)) {
            pendingTree64Requests.add(key);
        }
    }

    @Override
    public void processTree64Requests(int maxRequests) {
        ClientLevel world = client.level;
        if (world == null) {
            pendingTree64Requests.clear();
            queuedTree64Requests.clear();
            return;
        }

        int remaining = Math.max(1, maxRequests);
        while (remaining-- > 0) {
            Long key = pendingTree64Requests.poll();
            if (key == null) {
                return;
            }
            queuedTree64Requests.remove(key);

            int subChunkX = unpackSubChunkX(key);
            int subChunkY = unpackSubChunkY(key);
            int subChunkZ = unpackSubChunkZ(key);

            LevelChunkSection section = resolveSection(world, subChunkX, subChunkY, subChunkZ);
            if (section == null) {
                continue;
            }

            LevelChunkSectionTree64Access access = (LevelChunkSectionTree64Access) section;
            if (!access.entityCulling$isTreeDirty() && access.entityCulling$getTree64() != null) {
                continue;
            }

            Tree64SubChunk tree = buildFromSection(access, access.entityCulling$getTree64());
            access.entityCulling$setTree64(tree);
            access.entityCulling$setTreeDirty(false);
        }
    }

    private Tree64SubChunk buildFromSection(LevelChunkSectionTree64Access access, Tree64SubChunk existing) {
        Tree64SubChunk tree = existing != null ? existing : Tree64SubChunk.createEmpty();
        var states = access.entityCulling$getStates();

        for (int localY = 0; localY < 16; localY++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    BlockState state = states.get(localX, localY, localZ);
                    tree.setOpaqueLocal(localX, localY, localZ, isOpaqueState(state));
                }
            }
        }

        return tree;
    }

    private boolean isOpaqueState(BlockState state) {
        if (EntityCullingModBase.instance != null && EntityCullingModBase.instance.config.solidLeaves
                && state.getBlock() instanceof LeavesBlock) {
            return true;
        }
        //? if <= 1.21.1 {
        /*
         return state.isSolidRender(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        *///? } else {

        return state.isSolidRender();
        //? }
    }

    private LevelChunkSection resolveSection(int subChunkX, int subChunkY, int subChunkZ) {
        ClientLevel world = client.level;
        if (world == null) {
            return null;
        }
        return resolveSection(world, subChunkX, subChunkY, subChunkZ);
    }

    private LevelChunkSection resolveSection(ClientLevel world, int subChunkX, int subChunkY, int subChunkZ) {

        LevelChunk chunk;
        try {
            chunk = world.getChunk(subChunkX, subChunkZ);
        } catch (Throwable ex) {
            return null;
        }

        int sectionIndex = world.getSectionIndexFromSectionY(subChunkY);
        LevelChunkSection[] sections = chunk.getSections();
        if (sectionIndex < 0 || sectionIndex >= sections.length) {
            return null;
        }
        return sections[sectionIndex];
    }

    private static long packSubChunkKey(int x, int y, int z) {
        long lx = ((long) x) & 0x1FFFFFL;
        long ly = ((long) y) & 0x1FFFFFL;
        long lz = ((long) z) & 0x1FFFFFL;
        return lx | (ly << 21) | (lz << 42);
    }

    private static int unpackSigned21(long value) {
        int v = (int) (value & 0x1FFFFF);
        if ((v & 0x100000) != 0) {
            v |= ~0x1FFFFF;
        }
        return v;
    }

    private static int unpackSubChunkX(long key) {
        return unpackSigned21(key);
    }

    private static int unpackSubChunkY(long key) {
        return unpackSigned21(key >> 21);
    }

    private static int unpackSubChunkZ(long key) {
        return unpackSigned21(key >> 42);
    }

}
