package dev.huskcasaca.effortless.network.protocol.player;

import net.minecraft.network.protocol.game.*;

public interface ServerEffortlessPacketListener extends ServerPacketListener {

	void handle(ServerboundPlayerBreakBlockPacket packet);

	void handle(ServerboundPlayerBuildActionPacket packet);

	void handle(ServerboundPlayerPlaceBlockPacket packet);

	void handle(ServerboundPlayerSetBuildModePacket packet);

	void handle(ServerboundPlayerSetBuildModifierPacket packet);

	void handle(ServerboundPlayerSetBuildReachPacket packet);

}
