package dev.huskcasaca.effortless.screen.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.helper.ReachHelper;
import dev.huskcasaca.effortless.screen.widget.ExpandableScrollEntry;
import dev.huskcasaca.effortless.screen.widget.FixedCheckbox;
import dev.huskcasaca.effortless.screen.widget.NumberField;
import dev.huskcasaca.effortless.screen.widget.ScrollPane;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ArraySettingsPane extends ExpandableScrollEntry {

    protected List<NumberField> arrayNumberFieldList = new ArrayList<>();

    private FixedCheckbox buttonArrayEnabled;
    private NumberField textArrayOffsetX, textArrayOffsetY, textArrayOffsetZ, textArrayCount;

    public ArraySettingsPane(ScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public void init(List<Widget> renderables) {
        super.init(renderables);

        int y = top;
        buttonArrayEnabled = new FixedCheckbox(left - 15 + 8, y, "", false) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                setCollapsed(!buttonArrayEnabled.isChecked());
            }
        };
        renderables.add(buttonArrayEnabled);

        y = top + 20;
        textArrayOffsetX = new NumberField(font, renderables, left + 70, y, 50, 18);
        textArrayOffsetX.setNumber(0);
        textArrayOffsetX.setTooltip(new TextComponent("How much each copy is shifted."));
        arrayNumberFieldList.add(textArrayOffsetX);

        textArrayOffsetY = new NumberField(font, renderables, left + 140, y, 50, 18);
        textArrayOffsetY.setNumber(0);
        textArrayOffsetY.setTooltip(new TextComponent("How much each copy is shifted."));
        arrayNumberFieldList.add(textArrayOffsetY);

        textArrayOffsetZ = new NumberField(font, renderables, left + 210, y, 50, 18);
        textArrayOffsetZ.setNumber(0);
        textArrayOffsetZ.setTooltip(new TextComponent("How much each copy is shifted."));
        arrayNumberFieldList.add(textArrayOffsetZ);

        y = top + 50;
        textArrayCount = new NumberField(font, renderables, left + 55, y, 50, 18);
        textArrayCount.setNumber(5);
        textArrayCount.setTooltip(new TextComponent("How many copies should be made."));
        arrayNumberFieldList.add(textArrayCount);

        var modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
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
    public void drawEntry(PoseStack ms, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {
        int yy = y;
        int offset = 8;

        buttonArrayEnabled.render(ms, mouseX, mouseY, partialTicks);
        if (buttonArrayEnabled.isChecked()) {
            buttonArrayEnabled.y = yy;
            font.draw(ms, "Array enabled", left + offset, yy + 2, 0xFFFFFF);

            yy = y + 20;
            font.draw(ms, "Offset", left + offset, yy + 5, 0xFFFFFF);
            font.draw(ms, "X", left + 50 + offset, yy + 5, 0xFFFFFF);
            textArrayOffsetX.y = yy;
            font.draw(ms, "Y", left + 120 + offset, yy + 5, 0xFFFFFF);
            textArrayOffsetY.y = yy;
            font.draw(ms, "Z", left + 190 + offset, yy + 5, 0xFFFFFF);
            textArrayOffsetZ.y = yy;

            yy = y + 50;
            font.draw(ms, "Count", left + offset, yy + 5, 0xFFFFFF);
            textArrayCount.y = yy;

            int currentReach = Math.max(-1, getArrayReach());
            int maxReach = ReachHelper.getMaxReach(mc.player);
            ChatFormatting reachColor = isCurrentReachValid(currentReach, maxReach) ? ChatFormatting.GRAY : ChatFormatting.RED;
            String reachText = "Reach: " + reachColor + currentReach + ChatFormatting.GRAY + "/" + ChatFormatting.GRAY + maxReach;
            font.draw(ms, reachText, left + 176 + offset, yy + 5, 0xFFFFFF);

            arrayNumberFieldList.forEach(numberField -> numberField.drawNumberField(ms, mouseX, mouseY, partialTicks));
        } else {
            buttonArrayEnabled.y = yy;
            font.draw(ms, "Array disabled", left + offset, yy + 2, 0x999999);
        }

    }

    public void drawTooltip(PoseStack ms, Screen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonArrayEnabled.isChecked()) {
            arrayNumberFieldList.forEach(numberField -> numberField.drawTooltip(ms, scrollPane.parent, mouseX, mouseY));
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
        return 80;
    }

    private int getArrayReach() {
        try {
            //find largest offset
            double x = Math.abs(textArrayOffsetX.getNumber());
            double y = Math.abs(textArrayOffsetY.getNumber());
            double z = Math.abs(textArrayOffsetZ.getNumber());
            double largestOffset = Math.max(Math.max(x, y), z);
            return (int) (largestOffset * textArrayCount.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            return -1;
        }
    }

    private boolean isCurrentReachValid(int currentReach, int maxReach) {
        return currentReach <= maxReach && currentReach > -1;
    }
}
