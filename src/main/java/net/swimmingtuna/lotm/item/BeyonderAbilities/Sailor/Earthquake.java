package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class Earthquake extends SimpleAbilityItem {

    public Earthquake(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 4,600,500);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        earthquake(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void earthquake(PlayerMobEntity player, int sequence) {
        //EARTHQUAKE
        int sailorEarthquake = player.getPersistentData().getInt("sailorEarthquake");
        if (sailorEarthquake >= 0) {
            player.getPersistentData().putInt("sailorEarthquake", sailorEarthquake - 1);
        }
        if (!(sailorEarthquake != 0 && sailorEarthquake % 20 == 0 || sailorEarthquake == 1)) {
            return;
        }
        int radius = 100 - (sequence * 10);
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate((radius)))) {
            if (entity != player) {
                if (entity.onGround()) {
                    entity.hurt(player.damageSources().fall(), 35 - (sequence * 5));
                }
            }
        }
        AABB checkArea = player.getBoundingBox().inflate(radius);
        Random random = new Random();
        for (BlockPos blockPos : BlockPos.betweenClosedStream(checkArea).toList()) {

            if (!player.level().getBlockState(blockPos).isAir() && Earthquake.isOnSurface(player.level(), blockPos)) {
                if (random.nextInt(200) == 1) { // 50% chance to destroy a block
                    player.level().destroyBlock(blockPos, false);
                } else if (random.nextInt(200) == 2) { // 10% chance to spawn a stone entity
                    StoneEntity stoneEntity = new StoneEntity(player.level(), player);
                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(stoneEntity);
                    stoneEntity.teleportTo(blockPos.getX(), blockPos.getY() + 3, blockPos.getZ());
                    stoneEntity.setDeltaMovement(0, 3 + Math.random() * 3, 0);
                    stoneEntity.setStoneYRot((int) (Math.random() * 18));
                    stoneEntity.setStoneXRot((int) (Math.random() * 18));
                    scaleData.setScale((float) (1 + Math.random() * 2.0f));
                    player.level().addFreshEntity(stoneEntity);
                }
            }
        }
    }

    public static void earthquake(Player player, int sequence) {
        int sailorEarthquake = player.getPersistentData().getInt("sailorEarthquake");
        if (sailorEarthquake >= 1) {
            int radius = 75 - (sequence * 6);
            if (sailorEarthquake % 20 == 0) {
                for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate((radius)))) {
                    if (entity != player) {
                        if (entity.onGround()) {
                            entity.hurt(player.damageSources().fall(), 35 - (sequence * 5));
                        }
                    }
                }
            }
            if (sailorEarthquake % 2 == 0) {
                AABB checkArea = player.getBoundingBox().inflate(radius);
                Random random = new Random();
                for (BlockPos blockPos : BlockPos.betweenClosed(
                        new BlockPos((int) checkArea.minX, (int) checkArea.minY, (int) checkArea.minZ),
                        new BlockPos((int) checkArea.maxX, (int) checkArea.maxY, (int) checkArea.maxZ))) {

                    if (!player.level().getBlockState(blockPos).isAir() && Earthquake.isOnSurface(player.level(), blockPos)) {
                        if (random.nextInt(20) == 1) {
                            BlockState blockState = player.level().getBlockState(blockPos); // Use the desired block type here
                            if (player.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                                        blockPos.getX(),
                                        blockPos.getY() + 1,
                                        blockPos.getZ(),
                                        0, 0.0, 0.0, 0, 0);
                            }
                        }
                        if (random.nextInt(4000) == 1) { // 50% chance to destroy a block
                            player.level().destroyBlock(blockPos, false);
                        } else if (random.nextInt(10000) == 2) { // 10% chance to spawn a stone entity
                            StoneEntity stoneEntity = new StoneEntity(player.level(), player);
                            ScaleData scaleData = ScaleTypes.BASE.getScaleData(stoneEntity);
                            stoneEntity.teleportTo(blockPos.getX(), blockPos.getY() + 3, blockPos.getZ());
                            stoneEntity.setDeltaMovement(0, (3 + (Math.random() * (6 - 3))), 0);
                            stoneEntity.setStoneYRot((int) (Math.random() * 18));
                            stoneEntity.setStoneXRot((int) (Math.random() * 18));
                            scaleData.setScale((float) (1 + (Math.random()) * 2.0f));
                            player.level().addFreshEntity(stoneEntity);
                        }
                    }
                }
            }
            player.getPersistentData().putInt("sailorEarthquake", sailorEarthquake - 1);
        }
    }

    public static void earthquake(Player player) {
        if (!player.level().isClientSide()) {
            player.getPersistentData().putInt("sailorEarthquake", 200);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons an earthquake shooting stone into the ground\n" +
                "Spirituality Used: 600\n" +
                "Cooldown: 25 seconds").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static boolean isOnSurface(Level level, BlockPos pos) {
        return level.canSeeSky(pos.above()) || !level.getBlockState(pos.above()).isSolid();
    }
}