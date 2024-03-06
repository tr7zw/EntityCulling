package dev.tr7zw.entityculling.mixin;

import java.util.ArrayList;
import java.util.List;

import dev.tr7zw.entityculling.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.entityculling.EntityCullingMod;

@Mixin(InGameHud.class)
public class DebugHudMixin extends DrawContext {

    @Shadow private Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;maxMemory()J"))
    public void getLeftText(float bl, boolean i, int j, int par4, CallbackInfo ci) {
        if (EntityCullingMod.instance.tickedEntities != 0
                || EntityCullingMod.instance.skippedEntityTicks != 0) {
            EntityCullingMod.instance.tickedEntities = 0;
            EntityCullingMod.instance.skippedEntityTicks = 0;
        }
        if (!Config.Fields.showF3Info) {
            return;
        }
        List<String> list = getStrings();

        EntityCullingMod.instance.renderedBlockEntities = 0;
        EntityCullingMod.instance.skippedBlockEntities = 0;
        EntityCullingMod.instance.renderedEntities = 0;
        EntityCullingMod.instance.skippedEntities = 0;

        TextRenderer textRenderer = this.minecraft.textRenderer;
        for (int i1 = 0; i1 < list.size(); i1++) {
            String s = list.get(i1);
            this.drawTextWithShadow(textRenderer, s, 2, 106+i1*9, 0xE0E0E0);
        }
    }

    @Unique
    @NotNull
    private static List<String> getStrings() {
        List<String> list = new ArrayList<>();
        list.add("[Culling] Last pass: " + EntityCullingMod.instance.cullTask.lastTime + "ms");
        if (!Config.Fields.disableBlockEntityCulling) {
            list.add("[Culling] Rendered Block Entities: " + EntityCullingMod.instance.renderedBlockEntities
                    + " Skipped: " + EntityCullingMod.instance.skippedBlockEntities);
        }
        if (!Config.Fields.disableEntityCulling) {
            list.add("[Culling] Rendered Entities: " + EntityCullingMod.instance.renderedEntities + " Skipped: "
                    + EntityCullingMod.instance.skippedEntities);
            //list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
        }
        return list;
    }

}
