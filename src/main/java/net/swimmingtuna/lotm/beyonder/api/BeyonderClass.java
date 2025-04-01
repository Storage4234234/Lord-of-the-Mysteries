package net.swimmingtuna.lotm.beyonder.api;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface BeyonderClass {
    List<String> sequenceNames();

    List<Integer> spiritualityLevels();

    List<Integer> spiritualityRegen();

    List<Double> maxHealth();

    void tick(Player player, int sequence);

    Multimap<Integer, Item> getItems();

    List<Integer> mentalStrength();
    
    ChatFormatting getColorFormatting();

    default SimpleContainer getAbilityItemsContainer(int sequenceLevel) {
        // Create a container with 45 slots (5 rows of 9)
        SimpleContainer container = new SimpleContainer(45);

        // Create a map to store items by sequence level in their insertion order
        Map<Integer, List<ItemStack>> orderedItems = new LinkedHashMap<>();

        // Initialize the map for each relevant sequence level
        for (int i = 9; i >= sequenceLevel; i--) {
            orderedItems.put(i, new ArrayList<>());
        }

        // Fill the map with items from getItems() while preserving insertion order
        Multimap<Integer, Item> items = getItems();
        for (Map.Entry<Integer, Item> entry : items.entries()) {
            int level = entry.getKey();
            if (level >= sequenceLevel) {
                orderedItems.get(level).add(entry.getValue().getDefaultInstance());
            }
        }

        // Add items to container in order from highest to lowest sequence
        int slotIndex = 0;
        for (int i = 9; i >= sequenceLevel; i--) {
            List<ItemStack> levelItems = orderedItems.get(i);
            for (ItemStack stack : levelItems) {
                container.setItem(slotIndex++, stack);
            }
        }

        return container;
    }
}
