package dev.tr7zw.entityculling.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("INSTANCE")
    static Minecraft getInstance() {
        throw new UnsupportedOperationException();
    }

    @Accessor("timer")
    Timer getTimer();
}
