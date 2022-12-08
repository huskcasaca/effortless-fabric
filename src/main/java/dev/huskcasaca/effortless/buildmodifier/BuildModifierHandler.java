package dev.huskcasaca.effortless.buildmodifier;

import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.buildmodifier.mirror.RadialMirror;
import dev.huskcasaca.effortless.helper.CompatHelper;
import dev.huskcasaca.effortless.helper.InventoryHelper;
import dev.huskcasaca.effortless.helper.SurvivalHelper;
import dev.huskcasaca.effortless.mixin.BlockItemAccessor;
import dev.huskcasaca.effortless.render.BlockPreviewRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildModifierHandler {

    //Called from BuildModes
    public static void onBlockPlaced(Player player, List<BlockPos> startCoordinates, Direction sideHit, Vec3 hitVec, boolean placeStartPos) {
        var world = player.level;
//		AbstractRandomizerBagItem.renewRandomness();

        //Format hitvec to 0.x
        hitVec = new Vec3(Math.abs(hitVec.x - ((int) hitVec.x)), Math.abs(hitVec.y - ((int) hitVec.y)), Math.abs(hitVec.z - ((int) hitVec.z)));

        //find coordinates and blockstates
        List<BlockPos> coordinates = findCoordinates(player, startCoordinates);
        List<ItemStack> itemStacks = new ArrayList<>();
        List<BlockState> blockStates = findBlockStates(player, startCoordinates, hitVec, sideHit, itemStacks);

        //check if valid blockstates
        if (blockStates.size() == 0 || coordinates.size() != blockStates.size()) return;

        //remember previous blockstates for undo
        List<BlockState> previousBlockStates = new ArrayList<>(coordinates.size());
        List<BlockState> newBlockStates = new ArrayList<>(coordinates.size());
        for (var coordinate : coordinates) {
            previousBlockStates.add(world.getBlockState(coordinate));
        }

        if (world.isClientSide) {

            BlockPreviewRenderer.onBlocksPlaced();

            newBlockStates = blockStates;

        } else {

            //place blocks
            for (int i = placeStartPos ? 0 : 1; i < coordinates.size(); i++) {
                var blockPos = coordinates.get(i);
                var blockState = blockStates.get(i);
                var itemStack = itemStacks.get(i);

                if (world.isLoaded(blockPos)) {
                    //check itemstack empty
                    if (itemStack.isEmpty()) {
                        //try to find new stack, otherwise continue
                        itemStack = InventoryHelper.findItemStackInInventory(player, blockState.getBlock());
                        if (itemStack.isEmpty()) continue;
                    }
                    SurvivalHelper.placeBlock(world, player, blockPos, blockState, itemStack, sideHit, hitVec, false, false, false);
                }
            }

            //find actual new blockstates for undo
            for (var coordinate : coordinates) {
                newBlockStates.add(world.getBlockState(coordinate));
            }
        }

        //Set first previousBlockState to empty if in NORMAL mode, to make undo/redo work
        //(Block is placed by the time it gets here, and unplaced after this)
        if (!placeStartPos) previousBlockStates.set(0, Blocks.AIR.defaultBlockState());

        //If all new blockstates are air then no use in adding it, no block was actually placed
        //Can happen when e.g. placing one block in yourself
        if (Collections.frequency(newBlockStates, Blocks.AIR.defaultBlockState()) != newBlockStates.size()) {
            //add to undo stack
            var firstPos = startCoordinates.get(0);
            var secondPos = startCoordinates.get(startCoordinates.size() - 1);
            UndoRedo.addUndo(player, new BlockSet(coordinates, previousBlockStates, newBlockStates, hitVec, firstPos, secondPos));
        }
    }

    public static void onBlockBroken(Player player, List<BlockPos> startCoordinates, boolean breakStartPos) {
        var world = player.level;

        var coordinates = findCoordinates(player, startCoordinates);

        if (coordinates.isEmpty()) return;

        //remember previous blockstates for undo
        List<BlockState> previousBlockStates = new ArrayList<>(coordinates.size());
        List<BlockState> newBlockStates = new ArrayList<>(coordinates.size());
        for (var coordinate : coordinates) {
            previousBlockStates.add(world.getBlockState(coordinate));
        }

        if (world.isClientSide) {
            BlockPreviewRenderer.onBlocksBroken();

            //list of air blockstates
            for (int i = 0; i < coordinates.size(); i++) {
                newBlockStates.add(Blocks.AIR.defaultBlockState());
            }

        } else {

            //If the player is going to inst-break grass or a plant, make sure to only break other inst-breakable things
            boolean onlyInstaBreaking = !player.isCreative() &&
                    world.getBlockState(startCoordinates.get(0)).getDestroySpeed(world, startCoordinates.get(0)) == 0f;

            //break all those blocks
            for (int i = breakStartPos ? 0 : 1; i < coordinates.size(); i++) {
                var coordinate = coordinates.get(i);
                if (world.isLoaded(coordinate) && !world.isEmptyBlock(coordinate)) {
                    if (!onlyInstaBreaking || world.getBlockState(coordinate).getDestroySpeed(world, coordinate) == 0f) {
                        SurvivalHelper.breakBlock(world, player, coordinate, false);
                    }
                }
            }

            //find actual new blockstates for undo
            for (var coordinate : coordinates) {
                newBlockStates.add(world.getBlockState(coordinate));
            }
        }

        //Set first newBlockState to empty if in NORMAL mode, to make undo/redo work
        //(Block isn't broken yet by the time it gets here, and broken after this)
        if (!breakStartPos) newBlockStates.set(0, Blocks.AIR.defaultBlockState());

        //add to undo stack
        var firstPos = startCoordinates.get(0);
        var secondPos = startCoordinates.get(startCoordinates.size() - 1);
        var hitVec = new Vec3(0.5, 0.5, 0.5);
        UndoRedo.addUndo(player, new BlockSet(coordinates, previousBlockStates, newBlockStates, hitVec, firstPos, secondPos));

    }

    public static List<BlockPos> findCoordinates(Player player, List<BlockPos> posList) {
        List<BlockPos> coordinates = new ArrayList<>();
        //Add current blocks being placed too
        coordinates.addAll(posList);

        //Find mirror/array/radial mirror coordinates for each blockpos
        for (var blockPos : posList) {
            List<BlockPos> arrayCoordinates = Array.findCoordinates(player, blockPos);
            coordinates.addAll(arrayCoordinates);
            coordinates.addAll(Mirror.findCoordinates(player, blockPos));
            coordinates.addAll(RadialMirror.findCoordinates(player, blockPos));
            //get mirror for each array coordinate
            for (var coordinate : arrayCoordinates) {
                coordinates.addAll(Mirror.findCoordinates(player, coordinate));
                coordinates.addAll(RadialMirror.findCoordinates(player, coordinate));
            }
        }

        return coordinates;
    }

    public static List<BlockPos> findCoordinates(Player player, BlockPos blockPos) {
        return findCoordinates(player, new ArrayList<>(Collections.singletonList(blockPos)));
    }

    public static List<BlockState> findBlockStates(Player player, List<BlockPos> posList, Vec3 hitVec, Direction facing, List<ItemStack> itemStacks) {
        List<BlockState> blockStates = new ArrayList<>();
        itemStacks.clear();

        //Get itemstack
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty() || !CompatHelper.isItemBlockProxy(itemStack)) {
            itemStack = player.getItemInHand(InteractionHand.OFF_HAND);
        }
        if (itemStack.isEmpty() || !CompatHelper.isItemBlockProxy(itemStack)) {
            return blockStates;
        }

        //Get ItemBlock stack
        ItemStack itemBlock = ItemStack.EMPTY;
        if (itemStack.getItem() instanceof BlockItem) itemBlock = itemStack;
        else itemBlock = CompatHelper.getItemBlockFromStack(itemStack);
//		AbstractRandomizerBagItem.resetRandomness();

        //Add blocks in posList first
        for (var blockPos : posList) {
            if (!(itemStack.getItem() instanceof BlockItem)) itemBlock = CompatHelper.getItemBlockFromStack(itemStack);
            BlockState blockState = getBlockStateFromItem(itemBlock, player, blockPos, facing, hitVec, InteractionHand.MAIN_HAND);
//            Effortless.log(player, "getBlockStateFromItem " + blockState);
            if (blockState == null) continue;

            blockStates.add(blockState);
            itemStacks.add(itemBlock);
        }

        for (var blockPos : posList) {
            BlockState blockState = getBlockStateFromItem(itemBlock, player, blockPos, facing, hitVec, InteractionHand.MAIN_HAND);
            if (blockState == null) continue;

            List<BlockState> arrayBlockStates = Array.findBlockStates(player, blockPos, blockState, itemStack, itemStacks);
            blockStates.addAll(arrayBlockStates);

            blockStates.addAll(Mirror.findBlockStates(player, blockPos, blockState, itemStack, itemStacks));
            blockStates.addAll(RadialMirror.findBlockStates(player, blockPos, blockState, itemStack, itemStacks));
            //add mirror for each array coordinate
            List<BlockPos> arrayCoordinates = Array.findCoordinates(player, blockPos);
            for (int i = 0; i < arrayCoordinates.size(); i++) {
                var coordinate = arrayCoordinates.get(i);
                var blockState1 = arrayBlockStates.get(i);
                if (blockState1 == null) continue;

                blockStates.addAll(Mirror.findBlockStates(player, coordinate, blockState1, itemStack, itemStacks));
                blockStates.addAll(RadialMirror.findBlockStates(player, coordinate, blockState1, itemStack, itemStacks));
            }

            //Adjust blockstates for torches and ladders etc to place on a valid side
            //TODO optimize findCoordinates (done twice now)
            //TODO fix mirror
//            List<BlockPos> coordinates = findCoordinates(player, startPos);
//            for (int i = 0; i < blockStates.size(); i++) {
//                blockStates.set(i, blockStates.get(i).getBlock().getStateForPlacement(player.world, coordinates.get(i), facing,
//                        (float) hitVec.x, (float) hitVec.y, (float) hitVec.z, itemStacks.get(i).getMetadata(), player, EnumHand.MAIN_HAND));
//            }
        }

        return blockStates;
    }

    public static boolean isEnabled(ModifierSettingsManager.ModifierSettings modifierSettings, BlockPos startPos) {
        return Array.isEnabled(modifierSettings.arraySettings()) ||
                Mirror.isEnabled(modifierSettings.mirrorSettings(), startPos) ||
                RadialMirror.isEnabled(modifierSettings.radialMirrorSettings(), startPos) ||
                modifierSettings.quickReplace();
    }


    public static BlockState getBlockStateFromItem(ItemStack itemStack, Player player, BlockPos blockPos, Direction facing, Vec3 hitVec, InteractionHand hand) {
        var hitresult = new BlockHitResult(hitVec, facing, blockPos, false);

        var item = itemStack.getItem();

        if (item instanceof BlockItem) {
            return ((BlockItemAccessor) Item.byBlock(((BlockItem) item).getBlock())).callGetPlacementState(new BlockPlaceContext(player, hand, itemStack, hitresult));
        } else {
            return Block.byItem(item).getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, hand, new BlockHitResult(hitVec, facing, blockPos, false))));
        }
    }


    //Returns true if equal (or both null)
    public static boolean compareCoordinates(List<BlockPos> coordinates1, List<BlockPos> coordinates2) {
        if (coordinates1 == null && coordinates2 == null) return true;
        if (coordinates1 == null || coordinates2 == null) return false;

        //Check count, not actual values
        if (coordinates1.size() == coordinates2.size()) {
            if (coordinates1.size() == 1) {
                return coordinates1.get(0).equals(coordinates2.get(0));
            }
            return true;
        } else {
            return false;
        }

//        return coordinates1.equals(coordinates2);
    }
}
