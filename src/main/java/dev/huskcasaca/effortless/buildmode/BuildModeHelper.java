package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.entity.player.EffortlessDataProvider;
import dev.huskcasaca.effortless.entity.player.ModeSettings;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ClientboundPlayerBuildModePacket;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerSetBuildModePacket;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class BuildModeHelper {

    //Retrieves the buildsettings of a player through the modeCapability capability
    //Never returns null
    public static ModeSettings getModeSettings(Player player) {
        return ((EffortlessDataProvider) player).getModeSettings();
    }

    public static String getTranslatedModeOptionName(Player player) {
        BuildMode mode = BuildModeHelper.getModeSettings(player).buildMode();
        if (mode == BuildMode.DISABLE) {
            return I18n.get(mode.getNameKey());
        } else {
            StringBuilder modeName = new StringBuilder();
            for (BuildMode.Option option : mode.getOptions()) {
                modeName.append(I18n.get(BuildActionHandler.getOptionSetting(option).getNameKey()));
                modeName.append(" ");
            }
            return modeName + I18n.get(mode.getNameKey());
        }
    }

    public static void setModeSettings(Player player, ModeSettings modeSettings) {
        if (player == null) {
            Effortless.log("Cannot set buildmode settings, player is null");
            return;
        }
        ((EffortlessDataProvider) player).setModeSettings(modeSettings);
    }

    public static BuildMode getBuildMode(Player player) {
        return getModeSettings(player).buildMode();
    }

    public static void setBuildMode(Player player, BuildMode mode) {
        ModeSettings modeSettings = getModeSettings(player);
        modeSettings = new ModeSettings(mode, modeSettings.enableMagnet());
        setModeSettings(player, modeSettings);
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer) {
            Packets.sendToClient(new ClientboundPlayerBuildModePacket(getModeSettings(player)), (ServerPlayer) player);
        } else {
            Packets.sendToServer(new ServerboundPlayerSetBuildModePacket(getModeSettings(player)));
        }
    }

    public static boolean isEnableMagnet(Player player) {
        return getModeSettings(player).enableMagnet();
    }

    public static void setEnableMagnet(Player player, boolean enableMagnet) {
        ModeSettings modeSettings = getModeSettings(player);
        modeSettings = new ModeSettings(modeSettings.buildMode(), enableMagnet);
        setModeSettings(player, modeSettings);
    }


    public static String getSanitizeMessage(ModeSettings modeSettings, Player player) {
        int maxReach = ReachHelper.getMaxReachDistance(player);
        String error = "";

        //TODO sanitize

        return error;
    }

    public static ModeSettings sanitize(ModeSettings modeSettings, Player player) {
        return modeSettings;
    }

}
