package dev.huskcasaca.effortless.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
public class IconButton extends Button {

    private final ResourceLocation resourceLocation;
    private final int iconX, iconY, iconWidth, iconHeight, iconAltX, iconAltY;
    List<Component> tooltip = new ArrayList<>();
    private boolean useAltIcon = false;

    public IconButton(int x, int y, int iconX, int iconY, ResourceLocation resourceLocation, Button.OnPress onPress) {
        this(x, y, 20, 20, iconX, iconY, 20, 20, 20, 0, resourceLocation, onPress);
    }

    public IconButton(int x, int y, int width, int height, int iconX, int iconY, int iconWidth, int iconHeight, int iconAltX, int iconAltY, ResourceLocation resourceLocation, Button.OnPress onPress) {
        super(x, y, width, height, TextComponent.EMPTY, onPress);
        this.iconX = iconX;
        this.iconY = iconY;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.iconAltX = iconAltX;
        this.iconAltY = iconAltY;
        this.resourceLocation = resourceLocation;
    }

    public void setTooltip(Component tooltip) {
        setTooltip(Collections.singletonList(tooltip));
    }

    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    public void setUseAlternateIcon(boolean useAlternateIcon) {
        this.useAltIcon = useAlternateIcon;
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        super.render(ms, mouseX, mouseY, partialTicks);
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            RenderSystem.setShaderTexture(0, this.resourceLocation);
            int currentIconX = this.iconX;
            int currentIconY = this.iconY;

            if (useAltIcon) {
                currentIconX += iconAltX;
                currentIconY += iconAltY;
            }

            //Draws a textured rectangle at the current z-value. Used to be drawTexturedModalRect in Gui.
            this.blit(ms, this.x, this.y, currentIconX, currentIconY, this.iconWidth, this.iconHeight);
        }
    }

    public void drawTooltip(PoseStack ms, Screen screen, int mouseX, int mouseY) {
        boolean flag = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        if (flag) {
            screen.renderComponentTooltip(ms, tooltip, mouseX - 10, mouseY + 25);
        }
    }
}
