package dev.huskcasaca.effortless.screen.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessClient;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.mixin.KeyMappingAccessor;
import dev.huskcasaca.effortless.mixin.ScreenRenderablesAccessor;
import dev.huskcasaca.effortless.network.ModifierSettingsMessage;
import dev.huskcasaca.effortless.network.PacketHandler;
import dev.huskcasaca.effortless.screen.widget.ScrollPane;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ModifierSettingsScreen extends Screen {

    private ScrollPane scrollPane;
    private Button buttonClose;

    private MirrorSettingsPane mirrorSettingsPane;
    private ArraySettingsPane arraySettingsPane;
    private RadialMirrorSettingsPane radialMirrorSettingsPane;

    public ModifierSettingsScreen() {
        super(new TranslatableComponent("effortless.screen.modifier_settings"));
    }

    @Override
    //Create buttons and labels and add them to buttonList/labelList
    protected void init() {

        scrollPane = new ScrollPane(this, font, 8, height - 30);

        mirrorSettingsPane = new MirrorSettingsPane(scrollPane);
        scrollPane.AddListEntry(mirrorSettingsPane);

        arraySettingsPane = new ArraySettingsPane(scrollPane);
        scrollPane.AddListEntry(arraySettingsPane);

        radialMirrorSettingsPane = new RadialMirrorSettingsPane(scrollPane);
        scrollPane.AddListEntry(radialMirrorSettingsPane);

        scrollPane.init(((ScreenRenderablesAccessor) this).getRenderables());

        //Close button
        int y = height - 26;
        buttonClose = new Button(width / 2 - 100, y, 200, 20, new TextComponent("Close"), (button) -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.closeContainer();
            }
        });
        addRenderableOnly(buttonClose);
    }

    @Override
    //Process general logic, i.e. hide buttons
    public void tick() {
        scrollPane.updateScreen();

        handleMouseInput();
    }

    @Override
    //Set colors using GL11, use the fontObj field to display text
    //Use drawTexturedModalRect() to transfers areas of a texture resource to the screen
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);

        scrollPane.render(ms, mouseX, mouseY, partialTicks);

        buttonClose.render(ms, mouseX, mouseY, partialTicks);

        scrollPane.drawTooltip(ms, this, mouseX, mouseY);
    }


    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        scrollPane.charTyped(typedChar, keyCode);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int p_96553_, int p_96554_) {
        if (keyCode == ((KeyMappingAccessor) EffortlessClient.keyBindings[0]).getKey().getValue()) {
            return true;
        }
        return super.keyPressed(keyCode, p_96553_, p_96554_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        ((ScreenRenderablesAccessor) this).getRenderables().forEach(renderable -> {
            if (renderable instanceof Button button) {
                button.mouseClicked(mouseX, mouseY, mouseButton);
            }
        });
        return scrollPane.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (state != 0 || !scrollPane.mouseReleased(mouseX, mouseY, state)) {
            return super.mouseReleased(mouseX, mouseY, state);
        }
        return false;
    }

    public void handleMouseInput() {
        //super.handleMouseInput();
        scrollPane.handleMouseInput();

        //Scrolling numbers
//        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
//        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
//        numberFieldList.forEach(numberField -> numberField.handleMouseInput(mouseX, mouseY));
    }

    @Override
    public void removed() {
        scrollPane.onGuiClosed();

        var arraySettings = arraySettingsPane.getArraySettings();
        var mirrorSettings = mirrorSettingsPane.getMirrorSettings();
        var radialMirrorSettings = radialMirrorSettingsPane.getRadialMirrorSettings();

        var modifierSettings = ModifierSettingsManager.getModifierSettings(minecraft.player);

        modifierSettings = new ModifierSettingsManager.ModifierSettings(arraySettings, mirrorSettings, radialMirrorSettings, modifierSettings.quickReplace());

        //Sanitize
        String error = ModifierSettingsManager.getSanitizeMessage(modifierSettings, minecraft.player);
        if (!error.isEmpty()) Effortless.log(minecraft.player, error);

        modifierSettings = ModifierSettingsManager.sanitize(modifierSettings, minecraft.player);
        ModifierSettingsManager.setModifierSettings(minecraft.player, modifierSettings);

        //Send to server
        PacketHandler.sendToServer(new ModifierSettingsMessage(modifierSettings));

        // TODO: 17/9/22 grabMouse
//        Minecraft.getInstance().mouseHandler.grabMouse();
    }

}
