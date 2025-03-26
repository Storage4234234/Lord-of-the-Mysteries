package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.TwilightLightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TwilightLight extends SimpleAbilityItem {


    public TwilightLight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 0, 2000);
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
            float damage = BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTLIGHT.get());
            int numberOfRays = (int) damage / 5;
            for (int i = 0; i < numberOfRays; i++) {
                double radius = 2.0;
                double angle = i * (2 * Math.PI / numberOfRays);
                double startX = livingEntity.getX() + radius * Math.cos(angle);
                double startZ = livingEntity.getZ() + radius * Math.sin(angle);
                TwilightLightEntity ray = new TwilightLightEntity(livingEntity.level(), startX, livingEntity.getY() + 140, startZ, damage);
                ray.setMaxLifetime((int) damage);
                livingEntity.getPersistentData().putInt("twilightLight", (int) damage);
                ray.setDivisionAmount((int) (Math.random() * 5));
                livingEntity.level().addFreshEntity(ray);
            }
        }
    }

    public static void twilightLightTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("twilightLight");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            int damage = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTLIGHT.get());
            int y = tag.getInt("twilightLightArea");
            tag.putInt("twilightLight", x - 1);
            if (x >= 2 && y < damage) {
                tag.putInt("twilightLightArea", y - 1);
            }
            if (x == 1) {
                tag.putInt("twilightLightArea", 0);
            }
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(y))) {
                if (living != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, living)) {
                    living.getPersistentData().putInt("age", living.getPersistentData().getInt("age") + 4);
                    if (living instanceof Player player) {
                        if (player.tickCount % 10 == 0) {
                            player.displayClientMessage(Component.literal("You are being rapidly aged").withStyle(BeyonderUtil.ageStyle(living)).withStyle(ChatFormatting.BOLD), true);
                        }
                    }
                }
            }
            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                Random random = new Random();
                DustParticleOptions yellowDust = new DustParticleOptions(new Vector3f(1.0F, 1.0F, 0.0F), 1.0F);
                DustParticleOptions orangeYellowDust = new DustParticleOptions(new Vector3f(1.0F, 0.8F, 0.0F), 1.0F);
                DustParticleOptions darkOrangeDust = new DustParticleOptions(new Vector3f(1.0F, 0.5F, 0.0F), 1.0F);
                for (int i = 0; i < 20 * y; i++) {
                    double randX = livingEntity.getX() + (random.nextDouble() * 2 - 1) * y;
                    double randY = livingEntity.getY() + random.nextDouble() * 2;
                    double randZ = livingEntity.getZ() + (random.nextDouble() * 2 - 1) * y;
                    double randomInt = Math.random();
                    if (randomInt < 0.33) {
                        serverLevel.sendParticles(yellowDust, randX, randY, randZ, 1, 0, 0, 0, 0);
                    } else if (randomInt < 0.66 && randomInt > 0.34) {
                        serverLevel.sendParticles(orangeYellowDust, randX, randY, randZ, 1, 0, 0, 0, 0);
                    } else {
                        serverLevel.sendParticles(darkOrangeDust, randX, randY, randZ, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, cause rays of twilight to fall from the sky, causing all entities around you to be rapidly aged."));
        tooltipComponents.add(Component.literal("Left Click for Twilight: Globe"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("100 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }


}

