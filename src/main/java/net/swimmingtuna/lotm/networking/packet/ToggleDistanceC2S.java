package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.InvisibleHand.setDistanceBoolean;

public class ToggleDistanceC2S {
    public  ToggleDistanceC2S(){}

    public ToggleDistanceC2S(FriendlyByteBuf buf){

    }

    public void toByte(FriendlyByteBuf buf){

    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                setDistanceBoolean(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
