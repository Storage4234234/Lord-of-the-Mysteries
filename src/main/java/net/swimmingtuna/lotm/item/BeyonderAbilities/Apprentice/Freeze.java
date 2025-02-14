package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake.isOnSurface;

public class Freeze extends SimpleAbilityItem {
    public Freeze(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 70, 300);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand){
        if(!player.level().isClientSide && !interactionTarget.level().isClientSide){
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player);
            useSpirituality(player);
            freezeEntity(player, interactionTarget);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        freezeAura(player);
        return InteractionResult.SUCCESS;
    }

    public static void freezeEntity(LivingEntity livingEntity, LivingEntity target){
        target.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFREEZE.get()), 2, false, false));
    }

    public static void freezeAura(LivingEntity livingEntity) {
        int damage =(int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFREEZE.get());
        for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(damage))) {
            if (living != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, living)) {
                living.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFREEZE.get()) / 3, 2, false, false));
            }
        }
        Level level = livingEntity.level();
        BlockPos centerPos = livingEntity.blockPosition();
        for (int x = -damage; x <= damage; x++) {
            for (int z = -damage; z <= damage; z++) {
                for (int y = -damage; y <= damage; y++) {
                    BlockPos pos = centerPos.offset(x, y, z);
                    if (isOnSurface(level, pos)) {
                        freezeBlock(level, pos);
                    }
                }
            }
        }
    }

    public static void freezeBlock(Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        if (currentState.getBlock() == Blocks.WATER) {
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
        } else if (currentState.getBlock() == Blocks.LAVA) {
            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
        } else if (currentState.isAir()) {
            level.setBlock(pos, Blocks.PACKED_ICE.defaultBlockState(), 3);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, freezes the target for a small amount of time, or if none is selected, freeze all entities/blocks around you."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("70").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with blocks, pretty much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.BLUE);
    }
}