package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RemoveInvisibiltyS2C {
    private final UUID entityUUID;

    public RemoveInvisibiltyS2C( UUID entityUUID) {
        this.entityUUID = entityUUID;
    }

    public RemoveInvisibiltyS2C(FriendlyByteBuf buf) {
        this.entityUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityUUID);
    }

    public static void handle(RemoveInvisibiltyS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            for (LivingEntity living : serverPlayer.level().getEntitiesOfClass(LivingEntity.class, serverPlayer.getBoundingBox().inflate(200))) {
                if (living.getUUID() == msg.entityUUID) {
                    living.removeEffect(MobEffects.INVISIBILITY);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
