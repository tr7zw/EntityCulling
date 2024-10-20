package dev.tr7zw.entityculling.versionless;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public int configVersion = 6;
    public boolean renderNametagsThroughWalls = true;
    public Set<String> blockEntityWhitelist = new HashSet<>(
            Arrays.asList("minecraft:beacon", "create:rope_pulley", "create:hose_pulley", "betterend:eternal_pedestal",
                    "botania:magic_missile", "botania:flame_ring", "botania:falling_star"));
    public Set<String> entityWhitelist = new HashSet<>(Arrays.asList("botania:mana_burst", "drg_flares:drg_flares"));
    public int tracingDistance = 128;
    public boolean debugMode = false;
    public int sleepDelay = 10;
    public int hitboxLimit = 50;
    public boolean skipMarkerArmorStands = true;
    public boolean tickCulling = true;
    public Set<String> tickCullingWhitelist = new HashSet<>(
            Arrays.asList("minecraft:firework_rocket", "minecraft:boat", "create:carriage_contraption",
                    "create:contraption", "create:gantry_contraption", "create:stationary_contraption",
                    "mts:builder_existing", "mts:builder_rendering", "mts:builder_seat",
                    "drg_flares:drg_flares", "alexscaves:gum_worm", "alexscaves:gum_worm_segment"));
    public boolean disableF3 = false;
    public boolean skipEntityCulling = false;
    public boolean skipBlockEntityCulling = false;

}
