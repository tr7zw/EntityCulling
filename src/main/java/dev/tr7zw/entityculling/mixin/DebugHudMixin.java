package dev.tr7zw.entityculling.mixin;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingModBase;

@Mixin(InGameHud.class)
public class DebugHudMixin extends DrawContext {

    @Shadow private Minecraft minecraft;
    @Unique
    private int lastTickedEntities = 0;
    @Unique
    private int lastSkippedEntityTicks = 0;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;maxMemory()J"))
    public void getLeftText(float bl, boolean i, int j, int par4, CallbackInfo ci) {
        if (EntityCullingModBase.instance.tickedEntities != 0
                || EntityCullingModBase.instance.skippedEntityTicks != 0) {
            lastTickedEntities = EntityCullingModBase.instance.tickedEntities;
            lastSkippedEntityTicks = EntityCullingModBase.instance.skippedEntityTicks;
            EntityCullingModBase.instance.tickedEntities = 0;
            EntityCullingModBase.instance.skippedEntityTicks = 0;
        }
        /*if (EntityCullingModBase.instance.config.disableF3) {
            return;
        }*/
        List<String> list = new ArrayList<>();
        list.add("[Culling] Last pass: " + EntityCullingModBase.instance.cullTask.lastTime + "ms");
        //if (!EntityCullingModBase.instance.config.skipBlockEntityCulling) {
            list.add("[Culling] Rendered Block Entities: " + EntityCullingModBase.instance.renderedBlockEntities
                    + " Skipped: " + EntityCullingModBase.instance.skippedBlockEntities);
        //}
        //if (!EntityCullingModBase.instance.config.skipEntityCulling) {
            list.add("[Culling] Rendered Entities: " + EntityCullingModBase.instance.renderedEntities + " Skipped: "
                    + EntityCullingModBase.instance.skippedEntities);
            list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
        //}

        EntityCullingModBase.instance.renderedBlockEntities = 0;
        EntityCullingModBase.instance.skippedBlockEntities = 0;
        EntityCullingModBase.instance.renderedEntities = 0;
        EntityCullingModBase.instance.skippedEntities = 0;

        TextRenderer textRenderer = this.minecraft.textRenderer;
        for (int i1 = 0; i1 < list.size(); i1++) {
            String s = list.get(i1);
            this.drawTextWithShadow(textRenderer, s, 2, 110+i1*9, 0xE0E0E0);
        }
    }

}
