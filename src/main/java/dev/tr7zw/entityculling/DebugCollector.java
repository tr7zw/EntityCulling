package dev.tr7zw.entityculling;

import dev.tr7zw.transition.mc.*;
import lombok.*;
import net.minecraft.*;
import net.minecraft.core.registries.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class DebugCollector {
    private boolean requestStart = false;
    @Getter
    private boolean running = false;
    @Getter
    private final DataHolder dataHolder = new DataHolder();

    public void requestStart() {
        requestStart = true;
    }

    public void tick() {
        if (running) {
            dumpData();
            running = false;
        }
        if (requestStart) {
            dataHolder.clear();
            running = true;
            requestStart = false;
        }
    }

    private void dumpData() {
        File file = new File("entityculling_debug_" + System.currentTimeMillis() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(dataHolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClientUtil.sendChatMessage(ComponentProvider.literal("Debug data dumped to " + file.getAbsolutePath())
                .withStyle(ChatFormatting.GREEN));
        dataHolder.clear();
    }

    public void addEntity(Entity entity, boolean rendered, boolean ignoredCulling) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if (rendered) {
            dataHolder.renderedEntityCounts.computeIfAbsent(id, k -> new AtomicInteger()).incrementAndGet();
            dataHolder.renderedEntities++;
        } else {
            dataHolder.skippedEntityCounts.computeIfAbsent(id, k -> new AtomicInteger()).incrementAndGet();
            dataHolder.skippedEntities++;
        }
    }

    public void addBlockEntity(BlockEntity blockEntity, boolean rendered) {
        String id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()).toString();
        if (rendered) {
            dataHolder.renderedBlockEntityCounts.computeIfAbsent(id, k -> new AtomicInteger()).incrementAndGet();
            dataHolder.renderedBlockEntities++;
        } else {
            dataHolder.skippedBlockEntityCounts.computeIfAbsent(id, k -> new AtomicInteger()).incrementAndGet();
            dataHolder.skippedBlockEntities++;
        }
    }

    public static class DataHolder {
        public int consideredEntities = 0;
        public int consideredBlockEntities = 0;
        public int renderedEntities = 0;
        public int skippedEntities = 0;
        public int tickedEntities = 0;
        public int skippedEntityTicks = 0;
        public int renderedBlockEntities = 0;
        public int skippedBlockEntities = 0;
        Map<String, AtomicInteger> renderedEntityCounts = new HashMap<>();
        Map<String, AtomicInteger> skippedEntityCounts = new HashMap<>();
        Map<String, AtomicInteger> renderedBlockEntityCounts = new HashMap<>();
        Map<String, AtomicInteger> skippedBlockEntityCounts = new HashMap<>();

        public void clear() {
            consideredEntities = 0;
            consideredBlockEntities = 0;
            renderedEntities = 0;
            skippedEntities = 0;
            tickedEntities = 0;
            skippedEntityTicks = 0;
            renderedBlockEntities = 0;
            skippedBlockEntities = 0;
            renderedEntityCounts.clear();
            skippedEntityCounts.clear();
            renderedBlockEntityCounts.clear();
            skippedBlockEntityCounts.clear();
        }
    }

}
