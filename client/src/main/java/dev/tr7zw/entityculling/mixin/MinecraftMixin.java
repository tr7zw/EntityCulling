package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.EntityCullingMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "tick", at=@At("HEAD"))
    public void clientTick(CallbackInfo ci) {
        EntityCullingMod.instance.clientTick();
    }

    @Inject(method = "tick", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;tick()V"))
    public void worldTick(CallbackInfo ci) {
        EntityCullingMod.instance.worldTick();
    }
}
