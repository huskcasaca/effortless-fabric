package dev.huskcasaca.effortless.screen.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.buildmodifier.mirror.RadialMirror;
import dev.huskcasaca.effortless.buildreach.ReachHelper;
import dev.huskcasaca.effortless.screen.widget.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RadialMirrorSettingsPane extends ExpandableScrollEntry {

    protected static final ResourceLocation BUILDING_ICONS = new ResourceLocation(Effortless.MOD_ID, "textures/gui/building_icons.png");

    protected List<Button> radialMirrorButtonList = new ArrayList<>();
    protected List<IconButton> radialMirrorIconButtonList = new ArrayList<>();
    protected List<NumberField> radialMirrorNumberFieldList = new ArrayList<>();

    private NumberField textRadialMirrorPosX, textRadialMirrorPosY, textRadialMirrorPosZ, textRadialMirrorSlices, textRadialMirrorRadius;
    private FixedCheckbox buttonRadialMirrorEnabled, buttonRadialMirrorAlternate;
    private IconButton buttonCurrentPosition, buttonToggleOdd, buttonDrawPlanes, buttonDrawLines;
    private boolean drawPlanes, drawLines, toggleOdd;

    public RadialMirrorSettingsPane(ScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public void init(List<Widget> renderables) {
        super.init(renderables);

        int y = top - 2;
        buttonRadialMirrorEnabled = new FixedCheckbox(left - 15 + 8, y, "", false) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                setCollapsed(!buttonRadialMirrorEnabled.isChecked());
            }
        };
        renderables.add(buttonRadialMirrorEnabled);

        y = top + 18;
        textRadialMirrorPosX = new NumberField(font, renderables, left + 58, y, 62, 18);
        textRadialMirrorPosX.setNumber(0);
        textRadialMirrorPosX.setTooltip(
                Arrays.asList(new TextComponent("The position of the radial mirror."), new TextComponent("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        radialMirrorNumberFieldList.add(textRadialMirrorPosX);

        textRadialMirrorPosY = new NumberField(font, renderables, left + 138, y, 62, 18);
        textRadialMirrorPosY.setNumber(64);
        textRadialMirrorPosY.setTooltip(Arrays.asList(new TextComponent("The position of the radial mirror."), new TextComponent("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        radialMirrorNumberFieldList.add(textRadialMirrorPosY);

        textRadialMirrorPosZ = new NumberField(font, renderables, left + 218, y, 62, 18);
        textRadialMirrorPosZ.setNumber(0);
        textRadialMirrorPosZ.setTooltip(Arrays.asList(new TextComponent("The position of the radial mirror."), new TextComponent("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        radialMirrorNumberFieldList.add(textRadialMirrorPosZ);

        y = top + 47;
        textRadialMirrorSlices = new NumberField(font, renderables, left + 55, y, 50, 18);
        textRadialMirrorSlices.setNumber(4);
        textRadialMirrorSlices.setTooltip(Arrays.asList(new TextComponent("The number of repeating slices."), new TextComponent("Minimally 2.").withStyle(ChatFormatting.GRAY)));
        radialMirrorNumberFieldList.add(textRadialMirrorSlices);

        textRadialMirrorRadius = new NumberField(font, renderables, left + 218, y, 62, 18);
        textRadialMirrorRadius.setNumber(50);
        //TODO change to diameter (remove /2)
        textRadialMirrorRadius.setTooltip(Arrays.asList(new TextComponent("How far the radial mirror reaches from its center position."),
                new TextComponent("Max: ").withStyle(ChatFormatting.GRAY).append(new TextComponent(String.valueOf(ReachHelper.getMaxReach(mc.player) / 2)).withStyle(ChatFormatting.GOLD)),
                new TextComponent("Upgradeable in survival with reach upgrades.").withStyle(ChatFormatting.GRAY)));
        radialMirrorNumberFieldList.add(textRadialMirrorRadius);

        y = top + 72;
        buttonCurrentPosition = new IconButton(left + 5, y, 0, 0, BUILDING_ICONS, button -> {
            var pos = new Vec3(Math.floor(mc.player.getX()) + 0.5, Math.floor(mc.player.getY()) + 0.5, Math.floor(mc.player.getZ()) + 0.5);
            textRadialMirrorPosX.setNumber(pos.x);
            textRadialMirrorPosY.setNumber(pos.y);
            textRadialMirrorPosZ.setNumber(pos.z);
        });
        buttonCurrentPosition.setTooltip(new TextComponent("Set radial mirror position to current player position"));
        radialMirrorIconButtonList.add(buttonCurrentPosition);

        buttonToggleOdd = new IconButton(left + 35, y, 0, 20, BUILDING_ICONS, button -> {
            toggleOdd = !toggleOdd;
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
            if (toggleOdd) {
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set mirror position to corner of block"), new TextComponent("for even numbered builds")));
                textRadialMirrorPosX.setNumber(textRadialMirrorPosX.getNumber() + 0.5);
                textRadialMirrorPosY.setNumber(textRadialMirrorPosY.getNumber() + 0.5);
                textRadialMirrorPosZ.setNumber(textRadialMirrorPosZ.getNumber() + 0.5);
            } else {
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set mirror position to middle of block"), new TextComponent("for odd numbered builds")));
                textRadialMirrorPosX.setNumber(Math.floor(textRadialMirrorPosX.getNumber()));
                textRadialMirrorPosY.setNumber(Math.floor(textRadialMirrorPosY.getNumber()));
                textRadialMirrorPosZ.setNumber(Math.floor(textRadialMirrorPosZ.getNumber()));
            }
        });
        buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set radial mirror position to middle of block"), new TextComponent("for odd numbered builds")));
        radialMirrorIconButtonList.add(buttonToggleOdd);

        buttonDrawLines = new IconButton(left + 65, y, 0, 40, BUILDING_ICONS, button -> {
            drawLines = !drawLines;
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawLines.setTooltip(new TextComponent(drawLines ? "Hide lines" : "Show lines"));
        });
        buttonDrawLines.setTooltip(new TextComponent("Show lines"));
        radialMirrorIconButtonList.add(buttonDrawLines);

        buttonDrawPlanes = new IconButton(left + 95, y, 0, 60, BUILDING_ICONS, button -> {
            drawPlanes = !drawPlanes;
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawPlanes.setTooltip(new TextComponent(drawPlanes ? "Hide area" : "Show area"));
        });
        buttonDrawPlanes.setTooltip(new TextComponent("Show area"));
        radialMirrorIconButtonList.add(buttonDrawPlanes);

        y = top + 76;
        buttonRadialMirrorAlternate = new FixedCheckbox(left + 140, y, " Alternate", false);
        radialMirrorButtonList.add(buttonRadialMirrorAlternate);

        var modifierSettings = BuildModifierHelper.getModifierSettings(mc.player);
        if (modifierSettings != null) {
            var radialMirrorSettings = modifierSettings.radialMirrorSettings();
            buttonRadialMirrorEnabled.setIsChecked(radialMirrorSettings.enabled());
            textRadialMirrorPosX.setNumber(radialMirrorSettings.position().x);
            textRadialMirrorPosY.setNumber(radialMirrorSettings.position().y);
            textRadialMirrorPosZ.setNumber(radialMirrorSettings.position().z);
            textRadialMirrorSlices.setNumber(radialMirrorSettings.slices());
            buttonRadialMirrorAlternate.setIsChecked(radialMirrorSettings.alternate());
            textRadialMirrorRadius.setNumber(radialMirrorSettings.radius());
            drawLines = radialMirrorSettings.drawLines();
            drawPlanes = radialMirrorSettings.drawPlanes();
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawLines.setTooltip(new TextComponent(drawLines ? "Hide lines" : "Show lines"));
            buttonDrawPlanes.setTooltip(new TextComponent(drawPlanes ? "Hide area" : "Show area"));
            if (textRadialMirrorPosX.getNumber() == Math.floor(textRadialMirrorPosX.getNumber())) {
                toggleOdd = false;
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set radial mirror position to middle of block"), new TextComponent("for odd numbered builds")));
            } else {
                toggleOdd = true;
                buttonToggleOdd.setTooltip(Arrays.asList(new TextComponent("Set radial mirror position to corner of block"), new TextComponent("for even numbered builds")));
            }
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
        }

        renderables.addAll(radialMirrorButtonList);
        renderables.addAll(radialMirrorIconButtonList);

        setCollapsed(!buttonRadialMirrorEnabled.isChecked());
    }

    public void updateScreen() {
        radialMirrorNumberFieldList.forEach(NumberField::update);
    }


    @Override
    public void drawEntry(PoseStack ms, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {

        int yy = y;
        int offset = 8;

        buttonRadialMirrorEnabled.render(ms, mouseX, mouseY, partialTicks);
        if (buttonRadialMirrorEnabled.isChecked()) {
            buttonRadialMirrorEnabled.y = yy;
            font.draw(ms, "Radial mirror enabled", left + offset, yy + 2, 0xFFFFFF);

            yy = y + 18;
            font.draw(ms, "Position", left + offset, yy + 5, 0xFFFFFF);
            font.draw(ms, "X", left + 40 + offset, yy + 5, 0xFFFFFF);
            textRadialMirrorPosX.y = yy;
            font.draw(ms, "Y", left + 120 + offset, yy + 5, 0xFFFFFF);
            textRadialMirrorPosY.y = yy;
            font.draw(ms, "Z", left + 200 + offset, yy + 5, 0xFFFFFF);
            textRadialMirrorPosZ.y = yy;

            yy = y + 50;
            font.draw(ms, "Slices", left + offset, yy + 2, 0xFFFFFF);
            textRadialMirrorSlices.y = yy - 3;
            font.draw(ms, "Radius", left + 176 + offset, yy + 2, 0xFFFFFF);
            textRadialMirrorRadius.y = yy - 3;

            yy = y + 72;
            buttonCurrentPosition.y = yy;
            buttonToggleOdd.y = yy;
            buttonDrawLines.y = yy;
            buttonDrawPlanes.y = yy;

            yy = y + 76;
            buttonRadialMirrorAlternate.y = yy;

            radialMirrorButtonList.forEach(button -> button.render(ms, mouseX, mouseY, partialTicks));
            radialMirrorIconButtonList.forEach(button -> button.render(ms, mouseX, mouseY, partialTicks));
            radialMirrorNumberFieldList
                    .forEach(numberField -> numberField.drawNumberField(ms, mouseX, mouseY, partialTicks));
        } else {
            buttonRadialMirrorEnabled.y = yy;
            font.draw(ms, "Radial mirror disabled", left + offset, yy + 2, 0x999999);
        }

    }

    public void drawTooltip(PoseStack ms, Screen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonRadialMirrorEnabled.isChecked()) {
            radialMirrorIconButtonList.forEach(iconButton -> iconButton.drawTooltip(ms, scrollPane.parent, mouseX, mouseY));
            radialMirrorNumberFieldList.forEach(numberField -> numberField.drawTooltip(ms, scrollPane.parent, mouseX, mouseY));
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        for (NumberField numberField : radialMirrorNumberFieldList) {
            numberField.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        radialMirrorNumberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseEvent));

        boolean insideRadialMirrorEnabledLabel = mouseX >= left && mouseX < right && relativeY >= -2 && relativeY < 12;

        if (insideRadialMirrorEnabledLabel) {
            buttonRadialMirrorEnabled.playDownSound(this.mc.getSoundManager());
            buttonRadialMirrorEnabled.onClick(mouseX, mouseY);
        }

        return true;
    }

    public RadialMirror.RadialMirrorSettings getRadialMirrorSettings() {
        boolean radialMirrorEnabled = buttonRadialMirrorEnabled.isChecked();

        var radialMirrorPos = new Vec3(0, 64, 0);
        try {
            radialMirrorPos = new Vec3(textRadialMirrorPosX.getNumber(), textRadialMirrorPosY.getNumber(), textRadialMirrorPosZ
                    .getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Radial mirror position not a valid number.");
        }

        int radialMirrorSlices = 4;
        try {
            radialMirrorSlices = (int) textRadialMirrorSlices.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Radial mirror slices not a valid number.");
        }

        boolean radialMirrorAlternate = buttonRadialMirrorAlternate.isChecked();

        int radialMirrorRadius = 50;
        try {
            radialMirrorRadius = (int) textRadialMirrorRadius.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Mirror radius not a valid number.");
        }

        return new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPos, radialMirrorSlices, radialMirrorAlternate, radialMirrorRadius, drawLines, drawPlanes);
    }

    @Override
    protected String getName() {
        return "Radial mirror";
    }

    @Override
    protected int getExpandedHeight() {
        return 100;
    }
}
