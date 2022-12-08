package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.event.ClientScreenInputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
@Environment(EnvType.CLIENT)
public abstract class KeyEventMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At(value = "RETURN"))
    private void onKeyPress(long l, int i, int j, int k, int m, CallbackInfo ci) {
        if (l == minecraft.getWindow().getWindow()) {
            ClientScreenInputEvent.KEY_PRESS_EVENT.invoker().onKeyPress(i, j, k, m);
        }
    }

}
