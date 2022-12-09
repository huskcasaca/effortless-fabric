package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessClient;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.Buildable;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHandler;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.config.PreviewConfig;
import dev.huskcasaca.effortless.utils.CompatHelper;
import dev.huskcasaca.effortless.buildreach.ReachHelper;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BlockPreviewRenderer {

    private static final BlockPreviewRenderer INSTANCE = new BlockPreviewRenderer();

    public static BlockPreviewRenderer getInstance() {
        return INSTANCE;
    }

    private final Minecraft minecraft;
    private final List<PlacedData> placedDataList = new ArrayList<>();
    private List<BlockPos> previousCoordinates;
    private List<BlockState> previousBlockStates;
    private List<ItemStack> previousItemStacks;
    private BlockPos previousFirstPos;
    private BlockPos previousSecondPos;
    private int soundTime = 0;

    public BlockPreviewRenderer() {
        this.minecraft = Minecraft.getInstance();
    }

    public void render(Player player, PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Camera camera) {
        //Render placed blocks with dissolve effect
        //Use fancy shader if config allows, otherwise no dissolve
        if (PreviewConfig.useShader()) {
            for (PlacedData placed : placedDataList) {
                if (placed.coordinates != null && !placed.coordinates.isEmpty()) {
                    double totalTime = Mth.clampedLerp(30, 60, placed.firstPos.distSqr(placed.secondPos) / 100.0) * PreviewConfig.shaderDissolveTimeMultiplier();
                    float dissolve = (EffortlessClient.ticksInGame - placed.time) / (float) totalTime;
                    renderBlockPreviews(poseStack, multiBufferSource, placed.coordinates, placed.blockStates, placed.itemStacks, dissolve, placed.firstPos, placed.secondPos, false, placed.breaking);
                }
            }
        }
        //Expire
        placedDataList.removeIf(placed -> {
            double totalTime = Mth.clampedLerp(30, 60, placed.firstPos.distSqr(placed.secondPos) / 100.0) * PreviewConfig.shaderDissolveTimeMultiplier();
            return placed.time + totalTime < EffortlessClient.ticksInGame;
        });

        //Render block previews
        HitResult lookingAt = EffortlessClient.getLookingAt(player);
        if (BuildModeHelper.getBuildMode(player) == BuildMode.DISABLE)
            lookingAt = Minecraft.getInstance().hitResult;

        ItemStack mainhand = player.getMainHandItem();
        boolean toolInHand = !(!mainhand.isEmpty() && CompatHelper.isItemBlockProxy(mainhand));

        BlockPos startPos = null;
        Direction sideHit = null;
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

            sideHit = blockLookingAt.getDirection();
            hitVec = blockLookingAt.getLocation();
        }

        //Dont render if in normal mode and modifiers are disabled
        //Unless alwaysShowBlockPreview is true in config
        if (doRenderBlockPreviews(player, startPos)) {

            //Keep blockstate the same for every block in the buildmode
            //So dont rotate blocks when in the middle of placing wall etc.
            if (BuildModeHandler.isActive(player)) {
                Buildable buildModeInstance = BuildModeHelper.getBuildMode(player).instance;
                if (buildModeInstance.getSideHit(player) != null) sideHit = buildModeInstance.getSideHit(player);
                if (buildModeInstance.getHitVec(player) != null) hitVec = buildModeInstance.getHitVec(player);
            }

            if (sideHit != null) {

                //Should be red?
                boolean breaking = BuildModeHandler.currentlyBreakingClient.get(player) != null && BuildModeHandler.currentlyBreakingClient.get(player);

                //get coordinates
                List<BlockPos> startCoordinates = BuildModeHandler.findCoordinates(player, startPos, breaking || BuildModifierHelper.isQuickReplace(player));

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

                List<BlockPos> newCoordinates = BuildModifierHandler.findCoordinates(player, startCoordinates);

                sortOnDistanceToPlayer(newCoordinates, player);

                hitVec = new Vec3(Math.abs(hitVec.x - ((int) hitVec.x)), Math.abs(hitVec.y - ((int) hitVec.y)),
                        Math.abs(hitVec.z - ((int) hitVec.z)));

                //Get blockstates
                List<ItemStack> itemStacks = new ArrayList<>();
                List<BlockState> blockStates = new ArrayList<>();
                if (breaking) {
                    //Find blockstate of world
                    for (var coordinate : newCoordinates) {
                        blockStates.add(player.level.getBlockState(coordinate));
                    }
                } else {
                    blockStates = BuildModifierHandler.findBlockStates(player, startCoordinates, hitVec, sideHit, itemStacks);
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
                    if (newCoordinates.size() > 1 && blockStates.size() > 1 && soundTime < EffortlessClient.ticksInGame) {
                        soundTime = EffortlessClient.ticksInGame;

                        if (blockStates.get(0) != null) {
                            SoundType soundType = blockStates.get(0).getBlock().getSoundType(blockStates.get(0));
                            player.level.playSound(player, player.blockPosition(), breaking ? soundType.getBreakSound() : soundType.getPlaceSound(),
                                    SoundSource.BLOCKS, 0.3f, 0.8f);
                        }
                    }
                }

                //Render block previews
                if (!blockStates.isEmpty() && newCoordinates.size() == blockStates.size()) {
                    int blockCount;

                    //Use fancy shader if config allows, otherwise outlines
                    if (PreviewConfig.useShader() && newCoordinates.size() < PreviewConfig.shaderThresholdRounded()) {
                        blockCount = renderBlockPreviews(poseStack, multiBufferSource, newCoordinates, blockStates, itemStacks, 0f, firstPos, secondPos, !breaking, breaking);
                    } else {
                        VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);

                        var color = new Vec3(1f, 1f, 1f);
                        if (breaking) color = new Vec3(1f, 0f, 0f);

                        for (int i = newCoordinates.size() - 1; i >= 0; i--) {
                            VoxelShape collisionShape = blockStates.get(i).getCollisionShape(player.level, newCoordinates.get(i));
                            RenderUtils.renderBlockOutline(poseStack, buffer, newCoordinates.get(i), collisionShape, color);
                        }

                        RenderUtils.endLines(multiBufferSource);

                        blockCount = newCoordinates.size();
                    }

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

                        String dimensions = " (";
                        if (dim.getX() > 1) dimensions += dim.getX() + "x";
                        if (dim.getZ() > 1) dimensions += dim.getZ() + "x";
                        if (dim.getY() > 1) dimensions += dim.getY() + "x";
                        dimensions = dimensions.substring(0, dimensions.length() - 1);
                        if (dimensions.length() > 1) dimensions += ")";

                        Effortless.log(player, ChatFormatting.GOLD + BuildModeHelper.getTranslatedModeOptionName(player) + ChatFormatting.RESET + " of " + blockCount + " " + (blockCount == 1 ? "block" : "blocks") + dimensions, true);
                    }
                }

            }

            VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
            //Draw outlines if tool in hand
            //Find proper raytrace: either normal range or increased range depending on canBreakFar
            HitResult objectMouseOver = Minecraft.getInstance().hitResult;
            HitResult breakingRaytrace = ReachHelper.isCanBreakFar(player) ? lookingAt : objectMouseOver;
            if (toolInHand && breakingRaytrace != null && breakingRaytrace.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockBreakingRaytrace = (BlockHitResult) breakingRaytrace;
                List<BlockPos> breakCoordinates = BuildModifierHandler.findCoordinates(player, blockBreakingRaytrace.getBlockPos());

                //Only render first outline if further than normal reach
                boolean excludeFirst = objectMouseOver != null && objectMouseOver.getType() == HitResult.Type.BLOCK;
                for (int i = excludeFirst ? 1 : 0; i < breakCoordinates.size(); i++) {
                    var coordinate = breakCoordinates.get(i);

                    var blockState = player.level.getBlockState(coordinate);
                    if (!blockState.isAir()) {
                        if (SurvivalHelper.canBreak(player.level, player, coordinate) || i == 0) {
                            VoxelShape collisionShape = blockState.getCollisionShape(player.level, coordinate);
                            RenderUtils.renderBlockOutline(poseStack, buffer, coordinate, collisionShape, new Vec3(0f, 0f, 0f));
                        }
                    }
                }
            }
            RenderUtils.endLines(multiBufferSource);
        }
    }

    //Whether to draw any block previews or outlines
    public static boolean doRenderBlockPreviews(Player player, BlockPos startPos) {
        return ConfigManager.getGlobalPreviewConfig().isAlwaysShowBlockPreview() || (BuildModeHelper.getBuildMode(player) != BuildMode.DISABLE);
    }

    protected static int renderBlockPreviews(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, List<BlockPos> coordinates, List<BlockState> blockStates,
                                             List<ItemStack> itemStacks, float dissolve, BlockPos firstPos,
                                             BlockPos secondPos, boolean checkCanPlace, boolean red) {
        var player = Minecraft.getInstance().player;
        var modifierSettings = BuildModifierHelper.getModifierSettings(player);
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        int blocksValid = 0;

        if (coordinates.isEmpty()) return blocksValid;

        for (int i = coordinates.size() - 1; i >= 0; i--) {
            var blockPos = coordinates.get(i);
            var blockState = blockStates.get(i);
            var itemstack = itemStacks.isEmpty() ? ItemStack.EMPTY : itemStacks.get(i);
            if (CompatHelper.isItemBlockProxy(itemstack))
                itemstack = CompatHelper.getItemBlockByState(itemstack, blockState);

            //Check if can place
            //If check is turned off, check if blockstate is the same (for dissolve effect)
            if ((!checkCanPlace /*&& player.world.getNewBlockState(blockPos) == blockState*/) || //TODO enable (breaks the breaking shader)
                    SurvivalHelper.canPlace(player.level, player, blockPos, blockState, itemstack, modifierSettings.enableQuickReplace(), Direction.UP)) {

                RenderUtils.renderBlockPreview(poseStack, multiBufferSource, dispatcher, blockPos, blockState, dissolve, firstPos, secondPos, red);
                blocksValid++;
            }
        }
        return blocksValid;
    }

    public void onBlocksPlaced() {
        onBlocksPlaced(previousCoordinates, previousItemStacks, previousBlockStates, previousFirstPos, previousSecondPos);
    }

    public void onBlocksPlaced(List<BlockPos> coordinates, List<ItemStack> itemStacks, List<BlockState> blockStates,
                               BlockPos firstPos, BlockPos secondPos) {
        var player = Minecraft.getInstance().player;

        //Check if block previews are enabled
        if (doRenderBlockPreviews(player, firstPos)) {

            //Save current coordinates, blockstates and itemstacks
            if (!coordinates.isEmpty() && blockStates.size() == coordinates.size() &&
                    coordinates.size() > 1 && coordinates.size() < PreviewConfig.shaderThresholdRounded()) {

                placedDataList.add(new PlacedData(EffortlessClient.ticksInGame, coordinates, blockStates,
                        itemStacks, firstPos, secondPos, false));
            }
        }

    }

    public void onBlocksBroken() {
        onBlocksBroken(previousCoordinates, previousItemStacks, previousBlockStates, previousFirstPos, previousSecondPos);
    }

    public void onBlocksBroken(List<BlockPos> coordinates, List<ItemStack> itemStacks, List<BlockState> blockStates,
                               BlockPos firstPos, BlockPos secondPos) {
        var player = Minecraft.getInstance().player;

        //Check if block previews are enabled
        if (doRenderBlockPreviews(player, firstPos)) {

            //Save current coordinates, blockstates and itemstacks
            if (!coordinates.isEmpty() && blockStates.size() == coordinates.size() &&
                    coordinates.size() > 1 && coordinates.size() < PreviewConfig.shaderThresholdRounded()) {

                sortOnDistanceToPlayer(coordinates, player);

                placedDataList.add(new PlacedData(EffortlessClient.ticksInGame, coordinates, blockStates,
                        itemStacks, firstPos, secondPos, true));
            }
        }

    }

    private static void sortOnDistanceToPlayer(List<BlockPos> coordinates, Player player) {

        coordinates.sort((lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal
            double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lhs).subtract(player.getEyePosition(1f)).lengthSqr();
            double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rhs).subtract(player.getEyePosition(1f)).lengthSqr();
            return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
        });

    }

    record PlacedData(
            float time,
            List<BlockPos> coordinates,
            List<BlockState> blockStates,
            List<ItemStack> itemStacks,
            BlockPos firstPos,
            BlockPos secondPos,
            boolean breaking
    ) {
    }
}
