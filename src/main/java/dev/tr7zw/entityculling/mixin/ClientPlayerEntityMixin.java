package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.entity.player.ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends CullableMixin {
    @Override
    public boolean isForcedVisible() {
        return true;
    }
}
