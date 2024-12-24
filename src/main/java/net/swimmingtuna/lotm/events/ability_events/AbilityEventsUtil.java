package net.swimmingtuna.lotm.events.ability_events;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AbilityEventsUtil {
    public static @NotNull String getString(Player otherPlayer, double horizontalAngle, Vec3 directionToPlayer) {
        String horizontalDirection;
        if (Math.abs(horizontalAngle) < Math.PI / 4) {
            horizontalDirection = "in front of";
        } else if (horizontalAngle < -Math.PI * 3 / 4 || horizontalAngle > Math.PI * 3 / 4) {
            horizontalDirection = "behind";
        } else if (horizontalAngle < 0) {
            horizontalDirection = "to the right of";
        } else {
            horizontalDirection = "to the left of";
        }

        String verticalDirection;
        if (directionToPlayer.y > 0.2) {
            verticalDirection = "above";
        } else if (directionToPlayer.y < -0.2) {
            verticalDirection = "below";
        } else {
            verticalDirection = "at the same level as";
        }

        return otherPlayer.getName().getString() + " is " + horizontalDirection + " and " + verticalDirection + " you.";
    }

    public static void removeArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armorStack = player.getItemBySlot(slot);
                if (!armorStack.isEmpty()) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }
}
