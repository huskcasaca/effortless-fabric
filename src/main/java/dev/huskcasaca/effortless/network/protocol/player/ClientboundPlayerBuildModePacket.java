package dev.huskcasaca.effortless.network.protocol.player;

import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.entity.player.ModeSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

/**
 * Shares mode settings (see ModeSettingsManager) between server and client
 */
public record ClientboundPlayerBuildModePacket(
        ModeSettings modeSettings
) implements Packet<ClientEffortlessPacketListener> {

    public ClientboundPlayerBuildModePacket(FriendlyByteBuf friendlyByteBuf) {
        this(new ModeSettings(BuildMode.values()[friendlyByteBuf.readInt()], friendlyByteBuf.readBoolean()));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(modeSettings.buildMode().ordinal());
        friendlyByteBuf.writeBoolean(modeSettings.enableMagnet());
    }

    @Override
    public void handle(ClientEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}
