package dev.huskcasaca.effortless.screen.buildmode;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.screen.widget.ExtendedButton;
import dev.huskcasaca.effortless.screen.widget.Slider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlayerSettingsScreen extends Screen {

    protected int left, right, top, bottom;
    protected boolean showShaderList = false;
    private Button shaderTypeButton;
    private ShaderTypeList shaderTypeList;
    private Button closeButton;

    public PlayerSettingsScreen() {
        super(new TranslatableComponent(String.join(".", Effortless.MOD_ID, "screen", "player_settings")));
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);

        int yy = top;
        font.draw(poseStack, "Shader type", left, yy + 5, 0xFFFFFF);

        yy += 50;
        font.draw(poseStack, "Shader speed", left, yy + 5, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTicks);

        if (showShaderList)
            this.shaderTypeList.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
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

        private final Component name;

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
        public void setSelected(PlayerSettingsScreen.ShaderTypeList.ShaderTypeEntry entry) {
            super.setSelected(entry);
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            soundManager.reload();
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            Effortless.log("Selected shader " + entry.shaderType.name);
            shaderTypeButton.setMessage(entry.shaderType.name);
//            showShaderList = false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!showShaderList) return false;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (!showShaderList) return false;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double f, double g) {
            if (!showShaderList) return false;
            return super.mouseDragged(mouseX, mouseY, button, f, g);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrolled) {
            if (!showShaderList) return false;
            return super.mouseScrolled(mouseX, mouseY, scrolled);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!showShaderList) return false;
            return super.isMouseOver(mouseX, mouseY);
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
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(poseStack);
            int scrollbarPosition = this.getScrollbarPosition();
            int scrollbarPositionWithOffset = scrollbarPosition + 6;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
//            this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            float partialTicks = 32.0F;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(this.x0, this.y1, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.vertex(this.x1, this.y1, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.vertex(this.x1, this.y0, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.vertex(this.x0, this.y0, 0.0D).color(20, 20, 20, 180).endVertex();
            tesselator.end();
            int k = this.getRowLeft();
            int l = this.y0 + 4 - (int) this.getScrollAmount();
            // TODO: 7/9/22 access private field renderHeader
            if (renderHeader) {
                this.renderHeader(poseStack, k, l, tesselator);
            }

            int insideLeft = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
            int insideTop = this.y0 + 4;

            this.renderList(poseStack, insideLeft, insideTop, mouseX, mouseY, partialTicks);
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
//            tesselator.draw();
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.x0, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)(this.y1 - 4), 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x0, (double)(this.y1 - 4), 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            tesselator.draw();

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
                bufferbuilder.vertex(scrollbarPosition, this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(scrollbarPositionWithOffset, this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(scrollbarPositionWithOffset, this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex(scrollbarPosition, this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                tesselator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(scrollbarPosition, l1 + k1, 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(scrollbarPositionWithOffset, l1 + k1, 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(scrollbarPositionWithOffset, l1, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex(scrollbarPosition, l1, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                tesselator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(scrollbarPosition, l1 + k1 - 1, 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(scrollbarPositionWithOffset - 1, l1 + k1 - 1, 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(scrollbarPositionWithOffset - 1, l1, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex(scrollbarPosition, l1, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tesselator.end();
            }

//            this.renderDecorations(mouseX, mouseY);
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
            public void render(PoseStack poseStack, int itemIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
                if (rowTop + 10 > ShaderTypeList.this.y0 && rowTop + rowHeight - 5 < ShaderTypeList.this.y1)
                    drawString(poseStack, font, shaderType.name, ShaderTypeList.this.x0 + 8, rowTop + 4, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
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
