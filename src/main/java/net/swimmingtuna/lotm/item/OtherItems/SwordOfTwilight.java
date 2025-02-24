package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.GuardianBoxEntity;
import net.swimmingtuna.lotm.entity.HurricaneOfLightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.*;

public class SwordOfTwilight extends SwordItem {


    public SwordOfTwilight(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                if (!BeyonderUtil.currentPathwayMatches(livingEntity, BeyonderClassInit.WARRIOR.get()) && sequence >= 1) {
                    removeItemFromSlot(livingEntity, stack);
                } else {
                    if (BeyonderUtil.getSpirituality(livingEntity) >= 150) {
                        BeyonderUtil.useSpirituality(livingEntity, 150);
                    } else {
                        removeItemFromSlot(livingEntity, stack);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.getPersistentData().putInt("age", pTarget.getPersistentData().getInt("age") + 100);
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            Vec3 lookVec = livingEntity.getLookAngle();
            Vec3 lookScale = lookVec.scale(10);
            Vec3 entityPos = livingEntity.position();
            Vec3 particlePos = entityPos.add(lookScale);
            LivingEntity closestTarget = null;
            double closestAngle = Double.MAX_VALUE;
            for (LivingEntity target : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(200))) {
                if (target != livingEntity) {
                    Vec3 toTargetVec = target.position().subtract(entityPos).normalize();
                    double dotProduct = lookVec.dot(toTargetVec);
                    double angle = Math.toDegrees(Math.acos(dotProduct));
                    if (angle < closestAngle && angle < 10.0) {
                        closestAngle = angle;
                        closestTarget = target;
                    }
                }
            }
            if (closestTarget != null && entityPos.distanceTo(closestTarget.position()) >= 1 ) {
                Random random = new Random();
                double offsetX = closestTarget.getX() + (random.nextDouble() - 0.5) * 2;
                double offsetY = closestTarget.getY() + (random.nextDouble() - 0.5) * 2;
                double offsetZ = closestTarget.getZ() + (random.nextDouble() - 0.5) * 2;
                LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleInit.VOID_BREAK_PARTICLE.get(), particlePos.x, particlePos.y, particlePos.z, 0,0,0));
                LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleInit.VOID_BREAK_PARTICLE.get(), offsetX, offsetY, offsetZ, 0,0,0));
            }
        }
        return true;
    }

    public static void decrementTwilightSword(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getPersistentData().getInt("twilightSwordCooldown") >= 1) {
            event.getEntity().getPersistentData().putInt("twilightSwordCooldown", event.getEntity().getPersistentData().getInt("twilightSwordCooldown") - 1);
        }
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
                    int sequence = BeyonderUtil.getSequence(pPlayer);
                    if (sequence >= 3) {
                        HurricaneOfLightEntity.summonHurricaneOfLightDawn(pPlayer);
                    } else {
                        HurricaneOfLightEntity.summonHurricaneOfLightDeity(pPlayer);
                    }
                    BeyonderUtil.useSpirituality(pPlayer, 1000);
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

                    GuardianBoxEntity guardianBoxEntity2 = new GuardianBoxEntity(EntityInit.GUARDIAN_BOX_ENTITY.get(), pPlayer.level());
                    ScaleData scaleData2 = ScaleTypes.BASE.getScaleData(guardianBoxEntity);
                    scaleData2.setTargetScale(18 - (sequence));
                    guardianBoxEntity.setOwnerUUID(pPlayer.getUUID());
                    guardianBoxEntity.teleportTo(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
                    guardianBoxEntity.setMaxHealth(600 - (sequence * 30));
                    pPlayer.level().addFreshEntity(guardianBoxEntity2);
                    pPlayer.getCooldowns().addCooldown(this, 400);
                    BeyonderUtil.useSpirituality(pPlayer, 1000);

                }
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
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
