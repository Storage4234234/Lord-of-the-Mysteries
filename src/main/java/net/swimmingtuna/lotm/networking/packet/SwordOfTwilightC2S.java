package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
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
            LivingEntity closestTarget = null;
            double closestAngle = Double.MAX_VALUE;
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(200))) {
                if (target != player) {
                    Vec3 toTargetVec = target.position().subtract(entityPos).normalize();
                    double dotProduct = lookVec.dot(toTargetVec);
                    double angle = Math.toDegrees(Math.acos(dotProduct));
                    if (angle < closestAngle && angle < 10.0) {
                        closestAngle = angle;
                        closestTarget = target;
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

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }
}
