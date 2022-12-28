package dev.huskcasaca.effortless.buildmode.threeclick;

import dev.huskcasaca.effortless.buildmode.ThreeClickBuildable;
import dev.huskcasaca.effortless.buildmode.twoclick.Floor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DiagonalLine extends ThreeClickBuildable {

    //Add diagonal line from first to second
    public static List<BlockPos> getDiagonalLineBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, float sampleMultiplier) {
        List<BlockPos> list = new ArrayList<>();

        var first = new Vec3(x1, y1, z1).add(0.5, 0.5, 0.5);
        var second = new Vec3(x2, y2, z2).add(0.5, 0.5, 0.5);

        int iterations = (int) Math.ceil(first.distanceTo(second) * sampleMultiplier);
        for (double t = 0; t <= 1.0; t += 1.0 / iterations) {
            Vec3 lerp = first.add(second.subtract(first).scale(t));
            BlockPos candidate = new BlockPos(lerp);
            //Only add if not equal to the last in the list
            if (list.isEmpty() || !list.get(list.size() - 1).equals(candidate))
                list.add(candidate);
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
        //Add diagonal line from first to second
        return getDiagonalLineBlocks(player, x1, y1, z1, x2, y2, z2, 10);
    }

    @Override
    public List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        //Add diagonal line from first to third
        return getDiagonalLineBlocks(player, x1, y1, z1, x3, y3, z3, 10);
    }
}
