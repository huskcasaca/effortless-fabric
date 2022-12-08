package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;

public enum BuildOption {
    BUILD_SPEED("build_speed", BuildAction.NORMAL_SPEED, BuildAction.FAST_SPEED),
    FILL("filling", BuildAction.FULL, BuildAction.HOLLOW),
    CUBE_FILL("filling", BuildAction.CUBE_FULL, BuildAction.CUBE_HOLLOW, BuildAction.CUBE_SKELETON),
    RAISED_EDGE("raised_edge", BuildAction.SHORT_EDGE, BuildAction.LONG_EDGE),
    LINE_THICKNESS("thickness", BuildAction.THICKNESS_1, BuildAction.THICKNESS_3, BuildAction.THICKNESS_5),
    CIRCLE_START("circle_start", BuildAction.CIRCLE_START_CORNER, BuildAction.CIRCLE_START_CENTER);

    private final String name;
    private final BuildAction[] actions;

    BuildOption(String name, BuildAction... actions) {
        this.name = name;
        this.actions = actions;
    }

    public String getNameKey() {
        return Effortless.MOD_ID + ".option." + name;
    }

    public BuildAction[] getActions() {
        return actions;
    }
}
