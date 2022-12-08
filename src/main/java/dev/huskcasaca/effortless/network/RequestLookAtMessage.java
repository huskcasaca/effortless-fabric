package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.EffortlessClient;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/***
 * Sends a message to the client asking for its lookat (objectmouseover) data.
 * This is then sent back with a BlockPlacedMessage.
 */
public record RequestLookAtMessage(
        boolean placeStartPos
) implements Message {

    public RequestLookAtMessage() {
        this(false);
    }

    public static void encode(RequestLookAtMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.placeStartPos);
    }

    public static RequestLookAtMessage decode(FriendlyByteBuf buf) {
        boolean placeStartPos = buf.readBoolean();
        return new RequestLookAtMessage(placeStartPos);
    }

    public boolean getPlaceStartPos() {
        return placeStartPos;
    }

    public static class Serializer implements MessageSerializer<RequestLookAtMessage> {

        @Override
        public void encode(RequestLookAtMessage message, FriendlyByteBuf buf) {
            RequestLookAtMessage.encode(message, buf);
        }

        @Override
        public RequestLookAtMessage decode(FriendlyByteBuf buf) {
            return RequestLookAtMessage.decode(buf);
        }

    }

    public static class ServerHandler implements ServerMessageHandler<RequestLookAtMessage> {

        @Override
        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, RequestLookAtMessage message, PacketSender responseSender) {

        }
    }

    @Environment(EnvType.CLIENT)
    public static class ClientHandler implements ClientMessageHandler<RequestLookAtMessage> {

        @Override
        public void handleClientSide(Minecraft client, LocalPlayer player, ClientPacketListener handler, RequestLookAtMessage message, PacketSender responseSender) {
            client.execute(() -> {
//            //Send back your info
//                var player = client.player;
//            //Prevent double placing in normal mode with placeStartPos false
//            //Unless QuickReplace is on, then we do need to place start pos.
                if (EffortlessClient.previousLookAt.getType() == HitResult.Type.BLOCK) {
                    PacketHandler.sendToServer(new BlockPlacedMessage((BlockHitResult) EffortlessClient.previousLookAt, message.getPlaceStartPos()));
                } else {
                    PacketHandler.sendToServer(new BlockPlacedMessage());
                }
            });
        }
    }
}
