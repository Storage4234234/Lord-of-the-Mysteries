package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import java.util.List;

public class TwilightAccelerate extends SimpleAbilityItem {

    public TwilightAccelerate(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 3000, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        saveDataReboot(player, player);
        if (!player.isShiftKeyDown()) {
            useSpirituality(player);
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        saveDataReboot(player, interactionTarget);
        return InteractionResult.SUCCESS;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on nothing or an ally, accelerates your/them in a positive way. Having their speed be higher, all their abilities that happen over time to occur quicker, their spirituality and health to regenerate faster, their benefitial potion effects to last longer, and harmful ones to go away quicker. If used on an enemy, accelerates their time negatively, causing them to age quick, lose spirituality fast, have beneficial effects go away faster, and harmful effects to last longer."));
        tooltipComponents.add(Component.literal("Left Click for Twilight: Light"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("3000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("60 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void saveDataReboot(LivingEntity livingEntity,LivingEntity target) {
        if (!livingEntity.level().isClientSide() && !target.level().isClientSide()) {
            CompoundTag tag = target.getPersistentData();
            if (livingEntity == target || BeyonderUtil.areAllies(livingEntity, target)) {
                tag.putInt("twilightAgeAccelerate", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTACCELERATE.get()));
            } else {
                tag.putInt("twilightAgeAccelerateEnemy", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTACCELERATE.get()) / 2);
            }
        }
    }

    public static void twilightAccelerateTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("twilightAgeAccelerate");
        int y = tag.getInt("twilightAgeAccelerateEnemy");
        if (!livingEntity.level().isClientSide()) {
            if (x >= 1) {
                if (x >= 2) {
                    if (livingEntity instanceof Player player) {
                        player.getAbilities().setFlyingSpeed(0.15f);
                        player.getAbilities().setWalkingSpeed(0.25f);
                    }
                }
                if (x == 1) {
                    if (livingEntity instanceof Player player) {
                        player.getAbilities().setFlyingSpeed(0.05f);
                        player.getAbilities().setWalkingSpeed(0.1f);
                    }
                }
                tag.putInt("twilightAgeAccelerate", x - 1);
                livingEntity.tick();
                BeyonderUtil.setSpirituality(livingEntity, BeyonderUtil.getSpirituality(livingEntity) + (10 - BeyonderUtil.getSequence(livingEntity)));
                BeyonderUtil.applyMobEffect(livingEntity, MobEffects.REGENERATION, 20,4,true,true);
                for (MobEffectInstance mobEffect : livingEntity.getActiveEffects()) {
                    int currentDuration = mobEffect.getDuration();
                    MobEffect type = mobEffect.getEffect();
                    if (type.isBeneficial()) {
                        livingEntity.addEffect(new MobEffectInstance(type, currentDuration + 3, mobEffect.getAmplifier(), mobEffect.isAmbient(), mobEffect.isVisible()));
                    } else {
                        livingEntity.addEffect(new MobEffectInstance(type, currentDuration / 2, mobEffect.getAmplifier(), mobEffect.isAmbient(), mobEffect.isVisible()));
                    }
                }
            }
            if (y >= 1) {
                BeyonderUtil.setSpirituality(livingEntity, BeyonderUtil.getSpirituality(livingEntity) - (int) ((10 - BeyonderUtil.getSequence(livingEntity)) / 2));
                tag.putInt("twilightAgeAccelerateEnemy", y - 1);
                if (y >= 2) {
                    if (livingEntity instanceof Player player) {
                        player.getAbilities().setFlyingSpeed(0.02f);
                        player.getAbilities().setWalkingSpeed(0.03f);
                    }
                }
                if (y == 1) {
                    if (livingEntity instanceof Player player) {
                        player.getAbilities().setFlyingSpeed(0.05f);
                        player.getAbilities().setWalkingSpeed(0.1f);
                    }
                }
                if (Math.random() > 0.95) {
                    tag.putInt("age", tag.getInt("age") + 20);
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD),true);
                    }
                }
                for (MobEffectInstance mobEffect : livingEntity.getActiveEffects()) {
                    int currentDuration = mobEffect.getDuration();
                    MobEffect type = mobEffect.getEffect();
                    if (type.isBeneficial()) {
                        livingEntity.addEffect(new MobEffectInstance(type, currentDuration - 1, mobEffect.getAmplifier(), mobEffect.isAmbient(), mobEffect.isVisible()));
                    } else {
                        livingEntity.addEffect(new MobEffectInstance(type, currentDuration + 3, mobEffect.getAmplifier(), mobEffect.isAmbient(), mobEffect.isVisible()));
                    }
                }
            }
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
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }
}
