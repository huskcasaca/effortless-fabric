package dev.huskcasaca.effortless.network.protocol.player;

import net.minecraft.network.PacketListener;

public interface ClientPlayerPacketListener extends PacketListener {

    void handle(ClientboundPlayerBuildModePacket packet);

    void handle(ClientboundPlayerBuildModifierPacket packet);

    void handle(ClientboundPlayerReachPacket packet);

    void handle(ClientboundPlayerRequestLookAtPacket packet);



}
