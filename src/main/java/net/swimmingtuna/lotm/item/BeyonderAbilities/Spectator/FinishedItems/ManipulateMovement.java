package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ManipulateMovement extends SimpleAbilityItem {

    public ManipulateMovement(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 4, 0, 600, 75, 75);
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext context) {
        Player player = context.getPlayer();
        int dreamIntoReality = (int) player.getAttribute(ModAttributes.DIR.get()).getValue();
        if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 4, 200 / dreamIntoReality, true)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player, 200 / dreamIntoReality);
        manipulateMovement(player, context);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, all living entities 150 blocks around you that are manipulated have their thoughts manipulated to move towards the clicked location"));
        tooltipComponents.add(Component.literal("Left Click for Manipulate Fondness"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("200").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public void manipulateMovement(Player player, UseOnContext context) {
        if (!player.level().isClientSide()) {
            boolean x = player.getPersistentData().getBoolean("manipulateMovementBoolean");
            if (!x) {
                player.getPersistentData().putBoolean("manipulateMovementBoolean", true);
                BlockPos pos = context.getClickedPos();
                player.getPersistentData().putInt("manipulateMovementX", pos.getX());
                player.getPersistentData().putInt("manipulateMovementY", pos.getY());
                player.getPersistentData().putInt("manipulateMovementZ", pos.getZ());
                player.displayClientMessage(Component.literal("Manipulate Movement Position is " + pos.getX() + " " + pos.getY() + " " + pos.getZ()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);

            }
            if (x) {
                player.getPersistentData().remove("manipulateMovementX");
                player.getPersistentData().remove("manipulateMovementY");
                player.getPersistentData().remove("manipulateMovementZ");
                player.getPersistentData().putBoolean("manipulateMovementBoolean", false);
                player.displayClientMessage(Component.literal("Manipulate Movement Position Reset").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }

        }
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public static void manipulateMovement(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        //MANIPULATE MOVEMENT
        if (!livingEntity.getPersistentData().getBoolean("manipulateMovementBoolean")) {
            return;
        }
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(250))) {
            if (!entity.hasEffect(ModEffects.MANIPULATION.get())) {
                continue;
            }
            int targetX = livingEntity.getPersistentData().getInt("manipulateMovementX");
            int targetY = livingEntity.getPersistentData().getInt("manipulateMovementY");
            int targetZ = livingEntity.getPersistentData().getInt("manipulateMovementZ");

            if (entity.distanceToSqr(targetX, targetY, targetZ) <= 8) {
                entity.removeEffect(ModEffects.MANIPULATION.get());
                continue;
            }

            if (!(entity instanceof Player)) {
                if (entity instanceof Mob mob) {
                    mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.7);
                }
                continue;
            }
            double entityX = entity.getX();
            double entityY = entity.getY();
            double entityZ = entity.getZ();

            double dx = targetX - entityX;
            double dy = targetY - entityY;
            double dz = targetZ - entityZ;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 0) {
                dx /= distance;
                dy /= distance;
                dz /= distance;
            }

            double speed = 3.0 / 20;

            BlockPos frontBlockPos = new BlockPos((int) (entityX + dx), (int) (entityY + dy), (int) (entityZ + dz));
            BlockPos frontBlockPos1 = new BlockPos((int) (entityX + dx * 2), (int) (entityY + dy * 2), (int) (entityZ + dz * 2));
            boolean pathIsClear = level.getBlockState(frontBlockPos).isAir() && level.getBlockState(frontBlockPos1).isAir();
            if (pathIsClear) {
                entity.setDeltaMovement(dx * speed, Math.min(0, dy * speed), dz * speed);
                entity.hurtMarked = true;
            } else {
                entity.setDeltaMovement(dx * speed, 0.25, dz * speed);
                entity.hurtMarked = true;
            }
        }
    }

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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 75, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 75, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && target.hasEffect(ModEffects.MANIPULATION.get())) {
            return (int) (100 - (target.distanceTo(livingEntity)));
        }
        return 0;
    }
}