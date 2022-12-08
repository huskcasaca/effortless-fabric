package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.buildreach.ReachHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public abstract class TwoClickBuildable extends MultipleClickBuildable {

    @Override
    public List<BlockPos> onRightClick(Player player, BlockPos blockPos, Direction sideHit, Vec3 hitVec, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();

        var rightClickTable = player.level.isClientSide ? rightClickClientTable : rightClickServerTable;
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
            sideHitTable.put(player.getUUID(), sideHit);
            hitVecTable.put(player.getUUID(), hitVec);
            //Keep list empty, dont place any blocks yet
        } else {
            //Second click, place blocks
            list = findCoordinates(player, blockPos, skipRaytrace);
            rightClickTable.put(player.getUUID(), 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(Player player, BlockPos blockPos, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        var rightClickTable = player.level.isClientSide ? rightClickClientTable : rightClickServerTable;
        int rightClickNr = rightClickTable.get(player.getUUID());
        var firstPos = firstPosTable.get(player.getUUID());

        if (rightClickNr == 0) {
            if (blockPos != null)
                list.add(blockPos);
        } else {
            var secondPos = findSecondPos(player, firstPos, skipRaytrace);
            if (secondPos == null) return list;

            //Limit amount of blocks we can place per row
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

            list.addAll(getAllBlocks(player, x1, y1, z1, x2, y2, z2));
        }

        return list;
    }

    //Finds the place of the second block pos based on criteria (floor must be on same height as first click, wall on same plane etc)
    protected abstract BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace);

    //After first and second pos are known, we want all the blocks
    protected abstract List<BlockPos> getAllBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2);
}
