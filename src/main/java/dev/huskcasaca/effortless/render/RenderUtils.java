package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.vertex.*;
import dev.huskcasaca.effortless.Effortless;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class RenderUtils {

    protected static VertexConsumer beginLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        return renderTypeBuffer.getBuffer(BuildRenderTypes.lines());
    }

    protected static void endLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

    protected static VertexConsumer beginPlanes(MultiBufferSource multiBufferSource) {
        return multiBufferSource.getBuffer(BuildRenderTypes.planes());
    }

    protected static void endPlanes(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

    protected static void renderBlockPreview(PoseStack poseStack, MultiBufferSource.BufferSource renderTypeBuffer, BlockRenderDispatcher dispatcher,
                                             BlockPos blockPos, BlockState blockState, float dissolve, BlockPos firstPos, BlockPos secondPos, boolean red) {
        if (blockState == null) return;

        poseStack.pushPose();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(blockPos.getX() - camera.x, blockPos.getY() - camera.y, blockPos.getZ() - camera.z);
//        poseStack.rotate(Vector3f.YP.rotationDegrees(-90f));
        poseStack.translate(-1 / 256f, -1 / 256f, -1 / 256f);
        poseStack.scale(129 / 128f, 129 / 128f, 129 / 128f);

        //Begin block preview rendering
        RenderType blockPreviewRenderType = BuildRenderTypes.getBlockPreviewRenderType(dissolve, blockPos, firstPos, secondPos, red);
        VertexConsumer buffer = renderTypeBuffer.getBuffer(blockPreviewRenderType);

        try {
            BakedModel model = dispatcher.getBlockModel(blockState);
            // TODO: 8/9/22
//            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer, blockState, model,
//                    1f, 1f, 1f, 0, OverlayTexture.NO_OVERLAY/*, ModelData.EMPTY, blockPreviewRenderType*/);
            dispatcher.getModelRenderer().tesselateBlock(Minecraft.getInstance().level, dispatcher.getBlockModel(blockState), blockState, blockPos, poseStack, buffer, false, new Random(), blockState.getSeed(firstPos), OverlayTexture.NO_OVERLAY);
        } catch (NullPointerException e) {
            Effortless.logger.warn("RenderHandler::renderBlockPreview cannot render " + blockState.getBlock().toString());

            //Render outline as backup, escape out of the current renderstack
            poseStack.popPose();
            renderTypeBuffer.endBatch();
            VertexConsumer lineBuffer = beginLines(renderTypeBuffer);
            renderBlockOutline(poseStack, lineBuffer, blockPos, new Vec3(1f, 1f, 1f));
            endLines(renderTypeBuffer);
            buffer = renderTypeBuffer.getBuffer(Sheets.translucentCullBlockSheet()); //any type will do, as long as we have something on the stack
            poseStack.pushPose();
        }

        renderTypeBuffer.endBatch();
        poseStack.popPose();
    }

    protected static void renderBlockOutline(PoseStack poseStack, VertexConsumer buffer, BlockPos pos, Vec3 color) {
        renderBlockOutline(poseStack, buffer, pos, pos, color);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    protected static void renderBlockOutline(PoseStack poseStack, VertexConsumer buffer, BlockPos pos1, BlockPos pos2, Vec3 color) {
        AABB aabb = new AABB(pos1, pos2.offset(1, 1, 1)).inflate(0.0020000000949949026);

        LevelRenderer.renderLineBox(poseStack, buffer, aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
//        WorldRenderer.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //Renders outline with given bounding box
    protected static void renderBlockOutline(PoseStack poseStack, VertexConsumer buffer, BlockPos pos, VoxelShape collisionShape, Vec3 color) {
//        WorldRenderer.drawShape(collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
        LevelRenderer.renderVoxelShape(poseStack, buffer, collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
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
