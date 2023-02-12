package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

@Mixin(ClientLevel.class)
public class ClientWorldMixin {

    private Minecraft mc = Minecraft.getInstance();

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    public void tickEntity(Entity entity, CallbackInfo info) {
        if (!EntityCullingModBase.instance.config.tickCulling) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // disabled
        }
        // Use abstract minecart instead of whitelist to also catch modded Minecarts
        if (entity == mc.player || entity == mc.cameraEntity || entity.isPassenger() || entity.isVehicle()
                || (entity instanceof AbstractMinecart)) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // never skip the client tick for the player or entities in vehicles/with
                    // passengers
        }
        if (EntityCullingModBase.instance.entityWhistelist.contains(entity.getType())) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // whitelisted, don't skip that tick
        }
        if (entity instanceof Cullable) {
            Cullable cull = (Cullable) entity;
            if (cull.isCulled() || cull.isOutOfCamera()) {
                basicTick(entity);
                EntityCullingModBase.instance.skippedEntityTicks++;
                info.cancel();
                return;
            } else {
                cull.setOutOfCamera(true);
            }
        }
        EntityCullingModBase.instance.tickedEntities++;
    }

    private void basicTick(Entity entity) {
        entity.setOldPosAndRot();
        ++entity.tickCount;
        if (entity instanceof LivingEntity living) {
            living.aiStep();
            if (living.hurtTime > 0)
                living.hurtTime--;
        }
        // the warden sounds are generated clientside instead of serverside, so simulate that part of the code here.
        if (entity instanceof Warden warden) {
            if (mc.level.isClientSide() && !warden.isSilent()
                    && warden.tickCount % getWardenHeartBeatDelay(warden) == 0) {
                mc.level.playLocalSound(warden.getX(), warden.getY(), warden.getZ(), SoundEvents.WARDEN_HEARTBEAT,
                        warden.getSoundSource(), 5.0F, warden.getVoicePitch(), false);
            }
        }
    }

    /**
     * Copy of that method, since it's private. No need to use an access widener for
     * this
     * 
     * @param warden
     * @return
     */
    private int getWardenHeartBeatDelay(Warden warden) {
        float f = warden.getClientAngerLevel() / AngerLevel.ANGRY.getMinimumAnger();
        return 40 - Mth.floor(Mth.clamp(f, 0.0F, 1.0F) * 30.0F);
    }

}
