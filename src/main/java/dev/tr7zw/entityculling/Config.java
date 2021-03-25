package dev.tr7zw.entityculling;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public int configVersion = 1;
    public boolean renderNametagsThroughWalls = true;
    public Set<String> blockEntityWhitelist = new HashSet<>(Arrays.asList("minecraft:beacon", "create:rope_pulley"));
    public int tracingDistance = 128;
    public boolean debugMode = false;
    
}
