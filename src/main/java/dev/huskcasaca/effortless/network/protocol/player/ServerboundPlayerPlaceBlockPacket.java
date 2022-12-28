package dev.huskcasaca.effortless.network.protocol.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/***
 * Sends a message to the server indicating that a player wants to place a block.
 * Received clientside: server has placed blocks and its letting the client know.
 */
public record ServerboundPlayerPlaceBlockPacket(
        boolean blockHit,
        BlockPos blockPos,
        Direction hitSide,
        Vec3 hitVec,
        boolean placeStartPos //prevent double placing in normal mode
) implements Packet<ServerEffortlessPacketListener> {


    public ServerboundPlayerPlaceBlockPacket() {
        this(false, BlockPos.ZERO, Direction.UP, new Vec3(0, 0, 0), true);
    }

    public ServerboundPlayerPlaceBlockPacket(BlockHitResult result, boolean placeStartPos) {
        this(result.getType() == HitResult.Type.BLOCK, result.getBlockPos(), result.getDirection(), result.getLocation(), placeStartPos);
    }

    public ServerboundPlayerPlaceBlockPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readBoolean(),
                friendlyByteBuf.readBlockPos(),
                Direction.from3DDataValue(friendlyByteBuf.readByte()),
                new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()),
                friendlyByteBuf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(blockHit);
        friendlyByteBuf.writeBlockPos(blockPos);
        friendlyByteBuf.writeByte(hitSide.get3DDataValue());
        friendlyByteBuf.writeDouble(hitVec.x);
        friendlyByteBuf.writeDouble(hitVec.y);
        friendlyByteBuf.writeDouble(hitVec.z);
        friendlyByteBuf.writeBoolean(placeStartPos);
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}
