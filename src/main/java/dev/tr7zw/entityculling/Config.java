package dev.tr7zw.entityculling;

import blue.endless.jankson.Comment;
import net.glasslauncher.mods.api.gcapi.api.ConfigName;
import net.glasslauncher.mods.api.gcapi.api.GConfig;

public class Config {

    @GConfig(value = "config", visibleName = "Entity Culling Config")
    public static final Fields FIELDS = new Fields();

    @SuppressWarnings("CanBeFinal")
    public static class Fields {

        @ConfigName("Disable Entity culling")
        public static Boolean disableEntityCulling = false;

        @ConfigName("Disable Block Entity culling")
        public static Boolean disableBlockEntityCulling = false;

        @ConfigName("F3 Info")
        public static Boolean showF3Info = true;

        @ConfigName("Make glass cull entities and blocks")
        @Comment("for funsies/debugging")
        public static Boolean glassCulls = false;
    }
}
