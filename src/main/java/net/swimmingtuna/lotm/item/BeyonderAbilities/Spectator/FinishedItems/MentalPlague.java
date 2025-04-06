package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
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
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MentalPlague extends SimpleAbilityItem {

    public MentalPlague(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 4, 200, 200,100,100);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        mentalPlauge(interactionTarget);
        addCooldown(player, this,  (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MENTAL_PLAGUE.get()));
        return InteractionResult.SUCCESS;
    }

    public void mentalPlauge(LivingEntity interactionTarget) {
        if (!interactionTarget.level().isClientSide()) {
            interactionTarget.addEffect(new MobEffectInstance(ModEffects.MENTALPLAGUE.get(), 620, 1));
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 100, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 100, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("pon use on a living entity, plants a plague seed in the target, sprouting after 30 seconds and dealing massive damage to it and all entities around it, be careful as this can effect the user"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("200").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void mentalPlague(LivingEntity entity) {
        //MENTAL PLAGUE
        int mentalPlagueTimer = entity.getPersistentData().getInt("MentalPlagueTimer");
        if (entity.hasEffect(ModEffects.MENTALPLAGUE.get())) {
            mentalPlagueTimer++;

            if (mentalPlagueTimer >= 600) {
                for (LivingEntity entity1 : entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(50))) {
                    applyEffectsAndDamage(entity1);

                }
                applyEffectsAndDamage(entity);
                mentalPlagueTimer = 0;
            }
        }
        entity.getPersistentData().putInt("MentalPlagueTimer", mentalPlagueTimer);
    }

    private static void applyEffectsAndDamage(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 2, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 1, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1, false, false));
        BeyonderUtil.applyMentalDamage(entity, entity, 20);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            if (livingEntity.hasEffect(MobEffects.INVISIBILITY)) {
                return (int) (100 - (target.distanceTo(livingEntity) / 2));
            } else {
                return (int) (100 - target.distanceTo(livingEntity));
            }
        }
        return 0;
    }
}
