package dev.tr7zw.entityculling.access;

import dev.tr7zw.entityculling.versionless.tree64.Tree64SubChunk;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

public interface LevelChunkSectionTree64Access {

    Tree64SubChunk entityCulling$getTree64();

    void entityCulling$setTree64(Tree64SubChunk tree64);

    boolean entityCulling$isTreeDirty();

    void entityCulling$setTreeDirty(boolean dirty);

    PalettedContainer<BlockState> entityCulling$getStates();

}
