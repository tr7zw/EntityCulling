package dev.tr7zw.entityculling;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;

import dev.tr7zw.entityculling.versionless.EntityCullingVersionlessBase;
import dev.tr7zw.entityculling.access.Cullable;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CullTask implements Runnable {

    public boolean requestCull = false;
    public boolean disableEntityCulling = false;
    public boolean disableBlockEntityCulling = false;

    private final OcclusionCullingInstance culling;
    private final int sleepDelay = EntityCullingModBase.instance.config.sleepDelay;
    private final int hitboxLimit = EntityCullingModBase.instance.config.hitboxLimit;
    public double lastTime = 0;

    // reused preallocated vars
    private Vec3d lastPos = new Vec3d(0, 0, 0);
    private Vec3d aabbMin = new Vec3d(0, 0, 0);
    private Vec3d aabbMax = new Vec3d(0, 0, 0);

    // Defensive copy of the required Client data
    @Setter
    private boolean ingame = false;
    @Setter
    private List<Cullable> entitiesForRendering = new ArrayList<>();
    @Setter
    private Map<BlockPos, Cullable> blockEntities = new HashMap<>();
    @Setter
    private Vec3 cameraMC = new Vec3(0, 0, 0);

    public CullTask(OcclusionCullingInstance culling) {
        this.culling = culling;
    }

    @Override
    public void run() {
        while (Minecraft.getInstance().isRunning()) { // client.isRunning() returns false at the start?!?
            try {
                Thread.sleep(sleepDelay);
                if (EntityCullingVersionlessBase.enabled && ingame) {
                    // getEyePosition can use a fixed delta as its debug only anyway;
                    if (requestCull
                            || !(cameraMC.x == lastPos.x && cameraMC.y == lastPos.y && cameraMC.z == lastPos.z)) {
                        long start = System.nanoTime();
                        requestCull = false;
                        lastPos.set(cameraMC.x, cameraMC.y, cameraMC.z);
                        Vec3d camera = lastPos;
                        culling.resetCache();
                        cullBlockEntities(cameraMC, camera);
                        cullEntities(cameraMC, camera);
                        lastTime = (System.nanoTime() - start) / 1_000_000.0;
                    }
                } else {
                    lastTime = 0; // Reset last time if not enabled
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
        Cullable cullable = null;
        Iterator<Cullable> iterable = entitiesForRendering.iterator();
        while (iterable.hasNext()) {
            cullable = iterable.next();
            if (cullable == null) {
                // assume the iterator is broken, cancel the loop
                // https://github.com/tr7zw/EntityCulling/issues/168
                break;
            }
            if (EntityCullingModBase.instance.isEntityDynamicWhitelisted(cullable)) {
                continue;
            }
            if (!cullable.isForcedVisible()) {
                if (cullable.isShouldEntityAppearGlowing()) {
                    cullable.setCulled(false);
                    continue;
                }
                if (!cullable.getEc$Position().closerThan(cameraMC,
                        EntityCullingModBase.instance.config.tracingDistance)) {
                    cullable.setCulled(false); // If your entity view distance is larger than tracingDistance just
                                               // render it
                    continue;
                }
                AABB boundingBox = cullable.getEc$BoundingBox();
                if (boundingBox == null || boundingBox.getXsize() > hitboxLimit || boundingBox.getYsize() > hitboxLimit
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
        Iterator<Entry<BlockPos, Cullable>> iterator = blockEntities.entrySet().iterator();
        Entry<BlockPos, Cullable> entry;
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
            if (EntityCullingModBase.instance.isBlockEntityDynamicWhitelisted(entry.getValue())) {
                continue;
            }
            Cullable cullable = entry.getValue();
            if (!cullable.isForcedVisible()) {
                BlockPos pos = entry.getKey();
                if (closerThan(pos, cameraMC, 64)) { // 64 is the fixed max tile view distance
                    AABB boundingBox = cullable.getEc$BoundingBox();//EntityCullingModBase.instance.setupAABB(entry.getValue(), pos);
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
