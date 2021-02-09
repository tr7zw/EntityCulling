package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.example.access.Cullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;

@Mixin(value = { Entity.class, BlockEntity.class })
public class CullableMixin implements Cullable {

	private long lasttime = 0;
	
	@Override
	public void setVisible() {
		lasttime = System.currentTimeMillis() + 250;
	}

	@Override
	public boolean forceVisible() {
		return lasttime > System.currentTimeMillis();
	}

}
