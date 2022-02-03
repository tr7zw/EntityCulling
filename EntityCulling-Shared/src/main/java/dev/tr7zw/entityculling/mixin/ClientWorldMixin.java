package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingModBase;
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
        if(!EntityCullingModBase.instance.config.tickCulling) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // disabled
        }
        // Use abstract minecart instead of whitelist to also catch modded Minecarts
        if(entity == mc.player || entity == mc.cameraEntity || entity.isPassenger() || entity.isVehicle() || (entity instanceof AbstractMinecart)) { 
            EntityCullingModBase.instance.tickedEntities++;
            return; // never skip the client tick for the player or entities in vehicles/with passengers
        }
        if(EntityCullingModBase.instance.entityWhistelist.contains(entity.getType())) {
            EntityCullingModBase.instance.tickedEntities++;
            return; // whitelisted, don't skip that tick
        }
        if(entity instanceof Cullable) {
            Cullable cull = (Cullable) entity;
            if(cull.isCulled() || cull.isOutOfCamera()) {
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
        entity.setPosAndOldPos(entity.getX(), entity.getY(), entity.getZ());
        ++entity.tickCount;
        if(entity instanceof LivingEntity) {
            ((LivingEntity)entity).aiStep();
            if (((LivingEntity)entity).hurtTime > 0)
                ((LivingEntity)entity).hurtTime--;
        }
        
    }
    
}
