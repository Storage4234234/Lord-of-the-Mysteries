package net.swimmingtuna.lotm.world.worlddata;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeyonderRecipeData extends SavedData {
    private static final String RECIPES_KEY = "BeyonderRecipes";
    private final Map<ItemStack, RecipeIngredients> beyonderRecipes = new HashMap<>();

    public static class RecipeIngredients {
        private final List<ItemStack> mainIngredients;
        private final List<ItemStack> supplementaryIngredients;

        public RecipeIngredients(List<ItemStack> mainIngredients, List<ItemStack> supplementaryIngredients) {
            this.mainIngredients = mainIngredients;
            this.supplementaryIngredients = supplementaryIngredients;
        }

        public List<ItemStack> getMainIngredients() {
            return new ArrayList<>(mainIngredients);
        }

        public List<ItemStack> getSupplementaryIngredients() {
            return new ArrayList<>(supplementaryIngredients);
        }
    }

    private BeyonderRecipeData() {}

    public static BeyonderRecipeData getInstance(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                BeyonderRecipeData::load,
                BeyonderRecipeData::create,
                RECIPES_KEY
        );
    }

    public boolean setRecipe(ItemStack potion, List<ItemStack> mainIngredients, List<ItemStack> supplementaryIngredients) {
        boolean recipeExists = beyonderRecipes.keySet().stream().anyMatch(existingPotion -> ItemStack.isSameItemSameTags(existingPotion, potion));
        if (recipeExists) {
            return false;
        }
        beyonderRecipes.put(potion, new RecipeIngredients(mainIngredients, supplementaryIngredients));
        setDirty();
        return true;
    }

    public boolean removeRecipe(ItemStack potion) {
        RecipeIngredients removedRecipe = null;
        for (ItemStack existingPotion : beyonderRecipes.keySet()) {
            if (ItemStack.isSameItemSameTags(existingPotion, potion)) {
                removedRecipe = beyonderRecipes.remove(existingPotion);
                break;
            }
        }
        if (removedRecipe != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public void clearRecipes() {
        beyonderRecipes.clear();
        setDirty();
    }

    public Map<ItemStack, RecipeIngredients> getBeyonderRecipes() {
        return new HashMap<>(beyonderRecipes);
    }

    public void sendPlayerRecipeValues(Player player) {
        if (beyonderRecipes.isEmpty()) {
            player.sendSystemMessage(Component.literal("No Beyonder recipes found.").withStyle(ChatFormatting.RED));
            return;
        }
        for (Map.Entry<ItemStack, RecipeIngredients> entry : beyonderRecipes.entrySet()) {
            StringBuilder recipeMessage = new StringBuilder("Potion: ").append(entry.getKey().getHoverName().getString()).append(" - Main Ingredients: ");

            for (ItemStack ingredient : entry.getValue().getMainIngredients()) {
                recipeMessage.append(ingredient.getHoverName().getString()).append(", ");
            }

            recipeMessage.append(" - Supplementary Ingredients: ");
            for (ItemStack ingredient : entry.getValue().getSupplementaryIngredients()) {
                recipeMessage.append(ingredient.getHoverName().getString()).append(", ");
            }
            player.sendSystemMessage(Component.literal(recipeMessage.toString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag recipeList = new ListTag();
        for (Map.Entry<ItemStack, RecipeIngredients> entry : beyonderRecipes.entrySet()) {
            CompoundTag recipeTag = new CompoundTag();
            CompoundTag potionTag = new CompoundTag();
            entry.getKey().save(potionTag);
            recipeTag.put("beyonderPotion", potionTag);
            ListTag mainIngredientsTag = new ListTag();
            for (ItemStack ingredient : entry.getValue().getMainIngredients()) {
                CompoundTag ingredientTag = new CompoundTag();
                ingredient.save(ingredientTag);
                mainIngredientsTag.add(ingredientTag);
            }
            recipeTag.put("mainIngredients", mainIngredientsTag);
            ListTag supplementaryIngredientsTag = new ListTag();
            for (ItemStack ingredient : entry.getValue().getSupplementaryIngredients()) {
                CompoundTag ingredientTag = new CompoundTag();
                ingredient.save(ingredientTag);
                supplementaryIngredientsTag.add(ingredientTag);
            }
            recipeTag.put("supplementaryIngredients", supplementaryIngredientsTag);

            recipeList.add(recipeTag);
        }

        compoundTag.put(RECIPES_KEY, recipeList);
        return compoundTag;
    }

    public static BeyonderRecipeData load(CompoundTag compoundTag) {
        BeyonderRecipeData data = new BeyonderRecipeData();
        if (compoundTag.contains(RECIPES_KEY)) {
            ListTag recipeList = compoundTag.getList(RECIPES_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < recipeList.size(); i++) {
                CompoundTag recipeTag = recipeList.getCompound(i);
                ItemStack potion = ItemStack.of(recipeTag.getCompound("beyonderPotion"));
                List<ItemStack> mainIngredients = new ArrayList<>();
                ListTag mainIngredientsTag = recipeTag.getList("mainIngredients", Tag.TAG_COMPOUND);
                for (int j = 0; j < mainIngredientsTag.size(); j++) {
                    mainIngredients.add(ItemStack.of(mainIngredientsTag.getCompound(j)));
                }
                List<ItemStack> supplementaryIngredients = new ArrayList<>();
                ListTag supplementaryIngredientsTag = recipeTag.getList("supplementaryIngredients", Tag.TAG_COMPOUND);
                for (int j = 0; j < supplementaryIngredientsTag.size(); j++) {
                    supplementaryIngredients.add(ItemStack.of(supplementaryIngredientsTag.getCompound(j)));
                }
                data.beyonderRecipes.put(potion, new RecipeIngredients(mainIngredients, supplementaryIngredients));
            }
        }
        return data;
    }

    public static BeyonderRecipeData create() {
        return new BeyonderRecipeData();
    }
}