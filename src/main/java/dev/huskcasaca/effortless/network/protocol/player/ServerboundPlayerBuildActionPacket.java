package dev.huskcasaca.effortless.network.protocol.player;

import dev.huskcasaca.effortless.building.BuildAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundPlayerBuildActionPacket(
        BuildAction action
) implements Packet<ServerEffortlessPacketListener> {

    public ServerboundPlayerBuildActionPacket(FriendlyByteBuf friendlyByteBuf) {
        this(BuildAction.values()[friendlyByteBuf.readInt()]);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(action.ordinal());
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}
