package dev.huskcasaca.effortless.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.huskcasaca.effortless.buildmode.BuildMode;
import dev.huskcasaca.effortless.buildmode.OneClickBuildable;
import dev.huskcasaca.effortless.buildmode.ThreeClickBuildable;
import dev.huskcasaca.effortless.buildmode.TwoClickBuildable;
import dev.huskcasaca.effortless.utils.SurvivalHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.TranslatableComponent;

public class BuildCommand {

    private static final DynamicCommandExceptionType ERROR_NO_SUCH_BUILD_MODE = new DynamicCommandExceptionType((object) -> {
        return new TranslatableComponent("commands.attribute.failed.build_mode", object);
    });

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {

        var effortlessCommand = Commands.literal("effortless");

        var placeCommand = Commands.literal("place");
        var breakCommand = Commands.literal("break");

        for (BuildMode buildMode : BuildMode.values()) {
            if (buildMode == BuildMode.DISABLE) continue;
            if (buildMode.getInstance() instanceof OneClickBuildable) {
                registerOneClickBuildModePlace(placeCommand, buildMode);
                registerOneClickBuildModeBreak(breakCommand, buildMode);
            } else if (buildMode.getInstance() instanceof TwoClickBuildable) {
                registerTwoClickBuildModePlace(placeCommand, buildMode);
                registerTwoClickBuildModeBreak(breakCommand, buildMode);
            } else if (buildMode.getInstance() instanceof ThreeClickBuildable) {
                registerThreeClickBuildModePlace(placeCommand, buildMode);
                registerThreeClickBuildModeBreak(breakCommand, buildMode);
            }
        }

        effortlessCommand.then(placeCommand);
        effortlessCommand.then(breakCommand);
        commandDispatcher.register(effortlessCommand);

    }

    private static void registerOneClickBuildModePlace(ArgumentBuilder<CommandSourceStack, ?> builder, BuildMode buildMode) {
        var command = Commands.literal(buildMode.getCommandName()).then(Commands.argument("firstPos", BlockPosArgument.blockPos()).then(Commands.argument("block", BlockStateArgument.block()).executes(context -> {
            var player = context.getSource().getPlayerOrException();
            if (player == null) return 1;

            var firstPos = BlockPosArgument.getLoadedBlockPos(context, "firstPos");

            var blockState = BlockStateArgument.getBlock(context, "block").getState();

            var coordinates = ((OneClickBuildable) buildMode.getInstance()).getFinalBlocks(player, firstPos.getX(), firstPos.getY(), firstPos.getZ());

            for (var blockPos : coordinates) {
                SurvivalHelper.useBlock(player.getLevel(), player, blockPos, blockState);
            }
            return 1;
        })));

        builder.then(command);
    }

    private static void registerTwoClickBuildModePlace(ArgumentBuilder<CommandSourceStack, ?> builder, BuildMode buildMode) {
        var command = Commands.literal(buildMode.getCommandName()).then(Commands.argument("firstPos", BlockPosArgument.blockPos()).then(Commands.argument("secondPos", BlockPosArgument.blockPos()).then(Commands.argument("block", BlockStateArgument.block()).executes(context -> {
            var player = context.getSource().getPlayerOrException();
            if (player == null) return 1;

            var firstPos = BlockPosArgument.getLoadedBlockPos(context, "firstPos");
            var secondPos = BlockPosArgument.getLoadedBlockPos(context, "secondPos");

            var blockState = BlockStateArgument.getBlock(context, "block").getState();

            var coordinates = ((TwoClickBuildable) buildMode.getInstance()).getFinalBlocks(player, firstPos.getX(), firstPos.getY(), firstPos.getZ(), secondPos.getX(), secondPos.getY(), secondPos.getZ());

            for (var blockPos : coordinates) {
                SurvivalHelper.useBlock(player.getLevel(), player, blockPos, blockState);
            }
            return 1;
        }))));

        builder.then(command);
    }

