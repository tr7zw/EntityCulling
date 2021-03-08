package dev.tr7zw.entityculling.access;

import net.minecraft.entity.data.TrackedData;

/**
 * This is needed because Mojang didn't use locks and finally correctly in the
 * DataTracker class, resulting in permanently locked DataTrackers when invalid
 * data is received from servers like CubeCraft. @Mojang @CubeCraft pls fix.
 * 
 * @author tr7zw
 *
 */
public interface DataTrackerAccessor {

	public <T> T getUnsafe(TrackedData<T> trackedData);

}
