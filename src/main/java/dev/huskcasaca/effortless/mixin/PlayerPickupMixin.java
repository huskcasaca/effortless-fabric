package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.buildreach.ReachHelper;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerPickupMixin {

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    public AABB inflate(AABB instance, double d, double e, double f) {
        final var player = ((Player) (Object) this);
        var enable = BuildModeHelper.getModeSettings(player).enableMagnet();
        var maxReach = ReachHelper.getReachSettings(player).maxReachDistance();

        if (enable) {
            return instance.inflate(maxReach, maxReach, maxReach);
        } else {
            return instance.inflate(d, e, f);
        }
    }

}
