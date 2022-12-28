package dev.huskcasaca.effortless.building;

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

    SPEED_NORMAL("speed_normal"),
    SPEED_FAST("speed_fast"),

    THICKNESS_1("thickness_1"),
    THICKNESS_3("thickness_3"),
    THICKNESS_5("thickness_5"),

    PLANE_FULL("plane_full"),
    PLANE_HOLLOW("plane_hollow"),

    CUBE_FULL("cube_full"),
    CUBE_HOLLOW("cube_hollow"),
    CUBE_SKELETON("cube_skeleton"),

    RAISE_SHORT_EDGE("raise_short_edge"),
    RAISE_LONG_EDGE("raise_long_edge"),

    CIRCLE_START_CORNER("circle_start_corner"),
    CIRCLE_START_CENTER("circle_start_center"),

    FACE_HORIZONTAL("face_horizontal"),
    FACE_VERTICAL("face_vertical");

    private final String name;

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
            case SPEED_NORMAL -> "normalSpeed";
            case SPEED_FAST -> "fastSpeed";
            case PLANE_FULL -> "planeFull";
            case PLANE_HOLLOW -> "planeHollow";
            case CUBE_FULL -> "cubeFull";
            case CUBE_HOLLOW -> "cubeHollow";
            case CUBE_SKELETON -> "cubeSkeleton";
            case RAISE_SHORT_EDGE -> "shortEdge";
            case RAISE_LONG_EDGE -> "longEdge";
            case THICKNESS_1 -> "thickness1";
            case THICKNESS_3 -> "thickness3";
            case THICKNESS_5 -> "thickness5";
            case CIRCLE_START_CORNER -> "circleStartCorner";
            case CIRCLE_START_CENTER -> "circleStartCenter";
            case FACE_HORIZONTAL -> "faceHorizontal";
            case FACE_VERTICAL -> "faceVertical";
        };
    }

    public String getNameKey() {
        // TODO: 15/9/22 use ResourceLocation
        return Effortless.MOD_ID + "." + "action" + "." + name;
    }

}
