package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.tr7zw.entityculling.versionless.EntityCullingVersionlessBase;
import dev.tr7zw.entityculling.versionless.access.Cullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(value = { Entity.class, BlockEntity.class })
public class CullableMixin implements Cullable {

    private long lasttime = 0;
    private boolean culled = false;
    private boolean outOfCamera = false;

    @Override
    public void setTimeout() {
        lasttime = System.currentTimeMillis() + 1000;
    }

    @Override
    public boolean isForcedVisible() {
        return lasttime > System.currentTimeMillis();
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
        if (!EntityCullingVersionlessBase.enabled)
            return false;
        return culled;
    }

    @Override
    public void setOutOfCamera(boolean value) {
        this.outOfCamera = value;
    }

    @Override
    public boolean isOutOfCamera() {
        if (!EntityCullingVersionlessBase.enabled)
            return false;
        return outOfCamera;
    }

}
