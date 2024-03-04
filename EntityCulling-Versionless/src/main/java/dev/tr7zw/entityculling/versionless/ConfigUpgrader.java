package dev.tr7zw.entityculling.versionless;

import java.util.Arrays;

public class ConfigUpgrader {

    public static boolean upgradeConfig(Config config) {
        boolean changed = false;
        if (config.configVersion <= 1) {
            config.blockEntityWhitelist.add("betterend:eternal_pedestal");
            config.configVersion = 2;
            changed = true;
        }
        if (config.configVersion <= 3) { // added tickCulling config
            config.configVersion = 3;
            changed = true;
        }
        if (config.configVersion < 4) {
            config.configVersion = 4;
            config.skipMarkerArmorStands = true;
            config.tickCullingWhitelist.add("minecraft:boat");
            changed = true;
        }
        if (config.configVersion < 5) {
            config.configVersion = 5;
            changed = true;
        }
        if (config.configVersion < 6) {
            config.configVersion = 6;
            changed = true;
            config.tickCullingWhitelist
                    .addAll(Arrays.asList("mts:builder_existing", "mts:builder_rendering", "mts:builder_seat"));
        }
        // check for more changes here

        return changed;
    }

}
