package dev.tr7zw.entityculling;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;

import dev.tr7zw.entityculling.access.Cullable;
import dev.tr7zw.entityculling.mixin.MinecraftAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.Chunk;

public class CullTask implements Runnable {

    public boolean requestCull = false;
    public boolean disableEntityCulling = false;
    public boolean disableBlockEntityCulling = false;

    private final OcclusionCullingInstance culling;
    private final Minecraft client = MinecraftAccessor.getInstance();
    private final int sleepDelay = 10; //EntityCullingModBase.instance.config.sleepDelay;
    private final int hitboxLimit =50; // EntityCullingModBase.instance.config.hitboxLimit;
    //private final Set<BlockEntityType<?>> blockEntityWhitelist;
    //private final Set<EntityType<?>> entityWhistelist;
    public long lastTime = 0;

    // reused preallocated vars
    private Vec3d lastPos = new Vec3d(0, 0, 0);
    private Vec3d aabbMin = new Vec3d(0, 0, 0);
    private Vec3d aabbMax = new Vec3d(0, 0, 0);

    public CullTask(OcclusionCullingInstance culling) {
        this.culling = culling;
        //this.blockEntityWhitelist = blockEntityWhitelist;
        //this.entityWhistelist = entityWhistelist;
    }

    @Override
    public void run() {
        while (client.running) { // client.isRunning() returns false at the start?!?
            try {
                Thread.sleep(sleepDelay);
                if (EntityCullingMod.enabled && client.world != null && client.player != null
                        && client.player.field_1645 > 10) {
                    net.minecraft.util.math.Vec3d cameraMC = /*false *//*EntityCullingModBase.instance.config.debugMode*//*
                            ? client.player.
                            : client.worldRenderer.field_1795.getPosition()*/ real(client.player);

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

    private void cullEntities(net.minecraft.util.math.Vec3d cameraMC, Vec3d camera) {
        if (disableEntityCulling) {
            return;
        }
        Entity entity;
        Iterator iterable = client.world.method_291().iterator();
        while (iterable.hasNext()) {
            try {
                entity = (Entity) iterable.next();
            } catch (NullPointerException | ConcurrentModificationException ex) {
                break; // We are not synced to the main thread, so NPE's/CME are allowed here and way
                       // less
                       // overhead probably than trying to sync stuff up for no really good reason
            }
            if (!(entity instanceof Cullable)) {
                continue; // Not sure how this could happen outside from mixin screwing up the inject into
                          // Entity
            }
            if (EntityCullingModBase.instance.isDynamicWhitelisted(entity)) {
                continue;
            }
            Cullable cullable = (Cullable) entity;
            if (!cullable.isForcedVisible()) {
                if (!isInRange(getPos(entity), cameraMC, 128 /*EntityCullingModBase.instance.config.tracingDistance*/)) {
                    cullable.setCulled(false); // If your entity view distance is larger than tracingDistance just
                                               // render it
                    continue;
                }
                Box boundingBox = entity.boundingBox;
                idk = false;
                idk(camera, cullable, boundingBox);
            }
        }
    }

    private void cullBlockEntities(net.minecraft.util.math.Vec3d cameraMC, Vec3d camera) {
        if (disableBlockEntityCulling) {
            return;
        }
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                Chunk chunk = client.world.method_214(client.player.chunkX + x, client.player.chunkZ + z);
                Iterator iterator = chunk.blockEntities.entrySet().iterator();
                Entry<BlockPos, BlockEntity> entry;
                while (iterator.hasNext()) {
                    try {
                        entry = (Entry<BlockPos, BlockEntity>) iterator.next();
                    } catch (NullPointerException | ConcurrentModificationException ex) {
                        break; // We are not synced to the main thread, so NPE's/CME are allowed here and way
                               // less
                        // overhead probably than trying to sync stuff up for no really good reason
                    }
                    /*if (blockEntityWhitelist.contains(entry.getValue().getType())) {
                        continue;
                    }*/
                    if (EntityCullingModBase.instance.isDynamicWhitelisted(entry.getValue())) {
                        continue;
                    }
                    Cullable cullable = (Cullable) entry.getValue();
                    if (!cullable.isForcedVisible()) {
                        BlockPos pos = entry.getKey();
                        idk = entry.getValue() instanceof SignBlockEntity;
                        if (idk) System.out.println("sup");
                        if (closerThan(pos, cameraMC, 64)) { // 64 is the fixed max tile view distance
                            if (idk) System.out.println("I am going insane");
                            Box boundingBox = EntityCullingModBase.instance.setupBox(entry.getValue(), pos);
                            idk(camera, cullable, boundingBox);
                        }
                    }
                }

            }
        }
    }

    boolean idk = false;

    private void idk(Vec3d camera, Cullable cullable, Box boundingBox) {
        if (Math.abs(boundingBox.maxX - boundingBox.minX) > hitboxLimit || Math.abs(boundingBox.maxY - boundingBox.minY) > hitboxLimit
                || Math.abs(boundingBox.maxZ - boundingBox.minZ) > hitboxLimit) {
            if (idk) System.out.println("too big");
            cullable.setCulled(false); // To big to bother to cull
            return;
        }
        aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        //System.out.println(boundingBox);
        boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
        if (!visible && idk) System.out.println("not visible!!!!");
        cullable.setCulled(!visible);
    }

    private net.minecraft.util.math.Vec3d real(ClientPlayerEntity player) {
        float f = ((MinecraftAccessor) MinecraftAccessor.getInstance()).getTimer().field_2370;
        double d = player.field_1637 + (player.x - player.field_1637) * (double)f;
        double d2 = player.field_1638 + (player.y - player.field_1638) * (double)f;
        double d3 = player.field_1639 + (player.z - player.field_1639) * (double)f;
        return net.minecraft.util.math.Vec3d.create(d, d2, d3);
    }

    public static net.minecraft.util.math.Vec3d getPos(Entity entity) {
        return net.minecraft.util.math.Vec3d.create(entity.x, entity.y, entity.z);
    }

    public static boolean isInRange(net.minecraft.util.math.Vec3d vec3d, net.minecraft.util.math.Vec3d pos, int maxDist) {
        return vec3d.squaredDistanceTo(pos) < maxDist*maxDist*maxDist;
    }

    // Vec3i forward compatibility functions
    private static boolean closerThan(BlockPos blockPos, net.minecraft.util.math.Vec3d position, double d) {
        return distSqr(blockPos, position.x, position.y, position.z, true) < d * d*d;
    }

    private static double distSqr(BlockPos blockPos, double d, double e, double f, boolean bl) {
        double g = bl ? 0.5D : 0.0D;
        double h = (double) blockPos.x + g - d;
        double i = (double) blockPos.y + g - e;
        double j = (double) blockPos.z + g - f;
        return h * h + i * i + j * j;
    }
}
