package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.network.protocol.player.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Packets {

    public static final ResourceLocation S2C_PLAYER_BUILD_MODE_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_build_mode");
    public static final ResourceLocation S2C_PLAYER_BUILD_MODIFIER_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_build_modifier");
    public static final ResourceLocation S2C_PLAYER_REACH_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_reach");
    public static final ResourceLocation S2C_PLAYER_REQUEST_LOOK_AT_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_request_look_at");

    public static final ResourceLocation C2S_PLAYER_BREAK_BLOCK_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_break_block");
    public static final ResourceLocation C2S_PLAYER_BUILD_ACTION_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_build_action");
    public static final ResourceLocation C2S_PLAYER_PLACE_BLOCK_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_place_block");
    public static final ResourceLocation C2S_PLAYER_SET_BUILD_MODE_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_set_build_mode");
    public static final ResourceLocation C2S_PLAYER_SET_BUILD_MODIFIER_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_set_build_modifier");
    public static final ResourceLocation C2S_PLAYER_SET_BUILD_REACH_PACKET = new ResourceLocation(Effortless.MOD_ID, "player_set_build_reach");

    private static final Map<ResourceLocation, Function<FriendlyByteBuf, ? extends Packet<? extends PacketListener>>> idToDeserializer = new HashMap<>() {{
        put(S2C_PLAYER_BUILD_MODE_PACKET, ClientboundPlayerBuildModifierPacket::new);
        put(S2C_PLAYER_BUILD_MODIFIER_PACKET, ClientboundPlayerBuildModifierPacket::new);
        put(S2C_PLAYER_REACH_PACKET, ClientboundPlayerReachPacket::new);
        put(S2C_PLAYER_REQUEST_LOOK_AT_PACKET, ClientboundPlayerRequestLookAtPacket::new);

        put(Packets.C2S_PLAYER_BREAK_BLOCK_PACKET, ServerboundPlayerBreakBlockPacket::new);
        put(Packets.C2S_PLAYER_BUILD_ACTION_PACKET, ServerboundPlayerBuildActionPacket::new);
        put(Packets.C2S_PLAYER_PLACE_BLOCK_PACKET, ServerboundPlayerPlaceBlockPacket::new);

        put(Packets.C2S_PLAYER_SET_BUILD_MODE_PACKET, ServerboundPlayerSetBuildModePacket::new);
        put(Packets.C2S_PLAYER_SET_BUILD_MODIFIER_PACKET, ServerboundPlayerSetBuildModifierPacket::new);
        put(Packets.C2S_PLAYER_SET_BUILD_REACH_PACKET, ServerboundPlayerSetBuildReachPacket::new);
    }};

    private static final Map<Class<?>, ResourceLocation> classToId = new HashMap<>() {{
        put(ClientboundPlayerBuildModePacket.class, S2C_PLAYER_BUILD_MODE_PACKET);
        put(ClientboundPlayerBuildModifierPacket.class, S2C_PLAYER_BUILD_MODIFIER_PACKET);
        put(ClientboundPlayerReachPacket.class, S2C_PLAYER_REACH_PACKET);
        put(ClientboundPlayerRequestLookAtPacket.class, S2C_PLAYER_REQUEST_LOOK_AT_PACKET);

        put(ServerboundPlayerBreakBlockPacket.class, Packets.C2S_PLAYER_BREAK_BLOCK_PACKET);
        put(ServerboundPlayerBuildActionPacket.class, Packets.C2S_PLAYER_BUILD_ACTION_PACKET);
        put(ServerboundPlayerPlaceBlockPacket.class, Packets.C2S_PLAYER_PLACE_BLOCK_PACKET);

        put(ServerboundPlayerSetBuildModePacket.class, Packets.C2S_PLAYER_SET_BUILD_MODE_PACKET);
        put(ServerboundPlayerSetBuildModifierPacket.class, Packets.C2S_PLAYER_SET_BUILD_MODIFIER_PACKET);
        put(ServerboundPlayerSetBuildReachPacket.class, Packets.C2S_PLAYER_SET_BUILD_REACH_PACKET);
    }};

    public static ResourceLocation getKey(Packet<?> packet) {
        return classToId.get(packet.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T extends PacketListener> Function<FriendlyByteBuf, ? extends Packet<T>> getDeserializer(ResourceLocation resourceLocation) {
        try {
            return (Function<FriendlyByteBuf, ? extends Packet<T>>) idToDeserializer.get(resourceLocation);
        } catch (ClassCastException e) {
            Effortless.log("Failed to cast packet deserializer for packet with id: " + resourceLocation);
            return null;
        }
    }

    public static <T extends ServerPacketListener> void sendToServer(Packet<T> packet) {
        Minecraft.getInstance().getConnection().send(packet);
    }

    public static <T extends PacketListener> void sendToClient(Packet<T> packet, ServerPlayer player) {
        player.connection.send(packet);
    }

}
