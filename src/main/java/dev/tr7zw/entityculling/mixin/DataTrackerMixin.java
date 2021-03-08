package dev.tr7zw.entityculling.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tr7zw.entityculling.access.DataTrackerAccessor;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.Entry;
import net.minecraft.entity.data.TrackedData;

@Mixin(DataTracker.class)
public class DataTrackerMixin implements DataTrackerAccessor {

	@Shadow
	private Map<Integer, Entry<?>> entries;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getUnsafe(TrackedData<T> trackedData) {
		return (T) this.entries.get(trackedData.getId()).get();
	}

}
