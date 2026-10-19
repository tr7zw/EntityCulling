package dev.tr7zw.entityculling.access;

import dev.tr7zw.entityculling.versionless.occlusionculling.provider.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.chunk.*;

public interface ChunkSectionAccess extends ChunkSectionProvider {
    PalettedContainer<BlockState> entityCulling$getStates();
}
