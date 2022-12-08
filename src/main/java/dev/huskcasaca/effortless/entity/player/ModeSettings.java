package dev.huskcasaca.effortless.entity.player;

import dev.huskcasaca.effortless.buildmode.BuildMode;

public record ModeSettings(
        BuildMode buildMode,
        boolean enableMagnet
) {

    public ModeSettings() {
        this(BuildMode.DISABLE, false);
    }

}
