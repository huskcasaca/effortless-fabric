package dev.huskcasaca.effortless;

import dev.huskcasaca.effortless.buildmodifier.BuildModifierHandler;
import dev.huskcasaca.effortless.entity.player.ModeSettings;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Effortless implements ModInitializer {

    public static final String MOD_ID = "effortless";
    public static final Logger logger = LogManager.getLogger();

    public static void onPlayerLogin(ServerPlayer player) {
        BuildModifierHandler.handleNewPlayer(player);
        BuildModeHandler.handleNewPlayer(player);
        ReachHelper.handleNewPlayer(player);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        UndoRedo.clear(player);
        // FIXME: 18/11/22
//        Packets.sendToClient(new ClearUndoMessage(), player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        BuildModifierHandler.handleNewPlayer(player);
        BuildModeHandler.handleNewPlayer(player);
        ReachHelper.handleNewPlayer(player);
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
//        //Set build mode to normal
        var modeSettings = BuildModeHelper.getModeSettings(player);
        modeSettings = new ModeSettings(
                BuildMode.DISABLE,
                modeSettings.enableMagnet()
        );
        BuildModeHelper.setModeSettings(player, modeSettings);

        var modifierSettings = BuildModifierHelper.getModifierSettings(player);
        modifierSettings = new ModifierSettings();

        BuildModifierHelper.setModifierSettings(player, modifierSettings);

        BuildModifierHandler.handleNewPlayer(player);
        BuildModeHandler.handleNewPlayer(player);
        ReachHelper.handleNewPlayer(player);

        UndoRedo.clear(player);
        // FIXME: 18/11/22
//        Packets.sendToClient(new ClearUndoMessage(), player);
    }

    //
    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        BuildModifierHelper.setModifierSettings(newPlayer, BuildModifierHelper.getModifierSettings(oldPlayer));
        BuildModeHelper.setModeSettings(newPlayer, BuildModeHelper.getModeSettings(oldPlayer));
        ReachHelper.setReachSettings(newPlayer, ReachHelper.getReachSettings(oldPlayer));
    }

    public static void log(String msg) {
        logger.info(msg);
    }

    public static void log(Player player, String msg) {
        log(player, msg, false);
    }

    public static void log(Player player, String msg, boolean actionBar) {
        player.displayClientMessage(new TextComponent(msg), actionBar);
    }

    //Log with translation supported, call either on client or server (which then sends a message)
    public static void logTranslate(Player player, String prefix, String translationKey, String suffix, boolean actionBar) {
//		proxy.logTranslate(player, prefix, translationKey, suffix, actionBar);
    }

    @Override
    public void onInitialize() {
    }
}
