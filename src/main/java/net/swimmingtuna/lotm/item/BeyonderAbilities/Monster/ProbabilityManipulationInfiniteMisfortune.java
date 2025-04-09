package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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

public class ProbabilityManipulationInfiniteMisfortune extends SimpleAbilityItem {

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public ProbabilityManipulationInfiniteMisfortune(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 0, 4000, 2400, 777, 777);
    }


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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 777, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 777, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with blocks, pretty much useless for this item
        return attributeBuilder.build();
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, causes the targeted entity, or if nothing is targeted, yourself, to gain infinite misfortune for 3 minutes"));
        tooltipComponents.add(Component.literal("Left click for Probability Manipulation: World Fortune. Shift to increase amount"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("4000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2 Minutes").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack pStack, LivingEntity player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        giveInfiniteMisfortune(pInteractionTarget);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void giveInfiniteMisfortune(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            livingEntity.getPersistentData().putInt("probabilityManipulationInfiniteMisfortune", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROBABILITYINFINITEMISFORTUNE.get()));
        }
    }


    public static void testEvent(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("probabilityManipulationInfiniteMisfortune");
        int y = tag.getInt("probabilityManipulationInfiniteFortune");
        int luckMeteor = tag.getInt("luckMeteor");
        int luckLightningLOTM = tag.getInt("luckLightningLOTM");
        int luckParalysis = tag.getInt("luckParalysis");
        int luckUnequipArmor = tag.getInt("luckUnequipArmor");
        int luckWarden = tag.getInt("luckWarden");
        int luckLightningMC = tag.getInt("luckLightningMC");
        int luckPoison = tag.getInt("luckPoison");
        int luckTornado = tag.getInt("luckTornado");
        int luckStone = tag.getInt("luckStone");
        int luckDoubleDamage = tag.getInt("luckDoubleDamage");
        int cantUseAbility = tag.getInt("cantUseAbility");
        if (!livingEntity.level().isClientSide()) {
            if (x >= 1) {
                if (cantUseAbility == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("cantUseAbility", cantUseAbility + 1);
                }
                if (luckMeteor == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckMeteor", 40);
                }
                if (luckLightningLOTM == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckLightningLOTM", 20);
                }
                if (luckParalysis == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckParalysis", 15);
                }
                if (luckUnequipArmor == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckUnequipArmor", 25);
                }
                if (luckWarden == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckWarden", 30);
                }
                if (luckLightningMC == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckLightningMC", 1);
                }
                if (luckPoison == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckPoison", 1);
                }
                if (luckTornado == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckTornado", 35);
                }
                if (luckStone == 0 && livingEntity.tickCount % 59 == 0) {
                    tag.putInt("luckStone", 10);
                }
                if (livingEntity.tickCount % 101 == 0) {
                    tag.putInt("luckDoubleDamage", luckDoubleDamage + 1);
                }
            }
            if (y >= 1) {
                if (livingEntity.tickCount % 101 == 0) {
                    tag.putInt("luckIgnoreDamage", tag.getInt("luckIgnoreDamage" + 1));
                    tag.putInt("luckDiamonds", tag.getInt("luckDiamonds" + 2));
                    tag.putInt("luckRegeneration", tag.getInt("luckRegeneration" + 1));
                    tag.putInt("windMovingProjectilesCounter", tag.getInt("windMovingProjectilesCounter" + 1));
                    tag.putInt("luckHalveDamage", tag.getInt("luckHalveDamage" + 2));
                    tag.putInt("luckIgnoreMobs", tag.getInt("luckIgnoreMobs" + 2));
                    tag.putInt("luckAttackerPoisoned", tag.getInt("luckAttackerPoisoned" + 2));
                }
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 100;
        }
        return 0;
    }

}