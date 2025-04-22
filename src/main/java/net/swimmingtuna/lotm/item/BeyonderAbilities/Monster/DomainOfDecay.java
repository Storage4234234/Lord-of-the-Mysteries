package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.blocks.MonsterDomainBlockEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DomainOfDecay extends SimpleAbilityItem {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public DomainOfDecay(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 4, 400, 600);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        makeDomainOfProvidence(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void monsterDomainIntHandler(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            float domainBlocks = 0;
            CompoundTag tag = livingEntity.getPersistentData();
            int radius = tag.getInt("monsterDomainRadius");
            if (livingEntity.tickCount % 10000 == 0) {
                int maxRadius = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.DECAYDOMAIN.get());
                tag.putInt("monsterDomainMaxRadius", maxRadius);
            }
            if (livingEntity.isShiftKeyDown() && (livingEntity.getMainHandItem().getItem() instanceof DomainOfDecay || livingEntity.getMainHandItem().getItem() instanceof DomainOfProvidence)) {
                tag.putInt("monsterDomainRadius", radius + 5);
                int maxRadius = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.DECAYDOMAIN.get());
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Current Domain Radius is " + radius).withStyle(BeyonderUtil.getStyle(player)), true);
                }
                if (radius >= maxRadius + 1) {
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("Current Domain Radius is 0").withStyle(BeyonderUtil.getStyle(player)), true);
                        tag.putInt("monsterDomainRadius", 0);
                    }
                }
            }
        }
    }


    private void makeDomainOfProvidence(LivingEntity player) {
        if (!player.level().isClientSide()) {
            Level level = player.level();
            BlockPos playerPos = player.getOnPos();
            AtomicBoolean foundOwnedDomain = new AtomicBoolean(false);

            BlockPos.betweenClosedStream(playerPos.offset(-5, -5, -5), playerPos.offset(5, 5, 5)).forEach(pos -> {
                if (level.getBlockEntity(pos) instanceof MonsterDomainBlockEntity domainEntity) {
                    if (domainEntity.getOwner() != null && domainEntity.getOwner() == player) {
                        level.removeBlock(pos, false);
                        foundOwnedDomain.set(true);
                        player.sendSystemMessage(Component.literal("Removed your domain at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()) );
                    }
                }
            });

            if (!foundOwnedDomain.get()) {
                Vec3 eyePosition = player.getEyePosition();
                Vec3 lookVector = player.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);
                ClipContext clipContext = new ClipContext(eyePosition, reachVector, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                BlockHitResult blockHit = level.clip(clipContext);
                if (blockHit.getType() != HitResult.Type.BLOCK) {
                    level.setBlock(playerPos, BlockInit.MONSTER_DOMAIN_BLOCK.get().defaultBlockState().setValue(LIT, false), 3);
                    if (level.getBlockEntity(playerPos) instanceof MonsterDomainBlockEntity domainEntity) {
                        domainEntity.setOwner(player);
                        int radius = player.getPersistentData().getInt("monsterDomainRadius");
                        domainEntity.setRadius(radius);
                        domainEntity.setBad(true);
                        domainEntity.setChanged();
                    }
                }
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, create a domain of decay in which crops will wither, ores turn to stone, and everything except you and your allies will be withered, have their armor and tools damaged, lose experience, receive hunger, wither, poison, lose all positive effects, increase misfortune, and lose luck. The strength of these effects increases with a smaller size. Use again within the domain to remove it."));
        tooltipComponents.add(Component.literal("Left Click to increase radius"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("400").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }
}
