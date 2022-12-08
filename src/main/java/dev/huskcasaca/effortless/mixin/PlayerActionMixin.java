package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.ModeSettingsManager;
import dev.huskcasaca.effortless.helper.CompatHelper;
import dev.huskcasaca.effortless.network.BlockBrokenMessage;
import dev.huskcasaca.effortless.network.BlockPlacedMessage;
import dev.huskcasaca.effortless.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.huskcasaca.effortless.EffortlessClient.getLookingAt;

@Mixin(Minecraft.class)
public class PlayerActionMixin {

    @Shadow
    @Nullable
    public HitResult hitResult;

    @Shadow
    protected int missTime;

//        Minecraft mc = Minecraft.getInstance();
//        LocalPlayer player = mc.player;
//        if (player == null) return;
//        var buildMode = ModeSettingsManager.getModeSettings(player).buildMode();
//
//        if (Minecraft.getInstance().screen != null ||
//                buildMode == BuildMode.VANILLA ||
//                RadialMenuScreen.instance.isVisible()) {
//            return;
//        }

    // TODO: 15/9/22 extract to EffortlessClient class
    // startAttack
    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        var player = Minecraft.getInstance().player;
        var buildMode = ModeSettingsManager.getModeSettings(player).buildMode();

        if (buildMode == BuildMode.VANILLA) return;

        if (this.hitResult.getType() != HitResult.Type.ENTITY) {
            if (player.isHandsBusy()) {
                // let vanilla handle attack action

                return;
            } else {
                if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                    // let vanilla handle entity attack

                } else {
                    // FIXME: 15/9/22 grass hit result is incorrect using getLookingAt
                    HitResult lookingAt = getLookingAt(player);
                    if (lookingAt != null && lookingAt.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockLookingAt = (BlockHitResult) lookingAt;

                        BuildModeHandler.onBlockBrokenMessage(player, new BlockBrokenMessage(blockLookingAt));
                        PacketHandler.sendToServer(new BlockBrokenMessage(blockLookingAt));

                        //play sound if further than normal
                        if ((blockLookingAt.getLocation().subtract(player.getEyePosition(1f))).lengthSqr() > 25f) {

                            var blockPos = blockLookingAt.getBlockPos();
                            var state = player.level.getBlockState(blockPos);
                            var soundtype = state.getBlock().getSoundType(state);
                            player.level.playSound(player, player.blockPosition(), soundtype.getBreakSound(), SoundSource.BLOCKS,
                                    0.4f, soundtype.getPitch());
                        }
                        cir.setReturnValue(true);
                    } else {
                        BuildModeHandler.onBlockBrokenMessage(player, new BlockBrokenMessage());
                        PacketHandler.sendToServer(new BlockBrokenMessage());
                        cir.setReturnValue(false);
                    }
                    player.swing(InteractionHand.MAIN_HAND);
                    cir.cancel();
                }
            }
        }


    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean bl, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        var buildMode = ModeSettingsManager.getModeSettings(player).buildMode();

        if (buildMode == BuildMode.VANILLA) return;

        if (!bl) {
            this.missTime = 0;
        }

        if (player.isHandsBusy()) {

        } else {
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {

            } else {
                ci.cancel();
            }
        }

    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), cancellable = true)
    private void onStartOption(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        var buildMode = ModeSettingsManager.getModeSettings(player).buildMode();

        if (buildMode == BuildMode.VANILLA) return;

        if (hitResult != null && hitResult.getType() != HitResult.Type.ENTITY) {

            if (buildMode == BuildMode.VANILLA) {
                // let vanilla handle use action

            } else {
                ItemStack currentItemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (currentItemStack.getItem() instanceof BlockItem || (CompatHelper.isItemBlockProxy(currentItemStack) && !player.isShiftKeyDown())) {

                    ItemStack itemStack = CompatHelper.getItemBlockFromStack(currentItemStack);

                    //find position in distance
                    HitResult lookingAt = getLookingAt(player);
                    if (lookingAt != null && lookingAt.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockLookingAt = (BlockHitResult) lookingAt;

                        BuildModeHandler.onBlockPlacedMessage(player, new BlockPlacedMessage(blockLookingAt, true));
                        PacketHandler.sendToServer(new BlockPlacedMessage(blockLookingAt, true));

                        //play sound if further than normal
                        if ((blockLookingAt.getLocation().subtract(player.getEyePosition(1f))).lengthSqr() > 25f &&
                                itemStack.getItem() instanceof BlockItem) {

                            var state = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
                            var blockPos = blockLookingAt.getBlockPos();
                            var soundType = state.getBlock().getSoundType(state);
                            player.level.playSound(player, player.blockPosition(), soundType.getPlaceSound(), SoundSource.BLOCKS,
                                    0.4f, soundType.getPitch());
                            player.swing(InteractionHand.MAIN_HAND);
                        }
                    } else {
                        BuildModeHandler.onBlockPlacedMessage(player, new BlockPlacedMessage());
                        PacketHandler.sendToServer(new BlockPlacedMessage());
                    }
                }

                ci.cancel();
            }

        }


    }
}
