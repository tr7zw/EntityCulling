package dev.tr7zw.entityculling;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import dev.tr7zw.entityculling.versionless.EntityCullingVersionlessBase;
import dev.tr7zw.util.ComponentProvider;
import dev.tr7zw.util.NMSHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;

public abstract class EntityCullingModBase extends EntityCullingVersionlessBase {

    public static EntityCullingModBase instance;
    public Set<BlockEntityType<?>> blockEntityWhitelist = new HashSet<>();
    public Set<EntityType<?>> entityWhistelist = new HashSet<>();
    public Set<EntityType<?>> tickCullWhistelist = new HashSet<>();
    public CullTask cullTask;
    protected KeyMapping keybind = new KeyMapping("key.entityculling.toggle", -1, "text.entityculling.title");
    protected KeyMapping keybindBoxes = new KeyMapping("key.entityculling.toggleBoxes", -1, "text.entityculling.title");
    private Set<Function<BlockEntity, Boolean>> dynamicBlockEntityWhitelist = new HashSet<>();
    private Set<Function<Entity, Boolean>> dynamicEntityWhitelist = new HashSet<>();

    public void onInitialize() {
        instance = this;
        super.onInitialize();
        culling = new OcclusionCullingInstance(config.tracingDistance, new Provider());
        cullTask = new CullTask(culling, blockEntityWhitelist, entityWhistelist);

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
        if (!lateInit) {
            lateInit = true;
            cullThread.start();
            for (String blockId : config.blockEntityWhitelist) {
                Optional<BlockEntityType<?>> block = BuiltInRegistries.BLOCK_ENTITY_TYPE
                        .getOptional(NMSHelper.getResourceLocation(blockId));
                block.ifPresent(b -> {
                    blockEntityWhitelist.add(b);
                });
            }
            for (String entityType : config.tickCullingWhitelist) {
                Optional<EntityType<?>> entity = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(NMSHelper.getResourceLocation(entityType));
                entity.ifPresent(e -> {
                    entityWhistelist.add(e);
                });
            }
            for (String entityType : config.entityWhitelist) {
                Optional<EntityType<?>> entity = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(NMSHelper.getResourceLocation(entityType));
                entity.ifPresent(e -> {
                    entityWhistelist.add(e);
                });
            }
        }
        if (keybind.isDown()) {
            if (pressed)
                return;
            pressed = true;
            enabled = !enabled;
            if (enabled) {
                NMSHelper.sendChatMessage(ComponentProvider.literal("Culling on").withStyle(ChatFormatting.GREEN));
            } else {
                NMSHelper.sendChatMessage(ComponentProvider.literal("Culling off").withStyle(ChatFormatting.RED));
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
                NMSHelper
                        .sendChatMessage(ComponentProvider.literal("Debug Cullboxes on").withStyle(ChatFormatting.GREEN));
            } else {
                NMSHelper.sendChatMessage(ComponentProvider.literal("Debug Cullboxes off").withStyle(ChatFormatting.RED));
            }
        } else {
            pressedBox = false;
        }
        cullTask.requestCull = true;
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
