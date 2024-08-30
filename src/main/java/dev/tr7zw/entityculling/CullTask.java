package dev.tr7zw.entityculling;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;

import dev.tr7zw.entityculling.versionless.EntityCullingVersionlessBase;
import dev.tr7zw.entityculling.versionless.access.Cullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CullTask implements Runnable {

    public boolean requestCull = false;
    public boolean disableEntityCulling = false;
    public boolean disableBlockEntityCulling = false;

    private final OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getInstance();
    private final int sleepDelay = EntityCullingModBase.instance.config.sleepDelay;
    private final int hitboxLimit = EntityCullingModBase.instance.config.hitboxLimit;
    private final Set<BlockEntityType<?>> blockEntityWhitelist;
    private final Set<EntityType<?>> entityWhistelist;
    public long lastTime = 0;

    // reused preallocated vars
    private Vec3d lastPos = new Vec3d(0, 0, 0);
    private Vec3d aabbMin = new Vec3d(0, 0, 0);
    private Vec3d aabbMax = new Vec3d(0, 0, 0);

    public CullTask(OcclusionCullingInstance culling, Set<BlockEntityType<?>> blockEntityWhitelist,
            Set<EntityType<?>> entityWhistelist) {
        this.culling = culling;
        this.blockEntityWhitelist = blockEntityWhitelist;
        this.entityWhistelist = entityWhistelist;
    }

    @Override
    public void run() {
        while (client.isRunning()) { // client.isRunning() returns false at the start?!?
            try {
                Thread.sleep(sleepDelay);
                if (EntityCullingVersionlessBase.enabled && client.level != null && client.player != null
                        && client.player.tickCount > 10) {
                    // getEyePosition can use a fixed delta as its debug only anyway
                    Vec3 cameraMC = EntityCullingModBase.instance.config.debugMode ? client.player.getEyePosition(0)
                            : client.gameRenderer.getMainCamera().getPosition();

                    if (requestCull
                            || !(cameraMC.x == lastPos.x && cameraMC.y == lastPos.y && cameraMC.z == lastPos.z)) {
                        long start = System.currentTimeMillis();
                        requestCull = false;
                        lastPos.set(cameraMC.x, cameraMC.y, cameraMC.z);
                        Vec3d camera = lastPos;
                        culling.resetCache();
                        cullBlockEntities(cameraMC, camera);
                        cullEntities(cameraMC, camera);
                        lastTime = (System.currentTimeMillis() - start);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Shutting down culling task!");
    }

    private void cullEntities(Vec3 cameraMC, Vec3d camera) {
        if (disableEntityCulling) {
            return;
        }
        Entity entity = null;
        Iterator<Entity> iterable = client.level.entitiesForRendering().iterator();
        while (iterable.hasNext()) {
            try {
                entity = iterable.next();
            } catch (NullPointerException | ConcurrentModificationException | ArrayIndexOutOfBoundsException ex) {
                break; // We are not synced to the main thread, so NPE's/CME are allowed here and way
                       // less
                       // overhead probably than trying to sync stuff up for no really good reason
            }
            if (entity == null) {
                // assume the iterator is broken, cancel the loop
                // https://github.com/tr7zw/EntityCulling/issues/168
                break;
            }
            if (!(entity instanceof Cullable)) {
                continue; // Not sure how this could happen outside from mixin screwing up the inject into
                          // Entity
            }
            if (entityWhistelist.contains(entity.getType())) {
                continue;
            }
            if (EntityCullingModBase.instance.isDynamicWhitelisted(entity)) {
                continue;
            }
            Cullable cullable = (Cullable) entity;
            if (!cullable.isForcedVisible()) {
                if (Minecraft.getInstance().shouldEntityAppearGlowing(entity) || isSkippableArmorstand(entity)) {
                    cullable.setCulled(false);
                    continue;
                }
                if (!entity.position().closerThan(cameraMC, EntityCullingModBase.instance.config.tracingDistance)) {
                    cullable.setCulled(false); // If your entity view distance is larger than tracingDistance just
                                               // render it
                    continue;
                }
                AABB boundingBox = entity.getBoundingBoxForCulling();
                if (boundingBox.getXsize() > hitboxLimit || boundingBox.getYsize() > hitboxLimit
                        || boundingBox.getZsize() > hitboxLimit) {
                    cullable.setCulled(false); // To big to bother to cull
                    continue;
                }
                aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
                cullable.setCulled(!visible);
            }
        }
    }

    private void cullBlockEntities(Vec3 cameraMC, Vec3d camera) {
        if (disableBlockEntityCulling) {
            return;
        }
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                LevelChunk chunk = client.level.getChunk(client.player.chunkPosition().x + x,
                        client.player.chunkPosition().z + z);
                Iterator<Entry<BlockPos, BlockEntity>> iterator = chunk.getBlockEntities().entrySet().iterator();
                Entry<BlockPos, BlockEntity> entry;
                while (iterator.hasNext()) {
                    try {
                        entry = iterator.next();
                    } catch (NullPointerException | ConcurrentModificationException ex) {
                        break; // We are not synced to the main thread, so NPE's/CME are allowed here and way
                               // less
                               // overhead probably than trying to sync stuff up for no really good reason
                    }
                    if (entry == null) {
                        // assume the iterator is broken, cancel the loop
                        // https://github.com/tr7zw/EntityCulling/issues/168
                        break;
                    }
                    if (blockEntityWhitelist.contains(entry.getValue().getType())) {
                        continue;
                    }
                    if (EntityCullingModBase.instance.isDynamicWhitelisted(entry.getValue())) {
                        continue;
                    }
                    Cullable cullable = (Cullable) entry.getValue();
                    if (!cullable.isForcedVisible()) {
                        BlockPos pos = entry.getKey();
                        if (closerThan(pos, cameraMC, 64)) { // 64 is the fixed max tile view distance
                            AABB boundingBox = EntityCullingModBase.instance.setupAABB(entry.getValue(), pos);
                            if (boundingBox.getXsize() > hitboxLimit || boundingBox.getYsize() > hitboxLimit
                                    || boundingBox.getZsize() > hitboxLimit) {
                                cullable.setCulled(false); // To big to bother to cull
                                continue;
                            }
                            aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                            aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                            boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
                            cullable.setCulled(!visible);
                        }
                    }
                }

            }
        }
    }

    private boolean isSkippableArmorstand(Entity entity) {
        if (!EntityCullingModBase.instance.config.skipMarkerArmorStands)
            return false;
        return entity instanceof ArmorStand && ((ArmorStand) entity).isInvisible();
    }

    // Vec3i forward compatibility functions
    private static boolean closerThan(BlockPos blockPos, Position position, double d) {
        return distSqr(blockPos, position.x(), position.y(), position.z(), true) < d * d;
    }

    private static double distSqr(BlockPos blockPos, double d, double e, double f, boolean bl) {
        double g = bl ? 0.5D : 0.0D;
        double h = (double) blockPos.getX() + g - d;
        double i = (double) blockPos.getY() + g - e;
        double j = (double) blockPos.getZ() + g - f;
        return h * h + i * i + j * j;
    }
}
