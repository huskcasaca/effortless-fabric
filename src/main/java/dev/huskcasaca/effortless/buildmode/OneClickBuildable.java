package dev.huskcasaca.effortless.buildmode;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public abstract class OneClickBuildable implements Buildable {

    public abstract List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1);

}
