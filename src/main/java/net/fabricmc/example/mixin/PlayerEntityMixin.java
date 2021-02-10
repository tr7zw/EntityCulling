package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {

	public PlayerEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	private Text cachedDisplayName = null;
	private Text cachedName = null;
	private AbstractTeam cachedTeam = null;
	
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	public Text getDisplayName(CallbackInfoReturnable<Text> info) {
		if(cachedDisplayName == null || !cachedName.equals(getName()) || cachedTeam != getScoreboardTeam()) {
			cachedName = getName();
			cachedTeam = getScoreboardTeam();
			cachedDisplayName = Team.modifyText(cachedTeam, cachedName);
		}
		info.setReturnValue(cachedDisplayName);
		info.cancel();
		return cachedDisplayName;
	}
	
}
