package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.events.ability_events.ModEvents;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.TickEventUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvisionBarrier extends SimpleAbilityItem {

    private final Map<BlockPos, BlockState> replacedBlocks = new HashMap<>();
    private final List<BlockPos> replacedAirBlocks = new ArrayList<>();
    private BlockPos domeCenter = null;

    public EnvisionBarrier(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 0, 0, 100);
    }


    public static void envisionBarrier(PlayerMobEntity player) {
        //ENVISION BARRIER
        if (player.getCurrentSequence() != 0) {
            return;
        }
        int barrierRadius = player.getPersistentData().getInt("BarrierRadius");
        for (Projectile projectile : player.level().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(100))) {
            if (projectile.distanceTo(player) <= 50) {
                List<Vec3> trajectory = ModEvents.predictProjectileTrajectory(projectile, player);
                BlockPos playerPos = player.blockPosition();
                List<BlockPos> blocksInRadius = TickEventUtil.getBlockPos(playerPos);
                if (trajectory.contains(blocksInRadius)) {
                    player.getPersistentData().putInt("BarrierRadius", (int) (projectile.distanceTo(player) - 5));
                }
            }
        }
        if (barrierRadius > 100) {
            barrierRadius = 0;
        }
        player.getPersistentData().putInt("BarrierRadius", barrierRadius);
    }

    public static void envisionBarrier(BeyonderHolder holder, Player player, Style style) {
        //ENVISION BARRIER
        if (holder.getCurrentSequence() != 0) {
            return;
        }
        int barrierRadius = player.getPersistentData().getInt("BarrierRadius");
        if (player.isShiftKeyDown() && player.getMainHandItem().getItem() instanceof EnvisionBarrier) {
            barrierRadius++;
            player.displayClientMessage(Component.literal("Barrier Radius: " + barrierRadius).withStyle(style), true);
        }
        if (barrierRadius > 101) {
            barrierRadius = 0;
            player.displayClientMessage(Component.literal("Barrier Radius: 0").withStyle(style), true);
        }
        player.getPersistentData().putInt("BarrierRadius", barrierRadius);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        int dreamIntoReality = (int) player.getAttribute(ModAttributes.DIR.get()).getValue();
        if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 0, 800 / dreamIntoReality)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player, 800 / dreamIntoReality);
        generateBarrier(player, level, player.getOnPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, makes a barrier around the user\n" +
                "Hold Shift to Increase Barrier Radius\n" +
                "Left Click for Envision Death\n" +
                "Spirituality Used: 800\n" +
                "Cooldown: None ").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private void generateBarrier(Player player, Level level, BlockPos playerPos) {
        if (!player.level().isClientSide()) {
            int radius = player.getPersistentData().getInt("BarrierRadius");
            int thickness = 1; // Adjust the thickness of the glass dome

            if (domeCenter != null) {
                // Remove the existing glass dome
                for (Map.Entry<BlockPos, BlockState> entry : replacedBlocks.entrySet()) {
                    BlockPos worldPos = domeCenter.offset(entry.getKey());
                    level.setBlockAndUpdate(worldPos, entry.getValue());
                }
                for (BlockPos airPos : replacedAirBlocks) {
                    BlockPos worldPos = domeCenter.offset(airPos);
                    level.setBlockAndUpdate(worldPos, Blocks.AIR.defaultBlockState());
                }
                replacedBlocks.clear();
                replacedAirBlocks.clear();
                domeCenter = null;
                return;
            }

            domeCenter = playerPos;
            replacedAirBlocks.clear();
            BlockState barrierState = Blocks.OBSIDIAN.defaultBlockState();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        double distanceFromCenter = pos.distSqr(BlockPos.ZERO);
                        if (distanceFromCenter <= (radius - thickness) * (radius - thickness)) {
                            // Skip placing glass blocks inside the dome
                            continue;
                        } else if (distanceFromCenter <= radius * radius) {
                            BlockPos worldPos = domeCenter.offset(pos);

                            BlockState currentState = level.getBlockState(worldPos);
                            if (currentState.isAir()) {
                                level.setBlockAndUpdate(worldPos, BlockInit.VISIONARY_BARRIER_BLOCK.get().defaultBlockState());
                                replacedAirBlocks.add(pos);
                            } else {
                                replacedBlocks.put(pos, currentState);
                                level.setBlockAndUpdate(worldPos, BlockInit.VISIONARY_BARRIER_BLOCK.get().defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
    }
}