package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.swimmingtuna.lotm.entity.MeteorEntity;
import net.swimmingtuna.lotm.entity.MeteorNoLevelEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WaterSphere extends SimpleAbilityItem {

    public WaterSphere(Properties properties) { //IMPORTANT!!!! FIGURE OUT HOW TO MAKE THIS WORK BY CLICKING ON A
        super(properties, BeyonderClassInit.SAILOR, 5, 300, 240);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        waterSphere(player);
        return InteractionResult.SUCCESS;
    }

    private static void waterSphere(Player player) {
        if (!player.level().isClientSide()) {
            player.getPersistentData().putInt("sailorSphere", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.WATER_SPHERE.get()));
        }
    }
    public static void waterSphereCheck(Player player, ServerLevel level) {
        //WATER SPHERE CHECK
        if (player.getPersistentData().getInt("sailorSphere") >= 5) {
            for (Entity entity : player.level().getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(4))) {
                if (!(entity instanceof LivingEntity) && !(entity instanceof MeteorEntity) && !(entity instanceof MeteorNoLevelEntity)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            BlockPos playerPos = player.blockPosition();
            double radius = 3.0;
            double minRemovalRadius = 4.0;
            double maxRemovalRadius = 7.0;

            // Create a sphere of water around the player
            for (int sphereX = (int) -radius; sphereX <= radius; sphereX++) {
                for (int sphereY = (int) -radius; sphereY <= radius; sphereY++) {
                    for (int sphereZ = (int) -radius; sphereZ <= radius; sphereZ++) {
                        double sphereDistance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                        if (!(sphereDistance <= radius)) {
                            continue;
                        }
                        BlockPos blockPos = playerPos.offset(sphereX, sphereY, sphereZ);
                        if (level.getBlockState(blockPos).isAir() && !level.getBlockState(blockPos).is(Blocks.WATER)) {
                            level.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
                        }
                    }
                }
            }
            for (int sphereX = (int) -maxRemovalRadius; sphereX <= maxRemovalRadius; sphereX++) {
                for (int sphereY = (int) -maxRemovalRadius; sphereY <= maxRemovalRadius; sphereY++) {
                    for (int sphereZ = (int) -maxRemovalRadius; sphereZ <= maxRemovalRadius; sphereZ++) {
                        double sphereDistance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                        if (!(sphereDistance <= maxRemovalRadius) || !(sphereDistance >= minRemovalRadius)) {
                            continue;
                        }
                        BlockPos blockPos = playerPos.offset(sphereX, sphereY, sphereZ);
                        if (level.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        if (player.getPersistentData().getInt("sailorSphere") >= 1 && player.getPersistentData().getInt("sailorSphere") <= 4) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 100, false, false));
            for (int sphereX = -6; sphereX <= 6; sphereX++) {
                for (int sphereY = -6; sphereY <= 6; sphereY++) {
                    for (int sphereZ = -6; sphereZ <= 6; sphereZ++) {
                        double sphereDistance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                        if (!(sphereDistance <= 6)) {
                            continue;
                        }
                        BlockPos blockPos = player.getOnPos().offset(sphereX, sphereY, sphereZ);
                        if (player.level().getBlockState(blockPos).getBlock() == Blocks.WATER) {
                            player.level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        if (player.getPersistentData().getInt("sailorSphere") >= 1) {
            player.getPersistentData().putInt("sailorSphere", player.getPersistentData().getInt("sailorSphere") - 1);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, surrounds you in a sphere of water for 10 seconds"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("12 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

}
