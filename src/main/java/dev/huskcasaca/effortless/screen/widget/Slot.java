package dev.huskcasaca.effortless.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class Slot extends AbstractContainerEventHandler implements Widget {
    protected final Minecraft minecraft;
    protected final int itemHeight;
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected int yDrag = -2;
    protected double yo;
    protected boolean visible = true;
    protected boolean renderSelection = true;
    protected boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;

    public Slot(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
        this.minecraft = mcIn;
        this.width = width;
        this.height = height;
        this.y0 = topIn;
        this.y1 = bottomIn;
        this.itemHeight = slotHeightIn;
        this.x0 = 0;
        this.x1 = width;
    }

    public boolean isVisible() {
        return this.visible;
    }

    protected abstract int getItemCount();

    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    protected boolean selectItem(int p_selectItem_1_, int p_selectItem_2_, double p_selectItem_3_, double p_selectItem_5_) {
        return true;
    }

    protected abstract boolean isSelectedItem(int slotIndex);

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected abstract void renderBackground();

    protected void updateItemPosition(int p_updateItemPosition_1_, int p_updateItemPosition_2_, int p_updateItemPosition_3_, float partialTicks) {
    }

    protected abstract void renderItem(PoseStack poseStack, int slotIndex, int posX, int posY, int heightIn, int mouseXIn, int mouseYIn, float partialTicks);

    protected void renderHeader(int p_renderHeader_1_, int p_renderHeader_2_, Tesselator tesselator) {
    }

    protected void clickedHeader(int p_clickedHeader_1_, int p_clickedHeader_2_) {
    }

    public int getItemAtPosition(double posX, double posY) {
        int i = this.x0 + this.width / 2 - this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2 + this.getRowWidth() / 2;
        int k = Mth.floor(posY - (double) this.y0) - this.headerHeight + (int) this.yo - 4;
        int l = k / this.itemHeight;
        return posX < (double) this.getScrollbarPosition() && posX >= (double) i && posX <= (double) j && l >= 0 && k >= 0 && l < this.getItemCount() ? l : -1;
    }

    protected void capYPosition() {
        this.yo = Mth.clamp(this.yo, 0.0D, this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public int getScroll() {
        return (int) this.yo;
    }

    public boolean isMouseInList(double posX, double posY) {
        return posY >= (double) this.y0 && posY <= (double) this.y1 && posX >= (double) this.x0 && posX <= (double) this.x1;
    }

    public abstract void render(PoseStack poseStack, int mouseXIn, int mouseYIn, float partialTicks);

    protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_) {
        this.scrolling = p_updateScrollingState_5_ == 0 && p_updateScrollingState_1_ >= (double) this.getScrollbarPosition() && p_updateScrollingState_1_ < (double) (this.getScrollbarPosition() + 6);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        if (this.isVisible() && this.isMouseInList(mouseX, mouseY)) {
            int i = this.getItemAtPosition(mouseX, mouseY);
            if (i == -1 && button == 0) {
                this.clickedHeader((int) (mouseX - (double) (this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int) (mouseY - (double) this.y0) + (int) this.yo - 4);
                return true;
            } else if (i != -1 && this.selectItem(i, button, mouseX, mouseY)) {
                if (this.children().size() > i) {
                    this.setFocused(this.children().get(i));
                }

                this.setDragging(true);
                return true;
            } else {
                return this.scrolling;
            }
        } else {
            return false;
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (super.mouseDragged(mouseX, mouseY, button, p_mouseDragged_6_, p_mouseDragged_8_)) {
            return true;
        } else if (this.isVisible() && button == 0 && this.scrolling) {
            if (mouseY < (double) this.y0) {
                this.yo = 0.0D;
            } else if (mouseY > (double) this.y1) {
                this.yo = this.getMaxScroll();
            } else {
                double d0 = this.getMaxScroll();
                if (d0 < 1.0D) {
                    d0 = 1.0D;
                }

                int i = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
                i = Mth.clamp(i, 32, this.y1 - this.y0 - 8);
                double d1 = d0 / (double) (this.y1 - this.y0 - i);
                if (d1 < 1.0D) {
                    d1 = 1.0D;
                }

                this.yo += p_mouseDragged_8_ * d1;
                this.capYPosition();
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrolled) {
        if (!this.isVisible()) {
            return false;
        } else {
            this.yo -= scrolled * (double) this.itemHeight / 2.0D;
            return true;
        }
    }

    public boolean keyPressed(int i, int j, int k) {
        if (!this.isVisible()) {
            return false;
        } else if (super.keyPressed(i, j, k)) {
            return true;
        } else if (i == 264) {
            this.moveSelection(1);
            return true;
        } else if (i == 265) {
            this.moveSelection(-1);
            return true;
        } else {
            return false;
        }
    }

    protected void moveSelection(int i) {
    }

    public boolean charTyped(char eventChar, int eventKey) {
        return this.isVisible() && super.charTyped(eventChar, eventKey);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.isMouseInList(mouseX, mouseY);
    }

    public int getRowWidth() {
        return 220;
    }

    protected void renderList(PoseStack poseStack, int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
        int i = this.getItemCount();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for (int j = 0; j < i; ++j) {
            int k = insideTop + j * this.itemHeight + this.headerHeight;
            int l = this.itemHeight - 4;
            if (k > this.y1 || k + l < this.y0) {
                this.updateItemPosition(j, insideLeft, k, partialTicks);
            }

            if (this.renderSelection && this.isSelectedItem(j)) {
                int i1 = this.x0 + this.width / 2 - this.getRowWidth() / 2;
                int j1 = this.x0 + this.width / 2 + this.getRowWidth() / 2;
                RenderSystem.disableTexture();
                float f = this.isFocused() ? 1.0F : 0.5F;
                RenderSystem.setShaderColor(f, f, f, 1.0F);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex(i1, k + l + 2, 0.0D).endVertex();
                bufferbuilder.vertex(j1, k + l + 2, 0.0D).endVertex();
                bufferbuilder.vertex(j1, k - 2, 0.0D).endVertex();
                bufferbuilder.vertex(i1, k - 2, 0.0D).endVertex();
                tesselator.end();
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex(i1 + 1, k + l + 1, 0.0D).endVertex();
                bufferbuilder.vertex(j1 - 1, k + l + 1, 0.0D).endVertex();
                bufferbuilder.vertex(j1 - 1, k - 1, 0.0D).endVertex();
                bufferbuilder.vertex(i1 + 1, k - 1, 0.0D).endVertex();
                tesselator.end();
                RenderSystem.enableTexture();
            }

            this.renderItem(poseStack, j, insideLeft, k, l, mouseXIn, mouseYIn, partialTicks);
        }

    }

    protected boolean isFocused() {
        return false;
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }
}
