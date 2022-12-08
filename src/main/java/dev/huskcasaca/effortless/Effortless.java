package dev.huskcasaca.effortless;

import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.ModeSettingsManager;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import dev.huskcasaca.effortless.helper.ReachHelper;
import dev.huskcasaca.effortless.network.AddUndoMessage;
import dev.huskcasaca.effortless.network.ClearUndoMessage;
import dev.huskcasaca.effortless.network.PacketHandler;
import dev.huskcasaca.effortless.network.RequestLookAtMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class Effortless implements ModInitializer {

    public static final String MOD_ID = "effortless";
    public static final Logger logger = LogManager.getLogger();

    public static boolean onBlockPlaced(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {

        if (world.isClientSide()) return true;
        if (!(player instanceof ServerPlayer)) return true;

//        if (!(event.getEntity() instanceof Player)) return true;

//        if (event.getEntity() instanceof FakePlayer) return true;

        //Cancel event if necessary
//        ServerPlayer player = ((ServerPlayer) event.getEntity());
        var buildMode = ModeSettingsManager.getModeSettings(player).buildMode();
        var modifierSettings = ModifierSettingsManager.getModifierSettings(player);

        if (buildMode == BuildMode.DISABLE) {
            return false;
        } else if (modifierSettings.quickReplace()) {
            //Cancel event and send message if QuickReplace
            PacketHandler.sendToClient(new RequestLookAtMessage(true), (ServerPlayer) player);
//            PacketHandler.sendToClient(new AddUndoMessage(pos, event.getBlockSnapshot().getReplacedBlock(), state), (ServerPlayer)  player);
            return false;
        } else {
            //NORMAL mode, let vanilla handle block placing
            //But modifiers should still work

            //Send message to client, which sends message back with raytrace info
            PacketHandler.sendToClient(new RequestLookAtMessage(false), (ServerPlayer) player);
//            PacketHandler.sendToClient(new AddUndoMessage(pos, event.getBlockSnapshot().getReplacedBlock(), state), (ServerPlayer) player);
            return true;
        }

        // TODO: 11/9/22 add achievements

//        Stat<ResourceLocation> blocksPlacedStat = StatList.CUSTOM.get(new ResourceLocation(Effortless.MODID, "blocks_placed"));
//        player.getStats().increment(player, blocksPlacedStat, 1);
//
//        System.out.println(player.getStats().getValue(blocksPlacedStat));
    }

    public static boolean onBlockBroken(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (world.isClientSide()) return true;
//        if (!(player instanceof ServerPlayer)) return true;

        //Cancel event if necessary
        //If cant break far then dont cancel event ever
        var buildMode = ModeSettingsManager.getModeSettings(player).buildMode();
        if (buildMode != BuildMode.DISABLE && ReachHelper.canBreakFar(player)) {
            return false;
        } else {
            //NORMAL mode, let vanilla handle block breaking
            //But modifiers and QuickReplace should still work
            //Dont break the original block yourself, otherwise Tinkers Hammer and Veinminer won't work
            BuildModeHandler.onBlockBroken(player, pos, false);

            //Add to undo stack in client
            if (player instanceof ServerPlayer && state != null && pos != null) {
                PacketHandler.sendToClient(new AddUndoMessage(pos, state, Blocks.AIR.defaultBlockState()), ((ServerPlayer) player));
            }
            return true;
        }
    }

    //
    public static void onPlayerLogin(ServerPlayer player) {
        ModifierSettingsManager.handleNewPlayer(player);
        ModeSettingsManager.handleNewPlayer(player);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        if (player.getCommandSenderWorld().isClientSide) return;
        UndoRedo.clear(player);
        PacketHandler.sendToClient(new ClearUndoMessage(), player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        ModifierSettingsManager.handleNewPlayer(player);
        ModeSettingsManager.handleNewPlayer(player);
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        if (player.getCommandSenderWorld().isClientSide) return;

        //Set build mode to normal
        var modeSettings = ModeSettingsManager.getModeSettings(player);
        modeSettings = new ModeSettingsManager.ModeSettings(
                BuildMode.DISABLE,
                modeSettings.enableMagnet()
        );
        ModeSettingsManager.setModeSettings(player, modeSettings);

        //Disable modifiers
        var modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        var arraySettings = modifierSettings.arraySettings();
        arraySettings = arraySettings.clone(false);
        var mirrorSettings = modifierSettings.mirrorSettings();
        mirrorSettings = mirrorSettings.clone(false);
        var radialMirrorSettings = modifierSettings.radialMirrorSettings();
        radialMirrorSettings = radialMirrorSettings.clone(false);
        modifierSettings = new ModifierSettingsManager.ModifierSettings(arraySettings, mirrorSettings, radialMirrorSettings, modifierSettings.quickReplace());
        ModifierSettingsManager.setModifierSettings(player, modifierSettings);

        ModifierSettingsManager.handleNewPlayer(player);
        ModeSettingsManager.handleNewPlayer(player);

        UndoRedo.clear(player);
        PacketHandler.sendToClient(new ClearUndoMessage(), player);
    }

    //
    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        ModifierSettingsManager.setModifierSettings(newPlayer, ModifierSettingsManager.getModifierSettings(oldPlayer));
        ModeSettingsManager.setModeSettings(newPlayer, ModeSettingsManager.getModeSettings(oldPlayer));
    }

    public static void log(String msg) {
        logger.info(msg);
    }

    public static void log(Player player, String msg) {
        log(player, msg, false);
    }

    public static void log(Player player, String msg, boolean actionBar) {
        player.displayClientMessage(new TextComponent(msg), actionBar);
    }

    //Log with translation supported, call either on client or server (which then sends a message)
    public static void logTranslate(Player player, String prefix, String translationKey, String suffix, boolean actionBar) {
//		proxy.logTranslate(player, prefix, translationKey, suffix, actionBar);
    }

    @Override
    public void onInitialize() {
        PacketHandler.register();

        ServerPlayerEvents.COPY_FROM.register(Effortless::onPlayerClone);
    }
}
