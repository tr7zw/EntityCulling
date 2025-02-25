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
        if (config.configVersion < 7) {
            config.configVersion = 7;
            changed = true;
            config.tickCullingWhitelist.addAll(Arrays.asList("minecraft:acacia_boat", "minecraft:acacia_chest_boat",
                    "minecraft:birch_boat", "minecraft:birch_chest_boat", "minecraft:cherry_boat",
                    "minecraft:cherry_chest_boat", "minecraft:dark_oak_boat", "minecraft:dark_oak_chest_boat",
                    "minecraft:jungle_boat", "minecraft:jungle_chest_boat", "minecraft:mangrove_boat",
                    "minecraft:mangrove_chest_boat", "minecraft:oak_boat", "minecraft:oak_chest_boat",
                    "minecraft:pale_oak_boat", "minecraft:pale_oak_chest_boat", "minecraft:spruce_boat",
                    "minecraft:spruce_chest_boat", "minecraft:bamboo_raft", "minecraft:bamboo_chest_raft"));
        }
        // check for more changes here

        return changed;
    }

}
