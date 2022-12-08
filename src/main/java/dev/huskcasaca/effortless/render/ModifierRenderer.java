package dev.huskcasaca.effortless.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class ModifierRenderer {

    protected static final Color colorX = new Color(255, 72, 52);
    protected static final Color colorY = new Color(67, 204, 51);
    protected static final Color colorZ = new Color(52, 247, 255);
    protected static final Color colorRadial = new Color(52, 247, 255);
    protected static final int lineAlpha = 200;
    protected static final int planeAlpha = 50;
    protected static final Vec3 epsilon = new Vec3(0.001, 0.001, 0.001); //prevents z-fighting

    public static void render(PoseStack matrixStack, MultiBufferSource.BufferSource renderTypeBuffer, ModifierSettings modifierSettings) {
        //Mirror lines and areas
        var m = modifierSettings.mirrorSettings();
        if (m != null && m.enabled() && (m.mirrorX() || m.mirrorY() || m.mirrorZ())) {
            Vec3 pos = m.position().add(epsilon);
            int radius = m.radius();

            if (m.mirrorX()) {
                Vec3 posA = new Vec3(pos.x, pos.y - radius, pos.z - radius);
                Vec3 posB = new Vec3(pos.x, pos.y + radius, pos.z + radius);

                drawMirrorPlane(matrixStack, renderTypeBuffer, posA, posB, colorX, m.drawLines(), m.drawPlanes(), true);
            }
            if (m.mirrorY()) {
                Vec3 posA = new Vec3(pos.x - radius, pos.y, pos.z - radius);
                Vec3 posB = new Vec3(pos.x + radius, pos.y, pos.z + radius);

                drawMirrorPlaneY(matrixStack, renderTypeBuffer, posA, posB, colorY, m.drawLines(), m.drawPlanes());
            }
            if (m.mirrorZ()) {
                Vec3 posA = new Vec3(pos.x - radius, pos.y - radius, pos.z);
                Vec3 posB = new Vec3(pos.x + radius, pos.y + radius, pos.z);

                drawMirrorPlane(matrixStack, renderTypeBuffer, posA, posB, colorZ, m.drawLines(), m.drawPlanes(), true);
            }

            //Draw axis coordinated colors if two or more axes are enabled
            //(If only one is enabled the lines are that planes color)
            if (m.drawLines() && ((m.mirrorX() && m.mirrorY()) || (m.mirrorX() && m.mirrorZ()) || (m.mirrorY() && m.mirrorZ()))) {
                drawMirrorLines(matrixStack, renderTypeBuffer, m);
            }
        }

        //Radial mirror lines and areas
        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
        if (radialMirrorSettings != null && radialMirrorSettings.enabled()) {
            Vec3 pos = radialMirrorSettings.position().add(epsilon);
            int radius = radialMirrorSettings.radius();

            float angle = 2f * ((float) Math.PI) / radialMirrorSettings.slices();
            Vec3 relStartVec = new Vec3(radius, 0, 0);
            if (radialMirrorSettings.slices() % 4 == 2) relStartVec = relStartVec.yRot(angle / 2f);

            for (int i = 0; i < radialMirrorSettings.slices(); i++) {
                Vec3 relNewVec = relStartVec.yRot(angle * i);
                Vec3 newVec = pos.add(relNewVec);

                Vec3 posA = new Vec3(pos.x, pos.y - radius, pos.z);
                Vec3 posB = new Vec3(newVec.x, pos.y + radius, newVec.z);
                drawMirrorPlane(matrixStack, renderTypeBuffer, posA, posB, colorRadial, radialMirrorSettings.drawLines(), radialMirrorSettings.drawPlanes(), false);
            }
        }
    }


    //----Mirror----

    protected static void drawMirrorPlane(PoseStack matrixStack, MultiBufferSource.BufferSource renderTypeBuffer, Vec3 posA, Vec3 posB, Color c, boolean drawLines, boolean drawPlanes, boolean drawVerticalLines) {

//        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha);
        Matrix4f matrixPos = matrixStack.last().pose();

        if (drawPlanes) {
            VertexConsumer buffer = RenderHandler.beginPlanes(renderTypeBuffer);

            buffer.vertex(matrixPos, (float) posA.x, (float) posA.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posA.x, (float) posB.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posB.x, (float) posA.y, (float) posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posB.x, (float) posB.y, (float) posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            //backface (using triangle strip)
            buffer.vertex(matrixPos, (float) posA.x, (float) posA.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posA.x, (float) posB.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();

            RenderHandler.endPlanes(renderTypeBuffer);
        }

        if (drawLines) {
            VertexConsumer buffer = RenderHandler.beginLines(renderTypeBuffer);

            Vec3 middle = posA.add(posB).scale(0.5);
            buffer.vertex(matrixPos, (float) posA.x, (float) middle.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posB.x, (float) middle.y, (float) posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            if (drawVerticalLines) {
                buffer.vertex(matrixPos, (float) middle.x, (float) posA.y, (float) middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
                buffer.vertex(matrixPos, (float) middle.x, (float) posB.y, (float) middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            }

            RenderHandler.endLines(renderTypeBuffer);
        }
    }

    protected static void drawMirrorPlaneY(PoseStack matrixStack, MultiBufferSource.BufferSource renderTypeBuffer, Vec3 posA, Vec3 posB, Color c, boolean drawLines, boolean drawPlanes) {

//        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        Matrix4f matrixPos = matrixStack.last().pose();

        if (drawPlanes) {
            VertexConsumer buffer = RenderHandler.beginPlanes(renderTypeBuffer);

            buffer.vertex(matrixPos, (float) posA.x, (float) posA.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posA.x, (float) posA.y, (float) posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posB.x, (float) posA.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posB.x, (float) posA.y, (float) posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            //backface (using triangle strip)
            buffer.vertex(matrixPos, (float) posA.x, (float) posA.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posA.x, (float) posA.y, (float) posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();

            RenderHandler.endPlanes(renderTypeBuffer);
        }

        if (drawLines) {
            VertexConsumer buffer = RenderHandler.beginLines(renderTypeBuffer);

            Vec3 middle = posA.add(posB).scale(0.5);
            buffer.vertex(matrixPos, (float) middle.x, (float) middle.y, (float) posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            buffer.vertex(matrixPos, (float) middle.x, (float) middle.y, (float) posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posA.x, (float) middle.y, (float) middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            buffer.vertex(matrixPos, (float) posB.x, (float) middle.y, (float) middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();

            RenderHandler.endLines(renderTypeBuffer);
        }
    }

    protected static void drawMirrorLines(PoseStack matrixStack, MultiBufferSource.BufferSource renderTypeBuffer, Mirror.MirrorSettings m) {

//        GL11.glColor4d(100, 100, 100, 255);
        VertexConsumer buffer = RenderHandler.beginLines(renderTypeBuffer);
        Matrix4f matrixPos = matrixStack.last().pose();

        Vec3 pos = m.position().add(epsilon);

        buffer.vertex(matrixPos, (float) pos.x - m.radius(), (float) pos.y, (float) pos.z).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();
        buffer.vertex(matrixPos, (float) pos.x + m.radius(), (float) pos.y, (float) pos.z).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();
        buffer.vertex(matrixPos, (float) pos.x, (float) pos.y - m.radius(), (float) pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        buffer.vertex(matrixPos, (float) pos.x, (float) pos.y + m.radius(), (float) pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        buffer.vertex(matrixPos, (float) pos.x, (float) pos.y, (float) pos.z - m.radius()).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();
        buffer.vertex(matrixPos, (float) pos.x, (float) pos.y, (float) pos.z + m.radius()).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();

        RenderHandler.endLines(renderTypeBuffer);
    }
}
