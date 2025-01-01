package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class StarOfLightning extends SimpleAbilityItem {

    public StarOfLightning(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 1, 3000, 800);
    }


    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        starOfLightning(player);
        return InteractionResult.SUCCESS;
    }

    private static void starOfLightning(Player player) {
        if (!player.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if (holder.getCurrentSequence() == 0) {
                player.getPersistentData().putInt("sailorLightningStar", 50);
            } else {
                player.getPersistentData().putInt("sailorLightningStar", 80);
            }
        }
    }


    public static void starOfLightning(Player player, CompoundTag playerPersistentData) {
        //STAR OF LIGHTNING
        int sailorLightningStar = playerPersistentData.getInt("sailorLightningStar");
        if (sailorLightningStar >= 2) {
            StarOfLightning.summonLightningParticles(player);
            player.level().playSound(player, player.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 10, 1);
            playerPersistentData.putInt("sailorLightningStar", sailorLightningStar - 1);
        }
        if (sailorLightningStar == 1) {
            playerPersistentData.putInt("sailorLightningStar", 0);
            for (int i = 0; i < 500; i++) {
                LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), player.level());
                lightningEntity.setSpeed(50);
                double sailorStarX = (Math.random() * 2 - 1);
                double sailorStarY = (Math.random() * 2 - 1);
                double sailorStarZ = (Math.random() * 2 - 1);
                lightningEntity.setDeltaMovement(sailorStarX, sailorStarY, sailorStarZ);
                lightningEntity.setMaxLength(10);
                lightningEntity.setOwner(player);
                lightningEntity.teleportTo(player.getX(), player.getY(), player.getZ());
                player.level().addFreshEntity(lightningEntity);
            }
        }
    }

    public static void summonLightningParticles(LivingEntity player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 500; i++) {
                double offsetX = (Math.random() * 5) - 2.5;
                double offsetY = (Math.random() * 5) - 2.5;
                double offsetZ = (Math.random() * 5) - 2.5;
                if (Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ) <= 2.5) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            player.getX() + offsetX,
                            player.getY() + offsetY,
                            player.getZ() + offsetZ,0,
                            0.0, 0.0, 0.0,0);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, gathers lightning in your body before letting it out in every direction\n" +
                "Spirituality Used: 3000\n" +
                "Cooldown: 40 seconds").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
