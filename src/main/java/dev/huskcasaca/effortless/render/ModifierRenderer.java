package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ModifierRenderer {

    private static final Color COLOR_X = new Color(255, 72, 52);
    private static final Color COLOR_Y = new Color(67, 204, 51);
    private static final Color COLOR_Z = new Color(52, 247, 255);
    private static final Color COLOR_RAD = new Color(52, 247, 255);
    private static final int ALPHA_LINE = 200;
    private static final int ALPHA_PLANE = 50;
    private static final Vec3 EPSILON = new Vec3(0.001, 0.001, 0.001); //prevents z-fighting
    private static final ModifierRenderer INSTANCE = new ModifierRenderer();
    private final Minecraft minecraft;

    private ModifierRenderer() {
        this.minecraft = Minecraft.getInstance();
    }

    public static ModifierRenderer getInstance() {
        return INSTANCE;
    }

    public void render(Player player, PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Camera camera) {
        //Mirror lines and areas
        var mirrorSettings = BuildModifierHelper.getModifierSettings(player).mirrorSettings();

        if (mirrorSettings != null && mirrorSettings.enabled() && (mirrorSettings.mirrorX() || mirrorSettings.mirrorY() || mirrorSettings.mirrorZ())) {
            var pos = mirrorSettings.position().subtract(camera.getPosition());
            renderMirror(poseStack, multiBufferSource, pos, mirrorSettings.radius(), mirrorSettings.getMirrorAxis(), mirrorSettings.drawPlanes(), mirrorSettings.drawLines());
        }

        //Radial mirror lines and areas
        var radialMirrorSettings = BuildModifierHelper.getModifierSettings(player).radialMirrorSettings();
        if (radialMirrorSettings != null && radialMirrorSettings.enabled()) {
            var pos = radialMirrorSettings.position().subtract(camera.getPosition());
            renderRadial(poseStack, multiBufferSource, pos.add(EPSILON), radialMirrorSettings.radius(), radialMirrorSettings.slices(), radialMirrorSettings.drawPlanes(), radialMirrorSettings.drawLines());
        }
    }

    private void drawAxisPlane(PoseStack poseStack, VertexConsumer buffer, Vec3 pos, Integer range, Direction.Axis axis, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();

        var min = pos.subtract(range, range, range);
        var max = pos.add(range, range, range);

        switch (axis) {
            case Y -> {
                buffer.vertex(matrix4f, (float) max.x(), (float) pos.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) min.x(), (float) pos.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) min.x(), (float) pos.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) max.x(), (float) pos.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
            }
            case Z -> {
                buffer.vertex(matrix4f, (float) max.x(), (float) min.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) min.x(), (float) min.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) min.x(), (float) max.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) max.x(), (float) max.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
            }
            case X -> {
                buffer.vertex(matrix4f, (float) pos.x(), (float) min.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) pos.x(), (float) min.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) pos.x(), (float) max.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
                buffer.vertex(matrix4f, (float) pos.x(), (float) max.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
            }
        }
    }

    private void drawAxisLine(PoseStack poseStack, VertexConsumer buffer, Vec3 pos, Integer range, Direction.Axis axis, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();

        var min = pos.subtract(range, range, range);
        var max = pos.add(range, range, range);

        switch (axis) {
            case Y -> {
                buffer.vertex(matrix4f, (float) pos.x, (float) min.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
                buffer.vertex(matrix4f, (float) pos.x, (float) max.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
            }
            case Z -> {
                buffer.vertex(matrix4f, (float) pos.x, (float) pos.y, (float) min.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
                buffer.vertex(matrix4f, (float) pos.x, (float) pos.y, (float) max.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
            }
            case X -> {
                buffer.vertex(matrix4f, (float) min.x, (float) pos.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
                buffer.vertex(matrix4f, (float) max.x, (float) pos.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
            }
        }
    }

    private void drawVerticalPlane(PoseStack poseStack, VertexConsumer buffer, Vec3 posA, Vec3 posB, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();

        var min = posA;
        var max = posB;

        buffer.vertex(matrix4f, (float) min.x(), (float) min.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
        buffer.vertex(matrix4f, (float) max.x(), (float) min.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
        buffer.vertex(matrix4f, (float) max.x(), (float) max.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
        buffer.vertex(matrix4f, (float) min.x(), (float) max.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_PLANE).endVertex();
    }

    private void drawLine(PoseStack poseStack, VertexConsumer buffer, Vec3 posA, Vec3 posB, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        buffer.vertex(matrix4f, (float) posA.x, (float) posA.y, (float) posA.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
        buffer.vertex(matrix4f, (float) posB.x, (float) posB.y, (float) posB.z).color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_LINE).normal(1F, 1F, 1F).endVertex();
    }

    private void renderMirror(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Vec3 pos, Integer radius, List<Direction.Axis> axis, boolean drawPlanes, boolean drawLines) {
        for (Direction.Axis a : axis) {
            var color = switch (a) {
                case X -> COLOR_X;
                case Y -> COLOR_Y;
                case Z -> COLOR_Z;
            };
            if (drawPlanes) {
                VertexConsumer buffer = RenderUtils.beginPlanes(multiBufferSource);
                drawAxisPlane(poseStack, buffer, pos, radius, a, color);
                multiBufferSource.endBatch();
            }
            if (drawLines) {
                VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
                for (Direction.Axis a1 : Direction.Axis.values()) {
                    if (a1 != a) {
                        drawAxisLine(poseStack, buffer, pos, radius, a1, color);
                    }
                }
                multiBufferSource.endBatch();
            }
        }
    }

    private void renderRadial(PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, Vec3 pos, Integer radius, Integer slices, boolean drawPlanes, boolean drawLines) {

        float angle = 2f * ((float) Math.PI) / slices;
        var relStartVec = new Vec3(radius, 0, 0);
        if (slices % 4 == 2) relStartVec = relStartVec.yRot(angle / 2f);

        for (int i = 0; i < slices; i++) {
            var relNewVec = relStartVec.yRot(angle * i);
            var newVec = pos.add(relNewVec);

            var posA = new Vec3(pos.x, pos.y - radius, pos.z);
            var posB = new Vec3(newVec.x, pos.y + radius, newVec.z);

            if (drawPlanes) {
                VertexConsumer buffer = RenderUtils.beginPlanes(multiBufferSource);
                drawVerticalPlane(poseStack, buffer, posA, posB, COLOR_RAD);
                multiBufferSource.endBatch();
            }
            if (drawLines) {
                VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
                drawLine(poseStack, buffer, new Vec3(posA.x(), pos.y(), posA.z()), new Vec3(posB.x(), pos.y(), posB.z()), COLOR_RAD);
                multiBufferSource.endBatch();
            }
        }

        if (drawLines) {
            VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
            drawLine(poseStack, buffer, new Vec3(pos.x(), pos.y() - radius, pos.z()), new Vec3(pos.x(), pos.y() + radius, pos.z()), COLOR_RAD);
            multiBufferSource.endBatch();
        }
    }

}

