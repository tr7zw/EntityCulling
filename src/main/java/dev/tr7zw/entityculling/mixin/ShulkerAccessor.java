package dev.tr7zw.entityculling.mixin;

import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(Shulker.class)
public interface ShulkerAccessor {

    //? if >= 1.17 {
    @Invoker
    boolean callUpdatePeekAmount();

    @Invoker
    void callOnPeekAmountChange();
    //? }

}
