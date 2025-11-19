package dev.tr7zw.entityculling;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import dev.tr7zw.entityculling.versionless.EntityCullingVersionlessBase;
import dev.tr7zw.transition.mc.ClientUtil;
import dev.tr7zw.transition.mc.ComponentProvider;
import dev.tr7zw.transition.mc.GeneralUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public abstract class EntityCullingModBase extends EntityCullingVersionlessBase {

    public static EntityCullingModBase instance;
    public Set<BlockEntityType<?>> blockEntityWhitelist = new HashSet<>();
    public Set<EntityType<?>> entityWhitelist = new HashSet<>();
    public Set<EntityType<?>> tickCullWhistelist = new HashSet<>();
    public CullTask cullTask;
    protected KeyMapping keybind = GeneralUtil.createKeyMapping("key.entityculling.toggle", -1,
            "text.entityculling.title");
    protected KeyMapping keybindBoxes = GeneralUtil.createKeyMapping("key.entityculling.toggleBoxes", -1,
            "text.entityculling.title");
    private Set<Function<BlockEntity, Boolean>> dynamicBlockEntityWhitelist = new HashSet<>();
    private Set<Function<Entity, Boolean>> dynamicEntityWhitelist = new HashSet<>();
    private int tickCounter = 0;
    public double lastTickTime = 0;
    //? if >= 1.21.9 {

    public net.minecraft.client.renderer.culling.Frustum frustum = null;
    //? }

    public void onInitialize() {
        instance = this;
        super.onInitialize();
        culling = new OcclusionCullingInstance(config.tracingDistance, new Provider());
        cullTask = new CullTask(culling, blockEntityWhitelist, entityWhitelist);

        cullThread = new Thread(cullTask, "CullThread");
        cullThread.setUncaughtExceptionHandler((thread, ex) -> {
            LOGGER.error("The CullingThread has crashed! Please report the following stacktrace!", ex);
        });

        initModloader();
    }

    public void worldTick() {
        cullTask.requestCull = true;
    }

    public void clientTick() {
        // late init
        if (!lateInit) {
            lateInit = true;
            cullThread.start();
            for (String blockId : config.blockEntityWhitelist) {
                Optional<BlockEntityType<?>> block = BuiltInRegistries.BLOCK_ENTITY_TYPE
                        .getOptional(GeneralUtil.getResourceLocation(blockId));
                block.ifPresent(b -> {
                    blockEntityWhitelist.add(b);
                });
            }
            for (String entityType : config.tickCullingWhitelist) {
                Optional<EntityType<?>> entity = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(GeneralUtil.getResourceLocation(entityType));
                entity.ifPresent(e -> {
                    tickCullWhistelist.add(e);
                });
            }
            for (String entityType : config.entityWhitelist) {
                Optional<EntityType<?>> entity = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(GeneralUtil.getResourceLocation(entityType));
                entity.ifPresent(e -> {
                    entityWhitelist.add(e);
                });
            }
        }
        // Handle keybinds
        if (keybind.isDown()) {
            if (pressed)
                return;
            pressed = true;
            enabled = !enabled;
            if (enabled) {
                ClientUtil.sendChatMessage(ComponentProvider.literal("Culling on").withStyle(ChatFormatting.GREEN));
            } else {
                ClientUtil.sendChatMessage(ComponentProvider.literal("Culling off").withStyle(ChatFormatting.RED));
            }
        } else {
            pressed = false;
        }
        if (keybindBoxes.isDown()) {
            if (pressedBox)
                return;
            pressedBox = true;
            debugHitboxes = !debugHitboxes;
            if (debugHitboxes) {
                ClientUtil.sendChatMessage(
                        ComponentProvider.literal("Debug Cullboxes on").withStyle(ChatFormatting.GREEN));
            } else {
                ClientUtil.sendChatMessage(
                        ComponentProvider.literal("Debug Cullboxes off").withStyle(ChatFormatting.RED));
            }
        } else {
            pressedBox = false;
        }
        // Cull logic preparation
        long start = System.nanoTime();
        Minecraft client = Minecraft.getInstance();
        boolean ingame = client.level != null && client.player != null && client.player.tickCount > 10;
        if (ingame && enabled) {
            boolean changed = false;
            if (tickCounter++ % config.captureRate == 0) {
                if (!config.skipEntityCulling) {
                    cullTask.setEntitiesForRendering(
                            StreamSupport.stream(client.level.entitiesForRendering().spliterator(), false).toList());
                }
                if (!config.skipBlockEntityCulling) {
                    Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
                    for (int x = -8; x <= 8; x++) {
                        for (int z = -8; z <= 8; z++) {
                            LevelChunk chunk = client.level.getChunk(client.player.chunkPosition().x + x,
                                    client.player.chunkPosition().z + z);
                            blockEntities.putAll(chunk.getBlockEntities());
                        }
                    }
                    cullTask.setBlockEntities(blockEntities);
                }
                changed = true;
            }

            cullTask.setIngame(true);
            cullTask.setCameraMC(EntityCullingModBase.instance.config.debugMode ? client.player.getEyePosition(0)
                    : client.gameRenderer.getMainCamera()
                            /*? >= 1.21.11 {*/ .position() /*?} else {*//* .getPosition() *//*?}*/);
            cullTask.requestCull = true;
            if (changed) {
                lastTickTime = (System.nanoTime() - start) / 1_000_000.0;
            }
        } else {
            cullTask.setIngame(false);
            cullTask.setEntitiesForRendering(Collections.emptyList());
            cullTask.setBlockEntities(Collections.emptyMap());
            lastTickTime = (System.nanoTime() - start) / 1_000_000.0;
        }
    }

    public abstract AABB setupAABB(BlockEntity entity, BlockPos pos);

    public boolean isDynamicWhitelisted(BlockEntity entity) {
        for (Function<BlockEntity, Boolean> fun : dynamicBlockEntityWhitelist) {
            if (fun.apply(entity)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDynamicWhitelisted(Entity entity) {
        for (Function<Entity, Boolean> fun : dynamicEntityWhitelist) {
            if (fun.apply(entity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a dynamic function that can return true to disable culling for a
     * BlockEntity temporarly.
     * 
     * @param function
     */
    public void addDynamicBlockEntityWhitelist(Function<BlockEntity, Boolean> function) {
        this.dynamicBlockEntityWhitelist.add(function);
    }

    /**
     * Add a dynamic function that can return true to disable culling for an entity
     * temporarly.
     * 
     * @param function
     */
    public void addDynamicEntityWhitelist(Function<Entity, Boolean> function) {
        this.dynamicEntityWhitelist.add(function);
    }

}
