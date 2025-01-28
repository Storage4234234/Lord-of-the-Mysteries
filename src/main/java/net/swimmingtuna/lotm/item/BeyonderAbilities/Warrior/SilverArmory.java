package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

public class SilverArmory extends SimpleAbilityItem {


    public SilverArmory(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        startGigantification(player);
        return InteractionResult.SUCCESS;
    }

    public static void startGigantification(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            float x = livingEntity.getHealth();
            livingEntity.setHealth(Math.max(0.1f, x - 10.0f));
            if (livingEntity instanceof Player player) {
                player.addItem(createEnchantedArmor(ItemInit.SILVER_HELMET.get().getDefaultInstance()));
                player.addItem(createEnchantedArmor(ItemInit.SILVER_CHESTPLATE.get().getDefaultInstance()));
                player.addItem(createEnchantedArmor(ItemInit.SILVER_LEGGINGS.get().getDefaultInstance()));
                player.addItem(createEnchantedArmor(ItemInit.SILVER_BOOTS.get().getDefaultInstance()));
            } else {
                livingEntity.setItemSlot(EquipmentSlot.HEAD, createEnchantedArmor(ItemInit.SILVER_HELMET.get().getDefaultInstance()));
                livingEntity.setItemSlot(EquipmentSlot.CHEST, createEnchantedArmor(ItemInit.SILVER_CHESTPLATE.get().getDefaultInstance()));
                livingEntity.setItemSlot(EquipmentSlot.LEGS, createEnchantedArmor(ItemInit.SILVER_LEGGINGS.get().getDefaultInstance()));
                livingEntity.setItemSlot(EquipmentSlot.FEET, createEnchantedArmor(ItemInit.SILVER_BOOTS.get().getDefaultInstance()));
            }
        }
    }

    private static ItemStack createEnchantedArmor(ItemStack armor) {
        armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        armor.enchant(Enchantments.UNBREAKING, 3);
        armor.enchant(Enchantments.FALL_PROTECTION, 3);
        return armor;
    }

}

