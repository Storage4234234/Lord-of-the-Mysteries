package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.ModArmorMaterials;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SilverSwordManifestation extends SimpleAbilityItem {


    public SilverSwordManifestation(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 3, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        manifestSword(player);
        return InteractionResult.SUCCESS;
    }

    public static void manifestSword(Player player) {
        if (!player.level().isClientSide()) {
            int selectedSlot = findClosestEmptySlot(player);
            if (selectedSlot != -1) {
                Inventory inventory = player.getInventory();
                ItemStack sword = createSword(ItemInit.SWORDOFSILVER.get().getDefaultInstance());
                inventory.setItem(selectedSlot, sword);
            }
            player.setHealth(player.getHealth() - 10.0f);
        }
    }

    private static ItemStack createSword(ItemStack armor) {
        armor.enchant(Enchantments.SHARPNESS, 5);
        armor.enchant(Enchantments.KNOCKBACK, 2);
        armor.enchant(Enchantments.UNBREAKING, 3);
        armor.enchant(Enchantments.FIRE_ASPECT, 2);
        return armor;
    }

    private static int findClosestEmptySlot(Player player) {
        Inventory inventory = player.getInventory();
        int selectedSlot = player.getInventory().selected;
        if (inventory.getItem(selectedSlot).isEmpty()) {
            return selectedSlot;
        }
        for (int distance = 1; distance < 9; distance++) {
            int rightSlot = (selectedSlot + distance) % 9;
            if (inventory.getItem(rightSlot).isEmpty()) {
                return rightSlot;
            }
            int leftSlot = (selectedSlot - distance + 9) % 9;
            if (inventory.getItem(leftSlot).isEmpty()) {
                return leftSlot;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("WIP."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}

