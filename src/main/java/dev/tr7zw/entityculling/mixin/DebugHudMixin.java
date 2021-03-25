package dev.tr7zw.entityculling.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.entityculling.EntityCullingMod;
import net.minecraft.client.gui.hud.DebugHud;

@Mixin(DebugHud.class)
public class DebugHudMixin {

    @Inject(method = "getLeftText", at = @At("RETURN"))
    public List<String> getLeftText(CallbackInfoReturnable<List<String>> callback) {
        List<String> list = callback.getReturnValue();
        list.add("[Culling] Last pass: " + EntityCullingMod.instance.cullTask.lastTime + "ms");
        list.add("[Culling] Rendered Block Entities: " + EntityCullingMod.instance.renderedBlockEntities + " Skipped: " + EntityCullingMod.instance.skippedBlockEntities);
        list.add("[Culling] Rendered Entities: " + EntityCullingMod.instance.renderedEntities + " Skipped: " + EntityCullingMod.instance.skippedEntities);
        
        EntityCullingMod.instance.renderedBlockEntities = 0;
        EntityCullingMod.instance.skippedBlockEntities = 0;
        EntityCullingMod.instance.renderedEntities = 0;
        EntityCullingMod.instance.skippedEntities = 0;
        return list;
    }
    
}
