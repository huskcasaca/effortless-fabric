package dev.huskcasaca.effortless.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.huskcasaca.effortless.buildreach.ReachHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class SettingsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean dedicated) {

        var effortlessCommand = Commands.literal("effortless");

        var playerSettingsCommand = Commands.argument("player", EntityArgument.players());

        playerSettingsCommand.then(Commands.literal("maxReachDistance").then(Commands.argument("value", IntegerArgumentType.integer(ReachHelper.MIN_MAX_REACH_DISTANCE, ReachHelper.MAX_MAX_REACH_DISTANCE)).executes(context -> {
            EntityArgument.getPlayers(context, "player").forEach(player -> {
                try {
                    var value = IntegerArgumentType.getInteger(context, "value");
                    ReachHelper.setMaxReachDistance(player, value);
                    context.getSource().sendSuccess(new TranslatableComponent("commands.effortless.max_reach_distance.success", player.getDisplayName(), value), true);
                } catch (Exception e) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.effortless.max_reach_distance.failure", player.getDisplayName()));
                }
            });
            return 0;
        })));

        playerSettingsCommand.then(Commands.literal("maxBlockPlacePerAxis").then(Commands.argument("value", IntegerArgumentType.integer(ReachHelper.MIN_MAX_BLOCK_PLACE_PER_AXIS, ReachHelper.MAX_MAX_BLOCK_PLACE_PER_AXIS)).executes(context -> {
            EntityArgument.getPlayers(context, "player").forEach(player -> {
                try {
                    var value = IntegerArgumentType.getInteger(context, "value");
                    ReachHelper.setMaxBlockPlacePerAxis(player, value);
                    context.getSource().sendSuccess(new TranslatableComponent("commands.effortless.max_block_place_per_axis.success", player.getDisplayName(), value), true);
                } catch (Exception e) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.effortless.max_block_place_per_axis.failure", player.getDisplayName()));
                }
            });
            return 0;
        })));

        playerSettingsCommand.then(Commands.literal("maxBlockPlaceAtOnce").then(Commands.argument("value", IntegerArgumentType.integer(ReachHelper.MIN_MAX_BLOCK_PLACE_AT_ONCE, ReachHelper.MAX_MAX_BLOCK_PLACE_AT_ONCE)).executes(context -> {
            EntityArgument.getPlayers(context, "player").forEach(player -> {
                try {
                    var value = IntegerArgumentType.getInteger(context, "value");
                    ReachHelper.setMaxBlockPlaceAtOnce(player, value);
                    context.getSource().sendSuccess(new TranslatableComponent("commands.effortless.max_block_place_at_once.success", player.getDisplayName(), value), true);
                } catch (Exception e) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.effortless.max_block_place_at_once.failure", player.getDisplayName()));
                }
            });
            return 0;
        })));

        playerSettingsCommand.then(Commands.literal("canBreakFar").then(Commands.argument("value", BoolArgumentType.bool()).executes(context -> {
            EntityArgument.getPlayers(context, "player").forEach(player -> {
                try {
                    var value = BoolArgumentType.getBool(context, "value");
                    ReachHelper.setCanBreakFar(player, value);
                    context.getSource().sendSuccess(new TranslatableComponent("commands.effortless.can_break_far.success", player.getDisplayName(), value), true);
                } catch (Exception e) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.effortless.can_break_far.failure", player.getDisplayName()));
                }
            });
            return 0;
        })));

        playerSettingsCommand.then(Commands.literal("enableUndo").then(Commands.argument("value", BoolArgumentType.bool()).executes(context -> {
            EntityArgument.getPlayers(context, "player").forEach(player -> {
                try {
                    var value = BoolArgumentType.getBool(context, "value");
                    ReachHelper.setEnableUndo(player, value);
                    context.getSource().sendSuccess(new TranslatableComponent("commands.effortless.enable_undo.success", player.getDisplayName(), value), true);
                } catch (Exception e) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.effortless.enable_undo.failure", player.getDisplayName()));
                }
            });
            return 0;
        })));

        playerSettingsCommand.then(Commands.literal("undoStackSize").then(Commands.argument("value", IntegerArgumentType.integer(ReachHelper.MIN_UNDO_STACK_SIZE, ReachHelper.MAX_UNDO_STACK_SIZE)).executes(context -> {
            EntityArgument.getPlayers(context, "player").forEach(player -> {
                try {
                    var value = IntegerArgumentType.getInteger(context, "value");
                    ReachHelper.setUndoStackSize(player, value);
                    context.getSource().sendSuccess(new TranslatableComponent("commands.effortless.undo_stack_size.success", player.getDisplayName(), value), true);
                } catch (Exception e) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.effortless.undo_stack_size.failure", player.getDisplayName()));
                }
            });
            return 0;
        })));

        commandDispatcher.register(effortlessCommand.then(playerSettingsCommand));
    }


}
