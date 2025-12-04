package dev.tr7zw.entityculling;

import net.glasslauncher.mods.gcapi3.api.ConfigEntry;
import net.glasslauncher.mods.gcapi3.api.ConfigRoot;

public class Config {

    @ConfigRoot(value = "config", visibleName = "Entity Culling Config")
    public static final ConfigFields FIELDS = new ConfigFields();

    public static class ConfigFields {

        @ConfigEntry(name = "Disable Entity culling")
        public Boolean disableEntityCulling = false;

        @ConfigEntry(name = "Disable Block Entity culling")
        public Boolean disableBlockEntityCulling = false;

        @ConfigEntry(name = "F3 Info")
        public Boolean showF3Info = true;

        @ConfigEntry(
                name = "F3 Info Y Offset",
                maxLength = 4096,
                minLength = -4096
        )
        public Integer f3InfoYOffset = 0;

        @ConfigEntry(name = "Make glass cull entities and blocks", description = "for funsies/debugging")
        public Boolean glassCulls = false;
    }
}
