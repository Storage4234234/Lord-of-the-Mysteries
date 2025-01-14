package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;

import java.util.function.Supplier;

public class DawnWeaponryLeftClickC2S {
    public DawnWeaponryLeftClickC2S() {

    }

    public DawnWeaponryLeftClickC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            CompoundTag tag = player.getPersistentData();
            int x = tag.getInt("dawnWeaponry");
                if (x <= 2) {
                    tag.putInt("dawnWeaponry", x + 1);
                } else {
                    tag.putInt("dawnWeaponry", 1);
                }
            });
        return true;
    }
}
