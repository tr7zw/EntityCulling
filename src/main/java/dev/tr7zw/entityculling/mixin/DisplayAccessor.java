package dev.tr7zw.entityculling.mixin;

import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(Display.class)
public interface DisplayAccessor {

    @Invoker
    public void invokeSetWidth(float f);

    @Invoker
    public void invokeSetHeight(float f);

}
