package dev.huskcasaca.effortless.gui.buildmode;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.gui.widget.ExtendedButton;
import dev.huskcasaca.effortless.gui.widget.Slider;
import dev.huskcasaca.effortless.mixin.ScreenRenderHeaderAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlayerSettingsScreen extends Screen {

    protected int left, right, top, bottom;
    protected boolean showShaderList = false;
    private Button shaderTypeButton;
    private ShaderTypeList shaderTypeList;
    private Button closeButton;

    public PlayerSettingsScreen() {
        super(new TranslatableComponent("effortless.screen.player_settings"));
    }

    @Override
    protected void init() {
        left = this.width / 2 - 140;
        right = this.width / 2 + 140;
        top = this.height / 2 - 100;
        bottom = this.height / 2 + 100;

        int yy = top;
        shaderTypeList = new ShaderTypeList(this.minecraft);
        addWidget(shaderTypeList);
        //TODO set selected name
        Component currentShaderName = ShaderType.DISSOLVE_BLUE.name;
        shaderTypeButton = new ExtendedButton(right - 180, yy, 180, 20, currentShaderName, (button) -> {
            showShaderList = !showShaderList;
        });
        addRenderableOnly(shaderTypeButton);

        yy += 50;
        Slider slider = new Slider(right - 200, yy, 200, 20, TextComponent.EMPTY, TextComponent.EMPTY, 0.5, 2.0, 1.0, true);
        addRenderableOnly(slider);

        closeButton = new ExtendedButton(left + 50, bottom - 20, 180, 20, new TextComponent("Done"), (button) -> this.minecraft.player.closeContainer());
        addRenderableOnly(closeButton);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);

        int yy = top;
        font.draw(ms, "Shader type", left, yy + 5, 0xFFFFFF);

        yy += 50;
        font.draw(ms, "Shader speed", left, yy + 5, 0xFFFFFF);

        super.render(ms, mouseX, mouseY, partialTicks);

        if (showShaderList)
            this.shaderTypeList.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (showShaderList) {
            if (!shaderTypeList.isMouseOver(mouseX, mouseY) && !shaderTypeButton.isMouseOver(mouseX, mouseY))
                showShaderList = false;
        }
        return true;
    }

    @Override
    public void removed() {
        ShaderTypeList.ShaderTypeEntry selectedShader = shaderTypeList.getSelected();
        //TODO save and remove
    }

    public enum ShaderType {
        DISSOLVE_BLUE("Dissolve Blue"),
        DISSOLVE_ORANGE("Dissolve Orange");

        public Component name;

        ShaderType(Component name) {
            this.name = name;
        }

        ShaderType(String name) {
            this.name = new TextComponent(name);
        }
    }

    //Inspired by LanguageScreen
    @Environment(EnvType.CLIENT)
    class ShaderTypeList extends ObjectSelectionList<PlayerSettingsScreen.ShaderTypeList.ShaderTypeEntry> {

        public ShaderTypeList(Minecraft mcIn) {
            super(mcIn, 180, 140, top + 20, top + 100, 18);
            this.setLeftPos(right - width);

            for (int i = 0; i < 40; i++) {

                for (ShaderType shaderType : ShaderType.values()) {
                    ShaderTypeEntry shaderTypeEntry = new ShaderTypeEntry(shaderType);
                    addEntry(shaderTypeEntry);
                    //TODO setSelected to this if appropriate
                }

            }

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        public void setSelected(PlayerSettingsScreen.ShaderTypeList.ShaderTypeEntry selected) {
            super.setSelected(selected);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            Effortless.log("Selected shader " + selected.shaderType.name);
            shaderTypeButton.setMessage(selected.shaderType.name);
//            showShaderList = false;
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            if (!showShaderList) return false;
            return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            if (!showShaderList) return false;
            return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }

        @Override
        public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
            if (!showShaderList) return false;
            return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }

        @Override
        public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
            if (!showShaderList) return false;
            return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        }

        @Override
        public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
            if (!showShaderList) return false;
            return super.isMouseOver(p_isMouseOver_1_, p_isMouseOver_3_);
        }

        protected boolean isFocused() {
            return PlayerSettingsScreen.this.getFocused() == this;
        }

        @Override
        protected int getScrollbarPosition() {
            return right - 6;
        }

        //From AbstractList, disabled parts
        @Override
        public void render(PoseStack ms, int p_render_1_, int p_render_2_, float p_render_3_) {
            this.renderBackground(ms);
            int i = this.getScrollbarPosition();
            int j = i + 6;
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
//            this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(this.x0, this.y1, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.vertex(this.x1, this.y1, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.vertex(this.x1, this.y0, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.vertex(this.x0, this.y0, 0.0D).color(20, 20, 20, 180).endVertex();
            tessellator.end();
            int k = this.getRowLeft();
            int l = this.y0 + 4 - (int) this.getScrollAmount();
            // TODO: 7/9/22 access private field renderHeader
            if (((ScreenRenderHeaderAccessor) this).isRenderHeader()) {
                this.renderHeader(ms, k, l, tessellator);
            }

            this.renderList(ms, k, l, p_render_1_, p_render_2_, p_render_3_);

            RenderSystem.disableDepthTest();
//            this.renderHoleBackground(0, this.y0, 255, 255);
//            this.renderHoleBackground(this.y1, this.height, 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableTexture();
//            int i1 = 4;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.x0, (double)(this.y0 + 4), 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)(this.y0 + 4), 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x0, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            tessellator.draw();
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.x0, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)(this.y1 - 4), 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x0, (double)(this.y1 - 4), 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            tessellator.draw();

            //SCROLLBAR
            int j1 = this.getMaxScroll();
            if (j1 > 0) {
                int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
                k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
                int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
                if (l1 < this.y0) {
                    l1 = this.y0;
                }

                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(i, this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(j, this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(j, this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(i, this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                tessellator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(i, l1 + k1, 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(j, l1 + k1, 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(j, l1, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(i, l1, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                tessellator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(i, l1 + k1 - 1, 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(j - 1, l1 + k1 - 1, 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(j - 1, l1, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(i, l1, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tessellator.end();
            }

//            this.renderDecorations(p_render_1_, p_render_2_);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

        public int getMaxScroll() {
            return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
        }

        @Environment(EnvType.CLIENT)
        public class ShaderTypeEntry extends ObjectSelectionList.Entry<ShaderTypeEntry> {
            private final ShaderType shaderType;

            public ShaderTypeEntry(ShaderType shaderType) {
                this.shaderType = shaderType;
            }

            @Override
            public void render(PoseStack ms, int itemIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
                if (rowTop + 10 > ShaderTypeList.this.y0 && rowTop + rowHeight - 5 < ShaderTypeList.this.y1)
                    drawString(ms, font, shaderType.name, ShaderTypeList.this.x0 + 8, rowTop + 4, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                if (p_mouseClicked_5_ == 0) {
                    setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Component getNarration() {
                return null;
            }
        }
    }
}
