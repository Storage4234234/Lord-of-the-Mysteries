package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SilverArmory extends SimpleAbilityItem {


    public SilverArmory(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 3, 0, 900);
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
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, conjure a set of armor made of mercury and silver which can be worn by allies. While worn, all damage under 20 will be negated, and supernatural damage heavily reduced."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("None. 10 Health").withStyle(ChatFormatting.RED)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("45 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}

