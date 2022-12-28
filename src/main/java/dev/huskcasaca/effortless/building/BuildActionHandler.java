package dev.huskcasaca.effortless.building;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.EffortlessClient;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.buildmodifier.UndoRedo;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

public class BuildActionHandler {

    // TODO: 21/12/22 remove
    private static BuildAction buildSpeed = BuildAction.SPEED_NORMAL;
    private static BuildAction lineThickness = BuildAction.THICKNESS_1;
    private static BuildAction planeFilling = BuildAction.PLANE_FULL;
    private static BuildAction cubeFilling = BuildAction.CUBE_FULL;
    private static BuildAction raisedEdge = BuildAction.RAISE_SHORT_EDGE;
    private static BuildAction circleStart = BuildAction.CIRCLE_START_CORNER;
    private static BuildAction orientation = BuildAction.FACE_HORIZONTAL;

    public static BuildAction getOptionSetting(BuildMode.Option option) {
        return switch (option) {
            case BUILD_SPEED -> getBuildSpeed();
            case LINE_THICKNESS -> getLineThickness();
            case PLANE_FILLING -> getPlaneFilling();
            case CUBE_FILLING -> getCubeFilling();
            case RAISED_EDGE -> getRaisedEdge();
            case CIRCLE_START -> getCircleStart();
            case ORIENTATION -> getOrientation();
        };
    }

    public static BuildAction getBuildSpeed() {
        return buildSpeed;
    }

    public static BuildAction getPlaneFilling() {
        return planeFilling;
    }

    public static BuildAction getCubeFilling() {
        return cubeFilling;
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

    public static BuildAction getOrientation() {
        return orientation;
    }

    public static BuildAction[] getOptions() {
        return new BuildAction[] {
                getBuildSpeed(),
                getLineThickness(),
                getPlaneFilling(),
                getCubeFilling(),
                getRaisedEdge(),
                getCircleStart(),
                getOrientation()
        };
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
                BuildModifierHelper.cycleReplaceMode(player);
                var modifierSettings = BuildModifierHelper.getModifierSettings(player);
                Effortless.log(player, ChatFormatting.GOLD + "Replace " + ChatFormatting.RESET + (modifierSettings.enableReplace() ? (modifierSettings.enableQuickReplace() ? (ChatFormatting.GREEN + "QUICK") : (ChatFormatting.GREEN + "ON")) : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
                break;
            case MAGNET:
                BuildModeHelper.setEnableMagnet(player, !BuildModeHelper.isEnableMagnet(player));
                Effortless.log(player, ChatFormatting.GOLD + "Item Magnet " + ChatFormatting.RESET + (BuildModeHelper.isEnableMagnet(player) ? (ChatFormatting.GREEN + "ON") : (ChatFormatting.RED + "OFF")) + ChatFormatting.RESET, true);
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

            case SPEED_NORMAL:
                buildSpeed = BuildAction.SPEED_NORMAL;
                break;
            case SPEED_FAST:
                buildSpeed = BuildAction.SPEED_FAST;
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

            case PLANE_FULL:
                planeFilling = BuildAction.PLANE_FULL;
                break;
            case PLANE_HOLLOW:
                planeFilling = BuildAction.PLANE_HOLLOW;
                break;
            case CUBE_FULL:
                cubeFilling = BuildAction.CUBE_FULL;
                break;
            case CUBE_HOLLOW:
                cubeFilling = BuildAction.CUBE_HOLLOW;
                break;
            case CUBE_SKELETON:
                cubeFilling = BuildAction.CUBE_SKELETON;
                break;
            case RAISE_SHORT_EDGE:
                raisedEdge = BuildAction.RAISE_SHORT_EDGE;
                break;
            case RAISE_LONG_EDGE:
                raisedEdge = BuildAction.RAISE_LONG_EDGE;
                break;
            case CIRCLE_START_CENTER:
                circleStart = BuildAction.CIRCLE_START_CENTER;
                break;
            case CIRCLE_START_CORNER:
                circleStart = BuildAction.CIRCLE_START_CORNER;
                break;
            case FACE_HORIZONTAL:
                orientation = BuildAction.FACE_HORIZONTAL;
                break;
            case FACE_VERTICAL:
                orientation = BuildAction.FACE_VERTICAL;
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
