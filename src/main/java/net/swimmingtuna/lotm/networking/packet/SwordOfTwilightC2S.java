package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwordOfTwilightC2S {
    public SwordOfTwilightC2S() {

    }

    public SwordOfTwilightC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            Vec3 lookVec = player.getLookAngle();
            Vec3 entityPos = player.position();
            Vec3 eyePos = new Vec3(entityPos.x, entityPos.y + player.getEyeHeight(), entityPos.z);
            LivingEntity closestTarget = null;
            double closestAngle = Double.MAX_VALUE;
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(200))) {
                if (target != player) {
                    Vec3 toTargetVec = target.position().subtract(entityPos).normalize();
                    double dotProduct = lookVec.dot(toTargetVec);
                    double angle = Math.toDegrees(Math.acos(dotProduct));

                    if (angle < 10.0) {
                        Vec3 targetPos = target.position().add(0, target.getEyeHeight() / 2, 0);

                        if (hasLineOfSight(player, eyePos, targetPos)) {
                            if (angle < closestAngle) {
                                closestAngle = angle;
                                closestTarget = target;
                            }
                        }
                    }
                }
            }

            if (closestTarget != null && entityPos.distanceTo(closestTarget.position()) >= 1) {
                removeItemFromSlot(player, player.getMainHandItem());
                closestTarget.getPersistentData().putUUID("twilightSwordOwnerUUID", player.getUUID());
                closestTarget.getPersistentData().putInt("twilightSwordSpawnTick", 21);
                player.getPersistentData().putInt("returnSwordOfTwilight", 21);
            }
        });
        return true;
    }

    private boolean hasLineOfSight(ServerPlayer player, Vec3 eyePos, Vec3 targetPos) {
        ClipContext context = new ClipContext(
                eyePos,
                targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        BlockHitResult result = player.level().clip(context);
        if (result.getType() == HitResult.Type.BLOCK) {
            double distToHit = eyePos.distanceTo(result.getLocation());
            double distToTarget = eyePos.distanceTo(targetPos);
            return Math.abs(distToHit - distToTarget) < 4;
        }
        return result.getType() == HitResult.Type.MISS;
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }
}