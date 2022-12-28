package dev.huskcasaca.effortless.buildmode.threeclick;

import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.buildmode.ThreeClickBuildable;
import dev.huskcasaca.effortless.buildmode.twoclick.Circle;
import dev.huskcasaca.effortless.buildmode.twoclick.Floor;
import dev.huskcasaca.effortless.buildmode.twoclick.Wall;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Cylinder extends ThreeClickBuildable {

    public static List<BlockPos> getCylinderBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

        if (BuildActionHandler.getOrientation() == BuildAction.FACE_HORIZONTAL) {
            List<BlockPos> circleBlocks = Circle.getFloorCircleBlocks(player, x1, y1, z1, x2, y2, z2);
            int lowest = Math.min(y1, y3);
            int highest = Math.max(y1, y3);

            for (int y = lowest; y <= highest; y++) {
                for (BlockPos blockPos : circleBlocks) {
                    list.add(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
                }
            }
        } else {
            List<BlockPos> circleBlocks = Circle.getWallCircleBlocks(player, x1, y1, z1, x2, y2, z2);
            if (x1 != x2) {
                int lowest = Math.min(z1, z3);
                int highest = Math.max(z1, z3);

                for (int z = lowest; z <= highest; z++) {
                    for (BlockPos blockPos : circleBlocks) {
                        list.add(new BlockPos(blockPos.getX(), blockPos.getY(), z));
                    }
                }
            } else {
                int lowest = Math.min(x1, x3);
                int highest = Math.max(x1, x3);

                for (int x = lowest; x <= highest; x++) {
                    for (BlockPos blockPos : circleBlocks) {
                        list.add(new BlockPos(x, blockPos.getY(), blockPos.getZ()));
                    }
                }
            }
        }

        return list;
    }

    @Override
    public BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace) {
        if (BuildActionHandler.getOrientation() == BuildAction.FACE_HORIZONTAL) {
            return Floor.findFloor(player, firstPos, skipRaytrace);
        } else {
            return Wall.findWall(player, firstPos, skipRaytrace);
        }
    }

    @Override
    public BlockPos findThirdPos(Player player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace) {
        if (BuildActionHandler.getOrientation() == BuildAction.FACE_HORIZONTAL) {
            return findHeight(player, secondPos, skipRaytrace);
        } else {
//           return findDepth(player, secondPos, skipRaytrace);
            if (firstPos.getX() == secondPos.getX()) {
                return findZDepth(player, secondPos, skipRaytrace);
            } else {
                return findXDepth(player, secondPos, skipRaytrace);
            }
        }
    }

    @Override
    public List<BlockPos> getIntermediateBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (BuildActionHandler.getOrientation() == BuildAction.FACE_HORIZONTAL) {
            return Circle.getFloorCircleBlocks(player, x1, y1, z1, x2, y2, z2);
        } else {
            return Circle.getWallCircleBlocks(player, x1, y1, z1, x2, y2, z2);
        }
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return getCylinderBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }
}