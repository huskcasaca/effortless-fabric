package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.buildmodifier.BlockSet;
import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
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
public record AddUndoMessage(
        BlockPos coordinate,
        BlockState previousBlockState,
        BlockState newBlockState
) implements Message {

    public AddUndoMessage() {
        this(BlockPos.ZERO, null, null);
    }

    public static void encode(AddUndoMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.coordinate.getX());
        buf.writeInt(message.coordinate.getY());
        buf.writeInt(message.coordinate.getZ());
        buf.writeInt(Block.getId(message.previousBlockState));
        buf.writeInt(Block.getId(message.newBlockState));
    }

    public static AddUndoMessage decode(FriendlyByteBuf buf) {
        var coordinate = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        var previousBlockState = Block.stateById(buf.readInt());
        var newBlockState = Block.stateById(buf.readInt());
        return new AddUndoMessage(coordinate, previousBlockState, newBlockState);
    }

    public static class Serializer implements MessageSerializer<AddUndoMessage> {

        @Override
        public void encode(AddUndoMessage message, FriendlyByteBuf buf) {
            AddUndoMessage.encode(message, buf);
        }

        @Override
        public AddUndoMessage decode(FriendlyByteBuf buf) {
            return AddUndoMessage.decode(buf);
        }

    }

    public static class ServerHandler implements ServerMessageHandler<AddUndoMessage> {

        @Override
        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, AddUndoMessage message, PacketSender responseSender) {
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ClientHandler implements ClientMessageHandler<AddUndoMessage> {

        @Override
        public void handleClientSide(Minecraft client, LocalPlayer player, ClientPacketListener handler, AddUndoMessage message, PacketSender responseSender) {
            client.execute(() -> {
                if (player != null) {
                    UndoRedo.addUndo(player, new BlockSet(
                            new ArrayList<>() {{
                                add(message.coordinate());
                            }},
                            new ArrayList<>() {{
                                add(message.previousBlockState());
                            }},
                            new ArrayList<>() {{
                                add(message.newBlockState());
                            }},
                            new Vec3(0, 0, 0),
                            message.coordinate(), message.coordinate()
                    ));
                }
            });
        }
    }
}
