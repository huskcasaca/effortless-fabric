package dev.huskcasaca.effortless.buildmode.oneclick;

import dev.huskcasaca.effortless.buildmode.OneClickBuildable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Disable extends OneClickBuildable {
    @Override
    public void initialize(Player player) {

    }

    @Override
    public List<BlockPos> onUse(Player player, BlockPos blockPos, Direction hitSide, Vec3 hitVec, boolean skipRaytrace) {
        if (blockPos == null) return Collections.emptyList();
        return getFinalBlocks(player, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public List<BlockPos> findCoordinates(Player player, BlockPos blockPos, boolean skipRaytrace) {
        if (blockPos == null) return Collections.emptyList();
        return getFinalBlocks(player, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public Direction getHitSide(Player player) {
        return null;
    }

    @Override
    public Vec3 getHitVec(Player player) {
        return null;
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1) {
        List<BlockPos> list = new ArrayList<>();
        list.add(new BlockPos(x1, y1, z1));
        return list;
    }
}
