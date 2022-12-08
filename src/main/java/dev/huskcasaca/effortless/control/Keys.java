package dev.huskcasaca.effortless.control;

import com.mojang.blaze3d.platform.InputConstants;
import dev.huskcasaca.effortless.Effortless;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public enum Keys {

    MODIFIER_MENU("modifier_menu", InputConstants.UNKNOWN.getValue()),
    SHOW_RADIAL_MENU("radial_menu", InputConstants.KEY_LALT),
    //	UNDO("undo", InputConstants.UNKNOWN.getValue()),
//	REDO("redo", InputConstants.UNKNOWN.getValue()),
    TOGGLE_REPLACE("toggle_replace", InputConstants.UNKNOWN.getValue()),
//	TOGGLE_ALT_PLACE("toggle_alt_place", InputConstants.UNKNOWN.getValue()),
    ;

    private KeyMapping keyMapping;
    private final String description;
    private final int key;
    private final boolean modifiable;

    Keys(String description, int defaultKey) {
        this.description = String.join(".", "key", Effortless.MOD_ID, description, "desc");
        this.key = defaultKey;
        this.modifiable = !description.isEmpty();
    }

    public static void register() {
        for (Keys key : values()) {
            key.keyMapping = new KeyMapping(key.description, InputConstants.Type.KEYSYM, key.key, "key.effortless.category");
            if (!key.modifiable)
                continue;

            KeyBindingHelper.registerKeyBinding(key.keyMapping);
        }

    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public boolean isDown() {
        if (!modifiable)
            return isKeyDown(key);
        return keyMapping.isDown();
    }

    public boolean isKeyDown() {
        if (!modifiable)
            return isKeyDown(key);
        return isKeyDown(keyMapping.key.getValue());
    }

    public String getBoundKey() {
        return keyMapping.getTranslatedKeyMessage()
                .getString()
                .toUpperCase();
    }

    public int getBoundCode() {
        return keyMapping.key
                .getValue();
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance()
                .getWindow()
                .getWindow(), key);
    }

    public static boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Minecraft.getInstance()
                .getWindow()
                .getWindow(), button) == 1;
    }

    public static boolean ctrlDown() {
        return Screen.hasControlDown();
    }

    public static boolean shiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean altDown() {
        return Screen.hasAltDown();
    }

}
