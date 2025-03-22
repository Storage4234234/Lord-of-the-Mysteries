package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.swimmingtuna.lotm.networking.LOTMNetworkHandler.sendToPlayer;

public class LightConcealment extends SimpleAbilityItem {


    public LightConcealment(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 3, 200, 500);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        lightConcealment(player);
        return InteractionResult.SUCCESS;
    }

    public static void lightConcealment(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            livingEntity.getPersistentData().putInt("warriorLightConcealment", 400 - (sequence * 60));
        }
    }

    public static void lightConcealmentTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int lightConcealment = tag.getInt("warriorLightConcealment");
        if (!livingEntity.level().isClientSide() && lightConcealment >= 1) {
            tag.putInt("warriorLightConcealment", lightConcealment - 1);
            for (Mob mob : livingEntity.level().getEntitiesOfClass(Mob.class, livingEntity.getBoundingBox().inflate(100))) {
                if (mob.getTarget() == livingEntity && mob.distanceTo(livingEntity) <= 10) {
                    mob.setTarget(null);
                }
            }
            for (Player player : livingEntity.level().players()) {
                int x = (int) ((int) livingEntity.getX() + ((Math.random() * 30) - 15));
                int y = (int) ((int) livingEntity.getY() + ((Math.random() * 30) - 15));
                int z = (int) ((int) livingEntity.getZ() + ((Math.random() * 30) - 15));
                for (int i = 0; i < 50; i++) {
                    double velocityX = (Math.random() * 2.0) - 1.0;
                    double velocityY = (Math.random() * 2.0) - 1.0;
                    double velocityZ = (Math.random() * 2.0) - 1.0;
                    double speed = 2.0;
                    velocityX *= speed;
                    velocityY *= speed;
                    velocityZ *= speed;
                    if (player == livingEntity) {
                        SendParticleS2C packet = new SendParticleS2C(
                                ParticleTypes.END_ROD,
                                x, y, z,
                                velocityX, velocityY, velocityZ
                        );
                        sendToPlayer(packet, (ServerPlayer) player);
                    } else {
                        SendParticleS2C packet = new SendParticleS2C(
                                ParticleInit.FLASH_PARTICLE.get(),
                                x, y, z,
                                velocityX, velocityY, velocityZ
                        );
                        sendToPlayer(packet, (ServerPlayer) player);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, shoot out light around you, shooting out huge chunks of light blinding all entities around you."));
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

