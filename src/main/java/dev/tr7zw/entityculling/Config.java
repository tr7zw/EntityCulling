package dev.tr7zw.entityculling;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public int configVersion = 4;
    public boolean renderNametagsThroughWalls = true;
    public Set<String> blockEntityWhitelist = new HashSet<>(Arrays.asList("tile.beacon"));
    public int tracingDistance = 128;
    public boolean debugMode = false;
    public int sleepDelay = 10;
    public int hitboxLimit = 50;
    public boolean skipMarkerArmorStands = true;
    public boolean showDebugScreenInfo = true;
    
}
