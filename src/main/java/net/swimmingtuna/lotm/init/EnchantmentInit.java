package net.swimmingtuna.lotm.init;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.enchantments.LightningStrikeEnchantment;

public class EnchantmentInit {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, LOTM.MOD_ID);

    public static RegistryObject<Enchantment> LIGHTNING_STRIKE = ENCHANTMENTS.register("lightning_strike",
            () -> new LightningStrikeEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
    public static RegistryObject<Enchantment> FLAME = ENCHANTMENTS.register("flame",
            () -> new LightningStrikeEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
    public static RegistryObject<Enchantment> WITHER_ENCHANTMENT = ENCHANTMENTS.register("wither",
            () -> new LightningStrikeEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
    public static RegistryObject<Enchantment> CONFUSION_ENCHANTMENT = ENCHANTMENTS.register("confusion",
            () -> new LightningStrikeEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
    public static RegistryObject<Enchantment> FREEZE = ENCHANTMENTS.register("freeze",
            () -> new LightningStrikeEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
