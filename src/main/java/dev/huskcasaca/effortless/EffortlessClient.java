package dev.huskcasaca.effortless;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.huskcasaca.effortless.buildmode.BuildModeHandler;
import dev.huskcasaca.effortless.buildmode.ModeSettingsManager;
import dev.huskcasaca.effortless.event.ClientReloadShadersEvent;
import dev.huskcasaca.effortless.event.ClientScreenEvent;
import dev.huskcasaca.effortless.event.ClientScreenInputEvent;
import dev.huskcasaca.effortless.helper.ReachHelper;
import dev.huskcasaca.effortless.mixin.KeyMappingAccessor;
import dev.huskcasaca.effortless.network.ModeSettingsMessage;
import dev.huskcasaca.effortless.network.PacketHandler;
import dev.huskcasaca.effortless.render.BuildRenderTypes;
import dev.huskcasaca.effortless.screen.buildmode.PlayerSettingsScreen;
import dev.huskcasaca.effortless.screen.buildmode.RadialMenuScreen;
import dev.huskcasaca.effortless.screen.buildmodifier.ModifierSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class EffortlessClient implements ClientModInitializer {

    public static KeyMapping[] keyBindings;
    public static HitResult previousLookAt;
    public static HitResult currentLookAt;
    public static int ticksInGame = 0;

    public static void onStartClientTick(Minecraft client) {
        //Update previousLookAt
        PacketHandler.registerClient();
        HitResult objectMouseOver = Minecraft.getInstance().hitResult;
        //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
        if (objectMouseOver == null) return;

        if (currentLookAt == null) {
            currentLookAt = objectMouseOver;
            previousLookAt = objectMouseOver;
            return;
        }

        if (objectMouseOver.getType() == HitResult.Type.BLOCK) {
            if (currentLookAt.getType() != HitResult.Type.BLOCK) {
                currentLookAt = objectMouseOver;
                previousLookAt = objectMouseOver;
            } else {
                if (((BlockHitResult) currentLookAt).getBlockPos() != ((BlockHitResult) objectMouseOver).getBlockPos()) {
                    previousLookAt = currentLookAt;
                    currentLookAt = objectMouseOver;
                }
            }
        }

    }

    public static void onEndClientTick(Minecraft client) {
        Screen gui = Minecraft.getInstance().screen;
        if (gui == null || !gui.isPauseScreen()) {
            ticksInGame++;
        }
    }


    //    @SubscribeEvent(receiveCanceled = true)
    public static void onKeyPress(int key, int scanCode, int action, int modifiers) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
//
//        //Remember to send packet to server if necessary
//        //Show Modifier Settings GUI
//        if (keyBindings[0].consumeClick()) {
//            openModifierSettings();
//        }
//
//        //QuickReplace toggle
//        if (keyBindings[1].consumeClick()) {
//            var modifierSettings = ModifierSettingsManager.getModifierSettings(player);
//            modifierSettings.setQuickReplace(!modifierSettings.quickReplace());
//            Effortless.log(player, ChatFormatting.GOLD + "Quick Replace " + ChatFormatting.RESET + (
//                    modifierSettings.quickReplace() ? "ON" : "OFF"));
//            PacketHandler.sendToServer(new ModifierSettingsMessage(modifierSettings));
//        }

        //Radial menu
        if (keyBindings[2].isDown()) {
            if (!RadialMenuScreen.instance.isVisible()) {
                Minecraft.getInstance().setScreen(RadialMenuScreen.instance);
            }
//            if (ReachHelper.getMaxReach(player) > 0) {
//            } else {
//                Effortless.log(player, "Build modes are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
//            }
        }
//
//        //Undo (Ctrl+Z)
//        if (keyBindings[3].consumeClick()) {
//            BuildAction undoAction = BuildAction.UNDO;
//            BuildActionHandler.performAction(player, undoAction);
//            PacketHandler.sendToServer(new ModeActionMessage(undoAction));
//        }
//
//        //Redo (Ctrl+Y)
//        if (keyBindings[4].consumeClick()) {
//            BuildAction redoAction = BuildAction.REDO;
//            BuildActionHandler.performAction(player, redoAction);
//            PacketHandler.sendToServer(new ModeActionMessage(redoAction));
//        }
//
//        //Change placement mode
//        if (keyBindings[5].consumeClick()) {
//            //Toggle between first two actions of the first option of the current build mode
//            BuildMode currentBuildMode = ModeSettingsManager.getModeSettings(player).buildMode();
//            if (currentBuildMode.options.length > 0) {
//                BuildOption option = currentBuildMode.options[0];
//                if (option.actions.length >= 2) {
//                    if (BuildActionHandler.getOptionSetting(option) == option.actions[0]) {
//                        BuildActionHandler.performAction(player, option.actions[1]);
//                        PacketHandler.sendToServer(new ModeActionMessage(option.actions[1]));
//                    } else {
//                        BuildActionHandler.performAction(player, option.actions[0]);
//                        PacketHandler.sendToServer(new ModeActionMessage(option.actions[0]));
//                    }
//                }
//            }
//        }

    }

    public static void openModifierSettings() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;

        //Disabled if max reach is 0, might be set in the config that way.
        if (ReachHelper.getMaxReach(player) == 0) {
            Effortless.log(player, "Build modifiers are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
        } else {

            mc.setScreen(null);
            mc.setScreen(new ModifierSettingsScreen());
        }
    }

    public static void openPlayerSettings() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PlayerSettingsScreen());

    }

    public static void onScreenEvent(Screen screen) {
        var player = Minecraft.getInstance().player;
        if (player != null) {

            var modeSettings = ModeSettingsManager.getModeSettings(player);
            ModeSettingsManager.setModeSettings(player, modeSettings);
            BuildModeHandler.initializeMode(player);
            PacketHandler.sendToServer(new ModeSettingsMessage(modeSettings));
        }
    }

    public static boolean isKeybindDown(int index) {
        return InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow().getWindow(),
                ((KeyMappingAccessor) EffortlessClient.keyBindings[index]).getKey().getValue()
        );
    }

    public static int getKey(int index) {
        return ((KeyMappingAccessor) EffortlessClient.keyBindings[index]).getKey().getValue();
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
        float f = player.getXRot();
        float g = player.getYRot();
        var vec3 = player.getEyePosition();
        float h = Mth.cos(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float i = Mth.sin(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float j = -Mth.cos(-f * ((float) Math.PI / 180));
        float k = Mth.sin(-f * ((float) Math.PI / 180));
        float l = i * j;
        float m = k;
        float n = h * j;
        double d = 5.0;
        var vec32 = vec3.add((double) l * 5.0, (double) m * 5.0, (double) n * 5.0);
        return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
    }

    public static HitResult getLookingAt(Player player) {
        Level world = player.level;

        //base distance off of player ability (config)
        float raytraceRange = ReachHelper.getPlacementReach(player) * 4;

        var look = player.getLookAngle();
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        var end = new Vec3(player.getX() + look.x * raytraceRange, player.getY() + player.getEyeHeight() + look.y * raytraceRange, player.getZ() + look.z * raytraceRange);
//        return player.rayTrace(raytraceRange, 1f, RayTraceFluidMode.NEVER);
        //TODO 1.14 check if correct
        return world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    public static void registerShaders(ResourceManager manager, ClientReloadShadersEvent.ShaderRegister.ShadersSink sink) throws IOException {
        sink.registerShader(
                // TODO: 10/9/22 use custom namespace
                new ShaderInstance(manager, "dissolve", DefaultVertexFormat.BLOCK),
                (shaderInstance) -> BuildRenderTypes.dissolveShaderInstance = shaderInstance
        );
    }

//    public Player getPlayerEntityFromContext(Supplier<NetworkEvent.Context> ctx) {
//        return (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT ? Minecraft.getInstance().player : ctx.get().getSender());
//    }
//
//    @Override
//    public void logTranslate(Player player, String prefix, String translationKey, String suffix, boolean actionBar) {
//        Forge_Effortless.log(Minecraft.getInstance().player, prefix + I18n.get(translationKey) + suffix, actionBar);
//    }

    @Override
    public void onInitializeClient() {
        // register key bindings
        keyBindings = new KeyMapping[6];

        // instantiate the key bindings
        keyBindings[0] = new KeyMapping("key.effortless.hud.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, "key.effortless.category");
        keyBindings[1] = new KeyMapping("key.effortless.replace.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, "key.effortless.category");
        keyBindings[2] = new KeyMapping("key.effortless.mode.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.effortless.category");
        keyBindings[3] = new KeyMapping("key.effortless.undo.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.effortless.category");
        keyBindings[4] = new KeyMapping("key.effortless.redo.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.effortless.category");
//        keyBindings[5] = new KeyMapping("key.effortless.altplace.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, "key.effortless.category");

        ClientScreenEvent.SCREEN_OPENING_EVENT.register(EffortlessClient::onScreenEvent);

        ClientScreenInputEvent.KEY_PRESS_EVENT.register(EffortlessClient::onKeyPress);

        ClientTickEvents.START_CLIENT_TICK.register(EffortlessClient::onStartClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(EffortlessClient::onEndClientTick);

        ClientReloadShadersEvent.REGISTER_SHADER.register(EffortlessClient::registerShaders);
    }

}
