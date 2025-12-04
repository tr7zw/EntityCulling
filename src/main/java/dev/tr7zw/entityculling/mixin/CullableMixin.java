package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = { Entity.class, BlockEntity.class })
public class CullableMixin implements Cullable {

    @Unique
    private long lastTime = 0;
    @Unique
    private boolean culled = false;
    @Unique
    private boolean outOfCamera = false;

    @Override
    public void setTimeout() {
        lastTime = System.currentTimeMillis() + 100;
    }

    @Override
    public boolean isForcedVisible() {
        return lastTime > System.currentTimeMillis();
    }

    @Override
    public void setCulled(boolean value) {
        this.culled = value;
        if (!value) {
            setTimeout();
        }
    }

    @Override
    public boolean isCulled() {
        if (!EntityCullingMod.enabled)
            return false;
        return culled;
    }

    @Override
    public void setOutOfCamera(boolean value) {
        this.outOfCamera = value;
    }

    @Override
    public boolean isOutOfCamera() {
        if (!EntityCullingMod.enabled)
            return false;
        return outOfCamera;
    }

}
