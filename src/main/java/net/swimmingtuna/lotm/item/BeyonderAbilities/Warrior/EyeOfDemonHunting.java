package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendDustParticleS2C;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;

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
            boolean x = tag.getBoolean("demonHuntingEye");
            tag.putBoolean("demonHuntingEye", !x);
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Eye of Demon Hunting turned " + (x ? "off" : "on")).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
            }
        }
    }


    public static void eyeTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (entity instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.level().isClientSide() && tag.getBoolean("demonHuntingEye") && entity.tickCount % 20 == 0) {
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
                    int particleCount = Math.max(50, (int)(50 * entityScale));
                    for (int i = 0; i < particleCount; i++) {
                        double phi = Math.random() * 2 * Math.PI;
                        double theta = Math.acos(1 - 2 * Math.random());
                        double offsetX = radius * Math.sin(theta) * Math.cos(phi);
                        double offsetY = radius * Math.cos(theta);
                        double offsetZ = radius * Math.sin(theta) * Math.sin(phi);
                        LOTMNetworkHandler.sendToPlayer(new SendDustParticleS2C(r, g, b, 1.0F, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.0, 0.0), serverPlayer);
                    }
                }
            }
        }
    }

}

