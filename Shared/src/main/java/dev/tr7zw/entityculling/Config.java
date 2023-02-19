package dev.tr7zw.entityculling;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public int configVersion = 5;
    public boolean renderNametagsThroughWalls = true;
    public Set<String> blockEntityWhitelist = new HashSet<>(
            Arrays.asList("minecraft:beacon", "create:rope_pulley", "create:hose_pulley", "betterend:eternal_pedestal",
                    "botania:magic_missile", "botania:flame_ring", "botania:falling_star"));
    public Set<String> entityWhitelist = new HashSet<>(Arrays.asList("botania:mana_burst"));
    public int tracingDistance = 128;
    public boolean debugMode = false;
    public int sleepDelay = 10;
    public int hitboxLimit = 50;
    public boolean skipMarkerArmorStands = true;
    public boolean tickCulling = true;
    public Set<String> tickCullingWhitelist = new HashSet<>(
            Arrays.asList("minecraft:firework_rocket", "minecraft:boat", "create:carriage_contraption",
                    "create:contraption", "create:gantry_contraption", "create:stationary_contraption"));
    public boolean disableF3 = false;
    public boolean skipEntityCulling = false;
    public boolean skipBlockEntityCulling = false;

}
