package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.example.Drawer;
import net.fabricmc.example.access.CachedText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

@Mixin(value = { TranslatableText.class, LiteralText.class })
public class TextMixin implements CachedText {

	private Drawer drawer = null;
	
	@Override
	public Drawer getDrawer() {
		return drawer;
	}

	@Override
	public void setDrawer(Drawer drawer) {
		this.drawer = drawer;
	}

}
