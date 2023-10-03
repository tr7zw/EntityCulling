package dev.tr7zw.entityculling.access;

import net.minecraft.entity.Entity;

public interface EntityRendererInter<T extends Entity> {

	boolean shadowShouldShowName(T entity);

	void shadowRenderNameTag(T entity, double x, double y, double z);

}