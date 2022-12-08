package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/***
 * Main render class for Effortless Building
 */
@Environment(EnvType.CLIENT)
public class RenderHandler {

    //    @SubscribeEvent
    public static void onRenderLevel(PoseStack poseStack) {

//        if (event.getPhase() != EventPriority.NORMAL || event.getStage() != AFTER_PARTICLES)
//            return;

        var matrixStack = poseStack;
        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var renderTypeBuffer = MultiBufferSource.immediate(bufferBuilder);

        var player = Minecraft.getInstance().player;
        var modeSettings = BuildModeHelper.getModeSettings(player);
        var modifierSettings = BuildModifierHelper.getModifierSettings(player);

        var projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        matrixStack.pushPose();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        //Mirror and radial mirror lines and areas
        ModifierRenderer.render(matrixStack, renderTypeBuffer, modifierSettings);

        //Render block previews
        BlockPreviewRenderer.render(matrixStack, renderTypeBuffer, player, modifierSettings, modeSettings);

        matrixStack.popPose();
    }

    protected static VertexConsumer beginLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        return renderTypeBuffer.getBuffer(BuildRenderTypes.EB_LINES);
    }

    protected static void endLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

    protected static VertexConsumer beginPlanes(MultiBufferSource.BufferSource renderTypeBuffer) {
        return renderTypeBuffer.getBuffer(BuildRenderTypes.EB_PLANES);
    }

    protected static void endPlanes(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

    protected static void renderBlockPreview(PoseStack matrixStack, MultiBufferSource.BufferSource renderTypeBuffer, BlockRenderDispatcher dispatcher,
                                             BlockPos blockPos, BlockState blockState, float dissolve, BlockPos firstPos, BlockPos secondPos, boolean red) {
        if (blockState == null) return;

        matrixStack.pushPose();
        matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
//        matrixStack.rotate(Vector3f.YP.rotationDegrees(-90f));
        matrixStack.translate(-0.01f, -0.01f, -0.01f);
        matrixStack.scale(1.02f, 1.02f, 1.02f);

        //Begin block preview rendering
        RenderType blockPreviewRenderType = BuildRenderTypes.getBlockPreviewRenderType(dissolve, blockPos, firstPos, secondPos, red);
        VertexConsumer buffer = renderTypeBuffer.getBuffer(blockPreviewRenderType);

        try {
            BakedModel model = dispatcher.getBlockModel(blockState);
            // TODO: 8/9/22  
            dispatcher.getModelRenderer().renderModel(matrixStack.last(), buffer, blockState, model,
                    1f, 1f, 1f, 0, OverlayTexture.NO_OVERLAY/*, ModelData.EMPTY, blockPreviewRenderType*/);
        } catch (NullPointerException e) {
            Effortless.logger.warn("RenderHandler::renderBlockPreview cannot render " + blockState.getBlock().toString());

            //Render outline as backup, escape out of the current renderstack
            matrixStack.popPose();
            renderTypeBuffer.endBatch();
            VertexConsumer lineBuffer = beginLines(renderTypeBuffer);
            renderBlockOutline(matrixStack, lineBuffer, blockPos, new Vec3(1f, 1f, 1f));
            endLines(renderTypeBuffer);
            buffer = renderTypeBuffer.getBuffer(Sheets.translucentCullBlockSheet()); //any type will do, as long as we have something on the stack
            matrixStack.pushPose();
        }

        renderTypeBuffer.endBatch();
        matrixStack.popPose();
    }

    protected static void renderBlockOutline(PoseStack matrixStack, VertexConsumer buffer, BlockPos pos, Vec3 color) {
        renderBlockOutline(matrixStack, buffer, pos, pos, color);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    protected static void renderBlockOutline(PoseStack matrixStack, VertexConsumer buffer, BlockPos pos1, BlockPos pos2, Vec3 color) {
        AABB aabb = new AABB(pos1, pos2.offset(1, 1, 1)).inflate(0.0020000000949949026);

        LevelRenderer.renderLineBox(matrixStack, buffer, aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
//        WorldRenderer.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //Renders outline with given bounding box
    protected static void renderBlockOutline(PoseStack matrixStack, VertexConsumer buffer, BlockPos pos, VoxelShape collisionShape, Vec3 color) {
//        WorldRenderer.drawShape(collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
        LevelRenderer.renderVoxelShape(matrixStack, buffer, collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //TODO
    //Sends breaking progress for all coordinates to renderglobal, so all blocks get visually broken
//    @Override
//    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
//        Minecraft mc = Minecraft.getInstance();
//
//        var modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
//        if (!BuildModifiers.isEnabled(modifierSettings, pos)) return;
//
//        List<BlockPos> coordinates = BuildModifiers.findCoordinates(mc.player, pos);
//        for (int i = 1; i < coordinates.size(); i++) {
//            var coordinate = coordinates.get(i);
//            if (SurvivalHelper.canBreak(mc.world, mc.player, coordinate)) {
//                //Send i as entity id because only one block can be broken per id
//                //Unless i happens to be the player id, then take something else
//                int fakeId = mc.player.getEntityId() != i ? i : coordinates.size();
//                mc.renderGlobal.sendBlockBreakProgress(fakeId, coordinate, progress);
//            }
//        }
//    }
}
