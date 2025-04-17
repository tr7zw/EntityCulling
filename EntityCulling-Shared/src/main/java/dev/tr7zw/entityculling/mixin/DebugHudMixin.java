package dev.tr7zw.entityculling.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.EntityCullingModBase;
import net.minecraft.client.gui.GuiOverlayDebug;

@Mixin(GuiOverlayDebug.class)
public class DebugHudMixin {

    public DebugHudMixin() {
        EntityCullingModBase.instance.clientTick();
    }

    @Inject(method = "call", at = @At("RETURN"))
    public List<String> getLeftText(CallbackInfoReturnable<List<String>> callback) {
        List<String> list = callback.getReturnValue();
        list.add("[Culling] Last pass: " + EntityCullingModBase.instance.cullTask.lastTime + "ms");
        list.add("[Culling] Rendered Block Entities: " + EntityCullingModBase.instance.renderedBlockEntities + " Skipped: " + EntityCullingModBase.instance.skippedBlockEntities);
        list.add("[Culling] Rendered Entities: " + EntityCullingModBase.instance.renderedEntities + " Skipped: " + EntityCullingModBase.instance.skippedEntities);
        //list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);

        EntityCullingModBase.instance.renderedBlockEntities = 0;
        EntityCullingModBase.instance.skippedBlockEntities = 0;
        EntityCullingModBase.instance.renderedEntities = 0;
        EntityCullingModBase.instance.skippedEntities = 0;

        return list;
    }

}
