package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessClient;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmode.Buildable;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHandler;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.config.PreviewConfig;
import dev.huskcasaca.effortless.utils.CompatHelper;
import dev.huskcasaca.effortless.utils.InventoryHelper;
import dev.huskcasaca.effortless.utils.SurvivalHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

@Environment(EnvType.CLIENT)
public class BlockPreviewRenderer {

    private static final BlockPreviewRenderer INSTANCE = new BlockPreviewRenderer();
    private final Minecraft minecraft;
    private final List<Preview> lastPlaced = new ArrayList<>();
    private final List<Preview> currentPlacing = new ArrayList<>();
    private List<BlockPos> previousCoordinates;
    private List<BlockState> previousBlockStates;
    private List<ItemStack> previousItemStacks;
    private BlockPos previousFirstPos;
    private BlockPos previousSecondPos;
    private int soundTime = 0;

    public BlockPreviewRenderer() {
        this.minecraft = Minecraft.getInstance();
    }

    public static BlockPreviewRenderer getInstance() {
        return INSTANCE;
    }

    //Whether to draw any block previews or outlines
    public static boolean doRenderBlockPreviews(Player player, BlockPos startPos) {
        return ConfigManager.getGlobalPreviewConfig().isAlwaysShowBlockPreview() || (BuildModeHelper.getBuildMode(player) != BuildMode.DISABLE);
    }

    public static List<BlockPosState> getBlockPosStates(List<BlockPos> coordinates, List<BlockState> blockStates) {
        if (coordinates.size() != blockStates.size()) {
            throw new IllegalArgumentException("Coordinates and blockstates must be the same size");
        }
        if (coordinates.isEmpty()) {
            return Collections.emptyList();
        }
        var result = new ArrayList<BlockPosState>();
        for (int i = 0; i < coordinates.size(); i++) {
            var coordinate = coordinates.get(i);
            var blockState = blockStates.get(i);
            if (coordinate == null || blockState == null) {
                throw new IllegalArgumentException("Coordinate or blockstate is null");
            }
            result.add(new BlockPosState(coordinate, blockState, null, null));
        }
        return result;
    }

    public static List<BlockPosState> getBlockPosStates(Player player, List<BlockPos> coordinates, List<BlockState> blockStates) {
        if (coordinates.size() != blockStates.size()) {
            throw new IllegalArgumentException("Coordinates and blockstates must be the same size");
        }
        if (coordinates.isEmpty()) {
            return Collections.emptyList();
        }
        var result = new ArrayList<BlockPosState>();
        for (int i = 0; i < coordinates.size(); i++) {
            var coordinate = coordinates.get(i);
            var blockState = blockStates.get(i);
            if (coordinate == null || blockState == null) {
                throw new IllegalArgumentException("Coordinate or blockstate is null");
            }
            result.add(new BlockPosState(coordinate, blockState, SurvivalHelper.canPlace(player.level, player, coordinate, blockState), SurvivalHelper.canBreak(player.level, player, coordinate)));
        }
        return result;
    }

    public static Map<Block, Integer> getPlayerBlockCount(Player player, List<BlockState> blockStates) {
        var result = new HashMap<Block, Integer>();
        blockStates.forEach(blockState -> {
            result.putIfAbsent(blockState.getBlock(), InventoryHelper.findTotalBlocksInInventory(player, blockState.getBlock()));
        });
        return result;
    }

