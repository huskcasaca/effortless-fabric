package dev.huskcasaca.effortless.buildreach;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessDataProvider;
import dev.huskcasaca.effortless.entity.player.ReachSettings;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ClientboundPlayerReachPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ReachHelper {

    public static int MIN_MAX_REACH_DISTANCE = 0;
    public static int MAX_MAX_REACH_DISTANCE = 512;

    public static int MIN_MAX_BLOCK_PLACE_PER_AXIS = 0;
    public static int MAX_MAX_BLOCK_PLACE_PER_AXIS = 512;

    public static int MIN_MAX_BLOCK_PLACE_AT_ONCE = 0;
    public static int MAX_MAX_BLOCK_PLACE_AT_ONCE = 100_000;

    public static int MIN_UNDO_STACK_SIZE = 0;
    public static int MAX_UNDO_STACK_SIZE = 100;

    public static ReachSettings getReachSettings(Player player) {
        return ((EffortlessDataProvider) player).getReachSettings();
    }

    public static void setReachSettings(Player player, ReachSettings reachSettings) {
        if (player == null) {
            Effortless.log("Cannot set reach settings, player is null");
            return;
        }
        ((EffortlessDataProvider) player).setReachSettings(reachSettings);
    }

    public static String getSanitizeMessage(ReachSettings modeSettings, Player player) {
        int maxReach = ReachHelper.getMaxReach(player);
        String error = "";
        //TODO sanitize
        return error;
    }

    public static ReachSettings sanitize(ReachSettings reachSettings, Player player) {
//        maxBlockPlaceAtOnce = Math.toIntExact(Math.round(maxBlockPlaceAtOnce / 1000.0)) * 1000;
        return new ReachSettings(
                Math.max(MIN_MAX_REACH_DISTANCE, Math.min(reachSettings.maxReachDistance(), MAX_MAX_REACH_DISTANCE)),
                Math.max(MIN_MAX_BLOCK_PLACE_PER_AXIS, Math.min(reachSettings.maxBlockPlacePerAxis(), MAX_MAX_BLOCK_PLACE_PER_AXIS)),
                Math.max(MIN_MAX_BLOCK_PLACE_AT_ONCE, Math.min(reachSettings.maxBlockPlaceAtOnce(), MAX_MAX_BLOCK_PLACE_AT_ONCE)),
                reachSettings.canBreakFar(),
                reachSettings.enableUndo(),
                Math.max(MIN_UNDO_STACK_SIZE, Math.min(reachSettings.undoStackSize(), MAX_UNDO_STACK_SIZE))
        );
    }

    public static void handleNewPlayer(ServerPlayer player) {
        if (!player.level.isClientSide) {
            Packets.sendToClient(new ClientboundPlayerReachPacket(((EffortlessDataProvider) player).getReachSettings()), (ServerPlayer) player);
        }
    }
    public static int getMaxReach(Player player) {
        return getReachSettings(player).maxReachDistance();
    }

    public static int getPlacementReach(Player player) {
        return getMaxReach(player) / 4;
    }

    public static int getMaxBlocksPlacedAtOnce(Player player) {
        return getReachSettings(player).maxBlockPlaceAtOnce();
    }

    public static int getMaxBlocksPerAxis(Player player) {
        return getReachSettings(player).maxBlockPlacePerAxis();
    }

    public static boolean canBreakFar(Player player) {
        return getReachSettings(player).canBreakFar();
    }

}
