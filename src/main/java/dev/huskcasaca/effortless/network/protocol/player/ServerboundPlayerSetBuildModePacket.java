package dev.huskcasaca.effortless.network.protocol.player;

import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.entity.player.ModeSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

/**
 * Shares mode settings (see ModeSettingsManager) between server and client
 */
public record ServerboundPlayerSetBuildModePacket(
        ModeSettings modeSettings
) implements Packet<ServerEffortlessPacketListener> {


    public ServerboundPlayerSetBuildModePacket(FriendlyByteBuf friendlyByteBuf) {
        this(new ModeSettings(BuildMode.values()[friendlyByteBuf.readInt()], friendlyByteBuf.readBoolean()));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(modeSettings.buildMode().ordinal());
        friendlyByteBuf.writeBoolean(modeSettings.enableMagnet());
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}
