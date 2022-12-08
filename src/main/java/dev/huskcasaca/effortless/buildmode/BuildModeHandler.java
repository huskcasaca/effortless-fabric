package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHandler;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.helper.ReachHelper;
import dev.huskcasaca.effortless.helper.SurvivalHelper;
import dev.huskcasaca.effortless.network.BlockBrokenMessage;
import dev.huskcasaca.effortless.network.BlockPlacedMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class BuildModeHandler {

    //Static variables are shared between client and server in singleplayer
    //We need them separate
    public static Dictionary<Player, Boolean> currentlyBreakingClient = new Hashtable<>();
    public static Dictionary<Player, Boolean> currentlyBreakingServer = new Hashtable<>();

    //Uses a network message to get the previous raytraceresult from the player
    //The server could keep track of all raytraceresults but this might lag with many players
    //Raytraceresult is needed for sideHit and hitVec
    public static void onBlockPlacedMessage(Player player, BlockPlacedMessage message) {

        //Check if not in the middle of breaking
        var currentlyBreaking = player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
        if (currentlyBreaking.get(player) != null && currentlyBreaking.get(player)) {
            //Cancel breaking
            initializeMode(player);
            return;
        }

        var modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        var modeSettings = ModeSettingsManager.getModeSettings(player);
        var buildMode = modeSettings.buildMode();

        BlockPos startPos = null;

        if (message.blockHit() && message.blockPos() != null) {
            startPos = message.blockPos();

            //Offset in direction of sidehit if not quickreplace and not replaceable
            //TODO 1.13 replaceable
            boolean replaceable = player.level.getBlockState(startPos).getMaterial().isReplaceable();
            boolean becomesDoubleSlab = SurvivalHelper.doesBecomeDoubleSlab(player, startPos, message.sideHit());
            if (!modifierSettings.quickReplace() && !replaceable && !becomesDoubleSlab) {
                startPos = startPos.relative(message.sideHit());
            }

            //Get under tall grass and other replaceable blocks
            if (modifierSettings.quickReplace() && replaceable) {
                startPos = startPos.below();
            }

            //Check if player reach does not exceed startpos
            int maxReach = ReachHelper.getMaxReach(player);
            if (buildMode != BuildMode.DISABLE && player.blockPosition().distSqr(startPos) > maxReach * maxReach) {
                Effortless.log(player, "Placement exceeds your reach.");
                return;
            }
        }

        //Even when no starting block is found, call buildmode instance
        //We might want to place things in the air
        List<BlockPos> coordinates = buildMode.instance.onRightClick(player, startPos, message.sideHit(), message.hitVec(), modifierSettings.quickReplace());

        if (coordinates.isEmpty()) {
            currentlyBreaking.put(player, false);
            return;
        }

        //Limit number of blocks you can place
        int limit = ReachHelper.getMaxBlocksPlacedAtOnce(player);
        if (coordinates.size() > limit) {
            coordinates = coordinates.subList(0, limit);
        }

        var sideHit = buildMode.instance.getSideHit(player);
        if (sideHit == null) sideHit = message.sideHit();

        var hitVec = buildMode.instance.getHitVec(player);
        if (hitVec == null) hitVec = message.hitVec();

        BuildModifierHandler.onBlockPlaced(player, coordinates, sideHit, hitVec, message.placeStartPos());

        //Only works when finishing a buildmode is equal to placing some blocks
        //No intermediate blocks allowed
        currentlyBreaking.remove(player);

    }

    //Use a network message to break blocks in the distance using clientside mouse input
    public static void onBlockBrokenMessage(Player player, BlockBrokenMessage message) {
        var startPos = message.blockHit() ? message.blockPos() : null;
        onBlockBroken(player, startPos, true);
    }

    public static void onBlockBroken(Player player, BlockPos startPos, boolean breakStartPos) {

        //Check if not in the middle of placing
        var currentlyBreaking = player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
        if (currentlyBreaking.get(player) != null && !currentlyBreaking.get(player)) {
            //Cancel placing
            initializeMode(player);
            return;
        }

        if (!ReachHelper.canBreakFar(player)) return;

        //If first click
        if (currentlyBreaking.get(player) == null) {
            //If startpos is null, dont do anything
            if (startPos == null) return;
        }

        var modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        var modeSettings = ModeSettingsManager.getModeSettings(player);

        //Get coordinates
        var buildMode = modeSettings.buildMode();
        var coordinates = buildMode.instance.onRightClick(player, startPos, Direction.UP, Vec3.ZERO, true);

        if (coordinates.isEmpty()) {
            currentlyBreaking.put(player, true);
            return;
        }

        //Let buildmodifiers break blocks
        BuildModifierHandler.onBlockBroken(player, coordinates, breakStartPos);

        //Only works when finishing a buildmode is equal to breaking some blocks
        //No intermediate blocks allowed
        currentlyBreaking.remove(player);
    }

    public static List<BlockPos> findCoordinates(Player player, BlockPos startPos, boolean skipRaytrace) {
        List<BlockPos> coordinates = new ArrayList<>();

        var modeSettings = ModeSettingsManager.getModeSettings(player);
        coordinates.addAll(modeSettings.buildMode().instance.findCoordinates(player, startPos, skipRaytrace));

        return coordinates;
    }

    public static void initializeMode(Player player) {
        //Resetting mode, so not placing or breaking
        if (player == null) {
            return;
        }
        var currentlyBreaking = player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
        currentlyBreaking.remove(player);

        ModeSettingsManager.getModeSettings(player).buildMode().instance.initialize(player);
    }

    public static boolean isCurrentlyPlacing(Player player) {
        var currentlyBreaking = player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
        return currentlyBreaking.get(player) != null && !currentlyBreaking.get(player);
    }

    public static boolean isCurrentlyBreaking(Player player) {
        var currentlyBreaking = player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
        return currentlyBreaking.get(player) != null && currentlyBreaking.get(player);
    }

    //Either placing or breaking
    public static boolean isActive(Player player) {
        var currentlyBreaking = player.level.isClientSide ? currentlyBreakingClient : currentlyBreakingServer;
        return currentlyBreaking.get(player) != null;
    }

    //Find coordinates on a line bound by a plane
    public static Vec3 findXBound(double x, Vec3 start, Vec3 look) {
        //then y and z are
        double y = (x - start.x) / look.x * look.y + start.y;
        double z = (x - start.x) / look.x * look.z + start.z;

        return new Vec3(x, y, z);
    }


    //-- Common build mode functionality --//

    public static Vec3 findYBound(double y, Vec3 start, Vec3 look) {
        //then x and z are
        double x = (y - start.y) / look.y * look.x + start.x;
        double z = (y - start.y) / look.y * look.z + start.z;

        return new Vec3(x, y, z);
    }

    public static Vec3 findZBound(double z, Vec3 start, Vec3 look) {
        //then x and y are
        double x = (z - start.z) / look.z * look.x + start.x;
        double y = (z - start.z) / look.z * look.y + start.y;

        return new Vec3(x, y, z);
    }

    //Use this instead of player.getLookVec() in any buildmodes code
    public static Vec3 getPlayerLookVec(Player player) {
        Vec3 lookVec = player.getLookAngle();
        double x = lookVec.x;
        double y = lookVec.y;
        double z = lookVec.z;

        //Further calculations (findXBound etc) don't like any component being 0 or 1 (e.g. dividing by 0)
        //isCriteriaValid below will take up to 2 minutes to raytrace blocks towards infinity if that is the case
        //So make sure they are close to but never exactly 0 or 1
        if (Math.abs(x) < 0.0001) x = 0.0001;
        if (Math.abs(x - 1.0) < 0.0001) x = 0.9999;
        if (Math.abs(x + 1.0) < 0.0001) x = -0.9999;

        if (Math.abs(y) < 0.0001) y = 0.0001;
        if (Math.abs(y - 1.0) < 0.0001) y = 0.9999;
        if (Math.abs(y + 1.0) < 0.0001) y = -0.9999;

        if (Math.abs(z) < 0.0001) z = 0.0001;
        if (Math.abs(z - 1.0) < 0.0001) z = 0.9999;
        if (Math.abs(z + 1.0) < 0.0001) z = -0.9999;

        return new Vec3(x, y, z);
    }

    public static boolean isCriteriaValid(Vec3 start, Vec3 look, int reach, Player player, boolean skipRaytrace, Vec3 lineBound, Vec3 planeBound, double distToPlayerSq) {
        boolean intersects = false;
        if (!skipRaytrace) {
            //collision within a 1 block radius to selected is fine
            ClipContext rayTraceContext = new ClipContext(start, lineBound, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
            HitResult rayTraceResult = player.level.clip(rayTraceContext);
            intersects = rayTraceResult != null && rayTraceResult.getType() == HitResult.Type.BLOCK &&
                    planeBound.subtract(rayTraceResult.getLocation()).lengthSqr() > 4;
        }

        return planeBound.subtract(start).dot(look) > 0 &&
                distToPlayerSq > 2 && distToPlayerSq < reach * reach &&
                !intersects;
    }

}
