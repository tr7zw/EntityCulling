//? if >= 1.21.9 {

package dev.tr7zw.entityculling.debugEntries;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.EntityCullingModBase;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class TickCullingEntry implements DebugScreenEntry {

    private int lastTickedEntities = 0;
    private int lastSkippedEntityTicks = 0;

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, Level level, LevelChunk levelChunk,
            LevelChunk levelChunk2) {
        debugScreenDisplayer.addToGroup(EntityCullingMod.DEBUG_CATEGORY_ID,
                "[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
        if (EntityCullingModBase.instance.tickedEntities != 0
                || EntityCullingModBase.instance.skippedEntityTicks != 0) {
            lastTickedEntities = EntityCullingModBase.instance.tickedEntities;
            lastSkippedEntityTicks = EntityCullingModBase.instance.skippedEntityTicks;
            EntityCullingModBase.instance.tickedEntities = 0;
            EntityCullingModBase.instance.skippedEntityTicks = 0;
        }
    }

    @Override
    public boolean isAllowed(boolean bl) {
        return true;
    }

    @Override
    public DebugEntryCategory category() {
        return EntityCullingMod.DEBUG_CATEGORY;
    }

}
//? }
