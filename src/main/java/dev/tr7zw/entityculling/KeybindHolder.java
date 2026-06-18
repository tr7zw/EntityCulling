package dev.tr7zw.entityculling;

import dev.tr7zw.transition.loader.*;
import dev.tr7zw.transition.mc.*;
import lombok.experimental.*;
import net.minecraft.client.*;

public final class KeybindHolder {
    public static final KeybindHolder INSTANCE = new KeybindHolder();
    private boolean initialized = false;
    public final KeyMapping keybind = GeneralUtil.createKeyMapping("key.entityculling.toggle", -1,
            "text.entityculling.title");
    public final KeyMapping keybindBoxes = GeneralUtil.createKeyMapping("key.entityculling.toggleBoxes", -1,
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
