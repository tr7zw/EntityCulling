package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//#if MC >= 12109
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {

    @Invoker
    public boolean invokeShouldShowName(LivingEntity livingEntity, double d);

}
//#else
//$$ @Mixin(targets = "net.minecraft.client.Minecraft") // dummy for older versions
//$$ public class LivingEntityRendererAccessor {}
//#endif
