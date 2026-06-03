package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.versionless.tree64.Tree64SubChunk;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.access.LevelChunkSectionTree64Access;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.chunk.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements LevelChunkSectionTree64Access {

    @Unique
    private volatile Tree64SubChunk entityCulling$tree64;

    @Unique
    private volatile boolean entityCulling$treeDirty = true;

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
        entityCulling$treeDirty = true;

    }

    @Unique
    private void onSetBlockState(int sectionX, int sectionY, int sectionZ, BlockState state) {
        Tree64SubChunk tree = entityCulling$tree64;
        if (tree == null) {
            entityCulling$treeDirty = true;
            return;
        }

        tree.setOpaqueLocal(sectionX, sectionY, sectionZ, isOpaqueState(state));
    }

    @Unique
    private boolean isOpaqueState(BlockState state) {
        if (EntityCullingModBase.instance != null && EntityCullingModBase.instance.config.solidLeaves
                && state.getBlock() instanceof LeavesBlock) {
            return true;
        }
        //? if <= 1.21.1 {
        /*
         return state.isSolidRender(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO);
        *///? } else {
        return state.isSolidRender();
        //? }
    }

    @Override
    public Tree64SubChunk entityCulling$getTree64() {
        return entityCulling$tree64;
    }

    @Override
    public void entityCulling$setTree64(Tree64SubChunk tree64) {
        this.entityCulling$tree64 = tree64;
    }

    @Override
    public boolean entityCulling$isTreeDirty() {
        return entityCulling$treeDirty;
    }

    @Override
    public void entityCulling$setTreeDirty(boolean dirty) {
        this.entityCulling$treeDirty = dirty;
    }

    @Override
    public PalettedContainer<BlockState> entityCulling$getStates() {
        return getStates();
    }

    @Shadow
    abstract PalettedContainer<BlockState> getStates();

}
