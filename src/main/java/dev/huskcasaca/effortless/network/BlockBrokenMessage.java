package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/***
 * Sends a message to the server indicating that a player wants to break a block
 */
public record BlockBrokenMessage(
        boolean blockHit,
        BlockPos blockPos,
        Direction sideHit,
        Vec3 hitVec
) implements Message {

    public BlockBrokenMessage() {
        // TODO: 17/9/22 use Vec3.ZERO?
        this(false, BlockPos.ZERO, Direction.UP, new Vec3(0, 0, 0));
    }

    public BlockBrokenMessage(BlockHitResult result) {
        this(result.getType() == HitResult.Type.BLOCK, result.getBlockPos(), result.getDirection(), result.getLocation());
    }

    public static void encode(BlockBrokenMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.blockHit);
        buf.writeInt(message.blockPos.getX());
        buf.writeInt(message.blockPos.getY());
        buf.writeInt(message.blockPos.getZ());
        buf.writeInt(message.sideHit.get3DDataValue());
        buf.writeDouble(message.hitVec.x);
        buf.writeDouble(message.hitVec.y);
        buf.writeDouble(message.hitVec.z);
    }

    public static BlockBrokenMessage decode(FriendlyByteBuf buf) {
        boolean blockHit = buf.readBoolean();
        var blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        var sideHit = Direction.from3DDataValue(buf.readInt());
        var hitVec = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new BlockBrokenMessage(blockHit, blockPos, sideHit, hitVec);
    }

    public static class Serializer implements MessageSerializer<BlockBrokenMessage> {

        @Override
        public void encode(BlockBrokenMessage message, FriendlyByteBuf buf) {
            BlockBrokenMessage.encode(message, buf);
        }

        @Override
        public BlockBrokenMessage decode(FriendlyByteBuf buf) {
            return BlockBrokenMessage.decode(buf);
        }

    }

    public static class ServerHandler implements ServerMessageHandler<BlockBrokenMessage> {

        @Override
        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, BlockBrokenMessage message, PacketSender responseSender) {
            server.execute(() -> {
                BuildModeHandler.onBlockBrokenMessage(player, message);
            });
        }

    }

    @Environment(EnvType.CLIENT)
    public static class ClientHandler implements ClientMessageHandler<BlockBrokenMessage> {

        @Override
        public void handleClientSide(Minecraft client, LocalPlayer player, ClientPacketListener handler, BlockBrokenMessage message, PacketSender responseSender) {
            // server only
        }

    }


}
