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
        SimpleContainer container = new SimpleContainer(45);
        Map<Integer, List<ItemStack>> orderedItems = new LinkedHashMap<>();
        for (int i = 9; i >= sequenceLevel; i--) {
            orderedItems.put(i, new ArrayList<>());
        }
        Multimap<Integer, Item> items = getItems();
        for (Map.Entry<Integer, Item> entry : items.entries()) {
            int level = entry.getKey();
            if (level >= sequenceLevel) {
                orderedItems.get(level).add(entry.getValue().getDefaultInstance());
            }
        }
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
