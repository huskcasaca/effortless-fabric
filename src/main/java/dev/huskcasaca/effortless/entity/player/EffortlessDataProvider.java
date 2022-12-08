package dev.huskcasaca.effortless.entity.player;

import dev.huskcasaca.effortless.entity.player.ModeSettings;
import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import dev.huskcasaca.effortless.entity.player.ReachSettings;


public interface EffortlessDataProvider {

    ModeSettings getModeSettings();

    void setModeSettings(ModeSettings modeSettings);

    ModifierSettings getModifierSettings();

    void setModifierSettings(ModifierSettings modifierSettings);

    ReachSettings getReachSettings();

    void setReachSettings(ReachSettings reachSettings);

}
