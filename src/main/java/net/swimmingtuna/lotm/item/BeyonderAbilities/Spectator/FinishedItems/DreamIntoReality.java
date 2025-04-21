package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;


public class DreamIntoReality extends SimpleAbilityItem {
    private static final String CAN_FLY = "CanFly";

    public DreamIntoReality(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 2, 300, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (player.getPersistentData().getBoolean(CAN_FLY)) {
            addCooldown(player);
        } else {
            useSpirituality(player);
            addCooldown(player, this, 20);
        }
        toggleFlying(player);
        return InteractionResult.SUCCESS;
    }

    private void toggleFlying(LivingEntity player) {
        if (!player.level().isClientSide()) {
            boolean canFly = player.getPersistentData().getBoolean(CAN_FLY);
            if (canFly) {
                stopFlying(player);
            } else {
                startFlying(player);
            }
        }
    }

    private void startFlying(LivingEntity player) { //marked
        if (!player.level().isClientSide()) {
            int dreamIntoReality = player.getPersistentData().getInt("dreamIntoReality");
            if (dreamIntoReality != 3) {
                player.getPersistentData().putBoolean(CAN_FLY, true);
                player.getPersistentData().putInt("dreamIntoReality", 4);
                ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);
                scaleData.setTargetScale(scaleData.getBaseScale() * 12);
                scaleData.markForSync(true);
                if (player instanceof Player pPlayer) {
                    Abilities playerAbilities = pPlayer.getAbilities();
                    if (!pPlayer.isCreative()) {
                        playerAbilities.mayfly = true;
                        playerAbilities.flying = true;
                        playerAbilities.setFlyingSpeed(0.1F);
                    }
                    pPlayer.onUpdateAbilities();
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                    }
                }
            }
        }
    }

    public static void stopFlying(LivingEntity livingEntity) { //marked
        if (!livingEntity.level().isClientSide()) {
            livingEntity.getPersistentData().putInt("dreamIntoReality", 1);
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            scaleData.setTargetScale(1);
            scaleData.markForSync(true);
            livingEntity.getPersistentData().putBoolean(CAN_FLY, false);
            if (livingEntity instanceof Player player) {
                Abilities playerAbilities = player.getAbilities();
                CompoundTag compoundTag = livingEntity.getPersistentData();
                int mindscape = compoundTag.getInt("inMindscape");
                if (!playerAbilities.instabuild || mindscape >= 1) {
                    playerAbilities.mayfly = false;
                    playerAbilities.flying = false;
                }
                playerAbilities.setFlyingSpeed(0.05F);
                player.onUpdateAbilities();
                if (livingEntity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                }
            }
        }
    }

    public static void dreamIntoReality(LivingEntity livingEntity) {
        //DREAM INTO REALITY
        boolean canFly = livingEntity.getPersistentData().getBoolean("CanFly");
        if (!canFly) {
            return;
        }
        if (BeyonderUtil.getSpirituality(livingEntity) >= 15) {
            if (livingEntity.tickCount % 2 == 0) {
                BeyonderUtil.useSpirituality(livingEntity, 10);
            }
        }
        if (BeyonderUtil.getSpirituality(livingEntity) <= 15) {
            DreamIntoReality.stopFlying(livingEntity);
        }
        if (BeyonderUtil.getSequence(livingEntity) == 2) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 4, false, false));
        } else if (BeyonderUtil.getSequence(livingEntity) == 1) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 4, false, false));
        } else if (BeyonderUtil.getSequence(livingEntity) == 0) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 4, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 5, false, false));
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, turns your dreams into reality, turning you a giant with strengthened abilities, quicker regeneration, and stronger melee hits."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("40 per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && ((float) BeyonderUtil.getMaxSpirituality(livingEntity) / BeyonderUtil.getSpirituality(livingEntity) >= 0.5f) && BeyonderUtil.getSequence(target) <= 4 && BeyonderUtil.getSequence(livingEntity) != -1) {
            return 55;
        } else if (livingEntity.getPersistentData().getBoolean("CanFly") && BeyonderUtil.getSpirituality(livingEntity) <= 1000) {
            return 100;
        } else {
            return 0;
        }
    }
}