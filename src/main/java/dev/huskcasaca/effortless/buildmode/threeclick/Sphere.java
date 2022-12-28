package dev.huskcasaca.effortless.buildmode.threeclick;

import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.buildmode.ThreeClickBuildable;
import dev.huskcasaca.effortless.buildmode.twoclick.Circle;
import dev.huskcasaca.effortless.buildmode.twoclick.Floor;
import dev.huskcasaca.effortless.buildmode.twoclick.Wall;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Sphere extends ThreeClickBuildable {

    public static List<BlockPos> getSphereBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

        float centerX = x1;
        float centerY = y1;
        float centerZ = z1;

        //Adjust for CIRCLE_START

        float radiusX;
        float radiusY;
        float radiusZ;

        if (BuildActionHandler.getOrientation() == BuildAction.FACE_HORIZONTAL) {
            if (BuildActionHandler.getCircleStart() == BuildAction.CIRCLE_START_CORNER) {
                centerX = x1 + (x2 - x1) / 2f;
                centerY = y1 + (y3 - y1) / 2f;
                centerZ = z1 + (z2 - z1) / 2f;
            } else {
                x1 = (int) (centerX - (x2 - centerX));
                y1 = (int) (centerY - (y3 - centerY));
                z1 = (int) (centerZ - (z2 - centerZ));
            }
            radiusX = Mth.abs(x2 - centerX);
            radiusY = Mth.abs(y3 - centerY);
            radiusZ = Mth.abs(z2 - centerZ);
        } else {
            if  (x1 == x2) {
                if (BuildActionHandler.getCircleStart() == BuildAction.CIRCLE_START_CORNER) {
                    centerX = x1 + (x3 - x1) / 2f;
                    centerY = y1 + (y2 - y1) / 2f;
                    centerZ = z1 + (z2 - z1) / 2f;
                } else {
                    x1 = (int) (centerX - (x3 - centerX));
                    y1 = (int) (centerY - (y2 - centerY));
                    z1 = (int) (centerZ - (z2 - centerZ));
                }
                radiusX = Mth.abs(x3 - centerX);
                radiusY = Mth.abs(y2 - centerY);
                radiusZ = Mth.abs(z2 - centerZ);
            } else {
                if (BuildActionHandler.getCircleStart() == BuildAction.CIRCLE_START_CORNER) {
                    centerX = x1 + (x2 - x1) / 2f;
                    centerY = y1 + (y2 - y1) / 2f;
                    centerZ = z1 + (z3 - z1) / 2f;
                } else {
                    x1 = (int) (centerX - (x2 - centerX));
                    y1 = (int) (centerY - (y2 - centerY));
                    z1 = (int) (centerZ - (z3 - centerZ));
                }
                radiusX = Mth.abs(x2 - centerX);
                radiusY = Mth.abs(y2 - centerY);
                radiusZ = Mth.abs(z3 - centerZ);
            }
        }
        if (BuildActionHandler.getPlaneFilling() == BuildAction.PLANE_FULL) {
            addSphereBlocks(list, x1, y1, z1, x3, y3, z3, centerX, centerY, centerZ, radiusX, radiusY, radiusZ);
        } else {
            addHollowSphereBlocks(list, x1, y1, z1, x3, y3, z3, centerX, centerY, centerZ, radiusX, radiusY, radiusZ);
        }

        return list;
    }

    public static void addSphereBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2,
                                       float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {

                    float distance = distance(l, m, n, centerX, centerY, centerZ);
                    float radius = calculateSpheroidRadius(centerX, centerY, centerZ, radiusX, radiusY, radiusZ, l, m, n);
                    if (distance < radius + 0.4f)
                        list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    public static void addHollowSphereBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2,
                                             float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {

                    float distance = distance(l, m, n, centerX, centerY, centerZ);
                    float radius = calculateSpheroidRadius(centerX, centerY, centerZ, radiusX, radiusY, radiusZ, l, m, n);
                    if (distance < radius + 0.4f && distance > radius - 0.6f)
                        list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    private static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return Mth.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
    }

    public static float calculateSpheroidRadius(float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ, int x, int y, int z) {
        //Twice ellipse radius
        float radiusXZ = Circle.calculateEllipseRadius(centerX, centerZ, radiusX, radiusZ, x, z);

        //TODO project x to plane

        return Circle.calculateEllipseRadius(centerX, centerY, radiusXZ, radiusY, x, y);
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
        return getSphereBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }
}