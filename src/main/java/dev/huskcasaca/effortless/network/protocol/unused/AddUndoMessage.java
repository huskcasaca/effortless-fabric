package dev.huskcasaca.effortless.network.protocol.unused;

import dev.huskcasaca.effortless.buildmodifier.BlockSet;
import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/***
 * Sends a message to the client asking to add a block to the undo stack.
 */
//public record AddUndoMessage(
//        BlockPos coordinate,
//        BlockState previousBlockState,
//        BlockState newBlockState
//) implements Packet<> {
//
//    public AddUndoMessage() {
//        this(BlockPos.ZERO, null, null);
//    }
//
//    public static void encode(AddUndoMessage message, FriendlyByteBuf friendlyByteBuf) {
//        friendlyByteBuf.writeInt(message.coordinate.getX());
//        friendlyByteBuf.writeInt(message.coordinate.getY());
//        friendlyByteBuf.writeInt(message.coordinate.getZ());
//        friendlyByteBuf.writeInt(Block.getId(message.previousBlockState));
//        friendlyByteBuf.writeInt(Block.getId(message.newBlockState));
//    }
//
//    public static AddUndoMessage decode(FriendlyByteBuf friendlyByteBuf) {
//        var coordinate = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
//        var previousBlockState = Block.stateById(friendlyByteBuf.readInt());
//        var newBlockState = Block.stateById(friendlyByteBuf.readInt());
//        return new AddUndoMessage(coordinate, previousBlockState, newBlockState);
//    }
//
//    public static class Serializer implements MessageSerializer<AddUndoMessage> {
//
//        @Override
//        public void encode(AddUndoMessage message, FriendlyByteBuf buf) {
//            AddUndoMessage.encode(message, buf);
//        }
//
//        @Override
//        public AddUndoMessage decode(FriendlyByteBuf buf) {
//            return AddUndoMessage.decode(buf);
//        }
//
//    }
//
//    public static class ServerHandler implements ServerMessageHandler<AddUndoMessage> {
//
//        @Override
//        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, AddUndoMessage message, PacketSender responseSender) {
//            // client only
//        }
//    }
//
//    @Environment(EnvType.CLIENT)
//    public static class ClientHandler implements ClientMessageHandler<AddUndoMessage> {
//
//        @Override
//        public void handleClientSide(Minecraft client, ClientPacketListener handler, AddUndoMessage message, PacketSender responseSender) {
//            client.execute(() -> {
//                if (client.player != null) {
//                    final var coordinates = new ArrayList<BlockPos>();
//                    final var previousBlockState = new ArrayList<BlockState>();
//                    final var newBlockState = new ArrayList<BlockState>();
//                    coordinates.add(message.coordinate());
//                    previousBlockState.add(message.previousBlockState());
//                    newBlockState.add(message.newBlockState());
//
//                    UndoRedo.addUndo(client.player, new BlockSet(
//                            coordinates,
//                            previousBlockState,
//                            newBlockState,
//                            new Vec3(0, 0, 0),
//                            message.coordinate(), message.coordinate()
//                    ));
//                }
//            });
//        }
//    }
//}
