package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.render.BlockPreviewRenderer;
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
 * Sends a message to the server indicating that a player wants to place a block.
 * Received clientside: server has placed blocks and its letting the client know.
 */
public record BlockPlacedMessage(
        boolean blockHit,
        BlockPos blockPos,
        Direction sideHit,
        Vec3 hitVec,
        boolean placeStartPos //prevent double placing in normal mode
) implements Message {


    public BlockPlacedMessage() {
        this(false, BlockPos.ZERO, Direction.UP, new Vec3(0, 0, 0), true);
    }

    public BlockPlacedMessage(BlockHitResult result, boolean placeStartPos) {
        this(result.getType() == HitResult.Type.BLOCK, result.getBlockPos(), result.getDirection(), result.getLocation(), placeStartPos);
    }

    public static void encode(BlockPlacedMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.blockHit);
        buf.writeInt(message.blockPos.getX());
        buf.writeInt(message.blockPos.getY());
        buf.writeInt(message.blockPos.getZ());
        buf.writeInt(message.sideHit.get3DDataValue());
        buf.writeDouble(message.hitVec.x);
        buf.writeDouble(message.hitVec.y);
        buf.writeDouble(message.hitVec.z);
        buf.writeBoolean(message.placeStartPos);
    }

    public static BlockPlacedMessage decode(FriendlyByteBuf buf) {
        boolean blockHit = buf.readBoolean();
        var blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        var sideHit = Direction.from3DDataValue(buf.readInt());
        var hitVec = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        boolean placeStartPos = buf.readBoolean();
        return new BlockPlacedMessage(blockHit, blockPos, sideHit, hitVec, placeStartPos);
    }

    public static class Serializer implements MessageSerializer<BlockPlacedMessage> {

        @Override
        public void encode(BlockPlacedMessage message, FriendlyByteBuf buf) {
            BlockPlacedMessage.encode(message, buf);
        }

        @Override
        public BlockPlacedMessage decode(FriendlyByteBuf buf) {
            return BlockPlacedMessage.decode(buf);
        }
    }

    public static class ServerHandler implements ServerMessageHandler<BlockPlacedMessage> {

        @Override
        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, BlockPlacedMessage message, PacketSender responseSender) {
            server.execute(() -> BuildModeHandler.onBlockPlacedMessage(player, message));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ClientHandler implements ClientMessageHandler<BlockPlacedMessage> {

        @Override
        public void handleClientSide(Minecraft client, LocalPlayer player, ClientPacketListener handler, BlockPlacedMessage message, PacketSender responseSender) {
            //Nod RenderHandler to do the dissolve shader effect
            client.execute(() -> BlockPreviewRenderer.onBlocksPlaced());
        }
    }

}
