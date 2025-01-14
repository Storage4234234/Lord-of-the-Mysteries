package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

public class DawnWeaponry extends SimpleAbilityItem {


    public DawnWeaponry(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        dawnWeaponry(player);
        return InteractionResult.SUCCESS;
    }

    public static void dawnWeaponry(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            int selectedSlot = findClosestEmptySlot(player);
            int x = tag.getInt("dawnWeaponry");
            if (selectedSlot != -1) {
                Inventory inventory = player.getInventory();
                switch (x) {
                    case 1 -> {
                        ItemStack sword = new ItemStack(ItemInit.SWORDOFDAWN.get());
                        inventory.setItem(selectedSlot, sword);
                    }
                    case 2 -> {
                        ItemStack axe = new ItemStack(ItemInit.PICKAXEOFDAWN.get());
                        inventory.setItem(selectedSlot, axe);
                    }
                    case 3 -> {
                        ItemStack trident = new ItemStack(ItemInit.SPEAROFDAWN.get());
                        inventory.setItem(selectedSlot, trident);
                    }
                }
            }
        }
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

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            if (player.tickCount % 2 == 0 && !level.isClientSide()) {
                int selectedSlot = findClosestEmptySlot(player);
                if (player.getMainHandItem().getItem() instanceof DawnWeaponry) {
                    player.displayClientMessage(Component.literal("Dawn Weaponry Choice is: " + dawnWeaponryString(player)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW), true);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    public static String dawnWeaponryString (Player pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        int dawnWeaponry = tag.getInt("dawnWeaponry");
        if (dawnWeaponry == 1) {
            return "Sword of Dawn";
        } else if (dawnWeaponry == 2) {
            return "Pickaxe of Dawn";
        } else if (dawnWeaponry == 3) {
            return "Spear of Dawn";
        }
        return "None";
    }
}

