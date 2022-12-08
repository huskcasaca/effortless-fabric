package dev.huskcasaca.effortless.helper;

import dev.huskcasaca.effortless.config.ConfigManager;
import net.minecraft.world.entity.player.Player;

public class ReachHelper {
    public static int getMaxReach(Player player) {
        return ConfigManager.getGlobalBuildConfig().getMaxReachDistance();
    }

    public static int getPlacementReach(Player player) {
        return getMaxReach(player) / 4;
    }

    public static int getMaxBlocksPlacedAtOnce(Player player) {
        return ConfigManager.getGlobalBuildConfig().getMaxBlockPlaceAtOnce();
    }

    public static int getMaxBlocksPerAxis(Player player) {
        return ConfigManager.getGlobalBuildConfig().getMaxBlockPlacePerAxis();
    }

    public static boolean canBreakFar(Player player) {
        return ConfigManager.getGlobalBuildConfig().isCanBreakFar();
    }
}
