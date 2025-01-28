package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class SendDustParticleS2C {
    private final float r;
    private final float g;
    private final float b;
    private final float scale;
    private final double x;
    private final double y;
    private final double z;
    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;

    public SendDustParticleS2C(float r, float g, float b, float scale, double x, double y, double z,
                               double deltaX, double deltaY, double deltaZ) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.scale = scale;
        this.x = x;
        this.y = y;
        this.z = z;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
    }

    public SendDustParticleS2C(FriendlyByteBuf buf) {
        this.r = buf.readFloat();
        this.g = buf.readFloat();
        this.b = buf.readFloat();
        this.scale = buf.readFloat();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.deltaX = buf.readDouble();
        this.deltaY = buf.readDouble();
        this.deltaZ = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
        buf.writeFloat(scale);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(deltaX);
        buf.writeDouble(deltaY);
        buf.writeDouble(deltaZ);
    }

    public static void handle(SendDustParticleS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                DustParticleOptions particleOptions = new DustParticleOptions(
                        new Vector3f(msg.r, msg.g, msg.b), msg.scale);
                level.addAlwaysVisibleParticle(particleOptions, msg.x, msg.y, msg.z,
                        msg.deltaX, msg.deltaY, msg.deltaZ);
            }
        });
        context.setPacketHandled(true);
    }
}