package dev.tr7zw.entityculling;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

/**
 * Stupid Forge bootstrap class to work around their removal of sidedness
 * 
 * @author tr7zw
 *
 */
@Mod("entityculling")
public class EntityCullingBootstrap {

    public EntityCullingBootstrap() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> EntityCullingMod::new);
    }
    
}
