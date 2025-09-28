package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
//#if MC < 12109
//$$import java.text.DecimalFormat;
//$$import java.util.List;
//$$import org.spongepowered.asm.mixin.injection.At;
//$$import org.spongepowered.asm.mixin.injection.Inject;
//$$import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$import dev.tr7zw.entityculling.EntityCullingModBase;
//#endif
import net.minecraft.client.gui.components.DebugScreenOverlay;

@Mixin(DebugScreenOverlay.class)
public class DebugHudMixin {

    //#if MC < 12109
    //$$    private int lastTickedEntities = 0;
    //$$    private int lastSkippedEntityTicks = 0;
    //$$    private final DecimalFormat entityCullingFormatter = new DecimalFormat("###.##");
    //$$    @Inject(method = "getGameInformation", at = @At("RETURN"))
    //$$    public List<String> getLeftText(CallbackInfoReturnable<List<String>> callback) {
    //$$        if (EntityCullingModBase.instance.tickedEntities != 0
    //$$                || EntityCullingModBase.instance.skippedEntityTicks != 0) {
    //$$            lastTickedEntities = EntityCullingModBase.instance.tickedEntities;
    //$$            lastSkippedEntityTicks = EntityCullingModBase.instance.skippedEntityTicks;
    //$$            EntityCullingModBase.instance.tickedEntities = 0;
    //$$            EntityCullingModBase.instance.skippedEntityTicks = 0;
    //$$        }
    //$$        if (EntityCullingModBase.instance.config.disableF3) {
    //$$            return callback.getReturnValue();
    //$$        }
    //$$        List<String> list = callback.getReturnValue();
    //$$        list.add(
    //$$                "[Culling] Last pass: " + entityCullingFormatter.format(EntityCullingModBase.instance.cullTask.lastTime)
    //$$                        + "ms/" + entityCullingFormatter.format(EntityCullingModBase.instance.lastTickTime) + "ms");
    //$$        if (!EntityCullingModBase.instance.config.skipBlockEntityCulling) {
    //$$            list.add("[Culling] Rendered Block Entities: " + EntityCullingModBase.instance.renderedBlockEntities
    //$$                    + " Skipped: " + EntityCullingModBase.instance.skippedBlockEntities);
    //$$        }
    //$$        if (!EntityCullingModBase.instance.config.skipEntityCulling) {
    //$$            list.add("[Culling] Rendered Entities: " + EntityCullingModBase.instance.renderedEntities + " Skipped: "
    //$$                    + EntityCullingModBase.instance.skippedEntities);
    //$$            list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
    //$$        }
    //$$
    //$$        EntityCullingModBase.instance.renderedBlockEntities = 0;
    //$$        EntityCullingModBase.instance.skippedBlockEntities = 0;
    //$$        EntityCullingModBase.instance.renderedEntities = 0;
    //$$        EntityCullingModBase.instance.skippedEntities = 0;
    //$$
    //$$        return list;
    //$$    }
    //#endif

}
