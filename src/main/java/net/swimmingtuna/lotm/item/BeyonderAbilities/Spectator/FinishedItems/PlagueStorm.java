package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PlagueStorm extends SimpleAbilityItem {

    public PlagueStorm(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 3, 400, 160,80,80);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        addCooldown(player, ItemInit.MENTAL_PLAGUE.get(), 200);
        addCooldown(player, ItemInit.MIND_STORM.get(), 200);
        useSpirituality(player);
        plagueStorm(player, interactionTarget);
        return InteractionResult.SUCCESS;
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 80, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 80, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on a living entity, combines the effects of Mental Plague and Mind Storm, having the effects of Mental Plague happen instantly"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("400").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("8 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public void plagueStorm(LivingEntity player, LivingEntity interactionTarget) {
        if (!player.level().isClientSide()) {
            AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
            interactionTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 1, false, false));
            for (LivingEntity entity : interactionTarget.level().getEntitiesOfClass(LivingEntity.class, interactionTarget.getBoundingBox().inflate(30 * dreamIntoReality.getValue()))) {
                if (entity == player || BeyonderUtil.areAllies(player, entity)) {
                    continue;
                }
                if (entity != interactionTarget) {
                    BeyonderUtil.applyMentalDamage(player, interactionTarget, BeyonderUtil.getDamage(player).get(ItemInit.PLAGUE_STORM.get()));
                } else {
                    BeyonderUtil.applyMentalDamage(player, interactionTarget,2 * BeyonderUtil.getDamage(player).get(ItemInit.PLAGUE_STORM.get()));
                }
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 2, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 1, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 1, false, false));
            }
        }
    }
    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }
}
