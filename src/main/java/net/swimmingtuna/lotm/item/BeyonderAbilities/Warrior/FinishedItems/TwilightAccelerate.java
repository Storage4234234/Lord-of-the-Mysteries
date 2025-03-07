package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TwilightAccelerate extends SimpleAbilityItem {

    public TwilightAccelerate(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 3000, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
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
    public InteractionResult useAbilityOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
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
        tooltipComponents.add(Component.literal("Upon use if you're shifting, save your current state including health, spirituality, potion effects, luck, misfortune, sanity, and corruption. If not shifting, load your saved state"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("2000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void saveDataReboot(LivingEntity livingEntity,LivingEntity target) {
        if (!livingEntity.level().isClientSide() && !target.level().isClientSide()) {
            CompoundTag tag = target.getPersistentData();
            if (livingEntity == target || BeyonderUtil.isAllyOf(livingEntity, target)) {
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
                    tag.putInt("age", tag.getInt("age") + 1);
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


    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }
}
