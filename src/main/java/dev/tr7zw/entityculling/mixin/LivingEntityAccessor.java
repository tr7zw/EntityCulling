package dev.tr7zw.entityculling.mixin;

import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    //? if > 1.20.1 {
    @Invoker
    void callRefreshDirtyAttributes();
    //? }

}
