package dev.tr7zw.entityculling.mixin;

import dev.tr7zw.entityculling.betterf3.EntityCullingModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ralf2oo2.betterf3.Betterf3;
import ralf2oo2.betterf3.modules.BaseModule;
import ralf2oo2.betterf3.modules.EmptyModule;

@Mixin(Betterf3.class)
public class BetterF3Mixin {

    @Inject(
            method = "<init>",
            at = @At("RETURN"),
            remap = false
    )
    protected void init(CallbackInfo ci) {
        if (!BaseModule.modules.isEmpty()) {
            if (BaseModule.modules.get(BaseModule.modules.size() - 1) instanceof EmptyModule) {
                BaseModule.modules.remove(BaseModule.modules.size() - 1);
            }
        }
        new EntityCullingModule().init();
    }
}
