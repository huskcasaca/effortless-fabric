package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.Effortless;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Send packet to client to translate and log the containing message
 */
public record TranslatedLogMessage(
        String prefix,
        String translationKey,
        String suffix,
        boolean actionBar
) implements Message {

    public TranslatedLogMessage() {
        this("", "", "", false);
    }

    public TranslatedLogMessage(String prefix, String translationKey, String suffix, boolean actionBar) {
        this.prefix = prefix;
        this.translationKey = translationKey;
        this.suffix = suffix;
        this.actionBar = actionBar;
    }

    public static void encode(TranslatedLogMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.prefix);
        buf.writeUtf(message.translationKey);
        buf.writeUtf(message.suffix);
        buf.writeBoolean(message.actionBar);
    }

    public static TranslatedLogMessage decode(FriendlyByteBuf buf) {
        return new TranslatedLogMessage(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean());
    }

    public static class Serializer implements MessageSerializer<TranslatedLogMessage> {

        @Override
        public void encode(TranslatedLogMessage message, FriendlyByteBuf buf) {
            TranslatedLogMessage.encode(message, buf);
        }

        @Override
        public TranslatedLogMessage decode(FriendlyByteBuf buf) {
            return TranslatedLogMessage.decode(buf);
        }

    }

    public static class ServerHandler implements ServerMessageHandler<TranslatedLogMessage> {

        @Override
        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, TranslatedLogMessage message, PacketSender responseSender) {

        }

    }

    public static class ClientHandler implements ClientMessageHandler<TranslatedLogMessage> {

        @Override
        public void handleClientSide(Minecraft client, LocalPlayer player, ClientPacketListener handler, TranslatedLogMessage message, PacketSender responseSender) {

            client.execute(() -> {
                Effortless.logTranslate(player, message.prefix, message.translationKey, message.suffix, message.actionBar);
            });
        }

    }
}
