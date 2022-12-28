package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class RenderUtils {

    private static final Random RAND = new Random();

    private static final Vec3 ERROR_OUTLINE_COLOR = new Vec3(1, 0, 0);
    private static final Vec3 DEFAULT_OUTLINE_COLOR = new Vec3(1, 1, 1);

    public static VertexConsumer beginLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        return renderTypeBuffer.getBuffer(BuildRenderType.lines());
    }

    public static void endLines(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

    public static VertexConsumer beginPlanes(MultiBufferSource multiBufferSource) {
        return multiBufferSource.getBuffer(BuildRenderType.planes());
    }

    public static void endPlanes(MultiBufferSource.BufferSource renderTypeBuffer) {
        renderTypeBuffer.endBatch();
    }

    public static void renderBlockDissolveShader(PoseStack poseStack, MultiBufferSource.BufferSource renderTypeBuffer, BlockRenderDispatcher dispatcher, BlockPos blockPos, BlockState blockState, float dissolve, BlockPos firstPos, BlockPos secondPos, boolean red) {
        if (blockState == null) return;

        poseStack.pushPose();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(blockPos.getX() - camera.x, blockPos.getY() - camera.y, blockPos.getZ() - camera.z);
//        poseStack.rotate(Vector3f.YP.rotationDegrees(-90f));
        poseStack.translate(-1 / 256f, -1 / 256f, -1 / 256f);
        poseStack.scale(129 / 128f, 129 / 128f, 129 / 128f);

        //Begin block preview rendering
        var blockPreviewRenderType = BuildRenderType.getBlockPreviewRenderType(dissolve, blockPos, firstPos, secondPos, red);
        var buffer = renderTypeBuffer.getBuffer(blockPreviewRenderType);

        var level = Minecraft.getInstance().level;
        var model = dispatcher.getBlockModel(blockState);
        var seed = blockState.getSeed(firstPos);
        // TODO: 8/9/22
        dispatcher.getModelRenderer().tesselateBlock(level, model, blockState, blockPos, poseStack, buffer, false, RAND, seed, OverlayTexture.NO_OVERLAY);

        renderTypeBuffer.endBatch();
        poseStack.popPose();
    }

    public static void renderBlockOutlines(PoseStack poseStack, MultiBufferSource.BufferSource renderTypeBuffer, BlockRenderDispatcher dispatcher, BlockPos blockPos, BlockState blockState, float dissolve, BlockPos firstPos, BlockPos secondPos, boolean red) {
        if (blockState == null) return;
        poseStack.pushPose();

        //Begin block preview rendering
        var buffer = renderTypeBuffer.getBuffer(RenderType.lines());

        RenderSystem.lineWidth(2f);

        var voxelShape = blockState.getShape(Minecraft.getInstance().level, blockPos);
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var color = red ? ERROR_OUTLINE_COLOR : DEFAULT_OUTLINE_COLOR;
        renderVoxelShape(poseStack, buffer, voxelShape, blockPos.getX() - camera.x(), blockPos.getY() - camera.y(), blockPos.getZ() - camera.z(), (float) color.x(), (float) color.y(), (float) color.z(), 1f);

        renderTypeBuffer.endBatch();
        poseStack.popPose();
    }

    private static void renderVoxelShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        List<AABB> list = voxelShape.toAabbs();
        for (AABB aabb : list) {
            renderShape(poseStack, vertexConsumer, Shapes.create(aabb.move(0.0, 0.0, 0.0)), d, e, f, g, h, i, j);
        }
    }

    private static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        PoseStack.Pose pose = poseStack.last();
        voxelShape.forAllEdges((k, l, m, n, o, p) -> {
            float q = (float)(n - k);
            float r = (float)(o - l);
            float s = (float)(p - m);
            float t = Mth.sqrt(q * q + r * r + s * s);
            q /= t;
            r /= t;
            s /= t;
            vertexConsumer.vertex(pose.pose(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(pose.normal(), q, r, s).endVertex();
            vertexConsumer.vertex(pose.pose(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(pose.normal(), q, r, s).endVertex();
        });
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
