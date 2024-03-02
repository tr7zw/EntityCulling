package dev.tr7zw.entityculling;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public abstract class EntityCullingModBase {

    public static EntityCullingModBase instance;
    //public Set<BlockEntityType<?>> blockEntityWhitelist = new HashSet<>();
    //public Set<EntityType<?>> entityWhistelist = new HashSet<>();
    //public Set<EntityType<?>> tickCullWhistelist = new HashSet<>();
    public CullTask cullTask;
    protected KeyBinding keybind = new KeyBinding("key.entityculling.toggle", -1);
    private Set<Function<BlockEntity, Boolean>> dynamicBlockEntityWhitelist = new HashSet<>();
    private Set<Function<Entity, Boolean>> dynamicEntityWhitelist = new HashSet<>();

    //public static final Logger LOGGER = LogManager.getLogger("EntityCulling");
    public OcclusionCullingInstance culling;
    public boolean debugHitboxes = false;
    public static boolean enabled = true;
    protected Thread cullThread;
    protected boolean pressed = false;
    protected boolean lateInit = false;
    public int renderedBlockEntities = 0;
    public int skippedBlockEntities = 0;
    public int renderedEntities = 0;
    public int skippedEntities = 0;
    public int tickedEntities = 0;
    public int skippedEntityTicks = 0;

    public void onInitialize() {
        instance = this;
        culling = new OcclusionCullingInstance(/*config.tracingDistance*/ 128, new Provider());
        cullTask = new CullTask(culling/*, blockEntityWhitelist, entityWhistelist*/);

        cullThread = new Thread(cullTask, "CullThread");
        cullThread.setUncaughtExceptionHandler((thread, ex) -> {
            System.out.println("The CullingThread has crashed! Please report the following stacktrace!" + ex.toString());
        });
    }

    public void worldTick() {
        cullTask.requestCull = true;
    }

    @SuppressWarnings("resource")
    public void clientTick() {
        if (!lateInit) {
            lateInit = true;
            cullThread.start();
        }
         /*   for (String blockId : config.blockEntityWhitelist) {
                Optional<BlockEntityType<?>> block = BuiltInRegistries.BLOCK_ENTITY_TYPE
                        .getOptional(new ResourceLocation(blockId));
                block.ifPresent(b -> {
                    blockEntityWhitelist.add(b);
                });
            }
            for (String entityType : config.tickCullingWhitelist) {
                Optional<EntityType<?>> entity = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(new ResourceLocation(entityType));
                entity.ifPresent(e -> {
                    entityWhistelist.add(e);
                });
            }
            for (String entityType : config.entityWhitelist) {
                Optional<EntityType<?>> entity = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(new ResourceLocation(entityType));
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
            LocalPlayer player = Minecraft.getInstance().player;
            if (enabled) {
                if (player != null) {
                    player.sendSystemMessage(Component.literal("Culling on").withStyle(ChatFormatting.GREEN));
                }
            } else {
                if (player != null) {
                    player.sendSystemMessage(Component.literal("Culling off").withStyle(ChatFormatting.RED));
                }
            }
        } else {
            pressed = false;
        }
        cullTask.requestCull = true;*/
    }

    public abstract Box setupBox(BlockEntity entity, BlockPos pos);

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
