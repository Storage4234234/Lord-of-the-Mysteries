package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.DawnRayEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LightOfDawn extends SimpleAbilityItem {


    public LightOfDawn(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 500);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        sunriseGleam(player);
        return InteractionResult.SUCCESS;
    }

    public static void sunriseGleam(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            int maxLifetime = 500 - (sequence * 50);
            float maxRadius = BeyonderUtil.getDamage(livingEntity).get(ItemInit.LIGHTOFDAWN.get());
            livingEntity.getPersistentData().putInt("lightOfDawnCounter", maxLifetime);
            int numberOfRays = 30 - (sequence * 4);
            for (int i = 0; i < numberOfRays; i++) {
                double radius = 2.0;
                double angle = i * (2 * Math.PI / numberOfRays);
                double startX = livingEntity.getX() + radius * Math.cos(angle);
                double startZ = livingEntity.getZ() + radius * Math.sin(angle);
                DawnRayEntity ray = new DawnRayEntity(livingEntity.level(), startX, livingEntity.getY() + 140, startZ, maxRadius);
                ray.setMaxLifetime(maxLifetime);
                ray.setDivisionAmount((int) (Math.random() * 5));
                livingEntity.level().addFreshEntity(ray);
            }
        }
    }
    private static final Random random = new Random();

    public static void sunriseGleamTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("lightOfDawnCounter");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            tag.putInt("lightOfDawnCounter", x -1);
            int maxRadius = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.LIGHTOFDAWN.get());
            int sequence = BeyonderUtil.getSequence(livingEntity);
            if (livingEntity.tickCount % 20 == 0) {
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < maxRadius * 5; i++) { // Spawn 50 particles per tick
                        double x1 = livingEntity.getX() + (random.nextDouble() * 2 - 1) * maxRadius;
                        double y1 = livingEntity.getY() + (random.nextDouble() * 2 - 1) * maxRadius;
                        double z1 = livingEntity.getZ() + (random.nextDouble() * 2 - 1) * maxRadius;
                        serverLevel.sendParticles(ParticleTypes.END_ROD, x1, y1, z1, 1, 0, -0.05, 0, 0.02);
                    }
                }
                for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(maxRadius))) {
                    if (BeyonderUtil.isPurifiable(entity)) {
                        entity.hurt(BeyonderUtil.magicSource(entity), 15.0f - sequence);
                        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, true, true));
                        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, true, true));
                    } else if (BeyonderUtil.areAllies(livingEntity, entity) || entity == livingEntity) {
                        entity.heal(3.0f);
                        List<MobEffectInstance> effectsToUpdate = new ArrayList<>();
                        for (MobEffectInstance effect : entity.getActiveEffects()) {
                            MobEffect type = effect.getEffect();
                            if (!type.isBeneficial()) {
                                int newDuration = effect.getDuration() / 2;
                                MobEffectInstance newEffect = new MobEffectInstance(type, newDuration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
                                effectsToUpdate.add(newEffect);
                            }
                        }
                        for (MobEffectInstance effect : effectsToUpdate) {
                            entity.removeEffect(effect.getEffect());
                            entity.addEffect(effect);
                        }
                    }
                    if (entity.getPersistentData().getDouble("corruption") >= 1) {
                        entity.getPersistentData().putDouble("corruption", entity.getPersistentData().getDouble("corruption") - 1);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, bathe the area around you in light. Causing all allies to be healed, have their corruption lowered, beneficial effects lengthened, and harmful effects removed. Any evil entities around you will be damaged heavily and weakned."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("200").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("25 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

}

