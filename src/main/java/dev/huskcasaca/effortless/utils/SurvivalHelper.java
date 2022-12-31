package dev.huskcasaca.effortless.utils;

import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SurvivalHelper {

    //Used for all placing of blocks in this mod.
    //Checks if area is loaded, if player has the right permissions, if existing block can be replaced (drops it if so) and consumes an item from the stack.
    //Based on ItemBlock#onItemUse
    public static boolean placeBlock(Level level, Player player, BlockPos pos, BlockState blockState,
                                     ItemStack origstack, Direction facing, Vec3 hitVec, boolean skipPlaceCheck,
                                     boolean skipCollisionCheck, boolean playSound) {
        if (!level.isLoaded(pos)) return false;
        ItemStack itemstack = origstack;

        if (blockState.isAir() || itemstack.isEmpty()) {
            dropBlock(level, player, pos);
            level.removeBlock(pos, false);
            return true;
        }

        //Randomizer bag, other proxy item synergy
        //Preliminary compatibility code for other items that hold blocks
        if (CompatHelper.isItemBlockProxy(itemstack))
            itemstack = CompatHelper.getItemBlockByState(itemstack, blockState);

        if (!(itemstack.getItem() instanceof BlockItem))
            return false;
        Block block = ((BlockItem) itemstack.getItem()).getBlock();


        //More manual with ItemBlock#placeBlockAt
        if (canPlace(level, player, pos, blockState /*, itemstack, skipCollisionCheck, facing.getOpposite()*/)) {
            //Drop existing block
            dropBlock(level, player, pos);

            //TryPlace sets block with offset and reduces itemstack count in creative, so we copy only parts of it
//            BlockItemUseContext blockItemUseContext = new BlockItemUseContext(level, player, itemstack, pos, facing, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z);
//            EnumActionResult result = ((ItemBlock) itemstack.getItem()).tryPlace(blockItemUseContext);
            if (!level.setBlock(pos, blockState, 3)) return false;
            BlockItem.updateCustomBlockEntityTag(level, player, pos, itemstack); //Actually BlockItem::onBlockPlaced but that is protected
            block.setPlacedBy(level, pos, blockState, player, itemstack);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, itemstack);
                ((ServerPlayer) player).getStats().increment(
                        player,
                        Stats.ITEM_USED.get(itemstack.getItem()),
                        1
                );
            }

            BlockState afterState = level.getBlockState(pos);

            if (playSound) {
                SoundType soundtype = afterState.getBlock().getSoundType(afterState);
                level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            }

            if (!player.isCreative() && Block.byItem(itemstack.getItem()) == block) {
                itemstack.shrink(1);
            }

            return true;
        }
        return false;

        //Using ItemBlock#onItemUse
//        EnumActionResult result;
//        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, EnumHand.MAIN_HAND, pos, facing, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(player, ReachHelper.getPlacementReach(player)));
//        if (true)
//        {
//            int i = itemstack.getMetadata();
//            int j = itemstack.getCount();
//            if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
//                EnumActionResult enumactionresult = itemstack.getItem().onItemUse(player, level, pos, EnumHand.MAIN_HAND, facing, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z);
//                itemstack.setItemDamage(i);
//                itemstack.setCount(j);
//                return enumactionresult == EnumActionResult.SUCCESS;
//            } else return false;
//        }
//        else
//        {
//            ItemStack copyForUse = itemstack.copy();
//            if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
//                result = itemstack.getItem().onItemUse(player, level, pos, EnumHand.MAIN_HAND, facing, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z);
//            if (itemstack.isEmpty()) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyForUse, EnumHand.MAIN_HAND);
//            return false;
//        }

    }

    public static boolean useBlock(Level level, Player player, BlockPos pos, BlockState blockState) {
        if (!level.isLoaded(pos)) return false;
        var itemStack = player.isCreative() ? new ItemStack(blockState.getBlock()) : InventoryHelper.findItemStackInInventory(player, blockState.getBlock());

        // FIXME: 27/12/22
        if (blockState.isAir()) {
            dropBlock(level, player, pos);
            level.removeBlock(pos, false);
            return true;
        }

        if (!(itemStack.getItem() instanceof BlockItem)) return false;
        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        if (!canPlace(level, player, pos, blockState /*, itemStack, skipCollisionCheck, facing.getOpposite()*/)) {
            return false;
        }
        dropBlock(level, player, pos);

        if (!level.setBlock(pos, blockState, 3)) return false;
        BlockItem.updateCustomBlockEntityTag(level, player, pos, itemStack); //Actually BlockItem::onBlockPlaced but that is protected
        block.setPlacedBy(level, pos, blockState, player, itemStack);
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, itemStack);
            ((ServerPlayer) player).getStats().increment(player, Stats.ITEM_USED.get(itemStack.getItem()), 1);
        }

        var afterState = level.getBlockState(pos);

        if (true) {
            var soundtype = afterState.getBlock().getSoundType(afterState);
            level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        }

        if (!player.isCreative() && Block.byItem(itemStack.getItem()) == block) {
            itemStack.shrink(1);
        }

        return true;

    }

    public static boolean breakBlock(Level level, Player player, BlockPos blockPos, boolean skipChecks) {
//        if (!level.isLoaded(blockPos) && !level.isEmptyBlock(blockPos)) return false;
        if (!skipChecks && !canBreak(level, player, blockPos)) return false;
        //Drop existing block
        if (level.isClientSide()) {
            Minecraft.getInstance().gameMode.destroyBlock(blockPos);
        } else {
            ((ServerPlayer) player).gameMode.destroyBlock(blockPos);
        }
        return true;
    }

    //Gives items directly to player
    public static boolean dropBlock(Level level, Player player, BlockPos pos) {
        if (!(player instanceof ServerPlayer)) return false;
        if (player.isCreative()) return false;
//        if (!level.isLoaded(pos) && !level.isEmptyBlock(pos)) return false;

        var blockState = level.getBlockState(pos);
//        if (blockState.isAir()) return false;

        var block = blockState.getBlock();

        if (!player.hasCorrectToolForDrops(blockState)) {
            return false;
        }

        block.playerDestroy(level, player, pos, blockState, level.getBlockEntity(pos), player.getMainHandItem());
        //TODO drop items in inventory instead of level
        return true;
    }


    public static boolean canPlace(Level level, Player player, BlockPos pos, BlockState newBlockState) {
        if (!level.mayInteract(player, pos)) return false;
        if (!player.getAbilities().mayBuild) return false;
        if (BuildModifierHelper.isReplace(player)) {
            if (player.isCreative()) return true;
            return !level.getBlockState(pos).is(BlockTags.FEATURES_CANNOT_REPLACE); // fluid
        }
        return level.getBlockState(pos).getMaterial().isReplaceable(); // fluid
    }

    //Can break using held tool? (or in creative)
    public static boolean canBreak(Level level, Player player, BlockPos pos) {
        if (!level.mayInteract(player, pos)) return false;
        if (!player.getAbilities().mayBuild) return false;
        if (player.isCreative()) return true;
        return !level.getBlockState(pos).is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public static boolean doesBecomeDoubleSlab(Player player, BlockPos pos, Direction facing) {
        BlockState placedBlockState = player.level.getBlockState(pos);

        ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (CompatHelper.isItemBlockProxy(itemstack))
            itemstack = CompatHelper.getItemBlockFromStack(itemstack);

        if (itemstack.isEmpty() || !(itemstack.getItem() instanceof BlockItem) || !(((BlockItem) itemstack.getItem()).getBlock() instanceof SlabBlock heldSlab))
            return false;

        return false;
    }
}
