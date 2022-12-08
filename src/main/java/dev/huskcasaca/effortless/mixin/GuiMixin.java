package dev.huskcasaca.effortless.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.config.ConfigManager;
import dev.huskcasaca.effortless.screen.buildmode.RadialMenuScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow private float autosaveIndicatorValue;

    @Shadow private float lastAutosaveIndicatorValue;

    @Shadow public abstract Font getFont();

    @Shadow private int screenWidth;

    @Shadow private int screenHeight;

    @Shadow protected abstract Player getCameraPlayer();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSavingIndicator(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void renderGui(PoseStack poseStack, float f, CallbackInfo ci) {
        renderBuildMode(poseStack);
    }

    private void renderBuildMode(PoseStack poseStack) {
        if (!ConfigManager.getGlobalPreviewConfig().isShowBuildInfo()) {
            return;
        }
        if ((minecraft.options.showAutosaveIndicator && (autosaveIndicatorValue > 0.0F || lastAutosaveIndicatorValue > 0.0F)) && (Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F)) > 8)) {
            return;
        }
        if (RadialMenuScreen.getInstance().isVisible()) {
            return;
        }
        var player = minecraft.player;
        var mode = BuildModeHelper.getBuildMode(getCameraPlayer());

        if (mode == BuildMode.DISABLE) {
            return;
        }

        var texts = new ArrayList<Component>();

        var modifier = BuildModifierHelper.getModifierSettings(player);

        if (modifier.radialMirrorSettings().enabled()) {
            texts.add(new TextComponent(
                    ChatFormatting.GOLD + "Radial Mirror" + ChatFormatting.RESET + " "
                            + ChatFormatting.WHITE + "S" + modifier.radialMirrorSettings().slices() + " "
                            + ChatFormatting.WHITE + "R" + ChatFormatting.WHITE + modifier.radialMirrorSettings().radius() + ChatFormatting.RESET
            ));
        }

        if (modifier.mirrorSettings().enabled()) {
            texts.add(new TextComponent(
                    ChatFormatting.GOLD + "Mirror" + ChatFormatting.RESET + " "
                            + (modifier.mirrorSettings().mirrorX() ? ChatFormatting.GREEN : ChatFormatting.WHITE) + "X" + (modifier.mirrorSettings().mirrorY() ? ChatFormatting.GREEN : ChatFormatting.WHITE) + "Y" + (modifier.mirrorSettings().mirrorZ() ? ChatFormatting.GREEN : ChatFormatting.WHITE) + "Z" + " "
                            + ChatFormatting.WHITE + "R" + ChatFormatting.WHITE + modifier.mirrorSettings().radius() + ChatFormatting.RESET
            ));
        }

        if (modifier.arraySettings().enabled()) {
            texts.add(new TextComponent(
                    ChatFormatting.GOLD + "Array" + ChatFormatting.RESET + " "
                            + ChatFormatting.WHITE + "X" + modifier.arraySettings().offset().getX() + "Y" + modifier.arraySettings().offset().getY() + "Z" + modifier.arraySettings().offset().getZ() + " "
                            + ChatFormatting.WHITE + "C" + ChatFormatting.WHITE + modifier.arraySettings().count() + ChatFormatting.RESET
            ));
        }


        if (modifier.enableReplace()) {
            texts.add(BuildModifierHelper.getReplaceModeName(player));
        }

        texts.add(new TextComponent(ChatFormatting.AQUA + BuildModeHelper.getTranslatedModeOptionName(player) + ChatFormatting.RESET));

        var font = this.getFont();
        var positionY = screenHeight - 15;
        for (Component text : texts) {
            int j = font.width(text);
            font.drawShadow(poseStack, text, screenWidth - j - 10, positionY, 16777215);
            positionY -= 10;
        }


    }
}
