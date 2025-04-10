package net.swimmingtuna.lotm.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.world.worlddata.BeyonderEntityData;
import net.swimmingtuna.lotm.world.worlddata.BeyonderRecipeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.swimmingtuna.lotm.commands.BeyonderCommand.ERROR_UNKNOWN_BEYONDER_CLASS;
import static net.swimmingtuna.lotm.util.BeyonderUtil.registerAllRecipes;

public class BeyonderEntityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("beyonderentity")
                        .requires(source -> source.hasPermission(2))
                        .then(literal("add")
                                .then(argument("entityType", ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE))
                                        .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .then(argument("pathway", BeyonderClassArgument.beyonderClass())
                                                .then(Commands.argument("sequence", IntegerArgumentType.integer(0, 9))
                                                        .executes(BeyonderEntityCommand::addEntityAsBeyonder)
                                                )
                                        )
                                )
                        )
                        .then(literal("remove")
                                .then(argument("entityType", ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE))
                                        .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .executes(BeyonderEntityCommand::removeEntityBeyonder)
                                )
                        )
                        .then(literal("clear")
                                .executes(BeyonderEntityCommand::clearAllEntityBeyonders)
                        )
                        .then(literal("list")
                                .executes(BeyonderEntityCommand::listEntityBeyonders)
                        )
        );
    }

    private static int addEntityAsBeyonder(CommandContext<CommandSourceStack> context) {
        try {
            Holder.Reference<EntityType<?>> entityTypeHolder = ResourceArgument.getSummonableEntityType(context, "entityType");
            EntityType<?> entityType = entityTypeHolder.value();
            ResourceLocation entityId = entityTypeHolder.key().location();

            BeyonderClass result = BeyonderClassArgument.getBeyonderClass(context, "pathway");
            int sequence = IntegerArgumentType.getInteger(context, "sequence");

            if (result == null) {
                throw ERROR_UNKNOWN_BEYONDER_CLASS.create(context.getInput());
            }

            // Ensure sequence is within valid range
            if (sequence < 0 || sequence > 9) {
                context.getSource().sendFailure(Component.literal("Sequence must be between 0 and 9")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            String sequenceName = result.sequenceNames().get(sequence);
            ServerLevel level = context.getSource().getLevel();
            BeyonderEntityData data = BeyonderEntityData.getInstance(level);
            boolean added = data.setEntityString(entityType, sequenceName);

            if (added) {
                context.getSource().sendSuccess(() -> Component.literal("Successfully assigned " +
                        entityId + " as a " + sequenceName).withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Entity already has this Beyonder assignment!")
                        .withStyle(ChatFormatting.YELLOW));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error adding entity as Beyonder: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }

    private static int removeEntityBeyonder(CommandContext<CommandSourceStack> context) {
        try {
            Holder.Reference<EntityType<?>> entityTypeHolder = ResourceArgument.getSummonableEntityType(context, "entityType");
            EntityType<?> entityType = entityTypeHolder.value();
            ResourceLocation entityId = entityTypeHolder.key().location();

            ServerLevel level = context.getSource().getLevel();
            BeyonderEntityData data = BeyonderEntityData.getInstance(level);

            String previousAssignment = data.getStringForEntity(entityType);
            boolean removed = data.removeEntity(entityType);

            if (removed) {
                context.getSource().sendSuccess(() -> Component.literal("Successfully removed Beyonder assignment '" +
                                previousAssignment + "' from " + entityId)
                        .withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Entity has no Beyonder assignment!")
                        .withStyle(ChatFormatting.YELLOW));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error removing entity Beyonder assignment: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }

    private static int clearAllEntityBeyonders(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderEntityData data = BeyonderEntityData.getInstance(level);
            data.clearMappings();

            context.getSource().sendSuccess(() -> Component.literal("Successfully cleared all entity Beyonder assignments")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error clearing entity Beyonder assignments: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }

    private static int listEntityBeyonders(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderEntityData data = BeyonderEntityData.getInstance(level);

            CommandSourceStack source = context.getSource();
            Player player = source.getPlayer();

            if (player != null) {
                data.sendPlayerMappings(player);
                return 1;
            } else {
                // If not a player, print to console
                Map<EntityType<?>, String> mappings = data.getAllEntityMappings();
                if (mappings.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No entity-Beyonder mappings found.")
                            .withStyle(ChatFormatting.YELLOW), false);
                    return 0;
                }

                for (Map.Entry<EntityType<?>, String> entry : mappings.entrySet()) {
                    ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey());
                    String beyonderName = entry.getValue();
                    source.sendSuccess(() -> Component.literal(entityId + " -> " + beyonderName)
                            .withStyle(ChatFormatting.WHITE), false);
                }
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error listing entity Beyonder assignments: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }
}