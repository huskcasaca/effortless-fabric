package dev.huskcasaca.effortless.buildmode.threeclick;

import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.buildmode.ThreeClickBuildable;
import dev.huskcasaca.effortless.buildmode.twoclick.Floor;
import dev.huskcasaca.effortless.buildmode.twoclick.Line;
import dev.huskcasaca.effortless.buildmode.twoclick.Wall;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Cube extends ThreeClickBuildable {

    public static List<BlockPos> getFloorBlocksUsingCubeFill(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        List<BlockPos> list = new ArrayList<>();

        if (BuildActionHandler.getCubeFilling() == BuildAction.CUBE_SKELETON)
            Floor.addHollowFloorBlocks(list, x1, x2, y1, z1, z2);
        else
            Floor.addFloorBlocks(list, x1, x2, y1, z1, z2);

        return list;
    }

    public static List<BlockPos> getCubeBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        List<BlockPos> list = new ArrayList<>();

        switch (BuildActionHandler.getCubeFilling()) {
            case CUBE_FULL:
                addCubeBlocks(list, x1, x2, y1, y2, z1, z2);
                break;
            case CUBE_HOLLOW:
                addHollowCubeBlocks(list, x1, x2, y1, y2, z1, z2);
                break;
            case CUBE_SKELETON:
                addSkeletonCubeBlocks(list, x1, x2, y1, y2, z1, z2);
                break;
        }

        return list;
    }

    public static void addCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {
                    list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    public static void addHollowCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        Wall.addXWallBlocks(list, x1, y1, y2, z1, z2);
        Wall.addXWallBlocks(list, x2, y1, y2, z1, z2);

        Wall.addZWallBlocks(list, x1, x2, y1, y2, z1);
        Wall.addZWallBlocks(list, x1, x2, y1, y2, z2);

        Floor.addFloorBlocks(list, x1, x2, y1, z1, z2);
        Floor.addFloorBlocks(list, x1, x2, y2, z1, z2);
    }

    public static void addSkeletonCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        Line.addXLineBlocks(list, x1, x2, y1, z1);
        Line.addXLineBlocks(list, x1, x2, y1, z2);
        Line.addXLineBlocks(list, x1, x2, y2, z1);
        Line.addXLineBlocks(list, x1, x2, y2, z2);

        Line.addYLineBlocks(list, y1, y2, x1, z1);
        Line.addYLineBlocks(list, y1, y2, x1, z2);
        Line.addYLineBlocks(list, y1, y2, x2, z1);
        Line.addYLineBlocks(list, y1, y2, x2, z2);

        Line.addZLineBlocks(list, z1, z2, x1, y1);
        Line.addZLineBlocks(list, z1, z2, x1, y2);
        Line.addZLineBlocks(list, z1, z2, x2, y1);
        Line.addZLineBlocks(list, z1, z2, x2, y2);
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
        return getFloorBlocksUsingCubeFill(player, x1, y1, z1, x2, y2, z2);
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return getCubeBlocks(player, x1, y1, z1, x3, y3, z3);
    }
}