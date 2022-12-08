package dev.huskcasaca.effortless.network;

import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager.ModifierSettings;
import dev.huskcasaca.effortless.buildmodifier.array.Array;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.buildmodifier.mirror.RadialMirror;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

/**
 * Shares modifier settings (see ModifierSettingsManager) between server and client
 */
public record ModifierSettingsMessage(
        ModifierSettings modifierSettings
) implements Message {

    public static void encode(ModifierSettingsMessage message, FriendlyByteBuf buf) {

        //ARRAY
        var arraySettings = message.modifierSettings.arraySettings();
        buf.writeBoolean(arraySettings != null);
        if (arraySettings != null) {
            buf.writeBoolean(arraySettings.enabled());
            buf.writeInt(arraySettings.offset().getX());
            buf.writeInt(arraySettings.offset().getY());
            buf.writeInt(arraySettings.offset().getZ());
            buf.writeInt(arraySettings.count());
        }

        //MIRROR
        var mirrorSettings = message.modifierSettings.mirrorSettings();
        buf.writeBoolean(mirrorSettings != null);
        if (mirrorSettings != null) {
            buf.writeBoolean(mirrorSettings.enabled());
            buf.writeDouble(mirrorSettings.position().x);
            buf.writeDouble(mirrorSettings.position().y);
            buf.writeDouble(mirrorSettings.position().z);
            buf.writeBoolean(mirrorSettings.mirrorX());
            buf.writeBoolean(mirrorSettings.mirrorY());
            buf.writeBoolean(mirrorSettings.mirrorZ());
            buf.writeInt(mirrorSettings.radius());
            buf.writeBoolean(mirrorSettings.drawLines());
            buf.writeBoolean(mirrorSettings.drawPlanes());
        }

        //RADIAL MIRROR
        var radialMirrorSettings = message.modifierSettings.radialMirrorSettings();
        buf.writeBoolean(radialMirrorSettings != null);
        if (radialMirrorSettings != null) {
            buf.writeBoolean(radialMirrorSettings.enabled());
            buf.writeDouble(radialMirrorSettings.position().x);
            buf.writeDouble(radialMirrorSettings.position().y);
            buf.writeDouble(radialMirrorSettings.position().z);
            buf.writeInt(radialMirrorSettings.slices());
            buf.writeBoolean(radialMirrorSettings.alternate());
            buf.writeInt(radialMirrorSettings.radius());
            buf.writeBoolean(radialMirrorSettings.drawLines());
            buf.writeBoolean(radialMirrorSettings.drawPlanes());
        }

        buf.writeBoolean(message.modifierSettings.quickReplace());
    }

    public static ModifierSettingsMessage decode(FriendlyByteBuf buf) {

        //ARRAY
        var arraySettings = new Array.ArraySettings();
        if (buf.readBoolean()) {
            boolean arrayEnabled = buf.readBoolean();
            var arrayOffset = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            int arrayCount = buf.readInt();
            arraySettings = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);
        }

        //MIRROR
        var mirrorSettings = new Mirror.MirrorSettings();
        if (buf.readBoolean()) {
            boolean mirrorEnabled = buf.readBoolean();
            var mirrorPosition = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            boolean mirrorX = buf.readBoolean();
            boolean mirrorY = buf.readBoolean();
            boolean mirrorZ = buf.readBoolean();
            int mirrorRadius = buf.readInt();
            boolean mirrorDrawLines = buf.readBoolean();
            boolean mirrorDrawPlanes = buf.readBoolean();
            mirrorSettings = new Mirror.MirrorSettings(mirrorEnabled, mirrorPosition, mirrorX, mirrorY, mirrorZ, mirrorRadius,
                    mirrorDrawLines, mirrorDrawPlanes);
        }

        //RADIAL MIRROR
        var radialMirrorSettings = new RadialMirror.RadialMirrorSettings();
        if (buf.readBoolean()) {
            boolean radialMirrorEnabled = buf.readBoolean();
            var radialMirrorPosition = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            int radialMirrorSlices = buf.readInt();
            boolean radialMirrorAlternate = buf.readBoolean();
            int radialMirrorRadius = buf.readInt();
            boolean radialMirrorDrawLines = buf.readBoolean();
            boolean radialMirrorDrawPlanes = buf.readBoolean();
            radialMirrorSettings = new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPosition, radialMirrorSlices,
                    radialMirrorAlternate, radialMirrorRadius, radialMirrorDrawLines, radialMirrorDrawPlanes);
        }

        boolean quickReplace = buf.readBoolean();

        var modifierSettings = new ModifierSettings(arraySettings, mirrorSettings, radialMirrorSettings, quickReplace);
        return new ModifierSettingsMessage(modifierSettings);
    }

    public static class Serializer implements MessageSerializer<ModifierSettingsMessage> {
        @Override
        public void encode(ModifierSettingsMessage message, FriendlyByteBuf buf) {
            ModifierSettingsMessage.encode(message, buf);
        }

        @Override
        public ModifierSettingsMessage decode(FriendlyByteBuf buf) {
            return ModifierSettingsMessage.decode(buf);
        }


    }

    public static class ServerHandler implements ServerMessageHandler<ModifierSettingsMessage> {

        @Override
        public void handleServerSide(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, ModifierSettingsMessage message, PacketSender responseSender) {
            server.execute(() -> {
                ModifierSettingsManager.setModifierSettings(player, ModifierSettingsManager.sanitize(message.modifierSettings, player));
            });
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ClientHandler implements ClientMessageHandler<ModifierSettingsMessage> {

        @Override
        public void handleClientSide(Minecraft client, LocalPlayer player, ClientPacketListener handler, ModifierSettingsMessage message, PacketSender responseSender) {
            client.execute(() -> {
                ModifierSettingsManager.setModifierSettings(player, ModifierSettingsManager.sanitize(message.modifierSettings, player));
            });
        }
    }
}


