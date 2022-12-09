package dev.huskcasaca.effortless.network.protocol.player;

import dev.huskcasaca.effortless.entity.player.ModifierSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

/**
 * Shares modifier settings (see ModifierSettingsManager) between server and client
 */
public record ClientboundPlayerBuildModifierPacket(
        ModifierSettings modifierSettings
) implements Packet<ClientEffortlessPacketListener> {

    public ClientboundPlayerBuildModifierPacket(FriendlyByteBuf friendlyByteBuf) {
        this(ModifierSettings.decodeBuf(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        ModifierSettings.write(friendlyByteBuf, modifierSettings);
    }

    @Override
    public void handle(ClientEffortlessPacketListener packetListener) {
        packetListener.handle(this);
    }

}


