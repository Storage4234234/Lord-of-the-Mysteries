package net.swimmingtuna.lotm.util.ClientData;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class ClientAbilityCooldownData {
    private static final Map<String, Integer> combinationCooldowns = new HashMap<>();

    public static void setAbilityCooldown(String combination, int remainingTicks) {
        if (remainingTicks <= 0) {
            combinationCooldowns.remove(combination);
        } else {
            combinationCooldowns.put(combination, remainingTicks);
        }
    }

    public static void clearAbilities() {
        combinationCooldowns.clear();
    }

    public static Map<String, Integer> getCooldowns() {
        return new HashMap<>(combinationCooldowns);
    }

    public static Integer getCooldownForCombination(String combination) {
        return combinationCooldowns.get(combination);
    }

    public static Integer getCooldownInSecondsForCombination(String combination) {
        Integer ticks = combinationCooldowns.get(combination);
        return ticks != null ? (int) Math.ceil(ticks / 20.0) : null;
    }

    public static void tickCooldowns() {
        combinationCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }
}