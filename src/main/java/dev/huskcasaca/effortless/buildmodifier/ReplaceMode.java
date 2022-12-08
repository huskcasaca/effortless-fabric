package dev.huskcasaca.effortless.buildmodifier;

import dev.huskcasaca.effortless.Effortless;

public enum ReplaceMode {
    DISABLED("replace_disabled"),
    NORMAL("normal_replace"),
    QUICK("quick_replace");

    public String name;

    ReplaceMode(String name) {
        this.name = name;
    }

    public String getCommandName() {
        return switch (this) {
            case DISABLED -> "disabled";
            case NORMAL -> "normal";
            case QUICK -> "quick";
        };
    }

    public String getNameKey() {
        // TODO: 15/9/22 use ResourceLocation
        return Effortless.MOD_ID + ".action." + name;
    }

}
