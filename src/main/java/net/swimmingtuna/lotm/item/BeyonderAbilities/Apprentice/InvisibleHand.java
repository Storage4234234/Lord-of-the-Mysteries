package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static net.swimmingtuna.lotm.util.BeyonderUtil.getEntityFromUUID;

public class InvisibleHand extends SimpleAbilityItem {
    public InvisibleHand(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 0, 0);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (livingEntity.getPersistentData().getUUID("invisibleHandEntity").equals(new UUID(0, 0))) {
            return InteractionResult.FAIL;
        }
        if (!checkAll(livingEntity)) {
            return InteractionResult.FAIL;
        }
        releaseEntity(livingEntity);
        return InteractionResult.SUCCESS;
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 25, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 25, AttributeModifier.Operation.ADDITION)); // adds a 12 block reach for interacting with blocks, pretty much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity livingEntity, LivingEntity interactionTarget, InteractionHand hand) {
        if (!livingEntity.level().isClientSide && !interactionTarget.level().isClientSide) {
            if (!checkAll(livingEntity)) {
                return InteractionResult.FAIL;
            }
            grabEntity(livingEntity, interactionTarget);
        }
        return InteractionResult.SUCCESS;
    }

    public static void grabEntity(LivingEntity livingEntity, LivingEntity target) {
        livingEntity.getPersistentData().putUUID("invisibleHandUUID", target.getUUID());
        livingEntity.getPersistentData().putInt("invisibleHandCounter", 100);
    }


    public static void releaseEntity(LivingEntity livingEntity) {
        livingEntity.getPersistentData().putInt("invisibleHandCounter", 0);
        livingEntity.getPersistentData().putDouble("invisibleHandDistance", 10);
    }

    public static void invisibleHandTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide() && livingEntity.getMainHandItem().getItem() instanceof InvisibleHand) {
            double distance = tag.getDouble("invisibleHandDistance");
            float maxDistance = BeyonderUtil.getDamage(livingEntity).get(ItemInit.INVISIBLEHAND.get());
            if (livingEntity.isShiftKeyDown()) {
                if (tag.getBoolean("invisibleHandIncrease")) {
                    if (distance < maxDistance) {
                        tag.putDouble("invisibleHandDistance", distance + 0.5);
                        if (livingEntity instanceof Player player) {
                            player.displayClientMessage(Component.literal("Distance: ").withStyle(BeyonderUtil.getStyle(player)).append(Component.literal(String.valueOf(distance)).withStyle(ChatFormatting.WHITE)), true);
                        }
                    }
                } else {
                    if (distance > 3) {
                        tag.putDouble("invisibleHandDistance", distance - 0.5);
                        if (livingEntity instanceof Player player) {
                            player.displayClientMessage(Component.literal("Distance: ").withStyle(BeyonderUtil.getStyle(player)).append(Component.literal(String.valueOf(distance)).withStyle(ChatFormatting.WHITE)), true);
                        }
                    } else {
                        tag.putDouble("invisibleHandDistance", 10);
                    }
                }
            }
        }
        if (!livingEntity.level().isClientSide() && tag.contains("invisibleHandUUID")) {
            int counter = tag.getInt("invisibleHandCounter");
            UUID targetUUID = tag.getUUID("invisibleHandUUID");
            double distance = tag.getDouble("invisibleHandDistance");
            float maxDistance = BeyonderUtil.getDamage(livingEntity).get(ItemInit.INVISIBLEHAND.get());
            LivingEntity target = getEntityFromUUID(livingEntity.level(), targetUUID);
            if (distance > maxDistance + 1) {
                tag.putDouble("invisibleHandDistance", 10);
            }
            if (counter == 1) {
                releaseEntity(livingEntity);
            }
            if (counter >= 1) {
                tag.putDouble("invisibleHandCounter", counter - 1);
            }
            if (target != null) {
                if (counter >= 1 && !target.level().isClientSide()) {
                    HitResult hitResult = livingEntity.pick(distance, 0.0F, false);
                    if (hitResult instanceof BlockHitResult blockHit) {
                        double x = blockHit.getLocation().x();
                        double y = blockHit.getLocation().y();
                        double z = blockHit.getLocation().z();
                        target.teleportTo(x, y, z);
                        if (BeyonderUtil.isAllyOf(livingEntity, target)) {
                            target.fallDistance = 0;
                        } else {
                            target.fallDistance = (float) (target.getY() - target.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) y));
                        }
                        if (target.fallDistance >= maxDistance) {
                            target.fallDistance = maxDistance;
                        }
                    }
                }
                if (counter >= 1 && counter <= 3) {
                    target.fallDistance = 0;
                }
            }
        }
    }


    public static void setDistanceBoolean(Player player) {
        player.getPersistentData().putBoolean("invisibleHandIncrease", !player.getPersistentData().getBoolean("invisibleHandIncrease"));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, creates an invisible hand to manipulate entities."));
        tooltipComponents.add(Component.literal("Left Click to switch between increasing/decreasing distance."));
        tooltipComponents.add(Component.literal("Shift to increase/decrease distance"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("none").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("none").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.BLUE);
    }
}