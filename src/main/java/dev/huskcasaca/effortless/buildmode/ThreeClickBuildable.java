package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.building.ReachHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class ThreeClickBuildable extends MultipleClickBuildable {
    protected Dictionary<UUID, BlockPos> secondPosTable = new Hashtable<>();

    //Finds height after floor has been chosen in buildmodes with 3 clicks
    public static BlockPos findHeight(Player player, BlockPos secondPos, boolean skipRaytrace) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        List<HeightCriteria> criteriaList = new ArrayList<>(3);

        //X
        var xBound = BuildModeHandler.findXBound(secondPos.getX(), start, look);
        criteriaList.add(new HeightCriteria(xBound, secondPos, start));

        //Z
        var zBound = BuildModeHandler.findZBound(secondPos.getZ(), start, look);
        criteriaList.add(new HeightCriteria(zBound, secondPos, start));

        //Remove invalid criteria
        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //If only 1 is valid, choose that one
        var selected = criteriaList.get(0);

        //If multiple are valid, choose based on criteria
        if (criteriaList.size() > 1) {
            //Select the one that is closest (from wall position to its line counterpart)
            for (int i = 1; i < criteriaList.size(); i++) {
                HeightCriteria criteria = criteriaList.get(i);
                if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
                    //Both very close to line, choose closest to player
                    if (criteria.distToPlayerSq < selected.distToPlayerSq)
                        selected = criteria;
                } else {
                    //Pick closest to line
                    if (criteria.distToLineSq < selected.distToLineSq)
                        selected = criteria;
                }
            }
        }
        return new BlockPos(selected.lineBound);
    }


    //Finds depth after wall has been chosen in buildmodes with 3 clicks
    public static BlockPos findDepth(Player player, BlockPos secondPos, boolean skipRaytrace) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        List<HeightCriteria> criteriaList = new ArrayList<>(3);

        //Y
