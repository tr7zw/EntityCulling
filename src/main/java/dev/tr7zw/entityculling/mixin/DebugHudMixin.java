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

    private int lastTickedEntities = 0;
    private int lastSkippedEntityTicks = 0;
    
    @Inject(method = "getLeftText", at = @At("RETURN"))
    public List<String> getLeftText(CallbackInfoReturnable<List<String>> callback) {
        if(EntityCullingMod.instance.tickedEntities != 0 || EntityCullingMod.instance.skippedEntityTicks != 0) {
            lastTickedEntities = EntityCullingMod.instance.tickedEntities;
            lastSkippedEntityTicks = EntityCullingMod.instance.skippedEntityTicks;
            EntityCullingMod.instance.tickedEntities = 0;
            EntityCullingMod.instance.skippedEntityTicks = 0;
        }
        List<String> list = callback.getReturnValue();
        list.add("[Culling] Last pass: " + EntityCullingMod.instance.cullTask.lastTime + "ms");
        list.add("[Culling] Rendered Block Entities: " + EntityCullingMod.instance.renderedBlockEntities + " Skipped: " + EntityCullingMod.instance.skippedBlockEntities);
        list.add("[Culling] Rendered Entities: " + EntityCullingMod.instance.renderedEntities + " Skipped: " + EntityCullingMod.instance.skippedEntities);
        list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
        
        EntityCullingMod.instance.renderedBlockEntities = 0;
        EntityCullingMod.instance.skippedBlockEntities = 0;
        EntityCullingMod.instance.renderedEntities = 0;
        EntityCullingMod.instance.skippedEntities = 0;

        return list;
    }
    
}
