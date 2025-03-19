package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtremeColdness extends SimpleAbilityItem {

    public ExtremeColdness(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 2, 1250,1200);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        extremeColdness(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public void extremeColdness(Player player) {
        if (!player.level().isClientSide()) {
            player.getPersistentData().putInt("sailorExtremeColdness", 1);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, let out freezing air which will freeze all surface blocks and entities around you, as wells as dealing damage"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1250").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }


    public static boolean canFreezeBlock(LivingEntity player, BlockPos targetPos) {
        Block block = player.level().getBlockState(targetPos).getBlock();
        return block != Blocks.BEDROCK && block != Blocks.AIR &&
                block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR &&
                block != Blocks.ICE;
    }

    public static void extremeColdness(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        //EXTREME COLDNESS
        int extremeColdness = tag.getInt("sailorExtremeColdness");
        if (extremeColdness >= BeyonderUtil.getDamage(livingEntity).get(ItemInit.EXTREME_COLDNESS.get())) {
            tag.putInt("sailorExtremeColdness", 0);
            extremeColdness = 0;
        }
        if (extremeColdness < 1) {
            return;
        }
        tag.putInt("sailorExtremeColdness", extremeColdness + 1);

        AABB areaOfEffect = livingEntity.getBoundingBox().inflate(extremeColdness);
        List<LivingEntity> entities = livingEntity.level().getEntitiesOfClass(LivingEntity.class, areaOfEffect);
        for (LivingEntity entity : entities) {
            if (entity != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, entity)) {
                int affectedBySailorExtremeColdness = entity.getPersistentData().getInt("affectedBySailorExtremeColdness");
                entity.getPersistentData().putInt("affectedBySailorExtremeColdness", affectedBySailorExtremeColdness + 1);
                entity.setTicksFrozen(1);
            }
        }
        List<Entity> entities1 = livingEntity.level().getEntitiesOfClass(Entity.class, areaOfEffect); //test thsi
        for (Entity entity : entities1) {
            if (!(entity instanceof LivingEntity)) {
                int affectedBySailorColdness = entity.getPersistentData().getInt("affectedBySailorColdness");
                entity.getPersistentData().putInt("affectedBySailorColdness", affectedBySailorColdness + 1);
                if (affectedBySailorColdness == 10) {
                    entity.setDeltaMovement(entity.getDeltaMovement().x() / 5, entity.getDeltaMovement().y() / 5, entity.getDeltaMovement().z() / 5);
                    entity.hurtMarked = true;
                    entity.getPersistentData().putInt("affectedBySailorColdness", 0);
                }
            }
        }
        BlockPos playerPos = livingEntity.blockPosition();
        int radius = extremeColdness;
        int blocksToProcessPerTick = 2000;
        int processedBlocks = 0;
        Map<BlockPos, Integer> heightMapCache = new HashMap<>();
        for (int dx = -radius; dx <= radius && processedBlocks < blocksToProcessPerTick; dx++) {
            for (int dz = -radius; dz <= radius && processedBlocks < blocksToProcessPerTick; dz++) {
                BlockPos surfacePos = playerPos.offset(dx, 0, dz);
                Integer surfaceY = heightMapCache.get(surfacePos);
                if (surfaceY == null) {
                    surfaceY = livingEntity.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, surfacePos).getY();
                    heightMapCache.put(surfacePos, surfaceY);
                }

                for (int dy = 0; dy < 3; dy++) {
                    BlockPos targetPos = new BlockPos(surfacePos.getX(), surfaceY - dy, surfacePos.getZ());
                    if (ExtremeColdness.canFreezeBlock(livingEntity, targetPos)) {
                        livingEntity.level().setBlockAndUpdate(targetPos, Blocks.ICE.defaultBlockState());
                        processedBlocks++;
                    }
                }
            }
        }
    }

    public static void extremeColdnessTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (!entity.level().isClientSide()) {
            int affectedBySailorExtremeColdness = tag.getInt("affectedBySailorExtremeColdness");
            if (!entity.level().isClientSide()) {
                if (entity instanceof Player player) {
                    player.setTicksFrozen(3);
                }
                if (affectedBySailorExtremeColdness == 5) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
                }
                if (affectedBySailorExtremeColdness == 10) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
                }
                if (affectedBySailorExtremeColdness == 15) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, false));
                }
                if (affectedBySailorExtremeColdness >= 20) {
                    entity.addEffect(new MobEffectInstance(ModEffects.AWE.get(), 100, 1, false, false));
                    tag.putInt("affectedBySailorExtremeColdness", 0);
                    affectedBySailorExtremeColdness = 0;
                    entity.hurt(entity.damageSources().freeze(), 30);
                }
            }
        }
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }
}