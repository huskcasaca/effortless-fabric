package dev.huskcasaca.effortless.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.screen.ScreenUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class provides a checkbox style control.
 */
@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FixedCheckbox extends Button {
    private final int boxWidth;
    private boolean isChecked;

    public FixedCheckbox(int xPos, int yPos, String displayString, boolean isChecked) {
        super(xPos, yPos, Minecraft.getInstance().font.width(displayString) + 2 + 11, 11, new TextComponent(displayString), b -> {
        });
        this.isChecked = isChecked;
        this.boxWidth = 11;
        this.height = 11;
        this.width = this.boxWidth + 2 + Minecraft.getInstance().font.width(displayString);
    }

    @Override
    public void renderButton(PoseStack ms, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.boxWidth && mouseY < this.y + this.height;
            ScreenUtils.blitWithBorder(ms, WIDGETS_LOCATION, this.x, this.y, 0, 46, this.boxWidth, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
            this.renderBg(ms, mc, mouseX, mouseY);
            int color = 14737632;
            int packedFGColor = 0;
            // FIXME: 8/9/22
            if (packedFGColor != 0) {
                color = packedFGColor;
            } else if (!this.active) {
                color = 10526880;
            }

            if (this.isChecked)
                drawCenteredString(ms, mc.font, "x", this.x + this.boxWidth / 2 + 1, this.y + 1, 14737632);

            drawString(ms, mc.font, getMessage(), this.x + this.boxWidth + 2, this.y + 2, color);
        }
    }

    @Override
    public void onPress() {
        this.isChecked = !this.isChecked;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}