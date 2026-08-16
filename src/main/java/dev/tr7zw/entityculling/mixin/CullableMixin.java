package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.EntityCullingMod;
import dev.tr7zw.entityculling.ducks.CullableExt;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = {Entity.class, TileEntity.class})
public class CullableMixin implements CullableExt {

    @Unique
    private long lastTime = 0;
    @Unique
    private boolean culled = false;
    @Unique
    private boolean outOfCamera = false;

    @Override
    public void entityCulling$setTimeout() {
        lastTime = System.currentTimeMillis() + 1000;
    }

    @Override
    public boolean entityCulling$isForcedVisible() {
        return lastTime > System.currentTimeMillis();
    }

    @Override
    public void entityCulling$setCulled(boolean value) {
        this.culled = value;
        if (!value) {
            entityCulling$setTimeout();
        }
    }

    @Override
    public boolean entityCulling$isCulled() {
        if (!EntityCullingMod.enabled) return false;
        return culled;
    }

    @Override
    public void entityCulling$setOutOfCamera(boolean value) {
        this.outOfCamera = value;
    }

    @Override
    public boolean entityCulling$isOutOfCamera() {
        if (!EntityCullingMod.enabled) return false;
        return outOfCamera;
    }

}
