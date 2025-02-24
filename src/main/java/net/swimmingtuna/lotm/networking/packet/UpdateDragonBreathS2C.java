package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.entity.DeathKnellBulletEntity;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;

import java.util.function.Supplier;

public class UpdateDragonBreathS2C {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double endX;
    private final double endY;
    private final double endZ;
    private final int entityId;

    public UpdateDragonBreathS2C(double startx, double startY, double startZ, double endX, double endY, double endZ, int entityId) {
        this.startX = startx;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.entityId = entityId;
    }

    // Serializer
    public static void encode(UpdateDragonBreathS2C msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.startX);
        buf.writeDouble(msg.startY);
        buf.writeDouble(msg.startZ);
        buf.writeDouble(msg.endX);
        buf.writeDouble(msg.endY);
        buf.writeDouble(msg.endZ);
        buf.writeInt(msg.entityId);
    }

    // Deserializer
    public static UpdateDragonBreathS2C decode(FriendlyByteBuf buf) {
        return new UpdateDragonBreathS2C(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt()
        );
    }

    public static void handle(UpdateDragonBreathS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(msg.entityId);
                if (entity instanceof DragonBreathEntity dragonBreathEntity) {
                    dragonBreathEntity.setPos(msg.startY, msg.startY, msg.startZ);
                    dragonBreathEntity.endPos = new Vec3(msg.endX, msg.endY, msg.endZ);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}