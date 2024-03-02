package dev.tr7zw.entityculling.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Shadow protected abstract void method_818(LivingEntity arg, String string, double d, double e, double f, int i);

    @Redirect(method = "method_821", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;method_818(Lnet/minecraft/entity/LivingEntity;Ljava/lang/String;DDDI)V"))
    public void po(LivingEntityRenderer instance, LivingEntity string, String d, double e, double f, double i, int j) {
        method_818(string, string.boundingBox.toString(), e, f, i, j);
    }

}
