package dev.huskcasaca.effortless.buildmodifier.array;

import dev.huskcasaca.effortless.buildmodifier.Modifier;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class Array implements Modifier {

    public static Set<BlockPos> findCoordinates(Player player, BlockPos startPos) {
        var coordinates = new LinkedHashSet<BlockPos>();

        //find arraysettings for the player
        var arraySettings = BuildModifierHelper.getModifierSettings(player).arraySettings();
        if (!isEnabled(arraySettings)) return Collections.emptySet();

        var pos = startPos;
        var offset = new Vec3i(arraySettings.offset.getX(), arraySettings.offset.getY(), arraySettings.offset.getZ());

        for (int i = 0; i < arraySettings.count() - 1; i++) {
            pos = pos.offset(offset);
            coordinates.add(pos);
        }

        return coordinates;
    }

    public static Map<BlockPos, BlockState> findBlockStates(Player player, BlockPos startPos, BlockState blockState, ItemStack itemStack, List<ItemStack> itemStacks) {
        var blockStates = new LinkedHashMap<BlockPos, BlockState>();

        //find arraysettings for the player that placed the block
        var arraySettings = BuildModifierHelper.getModifierSettings(player).arraySettings();
        if (!isEnabled(arraySettings)) return Collections.emptyMap();

        var pos = startPos;
        var offset = new Vec3i(arraySettings.offset.getX(), arraySettings.offset.getY(), arraySettings.offset.getZ());

        //Randomizer bag synergy
//		AbstractRandomizerBagItem randomizerBagItem = null;
//		Container bagInventory = null;
//		if (!itemStack.isEmpty() && itemStack.getItem() instanceof AbstractRandomizerBagItem) {
//			randomizerBagItem = (AbstractRandomizerBagItem) itemStack.getItem() ;
//			bagInventory = randomizerBagItem.getBagInventory(itemStack);
//		}

        for (int i = 0; i < arraySettings.count() - 1; i++) {
            pos = pos.offset(offset);

            //Randomizer bag synergy
//			if (randomizerBagItem != null) {
//				itemStack = randomizerBagItem.pickRandomStack(bagInventory);
//				blockState = BuildModifiers
//					.getBlockStateFromItem(itemStack, player, startPos, Direction.UP, new Vec3(0, 0, 0), InteractionHand.MAIN_HAND);
//			}

            //blockState = blockState.getBlock().getStateForPlacement(player.world, pos, )
            if (blockStates.get(pos) == null) {
                blockStates.put(pos, blockState);
                itemStacks.add(itemStack);
            }
        }

        return blockStates;
    }

    public static boolean isEnabled(ArraySettings a) {
        if (a == null || !a.enabled()) return false;

        return a.offset.getX() != 0 || a.offset.getY() != 0 || a.offset.getZ() != 0;
    }

    public record ArraySettings(
            boolean enabled,
            Vec3i offset,
            int count
    ) {

        public ArraySettings() {
            this(false, BlockPos.ZERO, 5);
        }

        public int reach() {
            //find largest offset
            int x = Math.abs(offset.getX());
            int y = Math.abs(offset.getY());
            int z = Math.abs(offset.getZ());
            int largestOffset = Math.max(Math.max(x, y), z);

            return count > 1 ? largestOffset * (count - 1) : 0;
        }

        public ArraySettings clone(boolean enabled) {
            return new ArraySettings(enabled, offset, count);
        }
    }

}
