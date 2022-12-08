package dev.huskcasaca.effortless.buildmodifier.mirror;

import dev.huskcasaca.effortless.buildmodifier.Modifier;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Mirror implements Modifier {

    public static List<BlockPos> findCoordinates(Player player, BlockPos startPos) {
        List<BlockPos> coordinates = new ArrayList<>();

        //find mirrorsettings for the player
        var mirrorSettings = ModifierSettingsManager.getModifierSettings(player).mirrorSettings();
        if (!isEnabled(mirrorSettings, startPos)) return coordinates;

        if (mirrorSettings.mirrorX) coordinateMirrorX(mirrorSettings, startPos, coordinates);
        if (mirrorSettings.mirrorY) coordinateMirrorY(mirrorSettings, startPos, coordinates);
        if (mirrorSettings.mirrorZ) coordinateMirrorZ(mirrorSettings, startPos, coordinates);

        return coordinates;
    }

    private static void coordinateMirrorX(MirrorSettings mirrorSettings, BlockPos oldBlockPos, List<BlockPos> coordinates) {
        //find mirror position
        double x = mirrorSettings.position.x + (mirrorSettings.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());
        coordinates.add(newBlockPos);

        if (mirrorSettings.mirrorY) coordinateMirrorY(mirrorSettings, newBlockPos, coordinates);
        if (mirrorSettings.mirrorZ) coordinateMirrorZ(mirrorSettings, newBlockPos, coordinates);
    }

    private static void coordinateMirrorY(MirrorSettings mirrorSettings, BlockPos oldBlockPos, List<BlockPos> coordinates) {
        //find mirror position
        double y = mirrorSettings.position.y + (mirrorSettings.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());
        coordinates.add(newBlockPos);

        if (mirrorSettings.mirrorZ) coordinateMirrorZ(mirrorSettings, newBlockPos, coordinates);
    }

    private static void coordinateMirrorZ(MirrorSettings mirrorSettings, BlockPos oldBlockPos, List<BlockPos> coordinates) {
        //find mirror position
        double z = mirrorSettings.position.z + (mirrorSettings.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);
        coordinates.add(newBlockPos);
    }

    public static List<BlockState> findBlockStates(Player player, BlockPos startPos, BlockState blockState, ItemStack itemStack, List<ItemStack> itemStacks) {
        List<BlockState> blockStates = new ArrayList<>();

        //find mirrorsettings for the player
        MirrorSettings mirrorSettings = ModifierSettingsManager.getModifierSettings(player).mirrorSettings();
        if (!isEnabled(mirrorSettings, startPos)) return blockStates;

        //Randomizer bag synergy
//		AbstractRandomizerBagItem randomizerBagItem = null;
        Container bagInventory = null;
//		if (!itemStack.isEmpty() && itemStack.getItem() instanceof AbstractRandomizerBagItem) {
//			randomizerBagItem = (AbstractRandomizerBagItem) itemStack.getItem() ;
//			bagInventory = randomizerBagItem.getBagInventory(itemStack);
//		}

        if (mirrorSettings.mirrorX)
            blockStateMirrorX(player, mirrorSettings, startPos, blockState, bagInventory, itemStack, InteractionHand.MAIN_HAND, blockStates, itemStacks);
        if (mirrorSettings.mirrorY)
            blockStateMirrorY(player, mirrorSettings, startPos, blockState, bagInventory, itemStack, InteractionHand.MAIN_HAND, blockStates, itemStacks);
        if (mirrorSettings.mirrorZ)
            blockStateMirrorZ(player, mirrorSettings, startPos, blockState, bagInventory, itemStack, InteractionHand.MAIN_HAND, blockStates, itemStacks);

        return blockStates;
    }

    private static void blockStateMirrorX(Player player, MirrorSettings mirrorSettings, BlockPos oldBlockPos, BlockState oldBlockState,
                                          Container bagInventory, ItemStack itemStack, InteractionHand hand, List<BlockState> blockStates, List<ItemStack> itemStacks) {
        //find mirror position
        double x = mirrorSettings.position.x + (mirrorSettings.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());

        //Randomizer bag synergy
//		if (bagInventory != null) {
//			itemStack = ((AbstractRandomizerBagItem)itemStack.getItem()).pickRandomStack(bagInventory);
//			oldBlockState = BuildModifiers.getBlockStateFromItem(itemStack, player, oldBlockPos, Direction.UP, new Vec3(0, 0, 0), hand);
//		}

        //Find blockstate
        BlockState newBlockState = oldBlockState == null ? null : oldBlockState.mirror(net.minecraft.world.level.block.Mirror.FRONT_BACK);

        //Store blockstate and itemstack
        blockStates.add(newBlockState);
        itemStacks.add(itemStack);

        if (mirrorSettings.mirrorY)
            blockStateMirrorY(player, mirrorSettings, newBlockPos, newBlockState, bagInventory, itemStack, hand, blockStates, itemStacks);
        if (mirrorSettings.mirrorZ)
            blockStateMirrorZ(player, mirrorSettings, newBlockPos, newBlockState, bagInventory, itemStack, hand, blockStates, itemStacks);
    }

    private static void blockStateMirrorY(Player player, MirrorSettings mirrorSettings, BlockPos oldBlockPos, BlockState oldBlockState,
                                          Container bagInventory, ItemStack itemStack, InteractionHand hand, List<BlockState> blockStates, List<ItemStack> itemStacks) {
        //find mirror position
        double y = mirrorSettings.position.y + (mirrorSettings.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());

        //Randomizer bag synergy
//		if (bagInventory != null) {
//			itemStack = ((AbstractRandomizerBagItem)itemStack.getItem()).pickRandomStack(bagInventory);
//			oldBlockState = BuildModifiers.getBlockStateFromItem(itemStack, player, oldBlockPos, Direction.UP, new Vec3(0, 0, 0), hand);
//		}

        //Find blockstate
        BlockState newBlockState = oldBlockState == null ? null : getVerticalMirror(oldBlockState);

        //Store blockstate and itemstack
        blockStates.add(newBlockState);
        itemStacks.add(itemStack);

        if (mirrorSettings.mirrorZ)
            blockStateMirrorZ(player, mirrorSettings, newBlockPos, newBlockState, bagInventory, itemStack, hand, blockStates, itemStacks);
    }

    private static void blockStateMirrorZ(Player player, MirrorSettings mirrorSettings, BlockPos oldBlockPos, BlockState oldBlockState,
                                          Container bagInventory, ItemStack itemStack, InteractionHand hand, List<BlockState> blockStates, List<ItemStack> itemStacks) {
        //find mirror position
        double z = mirrorSettings.position.z + (mirrorSettings.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);

        //Randomizer bag synergy
//		if (bagInventory != null) {
//			itemStack = ((AbstractRandomizerBagItem)itemStack.getItem()).pickRandomStack(bagInventory);
//			oldBlockState = BuildModifiers.getBlockStateFromItem(itemStack, player, oldBlockPos, Direction.UP, new Vec3(0, 0, 0), hand);
//		}

        //Find blockstate
        BlockState newBlockState = oldBlockState == null ? null : oldBlockState.mirror(net.minecraft.world.level.block.Mirror.LEFT_RIGHT);

        //Store blockstate and itemstack
        blockStates.add(newBlockState);
        itemStacks.add(itemStack);
    }

    public static boolean isEnabled(MirrorSettings mirrorSettings, BlockPos startPos) {
        if (mirrorSettings == null || !mirrorSettings.enabled || (!mirrorSettings.mirrorX && !mirrorSettings.mirrorY && !mirrorSettings.mirrorZ))
            return false;

        //within mirror distance
        return !(startPos.getX() + 0.5 < mirrorSettings.position.x - mirrorSettings.radius) && !(startPos.getX() + 0.5 > mirrorSettings.position.x + mirrorSettings.radius) &&
                !(startPos.getY() + 0.5 < mirrorSettings.position.y - mirrorSettings.radius) && !(startPos.getY() + 0.5 > mirrorSettings.position.y + mirrorSettings.radius) &&
                !(startPos.getZ() + 0.5 < mirrorSettings.position.z - mirrorSettings.radius) && !(startPos.getZ() + 0.5 > mirrorSettings.position.z + mirrorSettings.radius);
    }

    private static BlockState getVerticalMirror(BlockState blockState) {
        //Stairs
        if (blockState.getBlock() instanceof StairBlock) {
            if (blockState.getValue(StairBlock.HALF) == Half.BOTTOM) {
                return blockState.setValue(StairBlock.HALF, Half.TOP);
            } else {
                return blockState.setValue(StairBlock.HALF, Half.BOTTOM);
            }
        }

        //Slabs
        if (blockState.getBlock() instanceof SlabBlock) {
            if (blockState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
                return blockState;
            } else if (blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
                return blockState.setValue(SlabBlock.TYPE, SlabType.TOP);
            } else {
                return blockState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            }
        }

        //Buttons, endrod, observer, piston
        if (blockState.getBlock() instanceof DirectionalBlock) {
            if (blockState.getValue(DirectionalBlock.FACING) == Direction.DOWN) {
                return blockState.setValue(DirectionalBlock.FACING, Direction.UP);
            } else if (blockState.getValue(DirectionalBlock.FACING) == Direction.UP) {
                return blockState.setValue(DirectionalBlock.FACING, Direction.DOWN);
            }
        }

        //Dispenser, dropper
        if (blockState.getBlock() instanceof DispenserBlock) {
            if (blockState.getValue(DispenserBlock.FACING) == Direction.DOWN) {
                return blockState.setValue(DispenserBlock.FACING, Direction.UP);
            } else if (blockState.getValue(DispenserBlock.FACING) == Direction.UP) {
                return blockState.setValue(DispenserBlock.FACING, Direction.DOWN);
            }
        }

        return blockState;
    }

    public record MirrorSettings(
            boolean enabled,
            Vec3 position,
            boolean mirrorX,
            boolean mirrorY,
            boolean mirrorZ,
            int radius,
            boolean drawLines,
            boolean drawPlanes
    ) {
        public MirrorSettings() {
            this(
                    false,
                    new Vec3(0.5, 64.5, 0.5),
                    true,
                    false,
                    false,
                    10,
                    true,
                    true
            );
        }

        public int reach() {
            return radius * 2; //Change ModifierSettings#setReachUpgrade too
        }
    }
}
