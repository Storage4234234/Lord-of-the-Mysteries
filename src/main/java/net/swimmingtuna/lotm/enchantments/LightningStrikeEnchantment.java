package net.swimmingtuna.lotm.enchantments;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public class LightningStrikeEnchantment extends Enchantment {
    public LightningStrikeEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
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
        return Component.translatable("enchantment.lotm.lightning_strike");
    }

    private void handleLightningStrike(Entity pAttacker, Entity pTarget, int pLevel) {
        if (pAttacker.level() instanceof ServerLevel serverLevel && pLevel >= 1) {
            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, pAttacker.level());
            lightningBolt.teleportTo(pTarget.getX(), pTarget.getY(), pTarget.getZ());
            if (pAttacker instanceof LivingEntity livingAttacker) {
                ItemStack weapon = livingAttacker.getMainHandItem();
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(weapon);
                enchantments.remove(this);
                if (pLevel > 1) {
                    enchantments.put(this, pLevel - 1);
                }
                EnchantmentHelper.setEnchantments(enchantments, weapon);
                lightningBolt.setDamage(10);
            }
            pAttacker.level().addFreshEntity(lightningBolt);
        }
    }

    public static final EnchantmentCategory WEAPON_TOOL = EnchantmentCategory.create("WEAPON_TOOL", (item) -> (item instanceof TieredItem) || (item instanceof ProjectileWeaponItem) || (item.getClass().getSimpleName().toLowerCase().contains("weapon")) || (item.getClass().getSimpleName().toLowerCase().contains("tool")));

}