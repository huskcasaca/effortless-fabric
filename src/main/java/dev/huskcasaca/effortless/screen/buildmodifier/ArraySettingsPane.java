package dev.huskcasaca.effortless.screen.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.screen.widget.ExpandableScrollEntry;
import dev.huskcasaca.effortless.screen.widget.Checkbox;
import dev.huskcasaca.effortless.screen.widget.NumberField;
import dev.huskcasaca.effortless.screen.widget.ScrollPane;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ArraySettingsPane extends ExpandableScrollEntry {

    protected List<NumberField> arrayNumberFieldList = new ArrayList<>();

    private Checkbox buttonArrayEnabled;
    private NumberField textArrayOffsetX, textArrayOffsetY, textArrayOffsetZ, textArrayCount;

    public ArraySettingsPane(ScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public void init(List<Widget> renderables) {
        super.init(renderables);

        int y = top;
        buttonArrayEnabled = new Checkbox(left - 15 + 8, y, "", false) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                setCollapsed(!buttonArrayEnabled.isChecked());
            }
        };
        renderables.add(buttonArrayEnabled);

        y = top + 20;
        textArrayOffsetX = new NumberField(font, renderables, left + 60, y, 90, 18);
        textArrayOffsetX.setNumber(0);
        textArrayOffsetX.setTooltip(Component.literal("How much each copy is shifted."));
        arrayNumberFieldList.add(textArrayOffsetX);

        textArrayOffsetY = new NumberField(font, renderables, left + 60, y + 24, 90, 18);
        textArrayOffsetY.setNumber(0);
        textArrayOffsetY.setTooltip(Component.literal("How much each copy is shifted."));
        arrayNumberFieldList.add(textArrayOffsetY);

        textArrayOffsetZ = new NumberField(font, renderables, left + 60, y + 24 * 2, 90, 18);
        textArrayOffsetZ.setNumber(0);
        textArrayOffsetZ.setTooltip(Component.literal("How much each copy is shifted."));
        arrayNumberFieldList.add(textArrayOffsetZ);

        textArrayCount = new NumberField(font, renderables, left + 200, y, 80, 18);
        textArrayCount.setNumber(5);
        textArrayCount.setTooltip(Component.literal("How many copies should be made."));
        arrayNumberFieldList.add(textArrayCount);

        var modifierSettings = BuildModifierHelper.getModifierSettings(mc.player);
        if (modifierSettings != null) {
            var arraySettings = modifierSettings.arraySettings();
            buttonArrayEnabled.setIsChecked(arraySettings.enabled());
            textArrayOffsetX.setNumber(arraySettings.offset().getX());
            textArrayOffsetY.setNumber(arraySettings.offset().getY());
            textArrayOffsetZ.setNumber(arraySettings.offset().getZ());
            textArrayCount.setNumber(arraySettings.count());
        }

        setCollapsed(!buttonArrayEnabled.isChecked());
    }

    public void updateScreen() {
        arrayNumberFieldList.forEach(NumberField::update);
    }

    @Override
    public void drawEntry(PoseStack poseStack, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {
        int yy = y;
        int offset = 8;

        buttonArrayEnabled.render(poseStack, mouseX, mouseY, partialTicks);
        if (buttonArrayEnabled.isChecked()) {
            buttonArrayEnabled.y = yy;
            font.draw(poseStack, "Array enabled", left + offset, yy + 2, 0xFFFFFF);

            var positionOffsetX0 = left + 8;
            var positionOffsetX1 = left + 160;
            var positionOffsetY0 = y + 24;
            var positionOffsetY1 = y + 24 * 2;

            var textOffsetX = 40;
            var componentOffsetX = 15;
            var componentOffsetY = -5;

            font.draw(poseStack, "Offset", positionOffsetX0, positionOffsetY0, 0xFFFFFF);
            font.draw(poseStack, "X", positionOffsetX0 + textOffsetX, positionOffsetY0, 0xFFFFFF);
            font.draw(poseStack, "Y", positionOffsetX0 + textOffsetX, positionOffsetY0 + 24, 0xFFFFFF);
            font.draw(poseStack, "Z", positionOffsetX0 + textOffsetX, positionOffsetY0 + 24 * 2, 0xFFFFFF);
            textArrayOffsetX.y = positionOffsetY0 + componentOffsetY;
            textArrayOffsetY.y = positionOffsetY0 + componentOffsetY + 24;
            textArrayOffsetZ.y = positionOffsetY0 + componentOffsetY + 24 * 2;

            font.draw(poseStack, "Count", positionOffsetX1, positionOffsetY0, 0xFFFFFF);
            textArrayCount.y = positionOffsetY0 + componentOffsetY;

            int currentReach = Math.max(-1, getArrayReach());
            int maxReach = ReachHelper.getMaxReachDistance(mc.player);
            var reachColor = isCurrentReachValid(currentReach, maxReach) ? ChatFormatting.GRAY : ChatFormatting.RED;
            var reachText = "Reach  " + reachColor + currentReach + ChatFormatting.GRAY + "/" + ChatFormatting.GRAY + maxReach;

            font.draw(poseStack, reachText, positionOffsetX1, positionOffsetY1, 0xFFFFFF);

            arrayNumberFieldList.forEach(numberField -> numberField.drawNumberField(poseStack, mouseX, mouseY, partialTicks));
        } else {
            buttonArrayEnabled.y = yy;
            font.draw(poseStack, "Array disabled", left + offset, yy + 2, 0x999999);
        }

    }

    public void drawTooltip(PoseStack poseStack, Screen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonArrayEnabled.isChecked()) {
            arrayNumberFieldList.forEach(numberField -> numberField.drawTooltip(poseStack, scrollPane.parent, mouseX, mouseY));
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        for (NumberField numberField : arrayNumberFieldList) {
            numberField.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        arrayNumberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseEvent));

        boolean insideArrayEnabledLabel = mouseX >= left && mouseX < right && relativeY >= -2 && relativeY < 12;

        if (insideArrayEnabledLabel) {
            buttonArrayEnabled.playDownSound(this.mc.getSoundManager());
            buttonArrayEnabled.onClick(mouseX, mouseY);
        }

        return true;
    }

    public Array.ArraySettings getArraySettings() {
        boolean arrayEnabled = buttonArrayEnabled.isChecked();
        var arrayOffset = new BlockPos(0, 0, 0);
        try {
            arrayOffset = new BlockPos(textArrayOffsetX.getNumber(), textArrayOffsetY.getNumber(), textArrayOffsetZ.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Array offset not a valid number.");
        }

        int arrayCount = 5;
        try {
            arrayCount = (int) textArrayCount.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Array count not a valid number.");
        }

        return new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);
    }

    @Override
    protected String getName() {
        return "Array";
    }

    @Override
    protected int getExpandedHeight() {
        return 96;
    }

    private int getArrayReach() {
        try {
            //find largest offset
            double x = Math.abs(textArrayOffsetX.getNumber());
            double y = Math.abs(textArrayOffsetY.getNumber());
            double z = Math.abs(textArrayOffsetZ.getNumber());
            double largestOffset = Math.max(Math.max(x, y), z);
            var count = textArrayCount.getNumber();
            return (int) (count > 1 ? largestOffset * count : 0);
        } catch (NumberFormatException | NullPointerException ex) {
            return 0;
        }
    }

    private boolean isCurrentReachValid(int currentReach, int maxReach) {
        return currentReach <= maxReach && currentReach > -1;
    }
}