//        var yBound = BuildModeHandler.findYBound(secondPos.getY(), start, look);
//        criteriaList.add(new HeightCriteria(yBound, secondPos, start));

        //X
        var xBound = BuildModeHandler.findXBound(secondPos.getX(), start, look);
        criteriaList.add(new HeightCriteria(xBound, secondPos, start));

        //Z
        var zBound = BuildModeHandler.findZBound(secondPos.getZ(), start, look);
        criteriaList.add(new HeightCriteria(zBound, secondPos, start));

        //Remove invalid criteria
        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //If only 1 is valid, choose that one
        var selected = criteriaList.get(0);

        //If multiple are valid, choose based on criteria
        if (criteriaList.size() > 1) {
            //Select the one that is closest (from wall position to its line counterpart)
            for (int i = 1; i < criteriaList.size(); i++) {
                HeightCriteria criteria = criteriaList.get(i);
                if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
                    //Both very close to line, choose closest to player
                    if (criteria.distToPlayerSq < selected.distToPlayerSq)
                        selected = criteria;
                } else {
                    //Pick closest to line
                    if (criteria.distToLineSq < selected.distToLineSq)
                        selected = criteria;
                }
            }
        }
        return new BlockPos(selected.planeBound);
    }

    public static BlockPos findXDepth(Player player, BlockPos secondPos, boolean skipRaytrace) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        List<HeightCriteria> criteriaList = new ArrayList<>(3);

        //X
        var xBound = BuildModeHandler.findXBound(secondPos.getX(), start, look);
        criteriaList.add(new HeightCriteria(xBound, secondPos, start));

        //Remove invalid criteria
        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //If only 1 is valid, choose that one
        var selected = criteriaList.get(0);

        //If multiple are valid, choose based on criteria
        if (criteriaList.size() > 1) {
            //Select the one that is closest (from wall position to its line counterpart)
            for (int i = 1; i < criteriaList.size(); i++) {
                HeightCriteria criteria = criteriaList.get(i);
                if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
                    //Both very close to line, choose closest to player
                    if (criteria.distToPlayerSq < selected.distToPlayerSq)
                        selected = criteria;
                } else {
                    //Pick closest to line
                    if (criteria.distToLineSq < selected.distToLineSq)
                        selected = criteria;
                }
            }
        }
        return new BlockPos(selected.planeBound);
    }

    public static BlockPos findZDepth(Player player, BlockPos secondPos, boolean skipRaytrace) {
        var look = BuildModeHandler.getPlayerLookVec(player);
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

        List<HeightCriteria> criteriaList = new ArrayList<>(3);

        //Z
        var zBound = BuildModeHandler.findZBound(secondPos.getZ(), start, look);
        criteriaList.add(new HeightCriteria(zBound, secondPos, start));

        //Remove invalid criteria
        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //If only 1 is valid, choose that one
        var selected = criteriaList.get(0);

        //If multiple are valid, choose based on criteria
        if (criteriaList.size() > 1) {
            //Select the one that is closest (from wall position to its line counterpart)
            for (int i = 1; i < criteriaList.size(); i++) {
                HeightCriteria criteria = criteriaList.get(i);
                if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
                    //Both very close to line, choose closest to player
                    if (criteria.distToPlayerSq < selected.distToPlayerSq)
                        selected = criteria;
                } else {
                    //Pick closest to line
                    if (criteria.distToLineSq < selected.distToLineSq)
                        selected = criteria;
                }
            }
        }
        return new BlockPos(selected.planeBound);
    }


    @Override
    public void initialize(Player player) {
        super.initialize(player);
        secondPosTable.put(player.getUUID(), BlockPos.ZERO);
    }

    @Override
    public List<BlockPos> onUse(Player player, BlockPos blockPos, Direction hitSide, Vec3 hitVec, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();

        var rightClickTable = player.level.isClientSide ? rightClickTableClient : rightClickTableServer;
        int rightClickNr = rightClickTable.get(player.getUUID());
        rightClickNr++;
        rightClickTable.put(player.getUUID(), rightClickNr);

        if (rightClickNr == 1) {
            //If clicking in air, reset and try again
            if (blockPos == null) {
                rightClickTable.put(player.getUUID(), 0);
                return list;
            }

            //First click, remember starting position
            firstPosTable.put(player.getUUID(), blockPos);
            hitSideTable.put(player.getUUID(), hitSide);
            hitVecTable.put(player.getUUID(), hitVec);
            //Keep list empty, dont place any blocks yet
        } else if (rightClickNr == 2) {
            //Second click, find other floor point
            var firstPos = firstPosTable.get(player.getUUID());
            var secondPos = findSecondPos(player, firstPos, true);

            if (secondPos == null) {
                rightClickTable.put(player.getUUID(), 1);
                return list;
            }

            secondPosTable.put(player.getUUID(), secondPos);

        } else {
            //Third click, place diagonal wall with height
            list = findCoordinates(player, blockPos, skipRaytrace);
            rightClickTable.put(player.getUUID(), 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(Player player, BlockPos blockPos, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        var rightClickTable = player.level.isClientSide ? rightClickTableClient : rightClickTableServer;
        int rightClickNr = rightClickTable.get(player.getUUID());

        if (rightClickNr == 0) {
            if (blockPos != null)
                list.add(blockPos);
        } else if (rightClickNr == 1) {
            var firstPos = firstPosTable.get(player.getUUID());

            var secondPos = findSecondPos(player, firstPos, true);
            if (secondPos == null) return list;

            //Limit amount of blocks you can place per row
            int axisLimit = ReachHelper.getMaxBlockPlacePerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
            if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            //Add diagonal line from first to second
            list.addAll(getIntermediateBlocks(player, x1, y1, z1, x2, y2, z2));

        } else {
            var firstPos = firstPosTable.get(player.getUUID());
            var secondPos = secondPosTable.get(player.getUUID());

            var thirdPos = findThirdPos(player, firstPos, secondPos, skipRaytrace);
            if (thirdPos == null) return list;

            //Limit amount of blocks you can place per row
            int axisLimit = ReachHelper.getMaxBlockPlacePerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX(), x3 = thirdPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY(), y3 = thirdPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ(), z3 = thirdPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
            if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            if (x3 - x1 >= axisLimit) x3 = x1 + axisLimit - 1;
            if (x1 - x3 >= axisLimit) x3 = x1 - axisLimit + 1;
            if (y3 - y1 >= axisLimit) y3 = y1 + axisLimit - 1;
            if (y1 - y3 >= axisLimit) y3 = y1 - axisLimit + 1;
            if (z3 - z1 >= axisLimit) z3 = z1 + axisLimit - 1;
            if (z1 - z3 >= axisLimit) z3 = z1 - axisLimit + 1;

            //Add diagonal line from first to third
            list.addAll(getFinalBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3));
        }

        return list;
    }

    //Finds the place of the second block pos
    protected abstract BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace);

    //Finds the place of the third block pos
    protected abstract BlockPos findThirdPos(Player player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace);

    //After first and second pos are known, we want to visualize the blocks in a way (like floor for cube)
    protected abstract List<BlockPos> getIntermediateBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2);

    //After first, second and third pos are known, we want all the blocks
    public abstract List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3);

    static class HeightCriteria {
        Vec3 planeBound;
        Vec3 lineBound;
        double distToLineSq;
        double distToPlayerSq;

        HeightCriteria(Vec3 planeBound, BlockPos secondPos, Vec3 start) {
            this.planeBound = planeBound;
            this.lineBound = toLongestLine(this.planeBound, secondPos);
            this.distToLineSq = this.lineBound.subtract(this.planeBound).lengthSqr();
            this.distToPlayerSq = this.planeBound.subtract(start).lengthSqr();
        }

        //Make it from a plane into a line, on y axis only
        private Vec3 toLongestLine(Vec3 boundVec, BlockPos secondPos) {
            BlockPos bound = new BlockPos(boundVec);
            return new Vec3(secondPos.getX(), bound.getY(), secondPos.getZ());
        }

        //check if its not behind the player and its not too close and not too far
        //also check if raytrace from player to block does not intersect blocks
        public boolean isValid(Vec3 start, Vec3 look, int reach, Player player, boolean skipRaytrace) {

            return BuildModeHandler.isCriteriaValid(start, look, reach, player, skipRaytrace, lineBound, planeBound, distToPlayerSq);
        }
    }
}
