package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.blocks.MonsterDomainBlockEntity;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DomainOfDecay extends SimpleAbilityItem {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public DomainOfDecay(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 4, 400, 600);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        makeDomainOfProvidence(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        Player player = pContext.getPlayer();
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        removeDomainOfProvidence(pContext);
        return InteractionResult.SUCCESS;
    }

    public static void monsterDomainIntHandler(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            int sequence = holder.getSequence();
            int maxRadius = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.DECAYDOMAIN.get());
            int radius = tag.getInt("monsterDomainRadius");
            if (player.tickCount % 500 == 0) {
                tag.putInt("monsterDomainMaxRadius", maxRadius);
            }
            if (player.isShiftKeyDown() && (player.getMainHandItem().getItem() instanceof DomainOfDecay || player.getMainHandItem().getItem() instanceof DomainOfProvidence)) {
                tag.putInt("monsterDomainRadius", radius + 5);
                player.displayClientMessage(Component.literal("Current Domain Radius is " + radius).withStyle(BeyonderUtil.getStyle(player)), true);
                if (radius >= maxRadius + 1) {
                    player.displayClientMessage(Component.literal("Current Domain Radius is 0").withStyle(BeyonderUtil.getStyle(player)), true);
                    tag.putInt("monsterDomainRadius", 0);
                }
            }
        }
    }



    private void makeDomainOfProvidence(Player player) {
        if (!player.level().isClientSide()) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 reachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);
            ClipContext clipContext = new ClipContext(eyePosition, reachVector, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
            BlockHitResult blockHit = player.level().clip(clipContext);
            if (blockHit.getType() != HitResult.Type.BLOCK) {
                Level level = player.level();
                BlockPos pos = player.getOnPos();
                level.setBlock(pos, BlockInit.MONSTER_DOMAIN_BLOCK.get().defaultBlockState().setValue(LIT, false), 3);
                if (level.getBlockEntity(pos) instanceof MonsterDomainBlockEntity domainEntity) {
                    domainEntity.setOwner(player);
                    int radius = player.getPersistentData().getInt("monsterDomainRadius");
                    domainEntity.setRadius(radius);
                    domainEntity.setBad(true);
                    domainEntity.setChanged();
                }
            }
        }
    }

    private void removeDomainOfProvidence(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        if (!level.isClientSide()) {
            if (level.getBlockState(pos).is(BlockInit.MONSTER_DOMAIN_BLOCK.get())) {
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, put down a domain of decay, which will cause everything in the radius of it to encounter severe negative effects, the strength of them being stronger the smaller area. Examples include entities getting withered, ores turning to stone, crops dying, tools getting damaged, and more"));
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
}
