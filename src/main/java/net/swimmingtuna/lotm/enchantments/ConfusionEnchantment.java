package net.swimmingtuna.lotm.enchantments;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.Map;

import static net.swimmingtuna.lotm.enchantments.LightningStrikeEnchantment.WEAPON_TOOL;

public class ConfusionEnchantment extends Enchantment {
    public ConfusionEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    public void doPostAttack(LivingEntity pAttacker, Entity pTarget, int pLevel) {
        handleLightningStrike(pAttacker, pTarget, pLevel);
        super.doPostAttack(pAttacker, pTarget, pLevel);
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        Item item = stack.getItem();
        // Additional compatibility checks for modded items
        return WEAPON_TOOL.canEnchant(item) ||
                item.getClass().getSimpleName().toLowerCase().contains("spear") ||
                item.getClass().getSimpleName().toLowerCase().contains("dagger") ||
                item.getClass().getSimpleName().toLowerCase().contains("sword") ||
                item.getClass().getSimpleName().toLowerCase().contains("axe") ||
                item.getClass().getSimpleName().toLowerCase().contains("hammer");
    }

    @Override
    public void doPostHurt(LivingEntity pTarget, Entity pAttacker, int pLevel) {
        if (pAttacker instanceof Projectile) {
            handleLightningStrike(pAttacker, pTarget, pLevel);
        }
        super.doPostHurt(pTarget, pAttacker, pLevel);
    }

    @Override
    public Component getFullname(int level) {
        return Component.translatable("enchantment.lotm.confusion");
    }

    private void handleLightningStrike(Entity pAttacker, Entity pTarget, int pLevel) {
        if (pAttacker.level() instanceof ServerLevel serverLevel && pLevel >= 1) {
            if (pAttacker instanceof LivingEntity livingAttacker) {
                ItemStack weapon = livingAttacker.getMainHandItem();
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);
                enchantments.remove(this);
                if (pLevel > 1) {
                    enchantments.put(this, pLevel - 1);
                }
                EnchantmentHelper.setEnchantments(enchantments, weapon);
                int sequence = BeyonderUtil.getSequence(livingAttacker);
                if (pTarget instanceof LivingEntity living) {
                    BeyonderUtil.applyMobEffect(living, MobEffects.CONFUSION, 150, 1, true, true);
                }
            }
        }
    }
}