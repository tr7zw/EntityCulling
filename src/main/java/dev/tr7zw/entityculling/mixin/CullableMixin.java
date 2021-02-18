package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;

@Mixin(value = { Entity.class, BlockEntity.class })
public class CullableMixin implements Cullable {

	private long lasttime = 0;
	private boolean culled = false;
	
	@Override
	public void setTimeout() {
		lasttime = System.currentTimeMillis() + 250;
	}

	@Override
	public boolean isForcedVisible() {
		return lasttime > System.currentTimeMillis();
	}

	@Override
	public void setCulled(boolean value) {
		this.culled = value;
		if(!value) {
			setTimeout();
		}
	}

	@Override
	public boolean isCulled() {
		return culled;
	}

}
