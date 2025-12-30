package dev.tr7zw.entityculling.versionless;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public int configVersion = 7;
    public boolean renderNametagsThroughWalls = true;
    public Set<String> blockEntityWhitelist = new HashSet<>(
            Arrays.asList("minecraft:beacon", "create:rope_pulley", "create:hose_pulley", "betterend:eternal_pedestal",
                    "botania:magic_missile", "botania:flame_ring", "botania:falling_star"));
    public Set<String> entityWhitelist = new HashSet<>(
            Arrays.asList("botania:mana_burst", "drg_flares:drg_flares", "quark:soul_bead"));
    public int tracingDistance = 128;
    public boolean debugMode = false;
    public int sleepDelay = 10;
    public int hitboxLimit = 50;
    public int captureRate = 5;
    public boolean tickCulling = true;
    public Set<String> tickCullingWhitelist = new HashSet<>(Arrays.asList("minecraft:firework_rocket", "minecraft:boat",
            "minecraft:acacia_boat", "minecraft:acacia_chest_boat", "minecraft:birch_boat",
            "minecraft:birch_chest_boat", "minecraft:cherry_boat", "minecraft:cherry_chest_boat",
            "minecraft:dark_oak_boat", "minecraft:dark_oak_chest_boat", "minecraft:jungle_boat",
            "minecraft:jungle_chest_boat", "minecraft:mangrove_boat", "minecraft:mangrove_chest_boat",
            "minecraft:oak_boat", "minecraft:oak_chest_boat", "minecraft:pale_oak_boat",
            "minecraft:pale_oak_chest_boat", "minecraft:spruce_boat", "minecraft:spruce_chest_boat",
            "minecraft:bamboo_raft", "minecraft:bamboo_chest_raft", "create:carriage_contraption", "create:contraption",
            "create:gantry_contraption", "create:stationary_contraption", "mts:builder_existing",
            "mts:builder_rendering", "mts:builder_seat", "drg_flares:drg_flares", "drg_flares:drg_flare",
            "alexscaves:gum_worm", "alexscaves:gum_worm_segment", "avm_staff:campfire_flame", "minecraft:block_display",
            "minecraft:item_display", "minecraft:text_display", "voidscape:corrupted_pawn"));
    public boolean disableF3 = false;
    public boolean skipEntityCulling = false;
    public boolean skipBlockEntityCulling = false;
    public boolean blockEntityFrustumCulling = true;
    public boolean forceDisplayCulling = false;

}
