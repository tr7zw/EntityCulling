package dev.tr7zw.entityculling.mixin;

import java.util.ArrayList;
import java.util.List;

import dev.tr7zw.entityculling.Config;
import net.fabricmc.loader.api.FabricLoader;
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

//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/Lighting;turnOff()V"))
//    public void clearDebugVariables(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {
//        if (this.minecraft.options.debugHud) {
//            if (FabricLoader.getInstance().isModLoaded("betterf3")) {
//                this.minecraft.getRenderChunkDebugInfo();
//                this.minecraft.getRenderEntityDebugInfo();
//                this.minecraft.getWorldDebugInfo();
//                this.minecraft.getChunkSourceDebugInfo();
//            }
//
//            if (  (0 != EntityCullingMod.instance.tickedEntities)
//               || (0 != EntityCullingMod.instance.skippedEntityTicks)
//            ) {
//                EntityCullingMod.instance.tickedEntities = 0;
//                EntityCullingMod.instance.skippedEntityTicks = 0;
//            }
//
//            EntityCullingMod.instance.renderedBlockEntities = 0;
//            EntityCullingMod.instance.skippedBlockEntities = 0;
//            EntityCullingMod.instance.renderedEntities = 0;
//            EntityCullingMod.instance.skippedEntities = 0;
//        }
//    }
//
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;maxMemory()J"))
//    public void getLeftText(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {
//        if (!Config.FIELDS.showF3Info) {
//            return;
//        }
//
//        TextRenderer textRenderer = this.minecraft.textRenderer;
//        List<String> debugLines = getStrings();
//
//        for (int lineIndex = 0; lineIndex < debugLines.size(); lineIndex++) {
//            String debugString = debugLines.get(lineIndex);
//            this.drawTextWithShadow( textRenderer
//                                   , debugString
//                                   , 2
//                                   , 106 + Config.FIELDS.f3InfoYOffset + (lineIndex * 9)
//                                   , 0xE0E0E0 );
//        }
//    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;maxMemory()J"))
    public void getLeftText(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {
        if (  (0 != EntityCullingMod.instance.tickedEntities)
           || (0 != EntityCullingMod.instance.skippedEntityTicks)
        ) {
            EntityCullingMod.instance.tickedEntities = 0;
            EntityCullingMod.instance.skippedEntityTicks = 0;
        }

        if (!Config.FIELDS.showF3Info) {
            return;
        }

        List<String> debugLines = getStrings();
        TextRenderer textRenderer = this.minecraft.textRenderer;

        for (int lineIndex = 0; lineIndex < debugLines.size(); lineIndex++) {
            String debugString = debugLines.get(lineIndex);
            this.drawTextWithShadow( textRenderer
                    , debugString
                    , 2
                    , 106 + Config.FIELDS.f3InfoYOffset + (lineIndex * 9)
                    , 0xE0E0E0 );
        }

        EntityCullingMod.instance.renderedBlockEntities = 0;
        EntityCullingMod.instance.skippedBlockEntities = 0;
        EntityCullingMod.instance.renderedEntities = 0;
        EntityCullingMod.instance.skippedEntities = 0;
    }

    @Unique
    @NotNull
    private static List<String> getStrings() {
        List<String> list = new ArrayList<>();

        list.add("[Culling] Last pass: " + EntityCullingMod.instance.cullTask.lastTime + "ms");

        if (!Config.FIELDS.disableBlockEntityCulling) {
            list.add( "[Culling] Rendered Block Entities: "
                    + EntityCullingMod.instance.renderedBlockEntities
                    + " Skipped: "
                    + EntityCullingMod.instance.skippedBlockEntities );
        }

        if (!Config.FIELDS.disableEntityCulling) {
            list.add( "[Culling] Rendered Entities: "
                    + EntityCullingMod.instance.renderedEntities
                    + " Skipped: "
                    + EntityCullingMod.instance.skippedEntities );

            //list.add("[Culling] Ticked Entities: " + lastTickedEntities + " Skipped: " + lastSkippedEntityTicks);
        }
        return list;
    }

}
