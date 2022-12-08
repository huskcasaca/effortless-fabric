package dev.huskcasaca.effortless.screen.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.control.Keys;
import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.ServerboundPlayerSetBuildModifierPacket;
import dev.huskcasaca.effortless.screen.widget.ScrollPane;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModifierSettingsScreen extends Screen {

    private ScrollPane scrollPane;
    private Button buttonDone;

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

        arraySettingsPane = new ArraySettingsPane(scrollPane);
        scrollPane.addListEntry(arraySettingsPane);

        mirrorSettingsPane = new MirrorSettingsPane(scrollPane);
        scrollPane.addListEntry(mirrorSettingsPane);

        radialMirrorSettingsPane = new RadialMirrorSettingsPane(scrollPane);
        scrollPane.addListEntry(radialMirrorSettingsPane);

        scrollPane.init(this.renderables);

        //Close button
        int y = height - 26;
        buttonDone = new Button(width / 2 - 100, y, 200, 20, new TextComponent("Done"), (button) -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.closeContainer();
            }
        });
        addRenderableOnly(buttonDone);
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

        buttonDone.render(ms, mouseX, mouseY, partialTicks);

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
        if (keyCode == Keys.MODIFIER_MENU.getKeyMapping().key.getValue()) {
            return true;
        }
        return super.keyPressed(keyCode, p_96553_, p_96554_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        renderables.forEach(renderable -> {
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

        var modifierSettings = BuildModifierHelper.getModifierSettings(minecraft.player);

        modifierSettings = new ModifierSettings(arraySettings, mirrorSettings, radialMirrorSettings, modifierSettings.replaceMode());

        //Sanitize
        String error = BuildModifierHelper.getSanitizeMessage(modifierSettings, minecraft.player);
        if (!error.isEmpty()) Effortless.log(minecraft.player, error);

        modifierSettings = BuildModifierHelper.sanitize(modifierSettings, minecraft.player);
        BuildModifierHelper.setModifierSettings(minecraft.player, modifierSettings);

        //Send to server
        Packets.sendToServer(new ServerboundPlayerSetBuildModifierPacket(modifierSettings));

        // TODO: 17/9/22 grabMouse
//        Minecraft.getInstance().mouseHandler.grabMouse();
    }

}
