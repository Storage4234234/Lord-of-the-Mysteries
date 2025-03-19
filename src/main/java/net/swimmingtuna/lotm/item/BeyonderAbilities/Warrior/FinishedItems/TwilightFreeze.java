package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TwilightFreeze extends SimpleAbilityItem {

    public TwilightFreeze(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 3000, 1800);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        saveDataReboot(player, player, player.getPersistentData());
        useSpirituality(player);
        addCooldown(player);
        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        saveDataReboot(player, interactionTarget, interactionTarget.getPersistentData());
        return InteractionResult.SUCCESS;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on an ally or nothing, freeze them or yourself in time, causing your health, potion effects, luck, misfortune, sanity, corruption, age, and spirituality to all stay static for 30 seconds. If used on an enemy, they will be completely stuck and unable to move for 15 seconds."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("3000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("90 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void saveDataReboot(LivingEntity livingEntity, LivingEntity target, CompoundTag tag) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity == target || BeyonderUtil.isAllyOf(livingEntity, target)) {
                Collection<MobEffectInstance> activeEffects = target.getActiveEffects();
                tag.putInt("twilightFreezeCooldown", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTFREEZE.get()));
                tag.putInt("twilightPotionEffectsCount", activeEffects.size());
                int i = 0;
                for (MobEffectInstance effect : activeEffects) {
                    CompoundTag effectTag = new CompoundTag();
                    effect.save(effectTag);
                    tag.put("twilightPotionEffect_" + i, effectTag);
                    i++;
                }
                double luck = tag.getDouble("luck");
                double misfortune = tag.getDouble("misfortune");
                double sanity = tag.getDouble("sanity");
                double corruption = tag.getDouble("corruption");
                int age = tag.getInt("age");
                tag.putInt("twilightAge", age);
                tag.putInt("twilightLuck", (int) luck);
                tag.putInt("twilightMisfortune", (int) misfortune);
                tag.putInt("twilightSanity", (int) sanity);
                tag.putInt("twilightCorruption", (int) corruption);
                tag.putInt("twilightHealth", (int) target.getHealth());
                tag.putInt("twilightSpirituality", (int) BeyonderUtil.getSpirituality(target));
            } else {
                tag.putInt("inTwilight", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTFREEZE.get()) / 2);
            }
        }
    }

    private static void restoreDataReboot(LivingEntity player, CompoundTag tag) {
        for (MobEffectInstance activeEffect : new ArrayList<>(player.getActiveEffects())) {
            player.removeEffect(activeEffect.getEffect());
        }
        int age = tag.getInt("twilightAge");
        int sanity = tag.getInt("twilightSanity");
        int luck = tag.getInt("twilightLuck");
        int misfortune = tag.getInt("twilightMisfortune");
        int corruption = tag.getInt("twilightCorruption");
        int health = tag.getInt("twilightHealth");
        int spirituality = tag.getInt("twilightSpirituality");
        int effectCount = tag.getInt("twilightPotionEffectsCount");
        for (int i = 0; i < effectCount; i++) {
            CompoundTag effectTag = tag.getCompound("twilightPotionEffect_" + i);
            MobEffectInstance effect = MobEffectInstance.load(effectTag);
            if (effect != null) {
                player.addEffect(effect);
            }
        }
        tag.putInt("age", age);
        tag.putDouble("sanity", sanity);
        tag.putDouble("corruption", corruption);
        tag.putDouble("luck", luck);
        tag.putDouble("misfortune", misfortune);
        BeyonderUtil.setSpirituality(player, spirituality);
        player.setHealth(Math.max(1, health));
    }

    public static void twilightFreezeTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("twilightFreezeCooldown");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            int y = (int) x / 20;
            tag.putInt("twilightFreezeCooldown", x - 1);
            restoreDataReboot(livingEntity, tag);
            if (livingEntity instanceof Player player && player.tickCount % 20 == 0) {
                player.displayClientMessage(Component.literal("Frozen in time for " + y + " seconds").withStyle(ChatFormatting.YELLOW), true);
            }
        }
        if (!livingEntity.level().isClientSide() && livingEntity.getPersistentData().getInt("inTwilight") >= 1) {
            livingEntity.getPersistentData().putInt("inTwilight", livingEntity.getPersistentData().getInt("inTwilight") - 1);
        }
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }


    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }
}
