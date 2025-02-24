package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.GuardianBoxEntity;
import net.swimmingtuna.lotm.entity.HurricaneOfLightEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SwordOfGlory extends SwordItem {


    public SwordOfGlory(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
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

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        boolean canBePurified = pTarget.getName().getString().toLowerCase().contains("demon") || pTarget.getName().getString().toLowerCase().contains("ghost") || pTarget.getName().getString().toLowerCase().contains("wraith") || pTarget.getName().getString().toLowerCase().contains("zombie") || pTarget.getName().getString().toLowerCase().contains("undead") || pTarget.getPersistentData().getBoolean("isWraith");
        if (canBePurified) {
            pTarget.hurt(BeyonderUtil.magicSource(pAttacker), this.getDamage());
            pTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, false, false));
            pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pPlayer.level().isClientSide()) {
            if (!pPlayer.isShiftKeyDown()) {
                Vec3 eyePosition = pPlayer.getEyePosition();
                Vec3 lookVector = pPlayer.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * 5, lookVector.y * 5, lookVector.z * 5);
                BlockHitResult blockHit = pPlayer.level().clip(new ClipContext(eyePosition, reachVector, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, pPlayer));
                if (blockHit.getType() == HitResult.Type.MISS) {
                    HurricaneOfLightEntity.summonHurricaneOfLightGlory(pPlayer);
                    BeyonderUtil.useSpirituality(pPlayer, 200);
                    pPlayer.getCooldowns().addCooldown(this, 400);
                }
            } else if (pPlayer.isShiftKeyDown() && BeyonderUtil.getSequence(pPlayer) <= 5) {
                int boxCount = 0;
                for (GuardianBoxEntity guardianBox : pPlayer.level().getEntitiesOfClass(GuardianBoxEntity.class, pPlayer.getBoundingBox().inflate(200))) {
                    Optional<UUID> ownerUUID = Optional.of(pPlayer.getUUID());
                    if (Objects.equals(guardianBox.getOwnerUUID(), ownerUUID)) {
                        boxCount++;
                    }
                }
                if (boxCount == 0) {
                    GuardianBoxEntity guardianBoxEntity = new GuardianBoxEntity(EntityInit.GUARDIAN_BOX_ENTITY.get(), pPlayer.level());
                    int sequence = BeyonderUtil.getSequence(pPlayer);
                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(guardianBoxEntity);
                    scaleData.setTargetScale(18 - (sequence));
                    guardianBoxEntity.setOwnerUUID(pPlayer.getUUID());
                    guardianBoxEntity.teleportTo(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
                    guardianBoxEntity.setMaxHealth(500 - (sequence * 30));
                    pPlayer.level().addFreshEntity(guardianBoxEntity);
                    pPlayer.getCooldowns().addCooldown(this, 400);
                    BeyonderUtil.useSpirituality(pPlayer, 200);
                }
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (!(pLivingEntity instanceof Player player)) {
            return;
        }
        int i = this.getUseDuration(pStack) - pTimeCharged;
        float powerScale = getPowerForTime(i);
        if (!pLevel.isClientSide) {
            Arrow arrow = new Arrow(pLevel, player);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerScale * 3.0F, 1.0F);

            arrow.setCritArrow(powerScale > 0.5F);
            arrow.setBaseDamage(arrow.getBaseDamage() * 2.0);

            // Spawn the arrow in the world
            pLevel.addFreshEntity(arrow);
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000; // Same as bow
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPEAR; // Or BOW if you prefer that animation
    }

    private float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("WORK IN PROGRESS").withStyle(ChatFormatting.RED));
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("DAWN_ITEM", ChatFormatting.YELLOW);
    }


}
