package dev.huskcasaca.effortless.screen.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ScrollPane extends Slot {

    private final List<IScrollEntry> listEntries;
    public Screen parent;
    public Font font;
    private float scrollMultiplier = 1f;

    private int mouseX;
    private int mouseY;

    public ScrollPane(Screen parent, Font font, int top, int bottom) {
        super(Minecraft.getInstance(), parent.width, parent.height, top, bottom, 100);
        this.parent = parent;
        this.font = font;
        this.renderSelection = false;
        listEntries = new ArrayList<>();
    }

    public IScrollEntry getListEntry(int index) {
        return listEntries.get(index);
    }

    public void addListEntry(IScrollEntry listEntry) {
        listEntries.add(listEntry);
    }

    @Override
    protected int getItemCount() {
        return listEntries.size();
    }

    @Override
    protected boolean isSelectedItem(int slotIndex) {
        return false;
    }

    @Override
    protected int getScrollbarPosition() {
        //return width / 2 + 140 + 10;
        return width - 15;
    }

    @Override
    public int getRowWidth() {
        return 280;
    }

    //Removed background
    @Override
    public void render(PoseStack ms, int mouseXIn, int mouseYIn, float partialTicks) {
        if (this.visible) {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            this.renderBackground();
            int scrollbarLeft = this.getScrollbarPosition();
            int scrollbarRight = scrollbarLeft + 6;
            this.capYPosition();

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();

            int insideLeft = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
            int insideTop = this.y0 + 4 - (int) this.yo;
            if (this.renderHeader) {
                this.renderHeader(insideLeft, insideTop, tessellator);
            }

            //All entries
            this.renderList(ms, insideLeft, insideTop, mouseXIn, mouseYIn, partialTicks);
            RenderSystem.disableDepthTest();

            //Dirt overlays on top and bottom
//            this.renderHoleBackground(0, this.y0, 255, 255);
//            this.renderHoleBackground(this.y1, this.height, 255, 255);

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableTexture();

            //top
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.x0, (double)(this.y0 + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)(this.y0 + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)this.y0, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x0, (double)this.y0, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
//            tessellator.draw();

            //bottom
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.x0, (double)this.y1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)this.y1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)(this.y1 - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x0, (double)(this.y1 - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
//            tessellator.draw();

            //Draw scrollbar
            int maxScroll = this.getMaxScroll();
            if (maxScroll > 0) {
                int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
                k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
                int l1 = (int) this.yo * (this.y1 - this.y0 - k1) / maxScroll + this.y0;
                if (l1 < this.y0) {
                    l1 = this.y0;
                }

                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(scrollbarLeft, this.y1, 0.0F).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(scrollbarRight, this.y1, 0.0F).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(scrollbarRight, this.y0, 0.0F).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(scrollbarLeft, this.y0, 0.0F).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                tessellator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(scrollbarLeft, l1 + k1, 0.0F).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(scrollbarRight, l1 + k1, 0.0F).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(scrollbarRight, l1, 0.0F).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(scrollbarLeft, l1, 0.0F).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                tessellator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(scrollbarLeft, l1 + k1 - 1, 0.0F).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(scrollbarRight - 1, l1 + k1 - 1, 0.0F).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(scrollbarRight - 1, l1, 0.0F).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(scrollbarLeft, l1, 0.0F).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tessellator.end();
            }

            //this.renderDecorations(mouseXIn, mouseYIn);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    //SLOTHEIGHT MODIFICATIONS
    //SlotHeight is still relied on for determining how much to scroll
    @Override
    protected int getMaxPosition() {
        //Add every entry height
        int height = this.headerHeight;
        for (IScrollEntry entry : listEntries) {
            height += entry.getHeight();
        }
        return height;
    }

    @Override
    protected void renderBackground() {

    }

    @Override
    protected void renderItem(PoseStack ms, int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        this.getListEntry(slotIndex).drawEntry(ms, slotIndex, xPos, yPos, this.getRowWidth(), heightIn, mouseXIn, mouseYIn,
                this.getSlotIndexFromScreenCoords(mouseXIn, mouseYIn) == slotIndex, partialTicks);
    }

    public int getMaxPosition(int count) {
        //Add all count entry heights
        int height = this.headerHeight;
        for (int i = 0; i < count; i++) {
            IScrollEntry entry = listEntries.get(i);
            height += entry.getHeight();
        }
        return height;
    }

    public int getSlotIndexFromScreenCoords(double posX, double posY) {
        int left = this.x0 + (this.width - this.getRowWidth()) / 2;
        int right = this.x0 + (this.width + this.getRowWidth()) / 2;
        double relativeMouseY = getRelativeMouseY(mouseY, 0);

        //Iterate over every entry until relativeMouseY falls within its height
        for (int i = 0; i < listEntries.size(); i++) {
            IScrollEntry entry = listEntries.get(i);
            if (relativeMouseY <= entry.getHeight())
                return posX < this.getScrollbarPosition() && posX >= left && posX <= right && i >= 0 &&
                        relativeMouseY >= 0 && i < this.getItemCount() ? i : -1;
            relativeMouseY -= entry.getHeight();
        }
        return -1;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int selectedSlot = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
        double relativeX = getRelativeMouseX(mouseX);

        //Always pass through mouseclicked, to be able to unfocus textfields
        for (int i = 0; i < this.listEntries.size(); i++) {
            double relativeY = getRelativeMouseY(mouseY, i);
            this.getListEntry(i).mousePressed(selectedSlot, (int) mouseX, (int) mouseY, button, (int) relativeX, (int) relativeY);
        }


//        if (this.isMouseYWithinSlotBounds(mouseY))
//        {
//            int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
//
//            if (i >= 0)
//            {
//                int relativeX = getRelativeMouseX(mouseX);
//                int relativeY = getRelativeMouseY(mouseY, i);
//
//                if (this.getListEntry(i).mousePressed(i, mouseX, mouseY, mouseEvent, relativeX, relativeY))
//                {
//                    this.setEnabled(false);
//                    return true;
//                }
//            }
//        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (int i = 0; i < this.getItemCount(); ++i) {
            double relativeX = getRelativeMouseX(mouseX);
            double relativeY = getRelativeMouseY(mouseY, i);
            this.getListEntry(i).mouseReleased(i, (int) mouseX, (int) mouseY, button, (int) relativeX, (int) relativeY);
        }

        this.visible = true;
        return false;
    }

    public void handleMouseInput() {
        if (this.isMouseInList(this.mouseX, this.mouseY)) {
            if (minecraft.mouseHandler.isLeftPressed() && this.mouseY >= this.y0 &&
                    this.mouseY <= this.y1) {
                int i = this.x0 + (this.width - this.getRowWidth()) / 2;
                int j = this.x0 + (this.width + this.getRowWidth()) / 2;
                int slotIndex = getSlotIndexFromScreenCoords(this.mouseX, this.mouseY);
                double relativeMouseY = getRelativeMouseY(mouseY, slotIndex);

                if (slotIndex > -1) {
                    this.mouseClicked(this.mouseX, this.mouseY, 0);
                } else if (this.mouseX >= i && this.mouseX <= j && relativeMouseY < 0) {
                    this.clickedHeader(this.mouseX - i, this.mouseY - this.y0 + (int) this.yo - 4);
                }
            }

            if (minecraft.mouseHandler.isLeftPressed() && this.isVisible()) {
                if (this.yDrag == -1) {
                    boolean flag1 = true;

                    if (this.mouseY >= this.y0 && this.mouseY <= this.y1) {
                        int i2 = this.x0 + (this.width - this.getRowWidth()) / 2;
                        int j2 = this.x0 + (this.width + this.getRowWidth()) / 2;
                        int slotIndex = getSlotIndexFromScreenCoords(this.mouseX, this.mouseY);
                        double relativeMouseY = getRelativeMouseY(mouseY, slotIndex);

                        if (slotIndex > -1) {
                            this.mouseClicked(slotIndex, this.mouseX, this.mouseY);
                        } else if (this.mouseX >= i2 && this.mouseX <= j2 && relativeMouseY < 0) {
                            this.clickedHeader(this.mouseX - i2,
                                    this.mouseY - this.y0 + (int) this.yo - 4);
                            flag1 = false;
                        }

                        int i3 = this.getScrollbarPosition();
                        int j1 = i3 + 6;

                        if (this.mouseX >= i3 && this.mouseX <= j1) {
                            this.scrollMultiplier = -1.0F;
                            int maxScroll = this.getMaxScroll();

                            if (maxScroll < 1) {
                                maxScroll = 1;
                            }

                            int l1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) /
                                    (float) this.getMaxPosition());
                            l1 = Mth.clamp(l1, 32, this.y1 - this.y0 - 8);
                            this.scrollMultiplier /= (float) (this.y1 - this.y0 - l1) / (float) maxScroll;
                        } else {
                            this.scrollMultiplier = 1.0F;
                        }

                        if (flag1) {
                            this.yDrag = this.mouseY;
                        } else {
                            this.yDrag = -2;
                        }
                    } else {
                        this.yDrag = -2;
                    }
                } else if (this.yDrag >= 0) {
                    this.yo -= (float) (this.mouseY - this.yDrag) * this.scrollMultiplier;
                    this.yDrag = this.mouseY;
                }
            } else {
                this.yDrag = -1;
            }

        }
    }

    //Draw in center if it fits
    @Override
    protected void renderList(PoseStack ms, int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
        int itemCount = this.getItemCount();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        //Find y to start with
        int y = this.headerHeight + insideTop;
        int contentHeight = getMaxPosition();
        int insideHeight = this.y1 - this.y0 - 4;

        if (contentHeight < insideHeight) {
            //it fits, so we can center it vertically
            y += (insideHeight - contentHeight) / 2;
        }

        //Draw all entries
        for (int i = 0; i < itemCount; ++i) {
            int entryHeight = listEntries.get(i).getHeight();
            int entryHeight2 = entryHeight - 4;

            if (y > this.y1 || y + entryHeight2 < this.y0) {
                this.updateItemPosition(i, insideLeft, y, partialTicks);
            }

            if (this.renderSelection && this.isSelectedItem(i)) {
                int i1 = this.x0 + this.width / 2 - this.getRowWidth() / 2;
                int j1 = this.x0 + this.width / 2 + this.getRowWidth() / 2;
                RenderSystem.disableTexture();
                float f = this.isFocused() ? 1.0F : 0.5F;
                RenderSystem.setShaderColor(f, f, f, 1.0F);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex(i1, y + entryHeight2 + 2, 0.0D).endVertex();
                bufferbuilder.vertex(j1, y + entryHeight2 + 2, 0.0D).endVertex();
                bufferbuilder.vertex(j1, y - 2, 0.0D).endVertex();
                bufferbuilder.vertex(i1, y - 2, 0.0D).endVertex();
                tessellator.end();
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex(i1 + 1, y + entryHeight2 + 1, 0.0D).endVertex();
                bufferbuilder.vertex(j1 - 1, y + entryHeight2 + 1, 0.0D).endVertex();
                bufferbuilder.vertex(j1 - 1, y - 1, 0.0D).endVertex();
                bufferbuilder.vertex(i1 + 1, y - 1, 0.0D).endVertex();
                tessellator.end();
                RenderSystem.enableTexture();
            }

            this.renderItem(ms, i, insideLeft, y, entryHeight2, mouseXIn, mouseYIn, partialTicks);
            y += entryHeight;
        }
    }

    private double getRelativeMouseX(double mouseX) {
        int j = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
        return mouseX - j;
    }

    private double getRelativeMouseY(double mouseY, int contentIndex) {
        int k = this.y0 + 4 - this.getScroll() + getMaxPosition(contentIndex) + this.headerHeight;
        double relativeMouseY = mouseY - k;

        //Content might be centered, adjust relative mouse y accordingly
        int contentHeight = getMaxPosition();
        int insideHeight = this.y1 - this.y0 - 4;

        if (contentHeight < insideHeight) {
            //it fits, so we can center it vertically
            relativeMouseY -= (insideHeight - contentHeight) / 2f;
        }
        return relativeMouseY;
    }

    //PASSTHROUGHS
    public void init(List<Renderable> renderables) {
        for (IScrollEntry entry : this.listEntries) {
            entry.init(renderables);
        }
    }

    public void updateScreen() {
        for (IScrollEntry entry : this.listEntries)
            entry.updateScreen();
    }

    public void drawTooltip(PoseStack ms, Screen guiScreen, int mouseX, int mouseY) {
        for (IScrollEntry entry : this.listEntries)
            entry.drawTooltip(ms, guiScreen, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char eventChar, int eventKey) {
        for (IScrollEntry entry : this.listEntries)
            entry.charTyped(eventChar, eventKey);
        return false;
    }

    public void onGuiClosed() {
        for (IScrollEntry entry : this.listEntries)
            entry.onGuiClosed();
    }

    //Make protected values available
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public interface IScrollEntry {
        void init(List<Renderable> renderables);

        void updateScreen();

        void drawTooltip(PoseStack ms, Screen guiScreen, int mouseX, int mouseY);

        boolean charTyped(char eventChar, int eventKey);

        void onGuiClosed();

        int getHeight();

        void updatePosition(int slotIndex, int x, int y, float partialTicks);

        void drawEntry(PoseStack ms, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks);

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY);

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);
    }
}
