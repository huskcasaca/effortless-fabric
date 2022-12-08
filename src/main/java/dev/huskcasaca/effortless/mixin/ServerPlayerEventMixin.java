package dev.huskcasaca.effortless.mixin;


import dev.huskcasaca.effortless.Effortless;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerEventMixin {

    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void onPlayerChangeDimension(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cir) {
        Effortless.onPlayerChangedDimension((ServerPlayer) (Object) cir.getReturnValue());
    }

}
