package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.class_454;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingModBase;
import net.minecraft.client.Minecraft;

@Mixin(World.class)
public class ClientWorldMixin {

    @Unique
    private Minecraft mc = MinecraftAccessor.getInstance();

    @Inject(method = "method_193", at = @At("HEAD"), cancellable = true)
    public void tickEntity(Entity entity, boolean par2, CallbackInfo ci) {
        if (!(((Object)this) instanceof class_454)) return;
        /*if (!EntityCullingModBase.instance.config.tickCulling
                || EntityCullingModBase.instance.config.skipEntityCulling) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // disabled
        }*/
        // Use abstract minecart instead of whitelist to also catch modded Minecarts
        if (/*entity.noCulling ||*/ entity == mc.player || entity == mc.field_2807 || entity.field_1594 != null
                || entity.field_1593|| (entity instanceof MinecartEntity)) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // never skip the client tick for the player or entities in vehicles/with
                    // passengers. Also respect the "noCulling" flag
        }
        /*if (EntityCullingModBase.instance.entityWhistelist.contains(entity.getType())) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // whitelisted, don't skip that tick
        }*/
        if (entity instanceof Cullable cull) {
            if (cull.isCulled() || cull.isOutOfCamera()) {
                basicTick(entity);
                EntityCullingModBase.instance.skippedEntityTicks++;
                ci.cancel();
                return;
            } else {
                cull.setOutOfCamera(true);
            }
        }
        EntityCullingModBase.instance.tickedEntities++;
    }

    private void basicTick(Entity entity) {
        /*entity.method_1338(entity.x, entity.y, entity.z, entity.yaw, entity.pitch);
        ++entity.field_1645;
        if (entity instanceof LivingEntity living) {
            living.aiStep();
            if (living.hurtTime > 0)
                living.hurtTime--;
        }
        // the warden sounds are generated clientside instead of serverside, so simulate
        // that part of the code here.
        *//*if (entity instanceof Warden warden) {
            if (mc.level.isClientSide() && !warden.isSilent()
                    && warden.tickCount % getWardenHeartBeatDelay(warden) == 0) {
                mc.level.playLocalSound(warden.getX(), warden.getY(), warden.getZ(), SoundEvents.WARDEN_HEARTBEAT,
                        warden.getSoundSource(), 5.0F, warden.getVoicePitch(), false);
            }
        }*/
    }

    /**
     * Copy of that method, since it's private. No need to use an access widener for
     * this
     * 
     * @param warden
     * @return
     */
    /*private int getWardenHeartBeatDelay(Warden warden) {
        float f = warden.getClientAngerLevel() / AngerLevel.ANGRY.getMinimumAnger();
        return 40 - Mth.floor(Mth.clamp(f, 0.0F, 1.0F) * 30.0F);
    }
*/
}
