package dev.huskcasaca.effortless.network.protocol.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/***
 * Sends a message to the server indicating that a player wants to break a block
 */
public record ServerboundPlayerBreakBlockPacket(
        boolean blockHit,
        BlockPos blockPos,
        Direction sideHit,
        Vec3 hitVec
) implements Packet<ServerEffortlessPacketListener> {

    public ServerboundPlayerBreakBlockPacket() {
        // TODO: 17/9/22 use Vec3.ZERO?
        this(false, BlockPos.ZERO, Direction.UP, new Vec3(0, 0, 0));
    }

    public ServerboundPlayerBreakBlockPacket(BlockHitResult result) {
        this(result.getType() == HitResult.Type.BLOCK, result.getBlockPos(), result.getDirection(), result.getLocation());
    }

    public ServerboundPlayerBreakBlockPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readBoolean(), friendlyByteBuf.readBlockPos(), Direction.from3DDataValue(friendlyByteBuf.readByte()), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(blockHit);
        friendlyByteBuf.writeBlockPos(blockPos);
        friendlyByteBuf.writeInt(sideHit.get3DDataValue());
        friendlyByteBuf.writeDouble(hitVec.x);
        friendlyByteBuf.writeDouble(hitVec.y);
        friendlyByteBuf.writeDouble(hitVec.z);
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}
