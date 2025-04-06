package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnvisionLocation extends SimpleAbilityItem {

    public EnvisionLocation(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 0, 0, 0);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        int dreamIntoReality = (int) player.getAttribute(ModAttributes.DIR.get()).getValue();
        int blinkDistance = player.getPersistentData().getInt("BlinkDistance");
        if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 0, blinkDistance * 4, true)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player, this, 20 / dreamIntoReality);
        useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get()) * 2);
        envisionLocationBlink(player);
        return InteractionResult.SUCCESS;
    }

    public void envisionLocationBlink(LivingEntity player) {
        if (!player.level().isClientSide()) {
            int blinkDistance = player.getPersistentData().getInt("BlinkDistance");
            if (player instanceof Mob mob && mob.getTarget() != null) {
                blinkDistance = (int) mob.distanceTo(mob.getTarget());
            }
            Level level = player.level();
            if (BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SPECTATOR.get(), 0) && !player.level().isClientSide()&& BeyonderUtil.getSpirituality(player) >= blinkDistance * 8) {
                BeyonderUtil.useSpirituality(player, blinkDistance * 8);
                Vec3 lookVector = player.getLookAngle();
                double targetX = player.getX() + blinkDistance * lookVector.x();
                double targetY = (player.getY() + 1) + blinkDistance * lookVector.y();
                double targetZ = player.getZ() + blinkDistance * lookVector.z();
                player.teleportTo(targetX, targetY, targetZ);
                BlockPos playerPos = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
                BlockPos playerPos1 = new BlockPos((int) player.getX() + 1, (int) player.getY() + 1, (int) player.getZ() + 1);

                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            BlockPos targetPos = playerPos.offset(x, y, z);
                            BlockPos targetPos1 = playerPos.offset(x + 1, y + 1, z + 1);
                            BlockState blockState = level.getBlockState(targetPos);
                            if (blockState.is(Blocks.DIRT) || blockState.is(Blocks.STONE) || blockState.is(Blocks.IRON_ORE) || blockState.is(Blocks.COAL_ORE)
                                    || blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.SNOW) || blockState.is(Blocks.END_STONE) ||
                                    blockState.is(Blocks.DEEPSLATE) || blockState.is(Blocks.COPPER_ORE) || blockState.is(Blocks.SOUL_SAND) || blockState.is(Blocks.SOUL_SOIL) || blockState.is(Blocks.DEEPSLATE_COPPER_ORE) || blockState.is(Blocks.DEEPSLATE_COAL_ORE)) {
                                level.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
                                level.destroyBlock(playerPos, false);
                                level.destroyBlock(targetPos, false);
                                level.destroyBlock(targetPos1, false);
                                level.destroyBlock(playerPos1, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("While holding this item, type in coordinates or a player's name to envision your location to them. You can also right click to envision yourself in the direction you're looking"));
        tooltipComponents.add(Component.literal("Shift to increase blinking distance"));
        tooltipComponents.add(Component.literal("Left Click for Envision Location (Blink)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("500 / 4 x Blink Distance").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static boolean isThreeIntegers(String message) {
        message = message.replace(",", " ").trim();
        message = message.replaceAll("\\s+", " ");
        try {
            String[] parts = message.split(" ");
            if (parts.length != 3) return false;
            for (String part : parts) {
                Integer.parseInt(part);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && target.getHealth() <= livingEntity.getHealth()) {
            livingEntity.getPersistentData().putInt("BlinkDistance", (int) target.distanceTo(livingEntity));
           return (int) (100 - (target.getHealth()));
        } else if (livingEntity.getHealth() <= 20) {
            livingEntity.getPersistentData().putInt("BlinkDistance", 100);
            return  80;
        }
        return 10;
    }
}
