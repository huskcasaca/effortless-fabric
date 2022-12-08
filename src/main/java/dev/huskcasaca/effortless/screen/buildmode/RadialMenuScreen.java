package dev.huskcasaca.effortless.screen.buildmode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector4f;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessClient;
import dev.huskcasaca.effortless.buildmode.*;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.mixin.KeyMappingAccessor;
import dev.huskcasaca.effortless.network.ModeActionMessage;
import dev.huskcasaca.effortless.network.ModeSettingsMessage;
import dev.huskcasaca.effortless.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

import static dev.huskcasaca.effortless.buildmode.BuildActionHandler.*;

/**
 * Initially from Chisels and Bits by AlgorithmX2
 * https://github.com/AlgorithmX2/Chisels-and-Bits/blob/1.12/src/main/java/mod/chiselsandbits/client/gui/ChiselsAndBitsMenu.java
 */

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RadialMenuScreen extends Screen {

    public static final RadialMenuScreen instance = new RadialMenuScreen();
    private final Vector4f radialButtonColor = new Vector4f(0f, 0f, 0f, .5f);
    private final Vector4f sideButtonColor = new Vector4f(.5f, .5f, .5f, .5f);
    private final Vector4f highlightColor = new Vector4f(.6f, .8f, 1f, .6f);
    private final Vector4f selectedColor = new Vector4f(0f, .5f, 1f, .5f);
    private final Vector4f highlightSelectedColor = new Vector4f(0.2f, .7f, 1f, .7f);
    private final int whiteTextColor = 0xffffffff;
    private final int watermarkTextColor = 0x88888888;
    private final int descriptionTextColor = 0xdd888888;
    private final int optionTextColor = 0xeeeeeeff;
    private final double ringInnerEdge = 38;
    private final double ringOuterEdge = 75;
    private final double categoryLineOuterEdge = 42;
    private final double textDistance = 90;
    private final double buttonDistance = 120;
    private final float fadeSpeed = 0.3f;
    private final int descriptionHeight = 100;
    public BuildMode switchTo = null;
    public BuildAction doAction = null;
    public boolean performedActionUsingMouse;

    private float visibility;

    private BuildAction lastAction = null;

    public RadialMenuScreen() {
        super(new TranslatableComponent("effortless.screen.radial_menu"));
    }

    public static void playRadialMenuSound() {
        final float volume = 0.1f;
        if (volume >= 0.0001f) {
            SimpleSoundInstance sound = new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, volume, 1.0f, Minecraft.getInstance().player.blockPosition());
            Minecraft.getInstance().getSoundManager().play(sound);
        }
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

        if (!EffortlessClient.isKeybindDown(2)) {
            onClose();
        }
    }

    @Override
    public void render(PoseStack ms, final int mouseX, final int mouseY, final float partialTicks) {
        BuildMode currentBuildMode = ModeSettingsManager.getModeSettings(minecraft.player).buildMode();

        ms.pushPose();
//        ms.translate(0, 0, 200);

        visibility += fadeSpeed * partialTicks;
        if (visibility > 1f) visibility = 1f;

//        final int startColor = ((int) (visibility * 98) << 24) + 0x282828
//        final int endColor = ((int) (visibility * 128) << 24) + 0x282828;
//        fillGradient(ms, 0, 0, width, height, startColor, endColor);
        fill(ms, 0, 0, width, height, ((int) (visibility * 128) << 24) + 0x212121);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        final var tessellator = Tesselator.getInstance();
        final var buffer = tessellator.getBuilder();

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

        final ArrayList<MenuRegion> modes = new ArrayList<>();
        final ArrayList<MenuButton> buttons = new ArrayList<>();

        //Add build modes
        for (final BuildMode mode : BuildMode.values()) {
            modes.add(new MenuRegion(mode));
        }

//        -26 -13    0 -13
//        -26  13    0  13
//        -26  39    0  39

        //Add actions

        int baseY = -13;
        int buttonOffset = 26;

        buttons.add(new MenuButton(BuildAction.UNDO,     -buttonDistance - buttonOffset * 1, baseY + buttonOffset * 0, Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REDO,     -buttonDistance - buttonOffset * 0, baseY + buttonOffset * 0, Direction.EAST));
        buttons.add(new MenuButton(BuildAction.MAGNET,   -buttonDistance - buttonOffset * 1, baseY + buttonOffset * 1, Direction.WEST));
        buttons.add(new MenuButton(BuildAction.REPLACE,  -buttonDistance - buttonOffset * 0, baseY + buttonOffset * 1, Direction.EAST));
        buttons.add(new MenuButton(BuildAction.MODIFIER, -buttonDistance - buttonOffset * 1, baseY + buttonOffset * 2, Direction.WEST));
        buttons.add(new MenuButton(BuildAction.SETTINGS, -buttonDistance - buttonOffset * 0, baseY + buttonOffset * 2, Direction.EAST));
//        OPEN_PLAYER_SETTINGS

        //Add buildmode dependent options
        BuildOption[] options = currentBuildMode.options;
        BuildOption[] enabledOptions = options;
        final ArrayList<MenuButton> optionButtons = new ArrayList<>();
        for (int row = 0; row < options.length; row++) {
            for (int col = 0; col < options[row].getActions().length; col++) {
                BuildAction action = options[row].getActions()[col];
                MenuButton button = new MenuButton(action, buttonDistance + col * 26, -13 + row * 39, Direction.DOWN);
                buttons.add(button);
                optionButtons.add(button);
            }
            if (isMouseInButtonGroup(optionButtons, mouseXCenter, mouseYCenter)) {
                enabledOptions = new BuildOption[row + 1];
                System.arraycopy(options, 0, enabledOptions, 0, row + 1);
                break;
            }
        }

        switchTo = null;
        doAction = null;

        //Draw buildmode backgrounds
        drawRadialButtonBackgrounds(currentBuildMode, buffer, middleX, middleY, mouseXCenter, mouseYCenter, mouseRadians, ringInnerEdge, ringOuterEdge, quarterCircle, modes);

        //Draw action backgrounds
        drawSideButtonBackgrounds(buffer, middleX, middleY, mouseXCenter, mouseYCenter, buttons);

        tessellator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        drawIcons(ms, tessellator, buffer, middleX, middleY, ringInnerEdge, ringOuterEdge, modes, buttons);

        drawTexts(ms, currentBuildMode, middleX, middleY, textDistance, buttonDistance, modes, buttons, enabledOptions);

        ms.popPose();
    }

    private boolean isButtonHighlighted(MenuButton btn, double mouseXCenter, double mouseYCenter) {
        return btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;
    }

    private boolean isMouseInButtonGroup(ArrayList<MenuButton> btns, double mouseXCenter, double mouseYCenter) {
        if (btns.isEmpty()) return false;
        return btns.stream().map(btn -> btn.x1).min(Double::compare).get() <= mouseXCenter && btns.stream().map(btn -> btn.x2).max(Double::compare).get() >= mouseXCenter
                && btns.stream().map(btn -> btn.y1).min(Double::compare).get() <= mouseYCenter && btns.stream().map(btn -> btn.y2).max(Double::compare).get() >= mouseYCenter;
    }

    private void drawRadialButtonBackgrounds(BuildMode currentBuildMode, BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, double mouseRadians, double ringInnerEdge, double ringOuterEdge, double quarterCircle, ArrayList<MenuRegion> modes) {
        if (!modes.isEmpty()) {
            final int totalModes = Math.max(3, modes.size());
            final double fragment = Math.PI * 0.005; //gap between buttons in radians at inner edge
            final double fragment2 = Math.PI * 0.0025; //gap between buttons in radians at outer edge
            final double radiansPerObject = 2.0 * Math.PI / totalModes;

            for (int i = 0; i < modes.size(); i++) {
                MenuRegion menuRegion = modes.get(i);
                final double beginRadians = i * radiansPerObject - quarterCircle;
                final double endRadians = (i + 1) * radiansPerObject - quarterCircle;

                menuRegion.x1 = Math.cos(beginRadians);
                menuRegion.x2 = Math.cos(endRadians);
                menuRegion.y1 = Math.sin(beginRadians);
                menuRegion.y2 = Math.sin(endRadians);

                final double x1m1 = Math.cos(beginRadians + fragment) * ringInnerEdge;
                final double x2m1 = Math.cos(endRadians - fragment) * ringInnerEdge;
                final double y1m1 = Math.sin(beginRadians + fragment) * ringInnerEdge;
                final double y2m1 = Math.sin(endRadians - fragment) * ringInnerEdge;

                final double x1m2 = Math.cos(beginRadians + fragment2) * ringOuterEdge;
                final double x2m2 = Math.cos(endRadians - fragment2) * ringOuterEdge;
                final double y1m2 = Math.sin(beginRadians + fragment2) * ringOuterEdge;
                final double y2m2 = Math.sin(endRadians - fragment2) * ringOuterEdge;

                final boolean isSelected = currentBuildMode.ordinal() == i;
                final boolean isMouseInQuad = inTriangle(x1m1, y1m1, x2m2, y2m2, x2m1, y2m1, mouseXCenter, mouseYCenter)
                        || inTriangle(x1m1, y1m1, x1m2, y1m2, x2m2, y2m2, mouseXCenter, mouseYCenter);
                final boolean isHighlighted = beginRadians <= mouseRadians && mouseRadians <= endRadians && isMouseInQuad;

                Vector4f color = radialButtonColor;
                if (isSelected) color = selectedColor;
                if (isHighlighted) color = highlightColor;
                if (isSelected && isHighlighted) color = highlightSelectedColor;

                if (isHighlighted) {
                    menuRegion.highlighted = true;
                    switchTo = menuRegion.mode;
                }

                buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
                buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
                buffer.vertex(middleX + x2m2, middleY + y2m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
                buffer.vertex(middleX + x1m2, middleY + y1m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();

                //Category line
                color = menuRegion.mode.category.color;

                final double x1m3 = Math.cos(beginRadians + fragment) * categoryLineOuterEdge;
                final double x2m3 = Math.cos(endRadians - fragment) * categoryLineOuterEdge;
                final double y1m3 = Math.sin(beginRadians + fragment) * categoryLineOuterEdge;
                final double y2m3 = Math.sin(endRadians - fragment) * categoryLineOuterEdge;

                buffer.vertex(middleX + x1m1, middleY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
                buffer.vertex(middleX + x2m1, middleY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
                buffer.vertex(middleX + x2m3, middleY + y2m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
                buffer.vertex(middleX + x1m3, middleY + y1m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            }
        }
    }

    private void drawSideButtonBackgrounds(BufferBuilder buffer, double middleX, double middleY, double mouseXCenter, double mouseYCenter, ArrayList<MenuButton> buttons) {
        for (final MenuButton btn : buttons) {

            final boolean isSelected =
                    btn.action == getBuildSpeed() ||
                            btn.action == getFill() ||
                            btn.action == getCubeFill() ||
                            btn.action == getRaisedEdge() ||
                            btn.action == getLineThickness() ||
                            btn.action == getCircleStart();

            final boolean isHighlighted = btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter;

            Vector4f color = sideButtonColor;
            if (isSelected) color = selectedColor;
            if (isHighlighted) color = highlightColor;
            if (isSelected && isHighlighted) color = highlightSelectedColor;

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

    private void drawIcons(PoseStack ms, Tesselator tessellator, BufferBuilder buffer, double middleX, double middleY, double ringInnerEdge, double ringOuterEdge, ArrayList<MenuRegion> modes, ArrayList<MenuButton> buttons) {
        ms.pushPose();
        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        //Draw buildmode icons
        for (final MenuRegion menuRegion : modes) {

            final double x = (menuRegion.x1 + menuRegion.x2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);
            final double y = (menuRegion.y1 + menuRegion.y2) * 0.5 * (ringOuterEdge * 0.55 + 0.45 * ringInnerEdge);

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/mode/" + menuRegion.mode.name().toLowerCase() + ".png"));
            blit(ms, (int) (middleX + x - 8), (int) (middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        //Draw action icons
        for (final MenuButton button : buttons) {

            final double x = (button.x1 + button.x2) / 2 + 0.01;
            final double y = (button.y1 + button.y2) / 2 + 0.01;

            RenderSystem.setShaderTexture(0, new ResourceLocation(Effortless.MOD_ID, "textures/action/" + button.action.name().toLowerCase() + ".png"));
            blit(ms, (int) (middleX + x - 8), (int) (middleY + y - 8), 16, 16, 0, 0, 18, 18, 18, 18);
        }

        ms.popPose();
    }

    private void drawTexts(PoseStack ms, BuildMode currentBuildMode, double middleX, double middleY, double textDistance, double buttonDistance, ArrayList<MenuRegion> modes, ArrayList<MenuButton> buttons, BuildOption[] options) {
        //font.drawStringWithShadow("Actions", (int) (middleX - buttonDistance - 13) - font.getStringWidth("Actions") * 0.5f, (int) middleY - 38, 0xffffffff);

        //Draw option strings
        for (int i = 0; i < options.length; i++) {
            BuildOption option = options[i];
            font.drawShadow(ms, I18n.get(option.getNameKey()), (int) (middleX + buttonDistance - 9), (int) middleY - 37 + i * 39, optionTextColor);
        }

        String credits = I18n.get("effortless.credits");
        font.drawShadow(ms, credits, width - font.width(credits) - 4, height - 10, watermarkTextColor);

        //Draw buildmode text
        for (final MenuRegion menuRegion : modes) {

            if (menuRegion.highlighted) {
                final double x = (menuRegion.x1 + menuRegion.x2) * 0.5;
                final double y = (menuRegion.y1 + menuRegion.y2) * 0.5;

                int fixed_x = (int) (x * textDistance);
                int fixed_y = (int) (y * textDistance) - font.lineHeight / 2;
                String text = I18n.get(menuRegion.mode.getNameKey());

                if (x <= -0.2) {
                    fixed_x -= font.width(text);
                } else if (-0.2 <= x && x <= 0.2) {
                    fixed_x -= font.width(text) / 2;
                }

                font.drawShadow(ms, text, (int) middleX + fixed_x, (int) middleY + fixed_y, whiteTextColor);

                //Draw description
                text = I18n.get(menuRegion.mode.getDescriptionKey());
                font.drawShadow(ms, text, (int) middleX - font.width(text) / 2f, (int) middleY + descriptionHeight, descriptionTextColor);
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

                if (button.textSide == Direction.WEST) {

                    font.draw(ms, text, (int) (middleX + button.x1 - 8) - font.width(text),
                            (int) (middleY + button.y1 + 6), whiteTextColor);

                } else if (button.textSide == Direction.EAST) {

                    font.draw(ms, text, (int) (middleX + button.x2 + 8),
                            (int) (middleY + button.y1 + 6), whiteTextColor);

                } else if (button.textSide == Direction.UP || button.textSide == Direction.NORTH) {

                    font.draw(ms, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5),
                            (int) (middleY + button.y1 - 26), whiteTextColor);

                    font.draw(ms, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5),
                            (int) (middleY + button.y1 - 14), whiteTextColor);

                } else if (button.textSide == Direction.DOWN || button.textSide == Direction.SOUTH) {

                    font.draw(ms, text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(text) * 0.5),
                            (int) (middleY + button.y1 + 26), whiteTextColor);

                    font.draw(ms, keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.width(keybindFormatted) * 0.5),
                            (int) (middleY + button.y1 + 38), whiteTextColor);

                }

            }
        }
    }

    private String findKeybind(MenuButton button, BuildMode currentBuildMode) {
        String result = "";
        int keybindingIndex = -1;
        if (button.action == BuildAction.UNDO) keybindingIndex = 3;
        if (button.action == BuildAction.REDO) keybindingIndex = 4;
        if (button.action == BuildAction.REPLACE) keybindingIndex = 1;
        if (button.action == BuildAction.MODIFIER) keybindingIndex = 0;

        if (keybindingIndex != -1) {
            KeyMapping keyMap = EffortlessClient.keyBindings[keybindingIndex];

//			if (!keyMap.getKeyModifier().name().equals("none")) {
//				result = keyMap.getKeyModifier().name() + " ";
//			}
            result += I18n.get(((KeyMappingAccessor) keyMap).getKey().getName());
        }

//        if (currentBuildMode.options.length > 0) {
//            //Add (ctrl) to first two actions of first option
//            if (button.action == currentBuildMode.options[0].actions[0]
//                    || button.action == currentBuildMode.options[0].actions[1]) {
//                result = I18n.get(((KeyMappingAccessor) EffortlessClient.keyBindings[5]).getKey().getName());
//                if (result.equals("Left Control")) result = "Ctrl";
//            }
//        }
        return result;
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        performAction(true);

        return super.mouseClicked(mouseX, mouseY, mouseButton);
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
                BuildMode mode = ModeSettingsManager.getModeSettings(player).buildMode();
                if (mode == BuildMode.DISABLE) {
                    Effortless.log(player, ModeSettingsManager.getTranslatedModeOptionName(player), true);
                } else {
                    Effortless.log(player, ChatFormatting.GOLD + ModeSettingsManager.getTranslatedModeOptionName(player) + ChatFormatting.RESET, true);
                }
            } else {
                var modeSettings = ModeSettingsManager.getModeSettings(player);
                var modifierSettings = ModifierSettingsManager.getModifierSettings(player);
                switch (lastAction) {
                    case UNDO -> {
                        Effortless.log(player, "Undo", true);
                    }
                    case REDO -> {
                        Effortless.log(player, "Redo", true);
                    }
                    case REPLACE -> {
                        Effortless.log(player, ChatFormatting.GOLD + "Replace " + ChatFormatting.RESET + (
                                modifierSettings.quickReplace() ? (ChatFormatting.GREEN + "ON") : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void performAction(boolean fromMouseClick) {
        var player = Minecraft.getInstance().player;

        var modeSettings = ModeSettingsManager.getModeSettings(player);

        if (switchTo != null) {
            playRadialMenuSound();

            lastAction = null;
            modeSettings = new ModeSettingsManager.ModeSettings(switchTo, modeSettings.enableMagnet());
            ModeSettingsManager.setModeSettings(player, modeSettings);
            if (player != null) {
                BuildModeHandler.initializeMode(player);
            }
            PacketHandler.sendToServer(new ModeSettingsMessage(modeSettings));

            if (fromMouseClick) performedActionUsingMouse = true;
        }

        //Perform button action
        BuildAction action = doAction;
        if (action != null) {
            playRadialMenuSound();
            lastAction = action;

            BuildActionHandler.performAction(player, action);
            PacketHandler.sendToServer(new ModeActionMessage(action));

            if (fromMouseClick) performedActionUsingMouse = true;
        }
    }

    private static class MenuButton {

        public final BuildAction action;
        public double x1, x2;
        public double y1, y2;
        public boolean highlighted;
        public String name;
        public Direction textSide;

        public MenuButton(final BuildAction action, final double x, final double y,
                          final Direction textSide) {
            this.name = I18n.get(action.getNameKey());
            this.action = action;
            x1 = x - 10;
            x2 = x + 10;
            y1 = y - 10;
            y2 = y + 10;
            this.textSide = textSide;
        }

    }

    static class MenuRegion {

        public final BuildMode mode;
        public double x1, x2;
        public double y1, y2;
        public boolean highlighted;

        public MenuRegion(final BuildMode mode) {
            this.mode = mode;
        }

    }

}

