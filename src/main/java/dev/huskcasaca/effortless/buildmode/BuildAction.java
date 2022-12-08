package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;

public enum BuildAction {
    UNDO("undo"),
    REDO("redo"),
    REPLACE("replace"),
    MAGNET("magnet"),
    MODIFIER("modifier"),
    // TODO: remove OPEN_PLAYER_SETTINGS
    OPEN_PLAYER_SETTINGS("open_player_settings"),
    SETTINGS("settings"),

    NORMAL_SPEED("normal_speed"),
    FAST_SPEED("fast_speed"),

    FULL("full"),
    HOLLOW("hollow"),

    CUBE_FULL("full"),
    CUBE_HOLLOW("hollow"),
    CUBE_SKELETON("skeleton"),

    SHORT_EDGE("short_edge"),
    LONG_EDGE("long_edge"),

    THICKNESS_1("thickness_1"),
    THICKNESS_3("thickness_3"),
    THICKNESS_5("thickness_5"),

    CIRCLE_START_CORNER("start_corner"),
    CIRCLE_START_CENTER("start_center");

    public String name;

    BuildAction(String name) {
        this.name = name;
    }

    public String getCommandName() {
        return switch (this) {
            case UNDO -> "undo";
            case REDO -> "redo";
            case REPLACE -> "replace";
            case MAGNET -> "magnet";
            case MODIFIER -> "modifier";
            case OPEN_PLAYER_SETTINGS -> "openPlayerSettings";
            case SETTINGS -> "settings";
            case NORMAL_SPEED -> "normalSpeed";
            case FAST_SPEED -> "fastSpeed";
            case FULL -> "full";
            case HOLLOW -> "hollow";
            case CUBE_FULL -> "cubeFull";
            case CUBE_HOLLOW -> "cubeHollow";
            case CUBE_SKELETON -> "cubeSkeleton";
            case SHORT_EDGE -> "shortEdge";
            case LONG_EDGE -> "longEdge";
            case THICKNESS_1 -> "thickness1";
            case THICKNESS_3 -> "thickness3";
            case THICKNESS_5 -> "thickness5";
            case CIRCLE_START_CORNER -> "circleStartCorner";
            case CIRCLE_START_CENTER -> "circleStartCenter";
        };
    }

    public String getNameKey() {
        // TODO: 15/9/22 use ResourceLocation
        return Effortless.MOD_ID + ".action." + name;
    }

}
