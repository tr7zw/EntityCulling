package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

@Mixin(ClientLevel.class)
public class ClientWorldMixin {

    private Minecraft mc = Minecraft.getInstance();
    
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    public void tickEntity(Entity entity, CallbackInfo info) {
        if(!EntityCullingMod.instance.config.tickCulling) {
            EntityCullingMod.instance.tickedEntities++;
            return; // disabled
        }
        // Use abstract minecart instead of whitelist to also catch modded Minecarts
        if(entity == mc.player || entity == mc.cameraEntity || entity.isPassenger() || entity.isVehicle() || (entity instanceof AbstractMinecart)) { 
            EntityCullingMod.instance.tickedEntities++;
            return; // never skip the client tick for the player or entities in vehicles/with passengers
        }
        if(EntityCullingMod.instance.tickCullWhistelist.contains(entity.getType())) {
            EntityCullingMod.instance.tickedEntities++;
            return; // whitelisted, don't skip that tick
        }
        if(entity instanceof Cullable) {
            Cullable cull = (Cullable) entity;
            if(cull.isCulled() || cull.isOutOfCamera()) {
                basicTick(entity);
                EntityCullingMod.instance.skippedEntityTicks++;
                info.cancel();
                return;
            } else {
                cull.setOutOfCamera(true);
            }
        }
        EntityCullingMod.instance.tickedEntities++;
    }
    
    private void basicTick(Entity entity) {
        entity.setOldPosAndRot();
        ++entity.tickCount;
        if(entity instanceof LivingEntity living) {
            living.aiStep();
        }
        
    }
    
}