    private static void registerThreeClickBuildModePlace(ArgumentBuilder<CommandSourceStack, ?> builder, BuildMode buildMode) {
        var command = Commands.literal(buildMode.getCommandName()).then(Commands.argument("firstPos", BlockPosArgument.blockPos()).then(Commands.argument("secondPos", BlockPosArgument.blockPos()).then(Commands.argument("thirdPos", BlockPosArgument.blockPos()).then(Commands.argument("block", BlockStateArgument.block()).executes(context -> {
            var player = context.getSource().getPlayerOrException();
            if (player == null) return 1;

            var firstPos = BlockPosArgument.getLoadedBlockPos(context, "firstPos");
            var secondPos = BlockPosArgument.getLoadedBlockPos(context, "secondPos");
            var thirdPos = BlockPosArgument.getLoadedBlockPos(context, "thirdPos");

            var blockState = BlockStateArgument.getBlock(context, "block").getState();

            var coordinates = ((ThreeClickBuildable) buildMode.getInstance()).getFinalBlocks(player, firstPos.getX(), firstPos.getY(), firstPos.getZ(), secondPos.getX(), secondPos.getY(), secondPos.getZ(), thirdPos.getX(), thirdPos.getY(), thirdPos.getZ());

            for (var blockPos : coordinates) {
                SurvivalHelper.useBlock(player.getLevel(), player, blockPos, blockState);
            }
            return 1;
        })))));

        builder.then(command);
    }

    private static void registerOneClickBuildModeBreak(ArgumentBuilder<CommandSourceStack, ?> builder, BuildMode buildMode) {
        var command = Commands.literal(buildMode.getCommandName()).then(Commands.argument("firstPos", BlockPosArgument.blockPos()).executes(context -> {
            var player = context.getSource().getPlayerOrException();
            if (player == null) return 1;

            var firstPos = BlockPosArgument.getLoadedBlockPos(context, "firstPos");

            var coordinates = ((OneClickBuildable) buildMode.getInstance()).getFinalBlocks(player, firstPos.getX(), firstPos.getY(), firstPos.getZ());

            for (var blockPos : coordinates) {
                SurvivalHelper.breakBlock(player.getLevel(), player, blockPos, false);
            }
            return 1;
        }));

        builder.then(command);
    }

    private static void registerTwoClickBuildModeBreak(ArgumentBuilder<CommandSourceStack, ?> builder, BuildMode buildMode) {
        var command = Commands.literal(buildMode.getCommandName()).then(Commands.argument("firstPos", BlockPosArgument.blockPos()).then(Commands.argument("secondPos", BlockPosArgument.blockPos()).executes(context -> {
            var player = context.getSource().getPlayerOrException();
            if (player == null) return 1;

            var firstPos = BlockPosArgument.getLoadedBlockPos(context, "firstPos");
            var secondPos = BlockPosArgument.getLoadedBlockPos(context, "secondPos");

            var coordinates = ((TwoClickBuildable) buildMode.getInstance()).getFinalBlocks(player, firstPos.getX(), firstPos.getY(), firstPos.getZ(), secondPos.getX(), secondPos.getY(), secondPos.getZ());

            for (var blockPos : coordinates) {
                SurvivalHelper.breakBlock(player.getLevel(), player, blockPos, false);
            }
            return 1;
        })));

        builder.then(command);
    }

    private static void registerThreeClickBuildModeBreak(ArgumentBuilder<CommandSourceStack, ?> builder, BuildMode buildMode) {
        var command = Commands.literal(buildMode.getCommandName()).then(Commands.argument("firstPos", BlockPosArgument.blockPos()).then(Commands.argument("secondPos", BlockPosArgument.blockPos()).then(Commands.argument("thirdPos", BlockPosArgument.blockPos()).executes(context -> {
            var player = context.getSource().getPlayerOrException();
            if (player == null) return 1;

            var firstPos = BlockPosArgument.getLoadedBlockPos(context, "firstPos");
            var secondPos = BlockPosArgument.getLoadedBlockPos(context, "secondPos");
            var thirdPos = BlockPosArgument.getLoadedBlockPos(context, "thirdPos");

            var coordinates = ((ThreeClickBuildable) buildMode.getInstance()).getFinalBlocks(player, firstPos.getX(), firstPos.getY(), firstPos.getZ(), secondPos.getX(), secondPos.getY(), secondPos.getZ(), thirdPos.getX(), thirdPos.getY(), thirdPos.getZ());

            for (var blockPos : coordinates) {
                SurvivalHelper.breakBlock(player.getLevel(), player, blockPos, false);
            }
            return 1;
        }))));

        builder.then(command);
    }

}
