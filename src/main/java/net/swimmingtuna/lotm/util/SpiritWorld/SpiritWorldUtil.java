package net.swimmingtuna.lotm.util.SpiritWorld;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.BatchedSpiritWorldUpdatePacketS2C;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpiritWorldUtil {


    public static void spiritWorldNoTarget(LivingEntity entity) {
        if (!entity.level().isClientSide()) {

            if (entity.tickCount % 20 == 0) {
                for (Mob mob : entity.level().getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(50))) {
                    boolean inSpiritWorld = entity.getPersistentData().getBoolean("inSpiritWorld") == mob.getPersistentData().getBoolean("inSpiritWorld");
                    if (inSpiritWorld) {
                        if (mob.getTarget() == entity) {
                            mob.setTarget(null);
                        }
                    }
                }
            }
        }
    }

    public static void spiritWorldChangeTargetEvent(LivingChangeTargetEvent event) {
        LivingEntity originalEntity = event.getEntity();
        LivingEntity targetEntity = event.getNewTarget();
        if (targetEntity != null) {
            boolean inSpiritWorld = originalEntity.getPersistentData().getBoolean("inSpiritWorld") == targetEntity.getPersistentData().getBoolean("inSpiritWorld");
            if (!inSpiritWorld) {
                event.setCanceled(true);
            }
        }
    }
    public static void sendSpiritWorldPackets(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        boolean isInSpiritWorld = tag.getBoolean("inSpiritWorld");
        Map<UUID, Boolean> updates = new HashMap<>();

        for (Player player : livingEntity.level().players()) {
            if (player instanceof ServerPlayer serverPlayer) {
                boolean playerInSpiritWorld = serverPlayer.getPersistentData().getBoolean("inSpiritWorld");
                boolean shouldBeVisible = isInSpiritWorld == playerInSpiritWorld;
                updates.put(livingEntity.getUUID(), !shouldBeVisible);
            }
        }

        if (!updates.isEmpty()) {
            LOTMNetworkHandler.sendToAllPlayers(new BatchedSpiritWorldUpdatePacketS2C(updates));
        }
    }

}
