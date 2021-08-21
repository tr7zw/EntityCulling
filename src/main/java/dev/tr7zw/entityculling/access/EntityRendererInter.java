package dev.tr7zw.entityculling.access;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public interface EntityRendererInter<T extends Entity> {

	boolean shadowShouldShowName(T entity);

	void shadowRenderNameTag(T entity, Component component, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int light);

}