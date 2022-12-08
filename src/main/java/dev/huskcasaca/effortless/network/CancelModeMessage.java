package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Sends a message to the server indicating that a buildmode needs to be canceled for a player
 */
public record CancelModeMessage(
) implements Message {

    public static void encode(CancelModeMessage message, FriendlyByteBuf buf) {
    }

    public static CancelModeMessage decode(FriendlyByteBuf buf) {
        return new CancelModeMessage();
    }

    public static class Serializer implements MessageSerializer<CancelModeMessage> {

        @Override
        public void encode(CancelModeMessage message, FriendlyByteBuf buf) {
            CancelModeMessage.encode(message, buf);
        }

        @Override
        public CancelModeMessage decode(FriendlyByteBuf buf) {
            return CancelModeMessage.decode(buf);
        }

    }

    public static class ServerHandler implements ServerMessageHandler<CancelModeMessage> {

        @Override
        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, CancelModeMessage message, PacketSender responseSender) {
            server.execute(() -> {
                BuildModeHandler.initializeMode(player);
            });
        }

    }

    @Environment(EnvType.CLIENT)
    public static class ClientHandler implements ClientMessageHandler<CancelModeMessage> {

        @Override
        public void handleClientSide(Minecraft client, LocalPlayer player, ClientPacketListener handler, CancelModeMessage message, PacketSender responseSender) {
            client.execute(() -> {
                BuildModeHandler.initializeMode(player);
            });
        }

    }
}
