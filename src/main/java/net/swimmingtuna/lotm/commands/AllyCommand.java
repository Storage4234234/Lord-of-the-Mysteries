package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;

import java.util.Set;
import java.util.UUID;

public class AllyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ally")
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(AllyCommand::addAlly)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(AllyCommand::removeAlly)))
                .then(Commands.literal("list")
                        .executes(AllyCommand::listAllies))
        );
    }

    private static int addAlly(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer source = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            if (source.getUUID().equals(target.getUUID())) {
                context.getSource().sendFailure(Component.literal("You cannot add yourself as an ally!").withStyle(ChatFormatting.RED));
                return 0;
            }
            ServerLevel level = source.serverLevel();
            PlayerAllyData allyData = level.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");

            // Check if already allies
            if (allyData.areAllies(source.getUUID(), target.getUUID())) {
                context.getSource().sendFailure(Component.literal("You are already allies with " + target.getScoreboardName()).withStyle(ChatFormatting.YELLOW));
                return 0;
            }
            allyData.addAlly(source.getUUID(), target.getUUID());
            context.getSource().sendSuccess(() ->
                    Component.literal("Added " + target.getScoreboardName() + " as an ally").withStyle(ChatFormatting.GREEN), true);
            target.sendSystemMessage(Component.literal(source.getScoreboardName() + " has added you as an ally").withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to add ally: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int removeAlly(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer source = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            ServerLevel level = source.serverLevel();
            PlayerAllyData allyData = level.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            if (!allyData.areAllies(source.getUUID(), target.getUUID())) {
                context.getSource().sendFailure(Component.literal("You are not allies with " + target.getScoreboardName()).withStyle(ChatFormatting.YELLOW));
                return 0;
            }
            allyData.removeAlly(source.getUUID(), target.getUUID());
            source.sendSystemMessage(Component.literal("Removed " + target.getScoreboardName() + " from allies").withStyle(ChatFormatting.GREEN));
            target.sendSystemMessage(Component.literal(source.getScoreboardName() + " has removed you as an ally").withStyle(ChatFormatting.RED));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to remove ally: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int listAllies(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer source = context.getSource().getPlayerOrException();
            ServerLevel level = source.serverLevel();
            PlayerAllyData allyData = level.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            Set<UUID> allies = allyData.getAllies(source.getUUID());
            if (allies.isEmpty()) {
                source.sendSystemMessage(Component.literal("You have no allies").withStyle(ChatFormatting.RED));
                return 1;
            }

            // Build list of ally names
            StringBuilder allyList = new StringBuilder("Your allies: ");
            boolean first = true;
            for (UUID allyUUID : allies) {
                ServerPlayer allyPlayer = level.getServer().getPlayerList().getPlayer(allyUUID);
                if (allyPlayer != null) {
                    if (!first) {
                        allyList.append(", ");
                    }
                    allyList.append(allyPlayer.getScoreboardName());
                    first = false;
                }
            }

            context.getSource().sendSuccess(() ->
                    Component.literal(allyList.toString()), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to list allies: " + e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}
