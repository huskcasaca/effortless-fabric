package dev.huskcasaca.effortless.gui.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.gui.widget.*;
import dev.huskcasaca.effortless.helper.ReachHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MirrorSettingsPane extends ExpandableScrollEntry {

    protected static final ResourceLocation BUILDING_ICONS = new ResourceLocation(Effortless.MOD_ID, "textures/gui/building_icons.png");

    protected List<Button> mirrorButtonList = new ArrayList<>();
    protected List<IconButton> mirrorIconButtonList = new ArrayList<>();
    protected List<NumberField> mirrorNumberFieldList = new ArrayList<>();

    private NumberField textMirrorPosX, textMirrorPosY, textMirrorPosZ, textMirrorRadius;
    private FixedCheckbox buttonMirrorEnabled, buttonMirrorX, buttonMirrorY, buttonMirrorZ;
    private IconButton buttonCurrentPosition, buttonToggleOdd, buttonDrawPlanes, buttonDrawLines;
    private boolean drawPlanes, drawLines, toggleOdd;

    public MirrorSettingsPane(ScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public void init(List<Widget> renderables) {
        super.init(renderables);

        int y = top - 2;
        buttonMirrorEnabled = new FixedCheckbox(left - 15 + 8, y, "", false) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                setCollapsed(!buttonMirrorEnabled.isChecked());
            }
        };
        renderables.add(buttonMirrorEnabled);

        y = top + 18;
        textMirrorPosX = new NumberField(font, renderables, left + 58, y, 62, 18);
        textMirrorPosX.setNumber(0);
        textMirrorPosX.setTooltip(
                Arrays.asList(new TextComponent("The position of the mirror."), new TextComponent("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorPosX);

        textMirrorPosY = new NumberField(font, renderables, left + 138, y, 62, 18);
        textMirrorPosY.setNumber(64);
        textMirrorPosY.setTooltip(Arrays.asList(new TextComponent("The position of the mirror."), new TextComponent("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorPosY);

        textMirrorPosZ = new NumberField(font, renderables, left + 218, y, 62, 18);
        textMirrorPosZ.setNumber(0);
        textMirrorPosZ.setTooltip(Arrays.asList(new TextComponent("The position of the mirror."), new TextComponent("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorPosZ);

        y = top + 50;
        buttonMirrorX = new FixedCheckbox(left + 60, y, " X", true);
        mirrorButtonList.add(buttonMirrorX);

        buttonMirrorY = new FixedCheckbox(left + 100, y, " Y", false);
        mirrorButtonList.add(buttonMirrorY);

        buttonMirrorZ = new FixedCheckbox(left + 140, y, " Z", false);
        mirrorButtonList.add(buttonMirrorZ);

        y = top + 47;
        textMirrorRadius = new NumberField(font, renderables, left + 218, y, 62, 18);
        textMirrorRadius.setNumber(50);
        //TODO change to diameter (remove /2)
        textMirrorRadius.setTooltip(Arrays.asList(new TextComponent("How far the mirror reaches in any direction."),
                new TextComponent("Max: ").withStyle(ChatFormatting.GRAY).append(new TextComponent(String.valueOf(ReachHelper.getMaxReach(mc.player) / 2)).withStyle(ChatFormatting.GOLD)),
                new TextComponent("Upgradeable in survival with reach upgrades.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorRadius);

        y = top + 72;
        buttonCurrentPosition = new IconButton(left + 5, y, 0, 0, BUILDING_ICONS, button -> {
            Vec3 pos = new Vec3(Math.floor(mc.player.getX()) + 0.5, Math.floor(mc.player.getY()) + 0.5, Math.floor(mc.player.getZ()) + 0.5);
            textMirrorPosX.setNumber(pos.x);
            textMirrorPosY.setNumber(pos.y);
            textMirrorPosZ.setNumber(pos.z);
        });
        buttonCurrentPosition.setTooltip(new TextComponent("Set mirror position to current player position"));
        mirrorIconButtonList.add(buttonCurrentPosition);

        buttonToggleOdd = new IconButton(left + 35, y, 0, 20, BUILDING_ICONS, button -> {
            toggleOdd = !toggleOdd;
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
            if (toggleOdd) {
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set mirror position to corner of block"), new TextComponent("for even numbered builds")));
                textMirrorPosX.setNumber(textMirrorPosX.getNumber() + 0.5);
                textMirrorPosY.setNumber(textMirrorPosY.getNumber() + 0.5);
                textMirrorPosZ.setNumber(textMirrorPosZ.getNumber() + 0.5);
            } else {
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set mirror position to middle of block"), new TextComponent("for odd numbered builds")));
                textMirrorPosX.setNumber(Math.floor(textMirrorPosX.getNumber()));
                textMirrorPosY.setNumber(Math.floor(textMirrorPosY.getNumber()));
                textMirrorPosZ.setNumber(Math.floor(textMirrorPosZ.getNumber()));
            }
        });
        buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set mirror position to middle of block"), new TextComponent("for odd numbered builds")));
        mirrorIconButtonList.add(buttonToggleOdd);

        buttonDrawLines = new IconButton(left + 65, y, 0, 40, BUILDING_ICONS, button -> {
            drawLines = !drawLines;
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawLines.setTooltip(new TextComponent(drawLines ? "Hide lines" : "Show lines"));
        });
        buttonDrawLines.setTooltip(new TextComponent("Show lines"));
        mirrorIconButtonList.add(buttonDrawLines);

        buttonDrawPlanes = new IconButton(left + 95, y, 0, 60, BUILDING_ICONS, button -> {
            drawPlanes = !drawPlanes;
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawPlanes.setTooltip(new TextComponent(drawPlanes ? "Hide area" : "Show area"));
        });
        buttonDrawPlanes.setTooltip(new TextComponent("Show area"));
        mirrorIconButtonList.add(buttonDrawPlanes);

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
        if (modifierSettings != null) {
            Mirror.MirrorSettings m = modifierSettings.getMirrorSettings();
            buttonMirrorEnabled.setIsChecked(m.enabled);
            textMirrorPosX.setNumber(m.position.x);
            textMirrorPosY.setNumber(m.position.y);
            textMirrorPosZ.setNumber(m.position.z);
            buttonMirrorX.setIsChecked(m.mirrorX);
            buttonMirrorY.setIsChecked(m.mirrorY);
            buttonMirrorZ.setIsChecked(m.mirrorZ);
            textMirrorRadius.setNumber(m.radius);
            drawLines = m.drawLines;
            drawPlanes = m.drawPlanes;
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawLines.setTooltip(new TextComponent(drawLines ? "Hide lines" : "Show lines"));
            buttonDrawPlanes.setTooltip(new TextComponent(drawPlanes ? "Hide area" : "Show area"));
            if (textMirrorPosX.getNumber() == Math.floor(textMirrorPosX.getNumber())) {
                toggleOdd = false;
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set mirror position to middle of block"), new TextComponent("for odd numbered builds")));
            } else {
                toggleOdd = true;
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set mirror position to corner of block"), new TextComponent("for even numbered builds")));
            }
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
        }

        renderables.addAll(mirrorButtonList);
        renderables.addAll(mirrorIconButtonList);

        setCollapsed(!buttonMirrorEnabled.isChecked());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        mirrorNumberFieldList.forEach(NumberField::update);
    }

    @Override
    public void drawEntry(PoseStack ms, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {

        int yy = y;
        int offset = 8;

        buttonMirrorEnabled.render(ms, mouseX, mouseY, partialTicks);
        if (buttonMirrorEnabled.isChecked()) {
            buttonMirrorEnabled.y = yy;
            font.draw(ms, "Mirror enabled", left + offset, yy + 2, 0xFFFFFF);

            yy = y + 18;
            font.draw(ms, "Position", left + offset, yy + 5, 0xFFFFFF);
            font.draw(ms, "X", left + 40 + offset, yy + 5, 0xFFFFFF);
            textMirrorPosX.y = yy;
            font.draw(ms, "Y", left + 120 + offset, yy + 5, 0xFFFFFF);
            textMirrorPosY.y = yy;
            font.draw(ms, "Z", left + 200 + offset, yy + 5, 0xFFFFFF);
            textMirrorPosZ.y = yy;

            yy = y + 50;
            font.draw(ms, "Direction", left + offset, yy + 2, 0xFFFFFF);
            buttonMirrorX.y = yy;
            buttonMirrorY.y = yy;
            buttonMirrorZ.y = yy;
            font.draw(ms, "Radius", left + 176 + offset, yy + 2, 0xFFFFFF);
            textMirrorRadius.y = yy - 3;

            yy = y + 72;
            buttonCurrentPosition.y = yy;
            buttonToggleOdd.y = yy;
            buttonDrawLines.y = yy;
            buttonDrawPlanes.y = yy;

            mirrorButtonList.forEach(button -> button.render(ms, mouseX, mouseY, partialTicks));
            mirrorIconButtonList.forEach(button -> button.render(ms, mouseX, mouseY, partialTicks));
            mirrorNumberFieldList.forEach(numberField -> numberField.drawNumberField(ms, mouseX, mouseY, partialTicks));
        } else {
            buttonMirrorEnabled.y = yy;
            font.draw(ms, "Mirror disabled", left + offset, yy + 2, 0x999999);
        }

    }

    public void drawTooltip(PoseStack ms, Screen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonMirrorEnabled.isChecked()) {
            mirrorIconButtonList.forEach(iconButton -> iconButton.drawTooltip(ms, scrollPane.parent, mouseX, mouseY));
            mirrorNumberFieldList.forEach(numberField -> numberField.drawTooltip(ms, scrollPane.parent, mouseX, mouseY));
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        for (NumberField numberField : mirrorNumberFieldList) {
            numberField.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        mirrorNumberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseEvent));

        boolean insideMirrorEnabledLabel = mouseX >= left && mouseX < right && relativeY >= -2 && relativeY < 12;

        if (insideMirrorEnabledLabel) {
            buttonMirrorEnabled.playDownSound(this.mc.getSoundManager());
            buttonMirrorEnabled.onClick(mouseX, mouseY);
        }

        return true;
    }

    public Mirror.MirrorSettings getMirrorSettings() {
        boolean mirrorEnabled = buttonMirrorEnabled.isChecked();

        Vec3 mirrorPos = new Vec3(0, 64, 0);
        try {
            mirrorPos = new Vec3(textMirrorPosX.getNumber(), textMirrorPosY.getNumber(), textMirrorPosZ.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Mirror position not a valid number.");
        }

        boolean mirrorX = buttonMirrorX.isChecked();
        boolean mirrorY = buttonMirrorY.isChecked();
        boolean mirrorZ = buttonMirrorZ.isChecked();

        int mirrorRadius = 50;
        try {
            mirrorRadius = (int) textMirrorRadius.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Mirror radius not a valid number.");
        }

        return new Mirror.MirrorSettings(mirrorEnabled, mirrorPos, mirrorX, mirrorY, mirrorZ, mirrorRadius, drawLines, drawPlanes);
    }

    @Override
    protected String getName() {
        return "Mirror";
    }

    @Override
    protected int getExpandedHeight() {
        return 100;
    }
}
