package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tr7zw.donor.DonorProvider;
import dev.tr7zw.donor.DonorSkinProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

@Mixin(AbstractClientPlayer.class)
@Unique
public class DonorAbstractClientPlayerMixin implements DonorProvider {

    public DonorSkinProvider trDonorProvider = null;
    
    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    public void getSkinTextureLocationDonor(CallbackInfoReturnable<ResourceLocation> ci) {
        ResourceLocation loc = getDonorSkinProvider().getSkin();
        if(loc != null) {
            ci.setReturnValue(loc);
            ci.cancel(); 
        }
    }

    @Override
    public DonorSkinProvider getDonorSkinProvider() {
        if(trDonorProvider == null) {
            trDonorProvider = new DonorSkinProvider(((AbstractClientPlayer)(Object)this).getUUID());
        }
        return trDonorProvider;
    }
    

    
}
