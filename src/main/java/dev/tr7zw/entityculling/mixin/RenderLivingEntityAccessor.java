package dev.tr7zw.entityculling.mixin;


import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RendererLivingEntity.class)
public interface RenderLivingEntityAccessor {

    @Invoker
    void callPassSpecialRender(EntityLivingBase p_77033_1_, double p_77033_2_, double p_77033_4_, double p_77033_6_);

}
