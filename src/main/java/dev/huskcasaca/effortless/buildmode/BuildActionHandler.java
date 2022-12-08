package dev.huskcasaca.effortless.buildmode;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessClient;
import dev.huskcasaca.effortless.buildmodifier.ModifierSettingsManager;
import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

public class BuildActionHandler {

    private static BuildAction buildSpeed = BuildAction.NORMAL_SPEED;
    private static BuildAction fill = BuildAction.FULL;
    private static BuildAction cubeFill = BuildAction.CUBE_FULL;
    private static BuildAction raisedEdge = BuildAction.SHORT_EDGE;
    private static BuildAction lineThickness = BuildAction.THICKNESS_1;
    private static BuildAction circleStart = BuildAction.CIRCLE_START_CORNER;

    public static BuildAction getOptionSetting(BuildOption option) {
        switch (option) {
            case BUILD_SPEED:
                return getBuildSpeed();
            case FILL:
                return getFill();
            case CUBE_FILL:
                return getCubeFill();
            case RAISED_EDGE:
                return getRaisedEdge();
            case LINE_THICKNESS:
                return getLineThickness();
            case CIRCLE_START:
                return getCircleStart();
            default:
                return null;
        }
    }

    public static BuildAction getBuildSpeed() {
        return buildSpeed;
    }

    public static BuildAction getFill() {
        return fill;
    }

    public static BuildAction getCubeFill() {
        return cubeFill;
    }

    public static BuildAction getRaisedEdge() {
        return raisedEdge;
    }

    public static BuildAction getLineThickness() {
        return lineThickness;
    }

    public static BuildAction getCircleStart() {
        return circleStart;
    }

    //Called on both client and server
    public static void performAction(Player player, BuildAction action) {
        if (action == null) return;

        switch (action) {
            case UNDO:
                UndoRedo.undo(player);
                break;
            case REDO:
                UndoRedo.redo(player);
                break;
            case REPLACE:
                var modifierSettings = ModifierSettingsManager.getModifierSettings(player);

                modifierSettings = new ModifierSettingsManager.ModifierSettings(
                        modifierSettings.arraySettings(), modifierSettings.mirrorSettings(), modifierSettings.radialMirrorSettings(),
                        !modifierSettings.quickReplace()
                );
                ModifierSettingsManager.setModifierSettings(player, modifierSettings);

                Effortless.log(player, ChatFormatting.GOLD + "Replace " + ChatFormatting.RESET + (
                        modifierSettings.quickReplace() ? (ChatFormatting.GREEN + "ON") : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                break;
            case MAGNET:
                var modeSettings = ModeSettingsManager.getModeSettings(player);
                modeSettings = new ModeSettingsManager.ModeSettings(modeSettings.buildMode(), !modeSettings.enableMagnet());
                ModeSettingsManager.setModeSettings(player, modeSettings);

                Effortless.log(player, ChatFormatting.GOLD + "Item Magnet " + ChatFormatting.RESET + (modeSettings.enableMagnet() ? (ChatFormatting.GREEN + "ON") : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                break;
            case MODIFIER:
                if (player.level.isClientSide)
                    EffortlessClient.openModifierSettings();
                break;
            case OPEN_PLAYER_SETTINGS:
                if (player.level.isClientSide)
                    EffortlessClient.openPlayerSettings();
                break;
            case SETTINGS:
                if (player.level.isClientSide)
                    EffortlessClient.openSettings();
                break;
            case NORMAL_SPEED:
                buildSpeed = BuildAction.NORMAL_SPEED;
                break;
            case FAST_SPEED:
                buildSpeed = BuildAction.FAST_SPEED;
                break;
            case FULL:
                fill = BuildAction.FULL;
                break;
            case HOLLOW:
                fill = BuildAction.HOLLOW;
                break;
            case CUBE_FULL:
                cubeFill = BuildAction.CUBE_FULL;
                break;
            case CUBE_HOLLOW:
                cubeFill = BuildAction.CUBE_HOLLOW;
                break;
            case CUBE_SKELETON:
                cubeFill = BuildAction.CUBE_SKELETON;
                break;
            case SHORT_EDGE:
                raisedEdge = BuildAction.SHORT_EDGE;
                break;
            case LONG_EDGE:
                raisedEdge = BuildAction.LONG_EDGE;
                break;
            case THICKNESS_1:
                lineThickness = BuildAction.THICKNESS_1;
                break;
            case THICKNESS_3:
                lineThickness = BuildAction.THICKNESS_3;
                break;
            case THICKNESS_5:
                lineThickness = BuildAction.THICKNESS_5;
                break;
            case CIRCLE_START_CENTER:
                circleStart = BuildAction.CIRCLE_START_CENTER;
                break;
            case CIRCLE_START_CORNER:
                circleStart = BuildAction.CIRCLE_START_CORNER;
                break;
        }

        if (player.level.isClientSide &&
                action != BuildAction.REPLACE &&
                action != BuildAction.MODIFIER &&
                action != BuildAction.OPEN_PLAYER_SETTINGS) {

            Effortless.logTranslate(player, "", action.getNameKey(), "", true);
        }
    }

}
