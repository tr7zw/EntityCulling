package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//#if MC >= 12109
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;

@Mixin(DebugScreenEntries.class)
public interface DebugScreenEntriesAccessor {

    @Invoker
    public static ResourceLocation invokeRegister(ResourceLocation arg, DebugScreenEntry arg2) {
        throw new AssertionError();
    }

}
//#else
//$$ @Mixin(targets = "net.minecraft.client.Minecraft") // dummy for older versions
//$$ public class DebugScreenEntriesAccessor {}
//#endif
