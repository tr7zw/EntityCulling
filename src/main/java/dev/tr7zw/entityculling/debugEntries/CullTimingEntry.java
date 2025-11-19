//? if >= 1.21.9 {

package dev.tr7zw.entityculling.debugEntries;

import java.text.DecimalFormat;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.EntityCullingModBase;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class CullTimingEntry implements DebugScreenEntry {

    private final DecimalFormat entityCullingFormatter = new DecimalFormat("###.##");

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, Level level, LevelChunk levelChunk,
            LevelChunk levelChunk2) {
        debugScreenDisplayer.addToGroup(EntityCullingMod.DEBUG_CATEGORY_ID,
                "[Culling] Last pass: " + entityCullingFormatter.format(EntityCullingModBase.instance.cullTask.lastTime)
                        + "ms/" + entityCullingFormatter.format(EntityCullingModBase.instance.lastTickTime) + "ms");
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
