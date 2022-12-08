package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.buildmode.ModeSettingsManager;
import dev.huskcasaca.effortless.config.ConfigManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerPickupMixin {

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    public AABB inflate(AABB instance, double d, double e, double f) {
        final var player = ((Player) (Object) this);
        var enable = ModeSettingsManager.getModeSettings(player).enableMagnet();
        var maxReach = ConfigManager.getGlobalBuildConfig().getMaxReachDistance();

        if (enable) {
            return instance.inflate(maxReach, maxReach, maxReach);
        } else {
            return instance.inflate(d, e, f);
        }
    }

}
