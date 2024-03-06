package dev.tr7zw.entityculling.mixin;

import net.minecraft.client.entity.living.player.InputPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InputPlayerEntity.class)
public class ClientPlayerEntityMixin extends CullableMixin {
    @Override
    public boolean isForcedVisible() {
        return true;
    }
}
