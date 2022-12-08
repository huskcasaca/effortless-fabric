package dev.huskcasaca.effortless.network.protocol.unused;

import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/***
 * Sends a message to the client asking to clear the undo and redo stacks.
 */
//public record ClearUndoMessage(
//
//) {
//
//    public static void encode(ClearUndoMessage message, FriendlyByteBuf buf) {
//
//    }
//
//    public static ClearUndoMessage decode(FriendlyByteBuf buf) {
//        return new ClearUndoMessage();
//    }
//
//    public static class Serializer implements MessageSerializer<ClearUndoMessage> {
//
//        @Override
//        public void encode(ClearUndoMessage message, FriendlyByteBuf buf) {
//            ClearUndoMessage.encode(message, buf);
//        }
//
//        @Override
//        public ClearUndoMessage decode(FriendlyByteBuf buf) {
//            return ClearUndoMessage.decode(buf);
//        }
//
//    }
//
//    public static class ServerHandler implements ServerMessageHandler<ClearUndoMessage> {
//
//        @Override
//        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, ClearUndoMessage message, PacketSender responseSender) {
//
//        }
//
//    }
//
//    @Environment(EnvType.CLIENT)
//    public static class ClientHandler implements ClientMessageHandler<ClearUndoMessage> {
//
//        @Override
//        public void handleClientSide(Minecraft client, ClientPacketListener handler, ClearUndoMessage message, PacketSender responseSender) {
//            client.execute(() -> {
//                UndoRedo.clear(client.player);
//            });
//        }
//
//    }
//
//}
