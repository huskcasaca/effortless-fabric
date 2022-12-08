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
@SuppressWarnings("deprecation")
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

    protected abstract boolean isSelectedItem(int p_isSelectedItem_1_);

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected abstract void renderBackground();

    protected void updateItemPosition(int p_updateItemPosition_1_, int p_updateItemPosition_2_, int p_updateItemPosition_3_, float p_updateItemPosition_4_) {
    }

    protected abstract void renderItem(PoseStack ms, int p_renderItem_1_, int p_renderItem_2_, int p_renderItem_3_, int p_renderItem_4_, int p_renderItem_5_, int p_renderItem_6_, float p_renderItem_7_);

    protected void renderHeader(int p_renderHeader_1_, int p_renderHeader_2_, Tesselator p_renderHeader_3_) {
    }

    protected void clickedHeader(int p_clickedHeader_1_, int p_clickedHeader_2_) {
    }

    public int getItemAtPosition(double p_getItemAtPosition_1_, double p_getItemAtPosition_3_) {
        int i = this.x0 + this.width / 2 - this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2 + this.getRowWidth() / 2;
        int k = Mth.floor(p_getItemAtPosition_3_ - (double) this.y0) - this.headerHeight + (int) this.yo - 4;
        int l = k / this.itemHeight;
        return p_getItemAtPosition_1_ < (double) this.getScrollbarPosition() && p_getItemAtPosition_1_ >= (double) i && p_getItemAtPosition_1_ <= (double) j && l >= 0 && k >= 0 && l < this.getItemCount() ? l : -1;
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

    public boolean isMouseInList(double p_isMouseInList_1_, double p_isMouseInList_3_) {
        return p_isMouseInList_3_ >= (double) this.y0 && p_isMouseInList_3_ <= (double) this.y1 && p_isMouseInList_1_ >= (double) this.x0 && p_isMouseInList_1_ <= (double) this.x1;
    }

    public abstract void render(PoseStack ms, int p_render_1_, int p_render_2_, float p_render_3_);

    protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_) {
        this.scrolling = p_updateScrollingState_5_ == 0 && p_updateScrollingState_1_ >= (double) this.getScrollbarPosition() && p_updateScrollingState_1_ < (double) (this.getScrollbarPosition() + 6);
    }

    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        this.updateScrollingState(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if (this.isVisible() && this.isMouseInList(p_mouseClicked_1_, p_mouseClicked_3_)) {
            int i = this.getItemAtPosition(p_mouseClicked_1_, p_mouseClicked_3_);
            if (i == -1 && p_mouseClicked_5_ == 0) {
                this.clickedHeader((int) (p_mouseClicked_1_ - (double) (this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int) (p_mouseClicked_3_ - (double) this.y0) + (int) this.yo - 4);
                return true;
            } else if (i != -1 && this.selectItem(i, p_mouseClicked_5_, p_mouseClicked_1_, p_mouseClicked_3_)) {
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

    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }

        return false;
    }

    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_)) {
            return true;
        } else if (this.isVisible() && p_mouseDragged_5_ == 0 && this.scrolling) {
            if (p_mouseDragged_3_ < (double) this.y0) {
                this.yo = 0.0D;
            } else if (p_mouseDragged_3_ > (double) this.y1) {
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

    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        if (!this.isVisible()) {
            return false;
        } else {
            this.yo -= p_mouseScrolled_5_ * (double) this.itemHeight / 2.0D;
            return true;
        }
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (!this.isVisible()) {
            return false;
        } else if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        } else if (p_keyPressed_1_ == 264) {
            this.moveSelection(1);
            return true;
        } else if (p_keyPressed_1_ == 265) {
            this.moveSelection(-1);
            return true;
        } else {
            return false;
        }
    }

    protected void moveSelection(int p_moveSelection_1_) {
    }

    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        return this.isVisible() && super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
        return this.isMouseInList(p_isMouseOver_1_, p_isMouseOver_3_);
    }

    public int getRowWidth() {
        return 220;
    }

    protected void renderList(PoseStack ms, int p_renderList_1_, int p_renderList_2_, int p_renderList_3_, int p_renderList_4_, float p_renderList_5_) {
        int i = this.getItemCount();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        for (int j = 0; j < i; ++j) {
            int k = p_renderList_2_ + j * this.itemHeight + this.headerHeight;
            int l = this.itemHeight - 4;
            if (k > this.y1 || k + l < this.y0) {
                this.updateItemPosition(j, p_renderList_1_, k, p_renderList_5_);
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
                tessellator.end();
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex(i1 + 1, k + l + 1, 0.0D).endVertex();
                bufferbuilder.vertex(j1 - 1, k + l + 1, 0.0D).endVertex();
                bufferbuilder.vertex(j1 - 1, k - 1, 0.0D).endVertex();
                bufferbuilder.vertex(i1 + 1, k - 1, 0.0D).endVertex();
                tessellator.end();
                RenderSystem.enableTexture();
            }

            this.renderItem(ms, j, p_renderList_1_, k, l, p_renderList_3_, p_renderList_4_, p_renderList_5_);
        }

    }

    protected boolean isFocused() {
        return false;
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }
}
