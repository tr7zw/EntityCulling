package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.transition.mc.*;
//? if >= 1.21.9 {
import net.minecraft.client.gui.components.debug.*;
import dev.tr7zw.entityculling.debugEntries.*;
//? }
import net.minecraft.core.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

//? if >= 26.4 {

@Mixin(DebugScreenEntries.class)
public class DebugScreenEntriesAccessor {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(final WritableRegistry<DebugScreenEntry> registry, CallbackInfo ci) {
        Registry.register(registry, GeneralUtil.getResourceLocation("entityculling", "cull_timing"),
                new CullTimingEntry());
        Registry.register(registry, GeneralUtil.getResourceLocation("entityculling", "culled_entities"),
                new CulledEntitiesEntry());
        Registry.register(registry, GeneralUtil.getResourceLocation("entityculling", "culled_block_entities"),
                new CulledBlockEntitiesEntry());
        Registry.register(registry, GeneralUtil.getResourceLocation("entityculling", "tick_culling"),
                new TickCullingEntry());
    }

}

//? } elif >= 1.21.9 {
/*
import net.minecraft.resources.*;

@Mixin(DebugScreenEntries.class)
public interface DebugScreenEntriesAccessor {

    @org.spongepowered.asm.mixin.gen.Invoker
    public static Identifier invokeRegister(Identifier arg, DebugScreenEntry arg2) {
        throw new AssertionError();
    }

}
*///? } else {
/*
 @Mixin(targets = "net.minecraft.client.Minecraft") // dummy for older versions
 public class DebugScreenEntriesAccessor {}
 */
//? }
