package dev.tr7zw.entityculling.util;

import dev.tr7zw.entityculling.*;
import dev.tr7zw.entityculling.access.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.chunk.*;

public class ECUtil {

    public static ChunkSectionAccess resolveSection(ClientLevel world, int subChunkX, int subChunkY, int subChunkZ) {

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
        if (sections[sectionIndex] instanceof ChunkSectionAccess access) {
            return access;
        }
        return null;
    }

    public static boolean isOpaqueFullCube(BlockState state) {
        if (EntityCullingModBase.instance.config.solidLeaves && state.getBlock() instanceof LeavesBlock) {
            return true;
        }
        //? if <= 1.21.1 {
        /*
         return state.isSolidRender(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO);
        *///? } else {

        return state.isSolidRender();
        //? }
    }

}
