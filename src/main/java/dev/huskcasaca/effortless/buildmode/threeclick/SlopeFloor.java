package dev.huskcasaca.effortless.buildmode.threeclick;

import dev.huskcasaca.effortless.buildmode.BuildAction;
import dev.huskcasaca.effortless.buildmode.BuildActionHandler;
import dev.huskcasaca.effortless.buildmode.ThreeClickBuildable;
import dev.huskcasaca.effortless.buildmode.twoclick.Floor;
import dev.huskcasaca.effortless.buildreach.ReachHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SlopeFloor extends ThreeClickBuildable {

    //Add slope floor from first to second
    public static List<BlockPos> getSlopeFloorBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

        int axisLimit = ReachHelper.getMaxBlockPlacePerAxis(player);

        //Determine whether to use x or z axis to slope up
        boolean onXAxis = true;

        int xLength = Math.abs(x2 - x1);
        int zLength = Math.abs(z2 - z1);

        if (BuildActionHandler.getRaisedEdge() == BuildAction.SHORT_EDGE) {
            //Slope along short edge
            if (zLength > xLength) onXAxis = false;
        } else {
            //Slope along long edge
            if (zLength <= xLength) onXAxis = false;
        }

        if (onXAxis) {
            //Along X goes up

            //Get diagonal line blocks
            List<BlockPos> diagonalLineBlocks = DiagonalLine.getDiagonalLineBlocks(player, x1, y1, z1, x2, y3, z1, 1f);

            //Limit amount of blocks we can place
            int lowest = Math.min(z1, z2);
            int highest = Math.max(z1, z2);

            if (highest - lowest >= axisLimit) highest = lowest + axisLimit - 1;

            //Copy diagonal line on x axis
            for (int z = lowest; z <= highest; z++) {
                for (BlockPos blockPos : diagonalLineBlocks) {
                    list.add(new BlockPos(blockPos.getX(), blockPos.getY(), z));
                }
            }

        } else {
            //Along Z goes up

            //Get diagonal line blocks
            List<BlockPos> diagonalLineBlocks = DiagonalLine.getDiagonalLineBlocks(player, x1, y1, z1, x1, y3, z2, 1f);

            //Limit amount of blocks we can place
            int lowest = Math.min(x1, x2);
            int highest = Math.max(x1, x2);

            if (highest - lowest >= axisLimit) highest = lowest + axisLimit - 1;

            //Copy diagonal line on x axis
            for (int x = lowest; x <= highest; x++) {
                for (BlockPos blockPos : diagonalLineBlocks) {
                    list.add(new BlockPos(x, blockPos.getY(), blockPos.getZ()));
                }
            }
        }

        return list;
    }

    @Override
    protected BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace) {
        return Floor.findFloor(player, firstPos, skipRaytrace);
    }

    @Override
    protected BlockPos findThirdPos(Player player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace) {
        return findHeight(player, secondPos, skipRaytrace);
    }

    @Override
    protected List<BlockPos> getIntermediateBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        return Floor.getFloorBlocks(player, x1, y1, z1, x2, y2, z2);
    }

    @Override
    protected List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return getSlopeFloorBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }
}