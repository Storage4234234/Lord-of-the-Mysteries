package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnWeaponry.hasFullDawnArmor;

public class DawnArmory extends SimpleAbilityItem {


    public DawnArmory(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        storeAndPutArmor(player);
        return InteractionResult.SUCCESS;
    }

    public static void storeAndPutArmor(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && livingEntity instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            boolean isDawnArmorOn = persistentData.getBoolean("dawnArmorOn");
            if (!isDawnArmorOn) {
                CompoundTag armorData = new CompoundTag();
                ListTag armorItems = new ListTag();
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack armorStack = player.getItemBySlot(slot);
                    if (!armorStack.isEmpty()) {
                        CompoundTag slotTag = new CompoundTag();
                        slotTag.putInt("dawnArmorSlot", slot.getIndex());
                        armorStack.save(slotTag);
                        armorItems.add(slotTag);
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
                player.setItemSlot(EquipmentSlot.HEAD, createEnchantedArmor(ItemInit.DAWN_HELMET.get().getDefaultInstance()));
                player.setItemSlot(EquipmentSlot.CHEST, createEnchantedArmor(ItemInit.DAWN_CHESTPLATE.get().getDefaultInstance()));
                player.setItemSlot(EquipmentSlot.LEGS,  createEnchantedArmor(ItemInit.DAWN_LEGGINGS.get().getDefaultInstance()));
                player.setItemSlot(EquipmentSlot.FEET, createEnchantedArmor(ItemInit.DAWN_BOOTS.get().getDefaultInstance()));
                armorData.put("dawnArmorItems", armorItems);
                persistentData.put("dawnStoredArmorData", armorData);
                persistentData.putBoolean("dawnArmorOn", true);
            } else {
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack currentArmor = player.getItemBySlot(slot);
                    if (!currentArmor.isEmpty() && (currentArmor.is(ItemInit.DAWN_HELMET.get()) || currentArmor.is(ItemInit.DAWN_CHESTPLATE.get()) || currentArmor.is(ItemInit.DAWN_LEGGINGS.get()) || currentArmor.is(ItemInit.DAWN_BOOTS.get()))) {
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
                if (persistentData.contains("dawnStoredArmorData")) {
                    CompoundTag armorData = persistentData.getCompound("dawnStoredArmorData");
                    ListTag armorItems = armorData.getList("dawnArmorItems", 10);
                    for (int i = 0; i < armorItems.size(); i++) {
                        CompoundTag slotTag = armorItems.getCompound(i);
                        int slotIndex = slotTag.getInt("dawnArmorSlot");
                        ItemStack armorStack = ItemStack.of(slotTag);
                        EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slotIndex);
                        if (slot != null) {
                            player.setItemSlot(slot, armorStack);
                        }
                    }
                    persistentData.remove("dawnStoredArmorData");
                }
                persistentData.putBoolean("dawnArmorOn", false);
            }
        }
    }

    private static void removeArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armorStack = player.getItemBySlot(slot);
                if (!armorStack.isEmpty()) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    public static void dawnArmorTickEvent(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.tickCount % 20 == 0 && !entity.level().isClientSide() && (BeyonderUtil.getPathway(entity) == BeyonderClassInit.WARRIOR.get() || BeyonderUtil.sequenceAbleCopy(entity)) && BeyonderUtil.getSequence(entity) <= 6) {
            if (hasFullDawnArmor(entity)) {
                BeyonderUtil.useSpirituality(entity, 15);
            }
        }
    }

    private static ItemStack createEnchantedArmor(ItemStack armor) {
        armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        armor.enchant(Enchantments.UNBREAKING, 3);
        armor.enchant(Enchantments.FALL_PROTECTION, 3);
        return armor;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, remove your current armor and conjure a set of dawn armor from light. If equipped, all damage lower than 10 will be ignored, and all supernatural damage will be halved. Use again to remove the armor."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("20 per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

}


