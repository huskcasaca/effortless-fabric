package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.vertex.*;
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

    private static final Color COLOR_PLANE = new Color(0, 0, 0, 72);
    private static final Color COLOR_LINE = new Color(0, 0, 0, 200);
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
            renderMirror(multiBufferSource, pos, mirrorSettings.radius(), mirrorSettings.getMirrorAxis(), mirrorSettings.drawPlanes(), mirrorSettings.drawLines());
        }

        //Radial mirror lines and areas
        var radialMirrorSettings = BuildModifierHelper.getModifierSettings(player).radialMirrorSettings();
        if (radialMirrorSettings != null && radialMirrorSettings.enabled()) {
            var pos = radialMirrorSettings.position().subtract(camera.getPosition());
            renderRadial(multiBufferSource, pos.add(EPSILON), radialMirrorSettings.radius(), radialMirrorSettings.slices(), radialMirrorSettings.drawPlanes(), radialMirrorSettings.drawLines());
        }
    }

    private void drawAxisPlane(VertexConsumer buffer, Vec3 pos, Integer range, Direction.Axis axis, Color color) {

        var min = pos.subtract(range, range, range);
        var max = pos.add(range, range, range);

        switch (axis) {
            case Y -> {
                buffer.vertex((float) max.x(), (float) pos.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) min.x(), (float) pos.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) min.x(), (float) pos.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) max.x(), (float) pos.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            }
            case Z -> {
                buffer.vertex((float) max.x(), (float) min.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) min.x(), (float) min.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) min.x(), (float) max.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) max.x(), (float) max.y(), (float) pos.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            }
            case X -> {
                buffer.vertex((float) pos.x(), (float) min.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) pos.x(), (float) min.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) pos.x(), (float) max.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
                buffer.vertex((float) pos.x(), (float) max.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
            }
        }
    }

    private void drawAxisLine(VertexConsumer buffer, Vec3 pos, Integer range, Direction.Axis axis, Color color) {
        var min = pos.subtract(range, range, range);
        var max = pos.add(range, range, range);

        switch (axis) {
            case Y -> {
                buffer.vertex((float) pos.x, (float) min.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
                buffer.vertex((float) pos.x, (float) max.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
            }
            case Z -> {
                buffer.vertex((float) pos.x, (float) pos.y, (float) min.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
                buffer.vertex((float) pos.x, (float) pos.y, (float) max.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
            }
            case X -> {
                buffer.vertex((float) min.x, (float) pos.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
                buffer.vertex((float) max.x, (float) pos.y, (float) pos.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
            }
        }
    }

    private void drawVerticalPlane(VertexConsumer buffer, Vec3 posA, Vec3 posB, Color color) {
        var min = posA;
        var max = posB;

        buffer.vertex((float) min.x(), (float) min.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        buffer.vertex((float) max.x(), (float) min.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        buffer.vertex((float) max.x(), (float) max.y(), (float) max.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        buffer.vertex((float) min.x(), (float) max.y(), (float) min.z()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
    }

    private void drawLine(VertexConsumer buffer, Vec3 posA, Vec3 posB, Color color) {
        buffer.vertex((float) posA.x, (float) posA.y, (float) posA.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
        buffer.vertex((float) posB.x, (float) posB.y, (float) posB.z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).normal(1F, 1F, 1F).endVertex();
    }

    private void renderMirror(MultiBufferSource.BufferSource multiBufferSource, Vec3 pos, Integer radius, List<Direction.Axis> axis, boolean drawPlanes, boolean drawLines) {
        if (drawPlanes) {for (Direction.Axis a : axis) {
            VertexConsumer buffer = RenderUtils.beginPlanes(multiBufferSource);
            drawAxisPlane(buffer, pos, radius, a, COLOR_PLANE);
            multiBufferSource.endBatch();
        }
        }
        if (drawLines) {
            for (Direction.Axis a : axis) {
                VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
                for (Direction.Axis a1 : Direction.Axis.values()) {
                    if (a1 != a) {
                        drawAxisLine(buffer, pos, radius, a1, COLOR_LINE);
                    }
                }
                multiBufferSource.endBatch();
            }
        }

    }

    private void renderRadial(MultiBufferSource.BufferSource multiBufferSource, Vec3 pos, Integer radius, Integer slices, boolean drawPlanes, boolean drawLines) {
        float angle = 2f * ((float) Math.PI) / slices;
        var relStartVec = new Vec3(radius, 0, 0);
        if (slices % 4 == 2) relStartVec = relStartVec.yRot(angle / 2f);

        if (drawPlanes) {
            for (int i = 0; i < slices; i++) {
                var relNewVec = relStartVec.yRot(angle * i);
                var newVec = pos.add(relNewVec);

                var posA = new Vec3(pos.x, pos.y - radius, pos.z);
                var posB = new Vec3(newVec.x, pos.y + radius, newVec.z);

                VertexConsumer buffer = RenderUtils.beginPlanes(multiBufferSource);
                drawVerticalPlane(buffer, posA, posB, COLOR_PLANE);
                multiBufferSource.endBatch();
            }
        }
        if (drawLines) {
            for (int i = 0; i < slices; i++) {
                var relNewVec = relStartVec.yRot(angle * i);
                var newVec = pos.add(relNewVec);

                var posA = new Vec3(pos.x, pos.y - radius, pos.z);
                var posB = new Vec3(newVec.x, pos.y + radius, newVec.z);
                VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
                drawLine(buffer, new Vec3(posA.x(), pos.y(), posA.z()), new Vec3(posB.x(), pos.y(), posB.z()), COLOR_LINE);
                multiBufferSource.endBatch();
            }
            VertexConsumer buffer = RenderUtils.beginLines(multiBufferSource);
            drawLine(buffer, new Vec3(pos.x(), pos.y() - radius, pos.z()), new Vec3(pos.x(), pos.y() + radius, pos.z()), COLOR_LINE);
            multiBufferSource.endBatch();
        }
    }

}

