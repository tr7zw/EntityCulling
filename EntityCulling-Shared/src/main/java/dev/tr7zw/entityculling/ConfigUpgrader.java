package dev.tr7zw.entityculling;

public class ConfigUpgrader {

    public static boolean upgradeConfig(Config config) {
        boolean changed = false;
        if(config.configVersion <= 1) {
            config.blockEntityWhitelist.add("betterend:eternal_pedestal");
            config.configVersion = 2;
            changed = true;
        }
        if(config.configVersion <= 3) { // added tickCulling config
            config.configVersion = 3;
            changed = true;
        }
        if(config.configVersion < 4){
            config.configVersion = 4;
            config.skipMarkerArmorStands = true;
            config.tickCullingWhitelist.add("minecraft:boat");
            changed = true;
        }
        // check for more changes here
        
        return changed;
    }
    
}
