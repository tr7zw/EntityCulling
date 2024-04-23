package dev.tr7zw.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import dev.tr7zw.entityculling.access.EntityRendererInter;
import dev.tr7zw.entityculling.versionless.access.Cullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class MixinTests {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testInjectedInterfaces() {
        Objenesis objenesis = new ObjenesisStd();
        assertTrue(objenesis.newInstance(Pig.class) instanceof Cullable);
        assertTrue(objenesis.newInstance(SignBlockEntity.class) instanceof Cullable);
        assertTrue(objenesis.newInstance(PigRenderer.class) instanceof EntityRendererInter);

    }

    @Test
    public void testMixins() {
        Objenesis objenesis = new ObjenesisStd();
        objenesis.newInstance(BlockEntityRenderDispatcher.class);
        objenesis.newInstance(ClientLevel.class);
        objenesis.newInstance(Pig.class);
        objenesis.newInstance(SignBlockEntity.class);
        objenesis.newInstance(DebugScreenOverlay.class);
        objenesis.newInstance(PigRenderer.class);
        objenesis.newInstance(LevelRenderer.class);
    }

}