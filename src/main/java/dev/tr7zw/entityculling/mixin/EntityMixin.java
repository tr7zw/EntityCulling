package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tr7zw.entityculling.access.DataTrackerAccessor;
import dev.tr7zw.entityculling.access.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class EntityMixin implements EntityAccessor {

    @Shadow
    private boolean glowing;
    @Shadow
    private World world;
    @Shadow
    private DataTracker dataTracker;
    @Shadow
    private static TrackedData<Byte> FLAGS;

    @Override
    public boolean isUnsafeGlowing() {
        return this.glowing || this.world.isClient && this.getUnsafeFlag(6);
    }

    private boolean getUnsafeFlag(int index) {
        Byte flagValues = ((DataTrackerAccessor) dataTracker).getUnsafe(FLAGS);
        if (flagValues == null)
            return false;
        return ((Byte) flagValues & 1 << index) != 0;
    }

}
