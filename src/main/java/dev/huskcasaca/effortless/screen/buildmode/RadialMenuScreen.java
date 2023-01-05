package dev.huskcasaca.effortless.screen.buildmode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import net.minecraft.client.sounds.SoundManager;
import com.mojang.math.Vector4f;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmode.*;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.control.Keys;
import dev.huskcasaca.effortless.entity.player.ModeSettings;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerBuildActionPacket;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerSetBuildModePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;

import static dev.huskcasaca.effortless.building.BuildActionHandler.*;

/**
 * Initially from Chisels and Bits by AlgorithmX2
 * https://github.com/AlgorithmX2/Chisels-and-Bits/blob/1.12/src/main/java/mod/chiselsandbits/client/gui/ChiselsAndBitsMenu.java
 */
@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RadialMenuScreen extends Screen {

    private static final RadialMenuScreen INSTANCE = new RadialMenuScreen();
    private static final Vector4f RADIAL_BUTTON_COLOR = new Vector4f(0f, 0f, 0f, .5f);
    private static final Vector4f SIDE_BUTTON_COLOR = new Vector4f(.5f, .5f, .5f, .5f);
    private static final Vector4f HIGHLIGHT_COLOR = new Vector4f(.6f, .8f, 1f, .6f);
    private static final Vector4f SELECTED_COLOR = new Vector4f(0f, .5f, 1f, .5f);
    private static final Vector4f HIGHLIGHT_SELECTED_COLOR = new Vector4f(0.2f, .7f, 1f, .7f);
    private static final int WHITE_TEXT_COLOR = 0xffffffff;
    private static final int WATERMARK_TEXT_COLOR = 0x88888888;
    private static final int DESCRIPTION_TEXT_COLOR = 0xdd888888;
    private static final int OPTION_TEXT_COLOR = 0xeeeeeeff;
    private static final double RING_INNER_EDGE = 38;
    private static final double RING_OUTER_EDGE = 80;
    private static final double CATEGORY_LINE_OUTER_EDGE = 42;
    private static final double TEXT_DISTANCE = 90;
    private static final double BUTTON_DISTANCE = 120;
    private static final float FADE_SPEED = 0.5f;
    private static final int DESCRIPTION_HEIGHT = 100;

    public static final int MODE_OPTION_ROW_HEIGHT = 39;

    public BuildMode switchTo = null;
    public BuildAction doAction = null;
    public boolean performedActionUsingMouse;

    private float visibility;

    private BuildAction lastAction = null;

    public RadialMenuScreen() {
        super(Component.translatable(String.join(".", Effortless.MOD_ID, "screen", "radial_menu")));
    }

    public static RadialMenuScreen getInstance() {
        return INSTANCE;
    }

    public static void playRadialMenuSound() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public boolean isVisible() {
        return Minecraft.getInstance().screen instanceof RadialMenuScreen;
    }

    @Override
    protected void init() {
        super.init();
        performedActionUsingMouse = false;
        visibility = 0f;
    }

    @Override
    public void tick() {
        super.tick();

        if (!Keys.SHOW_RADIAL_MENU.isKeyDown()) {
            onClose();
        }
    }

    @Override
    public void render(PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        visibility += FADE_SPEED * partialTicks;
        if (visibility > 1f) visibility = 1f;
        if (minecraft.level != null) {
            fillGradient(poseStack, 0, 0, this.width, this.height,  (int) (visibility * 0xC0) << 24 | 0x101010, (int) (visibility * 0xD0) << 24 | 0x101010);
        } else {
            this.renderDirtBackground(0);
        }

        BuildMode currentBuildMode = BuildModeHelper.getModeSettings(minecraft.player).buildMode();

        poseStack.pushPose();

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        final var tesselator = Tesselator.getInstance();
        final var buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        final double middleX = width / 2.0;
        final double middleY = height / 2.0;

        //Fix for high def (retina) displays: use custom mouse coordinates
        //Borrowed from GameRenderer::updateCameraAndRender
        int mouseXX = (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
        int mouseYY = (int) (minecraft.mouseHandler.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());

        final double mouseXCenter = mouseXX - middleX;
        final double mouseYCenter = mouseYY - middleY;
        double mouseRadians = Math.atan2(mouseYCenter, mouseXCenter);

        final double quarterCircle = Math.PI / 2.0;

        if (mouseRadians < -quarterCircle) {
            mouseRadians = mouseRadians + Math.PI * 2;
        }

        final ArrayList<ModeRegion> modes = new ArrayList<>();
        final ArrayList<MenuButton> buttons = new ArrayList<>();

        //Add build modes
        for (final BuildMode mode : BuildMode.values()) {
            modes.add(new ModeRegion(mode));
        }

        //Add actions
        int baseY = -13;
        int buttonOffset = 26;

        buttons.add(new MenuButton(BuildAction.UNDO,     -BUTTON_DISTANCE - buttonOffset, baseY + 0,            Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REDO,     -BUTTON_DISTANCE - 0,            baseY + 0,            Direction.EAST));
        buttons.add(new MenuButton(BuildAction.MODIFIER, -BUTTON_DISTANCE - buttonOffset, baseY + buttonOffset, Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REPLACE,  -BUTTON_DISTANCE - 0,            baseY + buttonOffset, Direction.EAST));

        //Add buildmode dependent options
        var options = currentBuildMode.getOptions();
        var optionsTexting = options.clone();
        var optionButtons = new ArrayList<MenuButton>();

        for (int row = 0; row < options.length; row++) {
            var buttonsInRow = new ArrayList<MenuButton>();
            for (int col = 0; col < options[row].getActions().length; col++) {
                var action = options[row].getActions()[col];
                var button = new MenuButton(action, BUTTON_DISTANCE + col * buttonOffset, options.length / -2f * MODE_OPTION_ROW_HEIGHT + 26 + row * MODE_OPTION_ROW_HEIGHT, Direction.DOWN);
                buttons.add(button);
                optionButtons.add(button);
                buttonsInRow.add(button);
            }
            if (isButtonHighlighted(buttonsInRow, mouseXCenter, mouseYCenter) && row + 1 < options.length) {
                optionsTexting[row + 1] = null;
            }
        }

        switchTo = null;
        doAction = null;

        //Draw buildmode backgrounds
        drawRadialButtonBackgrounds(currentBuildMode, buffer, middleX, middleY, mouseXCenter, mouseYCenter, mouseRadians, RING_INNER_EDGE, RING_OUTER_EDGE, quarterCircle, modes);

        //Draw action backgrounds
        drawSideButtonBackgrounds(buffer, middleX, middleY, mouseXCenter, mouseYCenter, buttons);

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        drawIcons(poseStack, tesselator, buffer, middleX, middleY, RING_INNER_EDGE, RING_OUTER_EDGE, modes, buttons);

        drawTexts(poseStack, currentBuildMode, middleX, middleY, TEXT_DISTANCE, BUTTON_DISTANCE, modes, buttons, optionsTexting);

        poseStack.popPose();
    }

    private boolean isButtonHighlighted(MenuButton btn, double mouseXCenter, double mouseYCenter) {
        return btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;
    }
    private boolean isButtonHighlighted(ArrayList<MenuButton> btns, double mouseXCenter, double mouseYCenter) {
        for (var btn : btns) {
            if (isButtonHighlighted(btn, mouseXCenter, mouseYCenter)) {
                return true;
            }
        }
        return false;

    }

    private boolean isMouseInButtonGroup(ArrayList<MenuButton> btns, double mouseXCenter, double mouseYCenter) {
        if (btns.isEmpty()) return false;


        return btns.stream().map(btn -> btn.x1).min(Double::compare).get() <= mouseXCenter && btns.stream().map(btn -> btn.x2).max(Double::compare).get() >= mouseXCenter && btns.stream().map(btn -> btn.y1).min(Double::compare).get() <= mouseYCenter && btns.stream().map(btn -> btn.y2).max(Double::compare).get() >= mouseYCenter;
    }

    private void drawRadialButtonBackgrounds(BuildMode currentBuildMode, BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, double mouseRadians, double ringInnerEdge, double ringOuterEdge, double quarterCircle, ArrayList<ModeRegion> modes) {
        if (modes.isEmpty()) {
            return;
        }
        final int totalModes = Math.max(3, modes.size());
        final double fragment = Math.PI * 0.005; //gap between buttons in radians at inner edge
        final double fragment2 = Math.PI * 0.0025; //gap between buttons in radians at outer edge
        final double radiansPerObject = 2.0 * Math.PI / totalModes;

        for (int i = 0; i < modes.size(); i++) {
            ModeRegion modeRegion = modes.get(i);
            final double beginRadians = (i - 0.5) * radiansPerObject - quarterCircle;
            final double endRadians = (i + 0.5) * radiansPerObject - quarterCircle;

            modeRegion.x1 = Math.cos(beginRadians);
            modeRegion.x2 = Math.cos(endRadians);
            modeRegion.y1 = Math.sin(beginRadians);
            modeRegion.y2 = Math.sin(endRadians);

            final double x1m1 = Math.cos(beginRadians + fragment) * ringInnerEdge;
            final double x2m1 = Math.cos(endRadians - fragment) * ringInnerEdge;
            final double y1m1 = Math.sin(beginRadians + fragment) * ringInnerEdge;
            final double y2m1 = Math.sin(endRadians - fragment) * ringInnerEdge;

            final double x1m2 = Math.cos(beginRadians + fragment2) * ringOuterEdge;
            final double x2m2 = Math.cos(endRadians - fragment2) * ringOuterEdge;
            final double y1m2 = Math.sin(beginRadians + fragment2) * ringOuterEdge;
            final double y2m2 = Math.sin(endRadians - fragment2) * ringOuterEdge;

            final boolean isSelected = currentBuildMode.ordinal() == i;
            final boolean isMouseInQuad = inTriangle(x1m1, y1m1, x2m2, y2m2, x2m1, y2m1, mouseXCenter, mouseYCenter) || inTriangle(x1m1, y1m1, x1m2, y1m2, x2m2, y2m2, mouseXCenter, mouseYCenter);
            final boolean isHighlighted = ((beginRadians <= mouseRadians && mouseRadians <= endRadians) || (beginRadians <= (mouseRadians - 2 * Math.PI) && (mouseRadians - 2 * Math.PI) <= endRadians)) && isMouseInQuad;

            Vector4f color = RADIAL_BUTTON_COLOR;
            if (isSelected) color = SELECTED_COLOR;
            if (isHighlighted) color = HIGHLIGHT_COLOR;
            if (isSelected && isHighlighted) color = HIGHLIGHT_SELECTED_COLOR;

            if (isHighlighted) {
                modeRegion.highlighted = true;
                switchTo = modeRegion.mode;
            }

            buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m2, middleY + y2m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x1m2, middleY + y1m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();

            //Category line
            color = modeRegion.mode.getCategory().getColor();

            final double x1m3 = Math.cos(beginRadians + fragment) * CATEGORY_LINE_OUTER_EDGE;
            final double x2m3 = Math.cos(endRadians - fragment) * CATEGORY_LINE_OUTER_EDGE;
            final double y1m3 = Math.sin(beginRadians + fragment) * CATEGORY_LINE_OUTER_EDGE;
            final double y2m3 = Math.sin(endRadians - fragment) * CATEGORY_LINE_OUTER_EDGE;

            buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x2m3, middleY + y2m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + x1m3, middleY + y1m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
        }
    }

    private void drawSideButtonBackgrounds(BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, ArrayList<MenuButton> buttons) {
        for (final MenuButton btn : buttons) {

            final boolean isSelected = Arrays.stream(getOptions()).toList().contains(btn.action);

            final boolean isHighlighted = btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;

            Vector4f color = SIDE_BUTTON_COLOR;
            if (isSelected) color = SELECTED_COLOR;
            if (isHighlighted) color = HIGHLIGHT_COLOR;
            if (isSelected && isHighlighted) color = HIGHLIGHT_SELECTED_COLOR;

            if (isHighlighted) {
                btn.highlighted = true;
                doAction = btn.action;
            }

            buffer.vertex(middleX + btn.x1, middleY + btn.y1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + btn.x1, middleY + btn.y2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + btn.x2, middleY + btn.y2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(middleX + btn.x2, middleY + btn.y1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
        }
    }

    private void drawIcons(PoseStack poseStack, Tesselator tesselator, BufferBuilder buffer, double middleX, double middleY, double ringInnerEdge, double ringOuterEdge, ArrayList<ModeRegion> modes, ArrayList<MenuButton> buttons) {
        poseStack.pushPose();
        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        //Draw buildmode icons
        for (final ModeRegion modeRegion : modes) {

            final double x = (modeRegion.x1 + modeRegion.x2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);
            final double y = (modeRegion.y1 + modeRegion.y2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/mode/" + modeRegion.mode.name().toLowerCase() + ".png"));
            blit(poseStack, (int) (middleX + x - 8), (int) (middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        //Draw action icons
        for (final MenuButton button : buttons) {

            final double x = (button.x1 + button.x2) / 2 + 0.01;
            final double y = (button.y1 + button.y2) / 2 + 0.01;

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/action/" + button.action.name().toLowerCase() + ".png"));
            blit(poseStack, (int) (middleX + x - 8), (int) (middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        poseStack.popPose();
    }

    private void drawTexts(PoseStack poseStack, BuildMode currentBuildMode, double middleX, double middleY, double textDistance, double buttonDistance, ArrayList<ModeRegion> modes, ArrayList<MenuButton> buttons, BuildMode.Option[] options) {
        //font.drawStringWithShadow("Actions", (int) (middleX - buttonDistance - 13) - font.getStringWidth("Actions") * 0.5f, (int) middleY - 38, 0xffffffff);

        //Draw option strings
        for (int row = 0; row < options.length; row++) {
            BuildMode.Option option = options[row];
            if (option == null) continue;
            font.drawShadow(poseStack, I18n.get(option.getNameKey()), (int) (middleX + buttonDistance - 9), (int) middleY + options.length / -2f * MODE_OPTION_ROW_HEIGHT + 3 + row * MODE_OPTION_ROW_HEIGHT, OPTION_TEXT_COLOR);
        }

        String credits = I18n.get(Effortless.MOD_ID + "."+ "credits");
        font.drawShadow(poseStack, credits, width - font.width(credits) - 10, height - 15, WATERMARK_TEXT_COLOR);

        //Draw buildmode text
        for (final ModeRegion modeRegion : modes) {

            if (modeRegion.highlighted) {
                final double x = (modeRegion.x1 + modeRegion.x2) * 0.5;
                final double y = (modeRegion.y1 + modeRegion.y2) * 0.5;

                int fixed_x = (int) (x * textDistance);
                int fixed_y = (int) (y * textDistance) - font.lineHeight / 2;
                String text = I18n.get(modeRegion.mode.getNameKey());

                if (x <= -0.2) {
                    fixed_x -= font.width(text);
                } else if (-0.2 <= x && x <= 0.2) {
                    fixed_x -= font.width(text) / 2;
                }

                font.drawShadow(poseStack, text, (int) middleX + fixed_x, (int) middleY + fixed_y, WHITE_TEXT_COLOR);

                //Draw description
                text = I18n.get(modeRegion.mode.getDescriptionKey());
                font.drawShadow(poseStack, text, (int) middleX - font.width(text) / 2f, (int) middleY + DESCRIPTION_HEIGHT, DESCRIPTION_TEXT_COLOR);
            }
        }

        //Draw action text
        for (final MenuButton button : buttons) {
            if (button.highlighted) {
                String text = ChatFormatting.AQUA + button.name;

                //Add keybind in brackets
                String keybind = findKeybind(button, currentBuildMode);
                String keybindFormatted = "";
                if (!keybind.isEmpty())
                    keybindFormatted = ChatFormatting.GRAY + "(" + WordUtils.capitalizeFully(keybind) + ")";

                switch (button.textSide) {
                    case WEST -> {
                        font.draw(poseStack, text, (int) (middleX + button.x1 - 8) - font.width(text), (int) (middleY + button.y1 + 6), WHITE_TEXT_COLOR);
                    }
                    case EAST -> {
                        font.draw(poseStack, text, (int) (middleX + button.x2 + 8), (int) (middleY + button.y1 + 6), WHITE_TEXT_COLOR);
                    }
                    case UP, NORTH -> {
                        font.draw(poseStack, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + button.y1 - 26), WHITE_TEXT_COLOR);
                        font.draw(poseStack, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + button.y1 - 14), WHITE_TEXT_COLOR);
                    }
                    case DOWN, SOUTH -> {
                        font.draw(poseStack, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5), (int) (middleY + button.y1 + 26), WHITE_TEXT_COLOR);
                        font.draw(poseStack, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5), (int) (middleY + button.y1 + 38), WHITE_TEXT_COLOR);
                    }
                }

            }
        }
    }

    private String findKeybind(MenuButton button, BuildMode currentBuildMode) {
        Keys keybindingIndex = null;
        if (button.action == BuildAction.REPLACE) keybindingIndex = Keys.TOGGLE_REPLACE;
        if (button.action == BuildAction.MODIFIER) keybindingIndex = Keys.MODIFIER_MENU;

        if (keybindingIndex == null) {
            return "";
        }

        return keybindingIndex.getKeyMapping().key.getName();

//        if (currentBuildMode.options.length > 0) {
//            //Add (ctrl) to first two actions of first option
//            if (button.action == currentBuildMode.options[0].actions[0]
//                    || button.action == currentBuildMode.options[0].actions[1]) {
//                result = I18n.get(((KeyMappingAccessor) EffortlessClient.keyBindings[5]).getKey().getName());
//                if (result.equals("Left Control")) result = "Ctrl";
//            }
//        }
    }

    private boolean inTriangle(final double x1, final double y1, final double x2, final double y2,
                               final double x3, final double y3, final double x, final double y) {
        final double ab = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
        final double bc = (x2 - x) * (y3 - y) - (x3 - x) * (y2 - y);
        final double ca = (x3 - x) * (y1 - y) - (x1 - x) * (y3 - y);
        return sign(ab) == sign(bc) && sign(bc) == sign(ca);
    }

    private int sign(final double n) {
        return n > 0 ? 1 : -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        performAction(true);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        //After onClose so it can open another screen
        if (!performedActionUsingMouse) {
            performAction(false);
        }
        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (lastAction == null) {
                BuildMode mode = BuildModeHelper.getModeSettings(player).buildMode();
                if (mode == BuildMode.DISABLE) {
                    Effortless.log(player, BuildModeHelper.getTranslatedModeOptionName(player), true);
                } else {
                    Effortless.log(player, ChatFormatting.GOLD + BuildModeHelper.getTranslatedModeOptionName(player) + ChatFormatting.RESET, true);
                }
            } else {
                var modeSettings = BuildModeHelper.getModeSettings(player);
                var modifierSettings = BuildModifierHelper.getModifierSettings(player);
                switch (lastAction) {
                    case UNDO -> {
                        Effortless.log(player, "Undo", true);
                    }
                    case REDO -> {
                        Effortless.log(player, "Redo", true);
                    }
                    case REPLACE -> {
                        Effortless.log(player, ChatFormatting.GOLD + "Replace " + ChatFormatting.RESET + (modifierSettings.enableReplace() ? (modifierSettings.enableQuickReplace() ? (ChatFormatting.GREEN + "QUICK") : (ChatFormatting.GREEN + "ON")) : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                    }
                    case MAGNET -> {
                        Effortless.log(player, ChatFormatting.GOLD + "Item Magnet " + ChatFormatting.RESET + (modeSettings.enableMagnet() ? (ChatFormatting.GREEN + "ON") : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                    }
                }
            }
        }
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
//        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void performAction(boolean fromMouseClick) {
        var player = Minecraft.getInstance().player;

        var modeSettings = BuildModeHelper.getModeSettings(player);

        if (switchTo != null) {
            playRadialMenuSound();

            lastAction = null;
            modeSettings = new ModeSettings(switchTo, modeSettings.enableMagnet());
            BuildModeHelper.setModeSettings(player, modeSettings);
            if (player != null) {
                BuildModeHandler.initializeMode(player);
            }
            Packets.sendToServer(new ServerboundPlayerSetBuildModePacket(modeSettings));

            if (fromMouseClick) performedActionUsingMouse = true;
        }

        //Perform button action
        BuildAction action = doAction;
        if (action != null) {
            playRadialMenuSound();
            lastAction = action;

            BuildActionHandler.performAction(player, action);
            Packets.sendToServer(new ServerboundPlayerBuildActionPacket(action));

            if (fromMouseClick) performedActionUsingMouse = true;
        }
    }

    static class MenuButton {

        private final BuildAction action;
        private final String name;
        private final Direction textSide;
        private double x1, x2;
        private double y1, y2;
        private boolean highlighted;

        public MenuButton(final BuildAction action, final double x, final double y,
                          final Direction textSide) {
            this.name = I18n.get(action.getNameKey());
            this.action = action;
            this.x1 = x - 10;
            this.x2 = x + 10;
            this.y1 = y - 10;
            this.y2 = y + 10;
            this.textSide = textSide;
        }

    }

    static class ModeRegion {

        private final BuildMode mode;
        private double x1, x2;
        private double y1, y2;
        private boolean highlighted;

        public ModeRegion(final BuildMode mode) {
            this.mode = mode;
        }

    }

}

