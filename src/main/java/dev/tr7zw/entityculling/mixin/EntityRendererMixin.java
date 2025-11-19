package dev.tr7zw.entityculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> implements EntityRendererInter<T> {

    @Override
    public boolean shadowShouldShowName(T entity) {
        //? if <= 1.21.1 {
/*
         return shouldShowName(entity);
        *///? } else {

        return ((EntityRenderer) (Object) this).createRenderState(entity, 0).nameTag != null;
        //? }
    }

    @Override
    public void shadowRenderNameTag(T entity, Component component, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int light, float delta) {
        //? if >= 1.21.9 {

        //? } else if >= 1.21.2 {
/*
         renderNameTag(((EntityRenderer) (Object) this).createRenderState(entity, delta), component, poseStack,
                multiBufferSource, light);
        *///? } else if >= 1.20.5 {
/*
         renderNameTag(entity, component, poseStack, multiBufferSource, light, delta);
        *///? } else {
/*
         renderNameTag(entity, component, poseStack, multiBufferSource, light);
        *///? }
    }

    //? if <= 1.21.1 {
/*
     @Shadow
     public abstract boolean shouldShowName(T entity);
    *///? }

    //? if < 1.21.9 {
/*
     //? if >= 1.21.2 {

         @Shadow
         public abstract void renderNameTag(net.minecraft.client.renderer.entity.state.EntityRenderState entityRenderState,
                 Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i);
     //? } else if >= 1.20.5 {
/^
         @Shadow
      public abstract void renderNameTag(T entity, Component component, PoseStack poseStack,
      MultiBufferSource multiBufferSource, int i, float f);
     ^///? } else {
/^
         @Shadow
         public abstract void renderNameTag(T entity, Component component, PoseStack poseStack,
      MultiBufferSource multiBufferSource, int i);
     ^///? }
    *///? }

    @Override
    public boolean entityCullingIgnoresCulling(T entity) {
        //? if <= 1.21.1 {
/*
         return entity.noCulling;
        *///? } else {

        return !affectedByCulling(entity);
        //? }
    }

    @Override
    public AABB entityCullingGetCullingBox(T entity) {
        //? if <= 1.21.1 {
/*
         return entity.getBoundingBoxForCulling();
        *///? } else {

        return getBoundingBoxForCulling(entity);
        //? }
    }

    //? if >= 1.21.2 {

    @Shadow
    abstract boolean affectedByCulling(T entity);

    @Shadow
    abstract AABB getBoundingBoxForCulling(T entity);
    //? }

}
