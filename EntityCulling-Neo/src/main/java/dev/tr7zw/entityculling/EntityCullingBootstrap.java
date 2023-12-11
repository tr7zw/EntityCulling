package dev.tr7zw.entityculling;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Stupid Forge bootstrap class to work around their removal of sidedness
 * 
 * @author tr7zw
 *
 */
@Mod("entityculling")
public class EntityCullingBootstrap {

    public EntityCullingBootstrap() {
        if (FMLEnvironment.dist.isClient()) {
            new EntityCullingMod();
        }
    }

}
