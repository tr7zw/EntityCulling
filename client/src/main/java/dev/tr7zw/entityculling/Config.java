package dev.tr7zw.entityculling;
public class Config {
    // TODO Ornithe config
    public static final Fields FIELDS = new Fields();

    @SuppressWarnings("CanBeFinal")
    public static class Fields {


        public static Boolean disableEntityCulling = false;


        public static Boolean disableBlockEntityCulling = false;


        public static Boolean showF3Info = true;


        public static Boolean glassCulls = true;
    }
}
