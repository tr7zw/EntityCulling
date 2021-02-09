package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

@Mixin(value = {Entity.class})
public abstract class EntityMixin {

	private Text cachedDisplayName = null;
	private Text cachedName = null;
	private AbstractTeam cachedTeam = null;
	
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	public Text getDisplayName(CallbackInfoReturnable<Text> info) {
		if(cachedDisplayName == null/* || !cachedName.equals(getName()) || cachedTeam != getScoreboardTeam()*/) {
			cachedName = getName();
			cachedTeam = getScoreboardTeam();
			cachedDisplayName = Team.modifyText(cachedTeam, cachedName)
					.styled(style -> style.withHoverEvent(this.getHoverEvent()).withInsertion(this.getUuidAsString()));
		}
		info.setReturnValue(cachedDisplayName);
		info.cancel();
		return cachedDisplayName;
	}
	
	@Shadow
	protected abstract Text getName();
	
	@Shadow
	protected abstract AbstractTeam getScoreboardTeam();
	
	@Shadow
	protected abstract HoverEvent getHoverEvent();
	
	@Shadow
	protected abstract String getUuidAsString();
	
}
