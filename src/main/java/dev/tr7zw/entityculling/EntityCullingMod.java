package dev.tr7zw.entityculling;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityCullingMod implements ClientModInitializer {

    public static EntityCullingMod instance;
    public CullTask cullTask;
    private final Set<Function<BlockEntity, Boolean>> dynamicBlockEntityWhitelist = new HashSet<>();
    private final Set<Function<Entity, Boolean>> dynamicEntityWhitelist = new HashSet<>();
    public OcclusionCullingInstance culling;
    public static boolean enabled = true;
    protected Thread cullThread;
    protected boolean lateInit = false;
    public int renderedBlockEntities = 0;
    public int skippedBlockEntities = 0;
    public int renderedEntities = 0;
    public int skippedEntities = 0;
    public int tickedEntities = 0;
    public int skippedEntityTicks = 0;

    public void onInitializeClient() {
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

    public void clientTick() {
        if (!lateInit) {
            lateInit = true;
            cullThread.start();
        }
    }

    public Box setupBox(BlockEntity entity, BlockPos pos)  {
        Block block = entity.getBlock();
        // Can't use createCached due to being off thread.
        return Box.create(block.minX+entity.x, block.minY+ entity.y, block.minZ + entity.z, block.maxX + entity.x, block.maxY + entity.y, block.maxZ + entity.z);
        //return entity.getBlock().getBoundingBox(entity.world, entity.x, entity.y, entity.z);
    }

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
