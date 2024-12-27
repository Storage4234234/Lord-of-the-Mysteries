package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worlddata.BeyonderRecipeData;

import java.util.ArrayList;
import java.util.List;

public class BeyonderRecipeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("beyonderrecipe")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("craftedItem", ItemArgument.item(buildContext))
                                .then(Commands.argument("ingredient1", ItemArgument.item(buildContext))
                                        .executes(BeyonderRecipeCommand::addRecipe)
                                        .then(Commands.argument("ingredient2", ItemArgument.item(buildContext))
                                                .executes(BeyonderRecipeCommand::addRecipe)
                                                .then(Commands.argument("ingredient3", ItemArgument.item(buildContext))
                                                        .executes(BeyonderRecipeCommand::addRecipe)
                                                        .then(Commands.argument("ingredient4", ItemArgument.item(buildContext))
                                                                .executes(BeyonderRecipeCommand::addRecipe)
                                                                .then(Commands.argument("ingredient5", ItemArgument.item(buildContext))
                                                                        .executes(BeyonderRecipeCommand::addRecipe)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("craftedItem", ItemArgument.item(buildContext))
                                .executes(BeyonderRecipeCommand::removeRecipe)
                        )
                )
                .then(Commands.literal("load")
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

    private static int addRecipe(CommandContext<CommandSourceStack> context) {
        try {
            ItemStack craftedItem = ItemArgument.getItem(context, "craftedItem").createItemStack(1, false);
            List<ItemStack> ingredients = new ArrayList<>();
            try {
                ingredients.add(ItemArgument.getItem(context, "ingredient1").createItemStack(1, false));
                try {
                    ingredients.add(ItemArgument.getItem(context, "ingredient2").createItemStack(1, false));
                    try {
                        ingredients.add(ItemArgument.getItem(context, "ingredient3").createItemStack(1, false));
                        try {
                            ingredients.add(ItemArgument.getItem(context, "ingredient4").createItemStack(1, false));
                            try {
                                ingredients.add(ItemArgument.getItem(context, "ingredient5").createItemStack(1, false));
                            } catch (IllegalArgumentException e) {

                            }
                        } catch (IllegalArgumentException e) {

                        }
                    } catch (IllegalArgumentException e) {

                    }
                } catch (IllegalArgumentException e) {

                }
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal("At least one ingredient is required!").withStyle(ChatFormatting.RED));
                return 0;
            }
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            boolean recipeAdded = recipeData.setRecipe(craftedItem, ingredients);
            if (recipeAdded) {
                context.getSource().sendSuccess(() -> Component.literal("Successfully added recipe for " +
                        craftedItem.getHoverName().getString()).withStyle(ChatFormatting.GREEN), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("A recipe for this item already exists!").withStyle(ChatFormatting.YELLOW));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error adding recipe: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED));
            return 0;
        }
    }
    private static int loadModpackRecipes(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            recipeData.clearRecipes();
            loadMonsterRecipes(context);
            loadSailorRecipes(context);
            loadSpectatorRecipes(context);
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

    private static void executeRecipeCommand(CommandContext<CommandSourceStack> context, String command) {
        try {
            context.getSource().getServer().getCommands().performPrefixedCommand(
                    context.getSource(), command.substring(1)); // Remove the leading '/' from command
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to execute recipe: " + command)
                    .withStyle(ChatFormatting.RED));
        }
    }

}

