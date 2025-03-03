package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateEntityLocationS2C {
    public final double x;
    public final double y;
    public final double z;
    public final double velocityX;
    public final double velocityY;
    public final double velocityZ;
    public final int entityId;

    public UpdateEntityLocationS2C(double x, double y, double z, double velocityX, double velocityY, double velocityZ, int entityId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.entityId = entityId;
    }

    public static void encode(UpdateEntityLocationS2C message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeDouble(message.velocityX);
        buffer.writeDouble(message.velocityY);
        buffer.writeDouble(message.velocityZ);
        buffer.writeInt(message.entityId);
    }

    public static UpdateEntityLocationS2C decode(FriendlyByteBuf buffer) {
        return new UpdateEntityLocationS2C(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readInt()
        );
    }

    public static void handle(UpdateEntityLocationS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(msg.entityId);
                if (entity != null) {
                    entity.lerpTo(msg.x, msg.y, msg.z, (float) Math.toDegrees(Math.atan2(msg.velocityZ, msg.velocityX)), (float) Math.toDegrees(Math.atan2(-msg.velocityY, Math.sqrt(msg.velocityX * msg.velocityX + msg.velocityZ * msg.velocityZ))), 3, false);
                    entity.setDeltaMovement(msg.velocityX, msg.velocityY, msg.velocityZ);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}