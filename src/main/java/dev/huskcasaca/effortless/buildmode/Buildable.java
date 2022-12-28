package dev.huskcasaca.effortless.buildmode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface Buildable {

    //Fired when a player selects a buildmode and when it needs to initializeMode
    void initialize(Player player);

    //Fired when a block would be placed
    //Return a list of coordinates where you want to place blocks
    List<BlockPos> onUse(Player player, BlockPos blockPos, Direction hitSide, Vec3 hitVec, boolean skipRaytrace);

    //Fired continuously for visualization purposes
    List<BlockPos> findCoordinates(Player player, BlockPos blockPos, boolean skipRaytrace);

    Direction getHitSide(Player player);

    Vec3 getHitVec(Player player);
}
