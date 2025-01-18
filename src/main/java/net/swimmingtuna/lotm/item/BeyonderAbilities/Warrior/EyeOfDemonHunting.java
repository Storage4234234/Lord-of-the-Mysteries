package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendDustParticleS2C;
import virtuoel.pehkui.api.ScaleTypes;

public class EyeOfDemonHunting extends SimpleAbilityItem {


    public EyeOfDemonHunting(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        enableOrDisableEyeOfDemonHunting(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableOrDisableEyeOfDemonHunting(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            boolean x = tag.getBoolean("eyeOfDemonHunting");
            tag.putBoolean("eyeOfDemonHunting", !x);
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Eye of Demon Hunting turned " + (x ? "off" : "on")).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
            }
        }
    }


    public static void eyeTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (entity instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.level().isClientSide() && tag.getBoolean("eyeOfDemonHunting") && entity.tickCount % 20 == 0) {
                for (LivingEntity living : serverPlayer.level().getEntitiesOfClass(LivingEntity.class, serverPlayer.getBoundingBox().inflate(80))) {
                    if (living == serverPlayer) {
                        continue;
                    }
                    float healthPercentage = (living.getHealth() / living.getMaxHealth()) * 100;
                    float entityScale = ScaleTypes.BASE.getScaleData(living).getScale();
                    float entityWidth = living.getBbWidth();
                    double radius = Math.max(0.5, (entityScale * entityWidth) / 1.5);
                    float r, g, b;
                    if (healthPercentage > 66) {
                        r = 0.0F;
                        g = 1.0F;
                        b = 0.0F;
                    } else if (healthPercentage > 33) {
                        r = 1.0F;
                        g = 0.55F;
                        b = 0.0F;
                    } else {
                        r = 1.0F;
                        g = 0.0F;
                        b = 0.0F;
                    }

                    double x = living.getX();
                    double y = living.getY() + (living.getBbHeight() * entityScale) / 2;
                    double z = living.getZ();
                    int particleCount = Math.max(8, (int)(8 * entityScale));

                    for (int i = 0; i < particleCount; i++) {
                        double angle = (i / (double)particleCount) * 2 * Math.PI;
                        double offsetX = Math.cos(angle) * radius;
                        double offsetZ = Math.sin(angle) * radius;
                        LOTMNetworkHandler.sendToPlayer(new SendDustParticleS2C(r, g, b, 1.0F, x + offsetX, y, z + offsetZ, 0.0, 0.0, 0.0), serverPlayer);
                    }
                }
            }
        }
    }
}

