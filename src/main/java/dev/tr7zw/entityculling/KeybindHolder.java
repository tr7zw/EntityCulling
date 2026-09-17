package dev.tr7zw.entityculling;

import dev.tr7zw.transition.loader.*;
import dev.tr7zw.transition.mc.*;
import lombok.experimental.*;
import net.minecraft.client.*;

public final class KeybindHolder {

    //? if >= 26.3 {

    private static final int UNBOUND_KEY = 0;
    //? } else {
    /*
    private static final int UNBOUND_KEY = -1;
     */
    //? }

    public static final KeybindHolder INSTANCE = new KeybindHolder();
    private boolean initialized = false;
    public final KeyMapping keybind = GeneralUtil.createKeyMapping("key.entityculling.toggle", UNBOUND_KEY,
            "text.entityculling.title");
    public final KeyMapping keybindBoxes = GeneralUtil.createKeyMapping("key.entityculling.toggleBoxes", UNBOUND_KEY,
            "text.entityculling.title");

    private KeybindHolder() {
    }

    public void registerKeybinds() {
        if (initialized)
            return;
        initialized = true;
        ModLoaderUtil.registerKeybind(keybind);
        //? if >= 1.21.4 {

        ModLoaderUtil.registerKeybind(keybindBoxes);
        //? }
    }

}
