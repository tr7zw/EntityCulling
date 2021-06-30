package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    private MinecraftClient mc = MinecraftClient.getInstance();
    
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    public void tickEntity(Entity entity, CallbackInfo info) {
        if(!EntityCullingMod.instance.config.tickCulling) {
            EntityCullingMod.instance.tickedEntities++;
            return; // disabled
        }
        if(entity == mc.player || entity == mc.cameraEntity || entity == mc.player.getVehicle()) {
            EntityCullingMod.instance.tickedEntities++;
            return; // never skip the client tick for the player itself
        }
        if(EntityCullingMod.instance.tickCullWhistelist.contains(entity.getType())) {
            EntityCullingMod.instance.tickedEntities++;
            return; // whitelisted, don't skip that tick
        }
        if(entity instanceof Cullable) {
            Cullable cull = (Cullable) entity;
            if(cull.isCulled() || cull.isOutOfCamera()) {
                entity.resetPosition();
                ++entity.age;
                EntityCullingMod.instance.skippedEntityTicks++;
                info.cancel();
                return;
            } else {
                cull.setOutOfCamera(true);
            }
        }
        EntityCullingMod.instance.tickedEntities++;
    }
    
}
