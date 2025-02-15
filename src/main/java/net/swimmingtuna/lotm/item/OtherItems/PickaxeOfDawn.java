package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PickaxeOfDawn extends PickaxeItem {

    public PickaxeOfDawn(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            HitResult hitResult = player.pick(20.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                Direction face = ((BlockHitResult)hitResult).getDirection();
                Direction[] directions = getPerpendicularDirections(face);
                Direction perpendicular1 = directions[0];
                Direction perpendicular2 = directions[1];
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        BlockPos newPos = pos.relative(perpendicular1, i).relative(perpendicular2, j);
                        BlockState blockState = level.getBlockState(newPos);
                        if (!blockState.isAir() && blockState.getDestroySpeed(level, newPos) >= 0) {
                            if (blockState.getDestroySpeed(level, newPos) > 0) {
                                stack.hurtAndBreak(1, entity, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                            }
                            level.destroyBlock(newPos, true, entity);
                        }
                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    private Direction[] getPerpendicularDirections(Direction face) {
        return switch (face) {
            case UP, DOWN -> new Direction[]{Direction.NORTH, Direction.EAST};
            case NORTH, SOUTH -> new Direction[]{Direction.UP, Direction.EAST};
            case EAST, WEST -> new Direction[]{Direction.UP, Direction.NORTH};
        };
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                if (livingEntity instanceof Player player) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                    if (!holder.currentClassMatches(BeyonderClassInit.WARRIOR) || sequence >= 7) {
                        player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
                    } else {
                        if (holder.getSpirituality() >= 25) {
                            holder.useSpirituality(25);
                        } else {
                            player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
                        }
                    }
                } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                    if (playerMobEntity.getCurrentPathway() != BeyonderClassInit.WARRIOR || playerMobEntity.getCurrentSequence() >= 7) {
                        removeItemFromSlot(livingEntity, stack);
                    } else {
                        if (playerMobEntity.getSpirituality() >= 25) {
                            playerMobEntity.useSpirituality(25);
                        } else {
                            removeItemFromSlot(livingEntity, stack);
                        }
                    }
                } else {
                    removeItemFromSlot(livingEntity, stack);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        return super.getDestroySpeed(pStack,pState) * 1.2f;
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("When you break a block, destroy all blocks in the direction you're looking as well."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("25 per second.").withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("DAWN_ITEM", ChatFormatting.YELLOW);
    }
}