package dev.huskcasaca.effortless.network.protocol.player;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

/***
 * Sends a message to the client asking for its lookat (objectmouseover) data.
 * This is then sent back with a BlockPlacedMessage.
 */
public record ClientboundPlayerRequestLookAtPacket(
        boolean placeStartPos
) implements Packet<ClientEffortlessPacketListener> {

    public ClientboundPlayerRequestLookAtPacket() {
        this(false);
    }

    public ClientboundPlayerRequestLookAtPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(placeStartPos);
    }

    @Override
    public void handle(ClientEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }
}
