package dev.tr7zw.entityculling.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <h1>Fixing Mojang calculation inaccuracies in Third-person mode</h1>
 *
 * <p>Description: When the camera is rotated more than ~85 degrees, the camera position goes into the block by thousandths.</p>
 * <br>
 * <p>Solution: Subtract 4 hundredths from the distance of the camera from the player. This solves the problem and is visually impossible to distinguish.</p>
 * @author BenXII12
 */
@Mixin(Camera.class)
public class CameraMixin {
    @Inject(at = @At("RETURN"), method = "getMaxZoom", cancellable = true)
    private void getDetachedCameraMaxDistanceFix(double startingDistance, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValueD() - 0.04);
    }
}
