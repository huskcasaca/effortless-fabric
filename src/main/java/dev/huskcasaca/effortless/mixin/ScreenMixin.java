package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.EffortlessClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ScreenMixin {

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;reset()V", shift = At.Shift.NONE), cancellable = true)
    private void onScreenOpening(@Nullable Screen screen, CallbackInfo ci) {
//		if (screen != null) {
//			ClientScreenEvent.SCREEN_OPENING_EVENT.invoker().onScreenOpening(screen);
//		}
    }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V", shift = At.Shift.NONE))
    private void onScreenClosing(@Nullable Screen screen, CallbackInfo ci) {
        EffortlessClient.onScreenEvent(screen);
    }


}
