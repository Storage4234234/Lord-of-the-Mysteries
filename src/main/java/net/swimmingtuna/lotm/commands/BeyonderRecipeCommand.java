package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.world.worlddata.BeyonderRecipeData;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.swimmingtuna.lotm.util.BeyonderUtil.registerAllRecipes;

public class BeyonderRecipeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("beyonderrecipe")
                        .requires(source -> source.hasPermission(2))
                        .then(literal("add")
                                .then(argument("craftedItem", ItemArgument.item(buildContext))
                                        .then(literal("ingredients")
                                                .then(argument("mainCount", IntegerArgumentType.integer(1, 5))
                                                        .then(argument("ingredient1", ItemArgument.item(buildContext))
                                                                .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 1))
                                                                .then(argument("ingredient2", ItemArgument.item(buildContext))
                                                                        .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 2))
                                                                        .then(argument("ingredient3", ItemArgument.item(buildContext))
                                                                                .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 3))
                                                                                .then(argument("ingredient4", ItemArgument.item(buildContext))
                                                                                        .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 4))
                                                                                        .then(argument("ingredient5", ItemArgument.item(buildContext))
                                                                                                .executes(ctx -> addRecipeWithFlexibleIngredients(ctx, 5))
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("remove")
                                .then(argument("craftedItem", ItemArgument.item(buildContext))
                                        .executes(BeyonderRecipeCommand::removeRecipe)
                                )
                                .then(literal("all")
                                        .executes(BeyonderRecipeCommand::removeAllRecipes)
                                )
                        )
                        .then(literal("load")
                                .executes(BeyonderRecipeCommand::loadModpackRecipes)
                        )
        );
    }

    private static int removeRecipe(CommandContext<CommandSourceStack> context) {
        try {
            ItemStack craftedItem = ItemArgument.getItem(context, "craftedItem").createItemStack(1, false);
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);

            boolean recipeRemoved = recipeData.removeRecipe(craftedItem);
            if (recipeRemoved) {
                context.getSource().sendSuccess(() -> Component.literal("Successfully removed recipe for " +
                        craftedItem.getHoverName().getString()).withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("No recipe found for this item!").withStyle(ChatFormatting.YELLOW));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error removing recipe: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }

    private static int removeAllRecipes(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            recipeData.clearRecipes();
            context.getSource().sendSystemMessage(Component.literal("Cleared all recipes").withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error removing recipe: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }


    private static int addRecipeWithFlexibleIngredients(CommandContext<CommandSourceStack> context, int ingredientCount) {
        try {
            // Get the crafted item
            ItemStack craftedItem = ItemArgument.getItem(context, "craftedItem").createItemStack(1, false);

            // Get the specified main ingredient count
            int mainIngredientCount = context.getArgument("mainCount", Integer.class);

            // Prepare lists for ingredients
            List<ItemStack> mainIngredients = new ArrayList<>();
            List<ItemStack> supplementaryIngredients = new ArrayList<>();

            // Validate main ingredient count
            if (mainIngredientCount > ingredientCount) {
                context.getSource().sendFailure(Component.literal("Main ingredient count cannot exceed total ingredient count.")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            // Collect ingredients
            for (int i = 1; i <= ingredientCount; i++) {
                ItemStack ingredient = ItemArgument.getItem(context, "ingredient" + i).createItemStack(1, false);

                // Categorize ingredients
                if (mainIngredients.size() < mainIngredientCount) {
                    mainIngredients.add(ingredient);
                } else {
                    supplementaryIngredients.add(ingredient);
                }
            }

            // Ensure at least one main ingredient
            if (mainIngredients.isEmpty()) {
                context.getSource().sendFailure(Component.literal("At least one main ingredient is required!")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            // Get recipe data and add recipe
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            boolean recipeAdded = recipeData.setRecipe(craftedItem, mainIngredients, supplementaryIngredients);

            if (recipeAdded) {
                // Construct success message
                StringBuilder message = new StringBuilder("Successfully added recipe for ")
                        .append(craftedItem.getHoverName().getString())
                        .append(" - Main Ingredients: ");

                for (ItemStack ingredient : mainIngredients) {
                    message.append(ingredient.getHoverName().getString()).append(", ");
                }

                message.append(" - Supplementary Ingredients: ");
                for (ItemStack ingredient : supplementaryIngredients) {
                    message.append(ingredient.getHoverName().getString()).append(", ");
                }

                context.getSource().sendSuccess(() -> Component.literal(message.toString())
                        .withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("A recipe for this item already exists!")
                        .withStyle(ChatFormatting.YELLOW));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error adding recipe: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }


    private static int loadModpackRecipes(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            recipeData.clearRecipes();
            registerAllRecipes(context);
            context.getSource().sendSuccess(() -> Component.literal("Successfully loaded all modpack recipes!")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error loading modpack recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }

    private static void loadMonsterRecipes(CommandContext<CommandSourceStack> context) {
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_9_potion bossominium:flower_of_genesis bossominium:redstone_hard_drive minecraft:rotten_flesh alexscaves:charred_remnant samurai_dynasty:jorogumo_eye");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_8_potion legendary_monsters:crystal_of_sandstorm alexscaves:sweet_tooth minecraft:netherite_scrap mutantmonsters:hulk_hammer legendary_monsters:primal_ice_shard");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_7_potion alexscaves:pure_darkness bossominium:soul_eye born_in_chaos_v1:spiritual_dust bossominium:possesed_metal bossominium:dead_charm");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_6_potion nether_star alexsmobs:void_worm_eye kom:nectra_egg cataclysm:monstrous_horn illageandspillage:bag_of_horrors");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_5_potion soulsweapons:chaos_crown cataclysm:witherite_ingot alexscaves:uranium kom:anglospike");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_4_potion iceandfire:dragon_skull_fire eeeabsmobs:guardian_core cataclysm:ignitium_ingot");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_3_potion minecraft:nether_star iceandfire:dragon_skull_ice");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:monster_2_potion terramity:giant_sniffers_hoof soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler");
    }

    private static void loadSailorRecipes(CommandContext<CommandSourceStack> context) {
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_9_potion bossominium:rusted_trident mowziesmobs:sol_visage aquamirae:fin aether:victory_medal samurai_dynasty:oni_horn");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_8_potion bossominium:mossy_stone_tablet iceandfire:sea_serpent_fang alexsmobs:warped_muscle prismarine_shard mutantmonsters:endersoul_hand");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_7_potion eeeabsmobs:heart_of_pagan aquamirae:abyssal_amethyst bossominium:decayed_mushroom mowziesmobs:ice_crystal faded_conquest_2:eye_of_the_storm");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_6_potion nether_star illageandspillage:spellbound_book minecraft:dragon_egg kom:caligan_saw minecraft:white_banner");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_5_potion cataclysm:gauntlet_of_guard aquamirae:frozen_key soulsweapons:essence_of_eventide alexscaves:immortal_embryo");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_4_potion iceandfire:dragon_skull_ice alexscaves:tectonic_shard cataclysm:abyssal_egg");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_3_potion soulsweapons:essence_of_luminescence iceandfire:dragon_skull_lightning");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:sailor_2_potion terramity:angel_feather soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler");
    }

    private static void loadSpectatorRecipes(CommandContext<CommandSourceStack> context) {
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_9_potion bossominium:golden_shard bossominium:forest_core minecraft:ender_pearl iceandfire:witherbone born_in_chaos_v1:nightmare_claw");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_8_potion minecraft:sandstone bossominium:the_golden_eye born_in_chaos_v1:seedof_chaos born_in_chaos_v1:spider_mandible alexscaves:heavy_bone");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_7_potion bossominium:pure_pearl deeperdarker:soul_crystal mutantmonsters:endersoul_hand legendary_monsters:withered_bone aether:gold_dungeon_key");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_6_potion illageandspillage:spellbound_book bossominium:ancient_scrap born_in_chaos_v1:lifestealer_bone born_in_chaos_v1:soul_cutlass");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_5_potion cataclysm:witherite_ingot soulsweapons:lord_soul_rose kom:sigil_of_revival soulsweapons:darkin_blade");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_4_potion iceandfire:dragon_skull_lightning sleepy_hollows:spectral_essence terramity:belt_of_the_gnome_king");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_3_potion iceandfire:dragon_skull_fire born_in_chaos_v1:lord_pumpkinheads_lamp");
        executeRecipeCommand(context, "/beyonderrecipe add lotm:spectator_2_potion terramity:fortunes_favor soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler");
    }

    public static void executeRecipeCommand(CommandContext<CommandSourceStack> context, String command) {
        try {
            context.getSource().getServer().getCommands().performPrefixedCommand(
                    context.getSource(), command.substring(1)); // Remove the leading '/' from command
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to execute recipe: " + command)
                    .withStyle(ChatFormatting.RED));
        }
    }

}

