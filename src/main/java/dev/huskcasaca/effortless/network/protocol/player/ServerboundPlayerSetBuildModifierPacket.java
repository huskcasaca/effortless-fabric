package dev.huskcasaca.effortless.network.protocol.player;

import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

/**
 * Shares modifier settings (see ModifierSettingsManager) between server and client
 */
public record ServerboundPlayerSetBuildModifierPacket(
        ModifierSettings modifierSettings
) implements Packet<ServerEffortlessPacketListener> {


    public ServerboundPlayerSetBuildModifierPacket(FriendlyByteBuf friendlyByteBuf) {
        this(ModifierSettings.decodeBuf(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        ModifierSettings.write(friendlyByteBuf, modifierSettings);
    }

    @Override
    public void handle(ServerEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}