    private static void renderBlockDissolveShader(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, List<BlockPosState> placeData, Map<Block, Integer> blocksLeft, BlockPos firstPos, BlockPos secondPos, float dissolve, boolean breaking) {
        var player = Minecraft.getInstance().player;
        var dispatcher = Minecraft.getInstance().getBlockRenderer();

        if (placeData.isEmpty()) return;

        var blockLeft = new HashMap<>(blocksLeft);

        for (BlockPosState blockPosState : placeData) {
            var blockPos = blockPosState.coordinate;
            var blockState = blockPosState.blockState;
            var canBreak = blockPosState.canBreak;
            var canPlace = blockPosState.canPlace;

            if (breaking) {
                if (canBreak != null && canBreak || SurvivalHelper.canBreak(player.level, player, blockPos)) {
                    RenderUtils.renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, true);
                }
            } else {
                if (canPlace != null && canPlace || SurvivalHelper.canPlace(player.level, player, blockPosState.coordinate, blockState)) {
                    if (player.isCreative()) {
                        RenderUtils.renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, false);
                        continue;
                    }
                    var count = blockLeft.get(blockState.getBlock());
                    if (count > 0) {
                        RenderUtils.renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, false);
                        blockLeft.put(blockState.getBlock(), count - 1);
                    } else {
                        RenderUtils.renderBlockDissolveShader(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, true);
                    }
                }
            }
        }
    }

    private static void renderBlockOutlines(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, List<BlockPosState> placeData, Map<Block, Integer> blocksLeft, BlockPos firstPos, BlockPos secondPos, float dissolve, boolean breaking) {
        var player = Minecraft.getInstance().player;
        var dispatcher = Minecraft.getInstance().getBlockRenderer();

        if (placeData.isEmpty()) return;

        var blockLeft = new HashMap<>(blocksLeft);

        for (BlockPosState blockPosState : placeData) {
            var blockPos = blockPosState.coordinate;
            var blockState = blockPosState.blockState;
            var canBreak = blockPosState.canBreak;
            var canPlace = blockPosState.canPlace;

            if (breaking) {
                if (canBreak != null && canBreak || SurvivalHelper.canBreak(player.level, player, blockPos)) {
                    RenderUtils.renderBlockOutlines(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, true);
                }
            } else {
                if (canPlace != null && canPlace || SurvivalHelper.canPlace(player.level, player, blockPosState.coordinate, blockState)) {
                    if (player.isCreative()) {
                        RenderUtils.renderBlockOutlines(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, false);
                        continue;
                    }
                    var count = blockLeft.get(blockState.getBlock());
                    if (count > 0) {
                        RenderUtils.renderBlockOutlines(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, false);
                        blockLeft.put(blockState.getBlock(), count - 1);
                    } else {
                        RenderUtils.renderBlockOutlines(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, true);
                    }
                }
            }
        }
    }

    private static UseResult getBlockUseResult(List<BlockPosState> placeData, Map<Block, Integer> blocksLeft, boolean breaking) {
        if (placeData.isEmpty()) {
            return UseResult.EMPTY;
        }
        var player = Minecraft.getInstance().player;

        var valid = new HashMap<Block, Integer>();
        var total = new HashMap<Block, Integer>();

        var left = new HashMap<>(blocksLeft);

        for (BlockPosState placeDatum : placeData) {
            var blockPos = placeDatum.coordinate;
            var blockState = placeDatum.blockState;

            if (breaking) {
                var canBreak = SurvivalHelper.canBreak(player.level, player, blockPos);
                if (canBreak) {
                    valid.put(blockState.getBlock(), valid.getOrDefault(blockState.getBlock(), 0) + 1);
                    total.put(blockState.getBlock(), total.getOrDefault(blockState.getBlock(), 0) + 1);
                }

            } else {
                var canPlace = SurvivalHelper.canPlace(player.level, player, placeDatum.coordinate, blockState);
                if (canPlace) {
                    if (player.isCreative()) {
                        valid.put(blockState.getBlock(), valid.getOrDefault(blockState.getBlock(), 0) + 1);
                    } else {
                        var count = left.get(blockState.getBlock());
                        if (count > 0) {
                            left.put(blockState.getBlock(), count - 1);
                            valid.put(blockState.getBlock(), valid.getOrDefault(blockState.getBlock(), 0) + 1);
                        }
                    }
                    total.put(blockState.getBlock(), total.getOrDefault(blockState.getBlock(), 0) + 1);
                }
            }
        }

        return new UseResult(valid, total);
    }

    private static void sortOnDistanceToPlayer(List<BlockPos> coordinates, Player player) {

        coordinates.sort((lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal
            double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lhs).subtract(player.getEyePosition(1f)).lengthSqr();
            double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rhs).subtract(player.getEyePosition(1f)).lengthSqr();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }

    public void render(Player player, PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Camera camera) {
        //Render placed blocks with dissolve effect
        //Use fancy shader if config allows, otherwise no dissolve
        if (PreviewConfig.useShader()) {
            for (Preview placed : lastPlaced) {
                if (placed.blockPosStates() != null && !placed.blockPosStates().isEmpty()) {
                    double totalTime = Mth.clampedLerp(30, 60, placed.firstPos.distSqr(placed.secondPos) / 100.0) * PreviewConfig.shaderDissolveTimeMultiplier();
                    float dissolve = (EffortlessClient.getTicksInGame() - placed.time) / (float) totalTime;
                    renderBlockDissolveShader(poseStack, multiBufferSource, placed.blockPosStates(), placed.useResult().valid(), placed.firstPos, placed.secondPos, dissolve, placed.breaking);
                }
            }
        }
        //Expire
        if (currentPlacing.isEmpty()) {
            Effortless.log(player, "", true);
        }
        currentPlacing.clear();
        lastPlaced.removeIf(placed -> {
            double totalTime = Mth.clampedLerp(30, 60, placed.firstPos.distSqr(placed.secondPos) / 100.0) * PreviewConfig.shaderDissolveTimeMultiplier();
            return placed.time + totalTime < EffortlessClient.getTicksInGame();
        });

        //Render block previews
        HitResult lookingAt = EffortlessClient.getLookingAt(player);
        if (BuildModeHelper.getBuildMode(player) == BuildMode.DISABLE)
            lookingAt = Minecraft.getInstance().hitResult;

        ItemStack mainhand = player.getMainHandItem();
        boolean toolInHand = !(!mainhand.isEmpty() && CompatHelper.isItemBlockProxy(mainhand));

        BlockPos startPos = null;
        Direction hitSide = null;
        Vec3 hitVec = null;

        //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
        if (lookingAt != null && lookingAt.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockLookingAt = (BlockHitResult) lookingAt;
            startPos = blockLookingAt.getBlockPos();

            //Check if tool (or none) in hand
            //TODO 1.13 replaceable
            boolean replaceable = player.level.getBlockState(startPos).getMaterial().isReplaceable();
            boolean becomesDoubleSlab = SurvivalHelper.doesBecomeDoubleSlab(player, startPos, blockLookingAt.getDirection());
            if (!BuildModifierHelper.isQuickReplace(player) && !toolInHand && !replaceable && !becomesDoubleSlab) {
                startPos = startPos.relative(blockLookingAt.getDirection());
            }

            //Get under tall grass and other replaceable blocks
            // TODO: 20/9/22 remove
            if (BuildModifierHelper.isQuickReplace(player) && !toolInHand && replaceable) {
                startPos = startPos.below();
            }

            hitSide = blockLookingAt.getDirection();
            hitVec = blockLookingAt.getLocation();
        }

        //Dont render if in normal mode and modifiers are disabled
        //Unless alwaysShowBlockPreview is true in config
        if (!doRenderBlockPreviews(player, startPos)) {
            return;
        }

        //Keep blockstate the same for every block in the buildmode
        //So dont rotate blocks when in the middle of placing wall etc.
        if (BuildModeHandler.isActive(player)) {
            Buildable buildModeInstance = BuildModeHelper.getBuildMode(player).getInstance();
            if (buildModeInstance.getHitSide(player) != null) hitSide = buildModeInstance.getHitSide(player);
            if (buildModeInstance.getHitVec(player) != null) hitVec = buildModeInstance.getHitVec(player);
        }

        if (hitSide == null) {
            return;
        }

        //Should be red?
        var breaking = BuildModeHandler.isCurrentlyBreaking(player);

        //get coordinates
        var skipRaytrace = breaking || BuildModifierHelper.isQuickReplace(player);
        var startCoordinates = BuildModeHandler.findCoordinates(player, startPos, skipRaytrace);

        //Remember first and last point for the shader
        var firstPos = BlockPos.ZERO;
        var secondPos = BlockPos.ZERO;
        if (!startCoordinates.isEmpty()) {
            firstPos = startCoordinates.get(0);
            secondPos = startCoordinates.get(startCoordinates.size() - 1);
        }

        //Limit number of blocks you can place
        int limit = ReachHelper.getMaxBlockPlaceAtOnce(player);
        if (startCoordinates.size() > limit) {
            startCoordinates = startCoordinates.subList(0, limit);
        }

        var newCoordinates = BuildModifierHandler.findCoordinates(player, startCoordinates);

//                sortOnDistanceToPlayer(newCoordinates, player);

        hitVec = new Vec3(Math.abs(hitVec.x - ((int) hitVec.x)), Math.abs(hitVec.y - ((int) hitVec.y)), Math.abs(hitVec.z - ((int) hitVec.z)));

        //Get blockstates
        var itemStacks = new ArrayList<ItemStack>();
        var blockStates = new ArrayList<BlockState>();
        if (breaking) {
            //Find blockstate of world
            for (var coordinate : newCoordinates) {
                blockStates.add(player.level.getBlockState(coordinate));
            }
        } else {
            blockStates.addAll(BuildModifierHandler.findBlockStates(player, startCoordinates, hitVec, hitSide, itemStacks).values());
        }


        //Check if they are different from previous
        //TODO fix triggering when moving player
        if (!BuildModifierHandler.compareCoordinates(previousCoordinates, newCoordinates)) {
            previousCoordinates = newCoordinates;
            //remember the rest for placed blocks
            previousBlockStates = blockStates;
            previousItemStacks = itemStacks;
            previousFirstPos = firstPos;
            previousSecondPos = secondPos;

            //if so, renew randomness of randomizer bag
//					AbstractRandomizerBagItem.renewRandomness();
            //and play sound (max once every tick)
            if (newCoordinates.size() > 1 && blockStates.size() > 1 && soundTime < EffortlessClient.getTicksInGame()) {
                soundTime = EffortlessClient.getTicksInGame();

                if (blockStates.get(0) != null) {
                    SoundType soundType = blockStates.get(0).getBlock().getSoundType(blockStates.get(0));
                    player.level.playSound(player, player.blockPosition(), breaking ? soundType.getBreakSound() : soundType.getPlaceSound(), SoundSource.BLOCKS, 0.3f, 0.8f);
                }
            }
        }

        //Render block previews
        if (blockStates.isEmpty() || newCoordinates.size() != blockStates.size()) {
            return;
        }

        //Use fancy shader if config allows, otherwise outlines
        switch (ConfigManager.getGlobalPreviewConfig().getBlockPreviewMode()) {
            case OUTLINES -> renderBlockOutlines(poseStack, multiBufferSource, getBlockPosStates(newCoordinates, blockStates), getPlayerBlockCount(player, blockStates), firstPos, secondPos, 0f, breaking);
//            case BLOCK_TEX -> renderBlockPreviews(poseStack, multiBufferSource, getBlockPosStates(newCoordinates, blockStates), getPlayerBlockCount(player, blockStates), firstPos, secondPos, 0f, breaking);
            case DISSOLVE_SHADER -> renderBlockDissolveShader(poseStack, multiBufferSource, getBlockPosStates(newCoordinates, blockStates), getPlayerBlockCount(player, blockStates), firstPos, secondPos, 0f, breaking);
        }

        var placeResult = getBlockUseResult(getBlockPosStates(newCoordinates, blockStates), getPlayerBlockCount(player, blockStates), breaking);

        currentPlacing.add(new Preview(getBlockPosStates(player, newCoordinates, blockStates), placeResult, firstPos, secondPos, EffortlessClient.getTicksInGame(), false));

        //Display block count and dimensions in actionbar
        if (BuildModeHandler.isActive(player)) {

            //Find min and max values (not simply firstPos and secondPos because that doesn't work with circles)
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            for (var pos : startCoordinates) {
                if (pos.getX() < minX) minX = pos.getX();
                if (pos.getX() > maxX) maxX = pos.getX();
                if (pos.getY() < minY) minY = pos.getY();
                if (pos.getY() > maxY) maxY = pos.getY();
                if (pos.getZ() < minZ) minZ = pos.getZ();
                if (pos.getZ() > maxZ) maxZ = pos.getZ();
            }
            var dim = new BlockPos(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);

            String dimensions = "(";
            if (dim.getX() > 1) dimensions += dim.getX() + "x";
            if (dim.getZ() > 1) dimensions += dim.getZ() + "x";
            if (dim.getY() > 1) dimensions += dim.getY() + "x";
            dimensions = dimensions.substring(0, dimensions.length() - 1);
            if (dimensions.length() > 1) dimensions += ")";


            var blockCounter = "" + ChatFormatting.WHITE + placeResult.validBlocks() + ChatFormatting.RESET + (placeResult.isFilled() ? " " : " + " + ChatFormatting.RED + placeResult.wantedBlocks() + ChatFormatting.RESET + " ") + (placeResult.totalBlocks() == 1 ? "block" : "blocks");

            Effortless.log(player, "%s%s%s of %s %s".formatted(ChatFormatting.GOLD, BuildModeHelper.getTranslatedModeOptionName(player), ChatFormatting.RESET, blockCounter, dimensions), true);
        } else {
            Effortless.log(player, "", true);
        }

        //            VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
//            //Draw outlines if tool in hand
//            //Find proper raytrace: either normal range or increased range depending on canBreakFar
//            HitResult objectMouseOver = Minecraft.getInstance().hitResult;
//            HitResult breakingRaytrace = ReachHelper.isCanBreakFar(player) ? lookingAt : objectMouseOver;
//            if (toolInHand && breakingRaytrace != null && breakingRaytrace.getType() == HitResult.Type.BLOCK) {
//                BlockHitResult blockBreakingRaytrace = (BlockHitResult) breakingRaytrace;
//                List<BlockPos> breakCoordinates = BuildModifierHandler.findCoordinates(player, blockBreakingRaytrace.getBlockPos());
//
//                //Only render first outline if further than normal reach
//                boolean excludeFirst = objectMouseOver != null && objectMouseOver.getType() == HitResult.Type.BLOCK;
//                for (int i = excludeFirst ? 1 : 0; i < breakCoordinates.size(); i++) {
//                    var coordinate = breakCoordinates.get(i);
//
//                    var blockState = player.level.getBlockState(coordinate);
//                    if (!blockState.isAir()) {
//                        if (SurvivalHelper.canBreak(player.level, player, coordinate) || i == 0) {
//                            VoxelShape collisionShape = blockState.getCollisionShape(player.level, coordinate);
//                            RenderUtils.renderBlockOutline(poseStack, buffer, coordinate, collisionShape, new Vec3(0f, 0f, 0f));
//                        }
//                    }
//                }
//            }
//            RenderUtils.endLines(multiBufferSource);
    }

    public void onBlocksPlaced() {
        onBlocksPlaced(previousCoordinates, previousItemStacks, previousBlockStates, previousFirstPos, previousSecondPos);
    }

    public void onBlocksPlaced(List<BlockPos> coordinates, List<ItemStack> itemStacks, List<BlockState> blockStates, BlockPos firstPos, BlockPos secondPos) {
        var player = Minecraft.getInstance().player;

        if (!doRenderBlockPreviews(player, firstPos)) {
            return;
        }
        if (coordinates != null && blockStates != null && !coordinates.isEmpty() && blockStates.size() == coordinates.size() && coordinates.size() > 1/*  && coordinates.size() < PreviewConfig.shaderThresholdRounded() */) {
            lastPlaced.add(new Preview(getBlockPosStates(player, coordinates, blockStates), getBlockUseResult(getBlockPosStates(coordinates, blockStates), getPlayerBlockCount(player, blockStates), false), firstPos, secondPos, EffortlessClient.getTicksInGame(), false));
        }
    }

    public void onBlocksBroken() {
        onBlocksBroken(previousCoordinates, previousItemStacks, previousBlockStates, previousFirstPos, previousSecondPos);
    }

    public void onBlocksBroken(List<BlockPos> coordinates, List<ItemStack> itemStacks, List<BlockState> blockStates,
                               BlockPos firstPos, BlockPos secondPos) {
        var player = Minecraft.getInstance().player;

        if (doRenderBlockPreviews(player, firstPos)) {
            return;
        }
        if (coordinates != null && blockStates != null && !coordinates.isEmpty() && blockStates.size() == coordinates.size() && coordinates.size() > 1/*  && coordinates.size() < PreviewConfig.shaderThresholdRounded() */) {
//                sortOnDistanceToPlayer(coordinates, player);
            lastPlaced.add(new Preview(getBlockPosStates(coordinates, blockStates), getBlockUseResult(getBlockPosStates(coordinates, blockStates), getPlayerBlockCount(player, blockStates), true), firstPos, secondPos, EffortlessClient.getTicksInGame(), true));
        }

    }

    public List<Preview> getLastPlaced() {
        return lastPlaced;
    }

    public List<Preview> getCurrentPlacing() {
        return currentPlacing;
    }

    record BlockPosState(
            BlockPos coordinate,
            BlockState blockState,
            Boolean canPlace,
            Boolean canBreak
    ) { }

    public record Preview(
            List<BlockPosState> blockPosStates,
            UseResult useResult,
            BlockPos firstPos,
            BlockPos secondPos,
            float time,
            boolean breaking
    ) {

        public List<ItemStack> getValidItemStacks() {
            var result = new ArrayList<ItemStack>();
            useResult.valid.forEach((block, count) -> {
                if (block.equals(Blocks.AIR)) return;
                while (count > 0) {
                    var itemStack = new ItemStack(block.asItem());
                    if (itemStack.getMaxStackSize() <= 0) continue;
                    var used = count > itemStack.getMaxStackSize() ? itemStack.getMaxStackSize() : count;
                    itemStack.setCount(used);
                    count -= used;
                    result.add(itemStack);
                }
            });
            return result;
        }

        public List<ItemStack> getInvalidItemStacks() {
            var result = new ArrayList<ItemStack>();
            useResult.total.forEach((block, count) -> {
                if (block.equals(Blocks.AIR)) return;
                count = count - useResult.valid.getOrDefault(block, 0);
                while (count > 0) {
                    var itemStack = new ItemStack(block.asItem());
                    if (itemStack.getMaxStackSize() <= 0) continue;
                    var used = count > itemStack.getMaxStackSize() ? itemStack.getMaxStackSize() : count;
                    itemStack.setCount(used);
                    count -= used;
                    result.add(itemStack);
                }
            });
            return result;
        }

    }

    record UseResult(
            Map<Block, Integer> valid,
            Map<Block, Integer> total
    ) {

        public static UseResult EMPTY = new UseResult(Collections.emptyMap(), Collections.emptyMap());

        public int validBlocks() {
            return valid.values().stream().mapToInt(Integer::intValue).sum();
        }

        public int totalBlocks() {
            return total.values().stream().mapToInt(Integer::intValue).sum();
        }

        public boolean isFilled() {
            return validBlocks() == totalBlocks();
        }

        public int wantedBlocks() {
            return totalBlocks() - validBlocks();
        }
    }

}
