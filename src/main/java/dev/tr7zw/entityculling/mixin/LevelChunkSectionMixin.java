package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.*;
import dev.tr7zw.entityculling.access.*;
import dev.tr7zw.entityculling.util.*;
import dev.tr7zw.entityculling.versionless.occlusionculling.provider.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.chunk.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelChunkSection.class)
public class LevelChunkSectionMixin implements ChunkSectionAccess {

    @Unique
    private volatile Object entityCulling$providerData;

    @Unique
    private volatile boolean entityCulling$Dirty = true;

    @Override
    public void ec$setProviderData(Object data) {
        this.entityCulling$providerData = data;
    }

    @Override
    public <T> T ec$getProviderData(Class<T> clazz) {
        if (clazz.isInstance(entityCulling$providerData)) {
            return clazz.cast(entityCulling$providerData);
        }
        return null;
    }

    @Override
    public boolean ec$isDirty() {
        return entityCulling$Dirty;
    }

    @Override
    public void ec$setDirty(boolean dirty) {
        this.entityCulling$Dirty = dirty;
    }

    @Override
    public PalettedContainer<BlockState> entityCulling$getStates() {
        return getStates();
    }

    @Shadow
    private PalettedContainer<BlockState> getStates() {
        throw new AssertionError();
    }

    @Inject(method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), require = 0)
    public void setBlockState(final int sectionX, final int sectionY, final int sectionZ, final BlockState state,
            CallbackInfoReturnable<BlockState> ci) {
        onSetBlockState(sectionX, sectionY, sectionZ, state);
    }

    @Inject(method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), require = 0)
    public void setBlockStateLegacy(final int sectionX, final int sectionY, final int sectionZ, final BlockState state,
            final boolean checkThreading, CallbackInfoReturnable<BlockState> ci) {
        onSetBlockState(sectionX, sectionY, sectionZ, state);
    }

    @Inject(method = "recalcBlockCounts", at = @At("HEAD"))
    public void recalcBlockCounts(CallbackInfo ci) {
        entityCulling$Dirty = true;
        if (EntityCullingModBase.instance != null) {
            EntityCullingModBase.instance.getDataProvider().recalcBlockCounts(this);
        }
    }

    @Unique
    private void onSetBlockState(int sectionX, int sectionY, int sectionZ, BlockState state) {
        if (EntityCullingModBase.instance != null) {
            EntityCullingModBase.instance.getDataProvider().onChange(this, sectionX, sectionY, sectionZ,
                    ECUtil.isOpaqueFullCube(state));
        }
    }

}
