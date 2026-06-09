package dev.tr7zw.entityculling.access;

import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public interface EntityRendererInter<T extends Entity> {

    boolean shadowShouldShowName(T entity);

    //? if < 26.1 {
    /*
    void shadowRenderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int light, float f);
    
     *///? }

    boolean entityCullingIgnoresCulling(T entity);

    AABB entityCullingGetCullingBox(T entity);

}
