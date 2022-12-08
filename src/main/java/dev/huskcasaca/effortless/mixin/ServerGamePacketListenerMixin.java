package dev.huskcasaca.effortless.mixin;

import dev.huskcasaca.effortless.buildreach.ReachHelper;
import dev.huskcasaca.effortless.buildmode.BuildActionHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.*;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin implements ServerEffortlessPacketListener {

    @Shadow public ServerPlayer player;

    @Shadow public abstract void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener);

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericFutureListener, CallbackInfo ci) {
        var resourceLocation = Packets.getKey(packet);
        if (resourceLocation == null) return;
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        send(new ClientboundCustomPayloadPacket(resourceLocation, buf), genericFutureListener);
        ci.cancel();
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket, CallbackInfo ci) {
        ResourceLocation resourceLocation = serverboundCustomPayloadPacket.getIdentifier();
        FriendlyByteBuf friendlyByteBuf = null;
        var deserializer = Packets.getDeserializer(resourceLocation);
        if (deserializer == null) {
            return;
        } else {
            ci.cancel();
        }
        try {
            friendlyByteBuf = serverboundCustomPayloadPacket.getData();
            deserializer.apply(friendlyByteBuf).handle(this);
        } finally {
            if (friendlyByteBuf != null) {
//                friendlyByteBuf.release();
            }
        }
    }

    @Override
    public void handle(ServerboundPlayerBreakBlockPacket packet) {
        BuildModeHandler.onBlockBrokenPacketReceived(player, packet);
    }

    @Override
    public void handle(ServerboundPlayerBuildActionPacket packet) {
        BuildActionHandler.performAction(player, packet.action());
    }

    @Override
    public void handle(ServerboundPlayerPlaceBlockPacket packet) {
        BuildModeHandler.onBlockPlacedPacketReceived(player, packet);
        // TODO: 18/11/22  //Nod RenderHandler to do the dissolve shader effect
        //            client.execute(() -> BlockPreviewRenderer.onBlocksPlaced());
    }

    @Override
    public void handle(ServerboundPlayerSetBuildModePacket packet) {
        BuildModeHelper.setModeSettings(player, BuildModeHelper.sanitize(packet.modeSettings(), player));
        BuildModeHandler.initializeMode(player);
    }

    @Override
    public void handle(ServerboundPlayerSetBuildModifierPacket packet) {
        BuildModifierHelper.setModifierSettings(player, BuildModifierHelper.sanitize(packet.modifierSettings(), player));
    }

    @Override
    public void handle(ServerboundPlayerSetBuildReachPacket packet) {
        ReachHelper.setReachSettings(player, ReachHelper.sanitize(packet.reachSettings(), player));
        BuildModeHandler.initializeMode(player);
    }

}
