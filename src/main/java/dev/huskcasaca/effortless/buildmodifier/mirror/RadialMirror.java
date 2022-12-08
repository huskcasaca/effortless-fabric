package dev.huskcasaca.effortless.buildmodifier.mirror;

import dev.huskcasaca.effortless.buildmodifier.Modifier;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RadialMirror implements Modifier {

    public static List<BlockPos> findCoordinates(Player player, BlockPos startPos) {
        List<BlockPos> coordinates = new ArrayList<>();

        //find radial mirror settings for the player
        var radialMirrorSettings = BuildModifierHelper.getModifierSettings(player).radialMirrorSettings();
        if (!isEnabled(radialMirrorSettings, startPos)) return coordinates;

        //get angle between slices
        double sliceAngle = 2 * Math.PI / radialMirrorSettings.slices;

        var startVec = new Vec3(startPos.getX() + 0.5f, startPos.getY() + 0.5f, startPos.getZ() + 0.5f);
        var relStartVec = startVec.subtract(radialMirrorSettings.position);

        double startAngleToCenter = Mth.atan2(relStartVec.x, relStartVec.z);
        if (startAngleToCenter < 0) startAngleToCenter += Math.PI;
        double startAngleInSlice = startAngleToCenter % sliceAngle;

        for (int i = 1; i < radialMirrorSettings.slices; i++) {
            double curAngle = sliceAngle * i;

            //alternate mirroring of slices
            if (radialMirrorSettings.alternate && i % 2 == 1) {
                curAngle = curAngle - startAngleInSlice + (sliceAngle - startAngleInSlice);
            }

            Vec3 relNewVec = relStartVec.yRot((float) curAngle);
            BlockPos newBlockPos = new BlockPos(radialMirrorSettings.position.add(relNewVec));
            if (!coordinates.contains(newBlockPos) && !newBlockPos.equals(startPos)) coordinates.add(newBlockPos);
        }

        return coordinates;
    }

    public static List<BlockState> findBlockStates(Player player, BlockPos startPos, BlockState blockState, ItemStack itemStack, List<ItemStack> itemStacks) {
        List<BlockState> blockStates = new ArrayList<>();
        List<BlockPos> coordinates = new ArrayList<>(); //to keep track of duplicates

        //find radial mirror settings for the player that placed the block
        var radialMirrorSettings = BuildModifierHelper.getModifierSettings(player).radialMirrorSettings();
        if (!isEnabled(radialMirrorSettings, startPos)) return blockStates;


        //get angle between slices
        double sliceAngle = 2 * Math.PI / radialMirrorSettings.slices;

        var startVec = new Vec3(startPos.getX() + 0.5f, startPos.getY() + 0.5f, startPos.getZ() + 0.5f);
        var relStartVec = startVec.subtract(radialMirrorSettings.position);

        double startAngleToCenter = Mth.atan2(relStartVec.x, relStartVec.z);
        double startAngleToCenterMod = startAngleToCenter < 0 ? startAngleToCenter + Math.PI : startAngleToCenter;
        double startAngleInSlice = startAngleToCenterMod % sliceAngle;

        //Rotate the original blockstate
        blockState = rotateOriginalBlockState(player, startPos, startAngleToCenter, blockState);

        //Randomizer bag synergy
//		AbstractRandomizerBagItem randomizerBagItem = null;
//		Container bagInventory = null;
//		if (!itemStack.isEmpty() && itemStack.getItem() instanceof AbstractRandomizerBagItem) {
//			randomizerBagItem = (AbstractRandomizerBagItem) itemStack.getItem() ;
//			bagInventory = randomizerBagItem.getBagInventory(itemStack);
//		}

        BlockState newBlockState;
        for (int i = 1; i < radialMirrorSettings.slices; i++) {
            newBlockState = blockState;
            double curAngle = sliceAngle * i;

            //alternate mirroring of slices
            if (radialMirrorSettings.alternate && i % 2 == 1) {
                curAngle = curAngle - startAngleInSlice + (sliceAngle - startAngleInSlice);
            }

            Vec3 relNewVec = relStartVec.yRot((float) curAngle);
            BlockPos newBlockPos = new BlockPos(radialMirrorSettings.position.add(relNewVec));
            if (coordinates.contains(newBlockPos) || newBlockPos.equals(startPos)) continue; //filter out duplicates
            coordinates.add(newBlockPos);

            //Randomizer bag synergy
//			if (randomizerBagItem != null) {
//				itemStack = randomizerBagItem.pickRandomStack(bagInventory);
//				newBlockState = BuildModifiers
//					.getBlockStateFromItem(itemStack, player, startPos, Direction.UP, new Vec3(0, 0, 0), InteractionHand.MAIN_HAND);
//
//				newBlockState = rotateOriginalBlockState(player, startPos, startAngleToCenter, newBlockState);
//			}

            //rotate
            newBlockState = rotateBlockState(player, startPos, relNewVec, newBlockState, radialMirrorSettings.alternate && i % 2 == 1);

            blockStates.add(newBlockState);
            itemStacks.add(itemStack);
        }

        return blockStates;
    }

    private static BlockState rotateOriginalBlockState(Player player, BlockPos startPos, double startAngleToCenter, BlockState blockState) {
        BlockState newBlockState = blockState;

        if (startAngleToCenter < -0.751 * Math.PI || startAngleToCenter > 0.749 * Math.PI) {
            newBlockState = blockState.rotate(Rotation.CLOCKWISE_180);
        } else if (startAngleToCenter < -0.251 * Math.PI) {
            newBlockState = blockState.rotate(Rotation.COUNTERCLOCKWISE_90);
        } else if (startAngleToCenter > 0.249 * Math.PI) {
            newBlockState = blockState.rotate(Rotation.CLOCKWISE_90);
        }

        return newBlockState;
    }

    private static BlockState rotateBlockState(Player player, BlockPos startPos, Vec3 relVec, BlockState blockState, boolean alternate) {
        BlockState newBlockState;
        double angleToCenter = Mth.atan2(relVec.x, relVec.z); //between -PI and PI

        if (angleToCenter < -0.751 * Math.PI || angleToCenter > 0.749 * Math.PI) {
            newBlockState = blockState.rotate(Rotation.CLOCKWISE_180);
            if (alternate) {
                newBlockState = newBlockState.mirror(Mirror.FRONT_BACK);
            }
        } else if (angleToCenter < -0.251 * Math.PI) {
            newBlockState = blockState.rotate(Rotation.CLOCKWISE_90);
            if (alternate) {
                newBlockState = newBlockState.mirror(Mirror.LEFT_RIGHT);
            }
        } else if (angleToCenter > 0.249 * Math.PI) {
            newBlockState = blockState.rotate(Rotation.COUNTERCLOCKWISE_90);
            if (alternate) {
                newBlockState = newBlockState.mirror(Mirror.LEFT_RIGHT);
            }
        } else {
            newBlockState = blockState;
            if (alternate) {
                newBlockState = newBlockState.mirror(Mirror.FRONT_BACK);
            }
        }

        return newBlockState;
    }

    public static boolean isEnabled(RadialMirrorSettings radialMirrorSettings, BlockPos startPos) {
        if (radialMirrorSettings == null || !radialMirrorSettings.enabled()) return false;

        return !(radialMirrorSettings.radius * radialMirrorSettings.radius < new Vec3(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5).subtract(radialMirrorSettings.position).lengthSqr());
    }

    public record RadialMirrorSettings(
            boolean enabled,
            Vec3 position,
            int slices,
            boolean alternate,
            int radius,
            boolean drawLines,
            boolean drawPlanes
    ) {

        public RadialMirrorSettings() {
            this(false, new Vec3(0.5, 64.5, 0.5), 4, false, 20, true, false);
        }

        public int reach() {
            return radius * 2;
        }

        public RadialMirrorSettings clone(boolean enabled) {
            return new RadialMirrorSettings(enabled, position, slices, alternate, radius, drawLines, drawPlanes);
        }
    }

}
