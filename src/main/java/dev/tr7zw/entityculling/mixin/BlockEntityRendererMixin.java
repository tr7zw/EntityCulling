package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.*;
import net.minecraft.client.renderer.blockentity.*;
import org.spongepowered.asm.mixin.*;

@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererMixin
        //? if fabric {
        extends BlockEntityRenderFabricExtension
//? }
{
    // Nothing additional here
}
