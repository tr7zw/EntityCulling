package dev.tr7zw.entityculling;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public int configVersion = 4;
    public boolean renderNametagsThroughWalls = true;
    public Set<String> blockEntityWhitelist = new HashSet<>(Arrays.asList(
            "minecraft:beacon",
            "create:rope_pulley",
            "create:hose_pulley",
            "betterend:eternal_pedestal",
            "draconicevolution:storage_core",
            "draconicevolution:core_stabilizer",
            "draconicevolution:reactor_core",
            "draconicevolution:reactor_stabilizer",
            "draconicevolution:reactor_injector",
            "draconicevolution:grinder"
    ));
    public int tracingDistance = 128;
    public boolean debugMode = false;
    public int sleepDelay = 10;
    public int hitboxLimit = 50;
    public boolean skipMarkerArmorStands = true;
    public boolean tickCulling = true;
    public Set<String> tickCullingWhitelist = new HashSet<>(Arrays.asList("minecraft:firework_rocket", "minecraft:boat"));
    
}
