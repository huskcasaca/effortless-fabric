package dev.huskcasaca.effortless.buildmode.threeclick;

import dev.huskcasaca.effortless.buildmode.ThreeClickBuildable;
import dev.huskcasaca.effortless.buildmode.twoclick.Circle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Tunnel extends ThreeClickBuildable {

    public static List<BlockPos> getCylinderBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

        //Get circle blocks (using CIRCLE_START and FILL options built-in)
        List<BlockPos> circleBlocks = Circle.getWallCircleBlocks(player, x1, y1, z1, x2, y2, z2);

        int lowest = Math.min(x1, x3);
        int highest = Math.max(x1, x3);

        //Copy circle on y axis
        for (int x = lowest; x <= highest; x++) {
            for (BlockPos blockPos : circleBlocks) {
                list.add(new BlockPos(x, blockPos.getY(), blockPos.getZ()));
            }
        }

        return list;
    }

    @Override
    public BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace) {
        return dev.huskcasaca.effortless.buildmode.twoclick.Wall.findWall(player, firstPos, skipRaytrace);
    }

    @Override
    public BlockPos findThirdPos(Player player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace) {
        return findDepth(player, secondPos, skipRaytrace);
    }

    @Override
    public List<BlockPos> getIntermediateBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        return Circle.getWallCircleBlocks(player, x1, y1, z1, x2, y2, z2);
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return getCylinderBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }
}