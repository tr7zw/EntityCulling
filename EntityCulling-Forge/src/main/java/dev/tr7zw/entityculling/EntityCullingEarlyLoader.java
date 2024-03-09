package dev.tr7zw.entityculling;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import zone.rong.mixinbooter.IEarlyMixinLoader;

public class EntityCullingEarlyLoader implements net.minecraftforge.fml.relauncher.IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("entityculling.mixins.json");
    }
    
}
