package dev.tr7zw.entityculling.versionless;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public int configVersion = 8;
    public boolean renderNametagsThroughWalls = true;
    // spotless:off
    public Set<String> blockEntityWhitelist = new HashSet<>(
            Arrays.asList(
              "betterend:eternal_pedestal",
              "botania:falling_star",
              "botania:flame_ring",
              "botania:magic_missile",
              "create:hose_pulley",
              "create:rope_pulley",
              "minecraft:beacon"
	));
    public Set<String> entityWhitelist = new HashSet<>(
            Arrays.asList(
              "botania:mana_burst",
			  "drg_flares:drg_flares",
			  "quark:soul_bead"
	));
	// spotless:on
    public int tracingDistance = 128;
    public boolean debugMode = false;
    public int sleepDelay = 10;
    public int hitboxLimit = 50;
    public int captureRate = 5;
    public boolean tickCulling = true;
    // spotless:off
    public Set<String> tickCullingWhitelist = new HashSet<>(
            Arrays.asList(
              "alexscaves:gum_worm",
	          "alexscaves:gum_worm_segment",
	          "avm_staff:campfire_flame",
			  "cinematiccataclysm:ancient_remnant_cutscene",
    		  "cinematiccataclysm:ignis_cutscene",
   		      "cinematiccataclysm:leviathan_cutscene",
   		      "cinematiccataclysm:maledictus_cutscene",
   		      "cinematiccataclysm:scylla_cutscene",
	          "create:carriage_contraption",
	          "create:contraption",
	          "create:gantry_contraption",
	          "create:stationary_contraption",
	          "createbigcannons:cannon_carriage",
	          "createbigcannons:pitch_contraption",
	          "drg_flares:drg_flare",
	          "drg_flares:drg_flares",
	          "minecraft:acacia_boat",
	          "minecraft:acacia_chest_boat",
	          "minecraft:bamboo_chest_raft",
	          "minecraft:bamboo_raft",
	          "minecraft:birch_boat",
	          "minecraft:birch_chest_boat",
	          "minecraft:block_display",
	          "minecraft:boat",
	          "minecraft:cherry_boat",
	          "minecraft:cherry_chest_boat",
	          "minecraft:dark_oak_boat",
	          "minecraft:dark_oak_chest_boat",
	          "minecraft:firework_rocket",
	          "minecraft:item_display",
	          "minecraft:jungle_boat",
	          "minecraft:jungle_chest_boat",
	          "minecraft:mangrove_boat",
	          "minecraft:mangrove_chest_boat",
	          "minecraft:oak_boat",
	          "minecraft:oak_chest_boat",
	          "minecraft:pale_oak_boat",
	          "minecraft:pale_oak_chest_boat",
	          "minecraft:spruce_boat",
	          "minecraft:spruce_chest_boat",
	          "minecraft:text_display",
	          "mts:builder_existing",
	          "mts:builder_rendering",
	          "mts:builder_seat",
	          "voidscape:corrupted_pawn"
	));
	// spotless:on
    public boolean disableF3 = false;
    public boolean skipEntityCulling = false;
    public boolean skipBlockEntityCulling = false;
    public boolean blockEntityFrustumCulling = true;
    public boolean forceDisplayCulling = false;
    public boolean solidLeaves = false;

}
