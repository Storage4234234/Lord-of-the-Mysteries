package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.function.Supplier;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.InvisibleHand.setDistanceBoolean;

public class TravelDoorC2S {
    public TravelDoorC2S(){}

    public TravelDoorC2S(FriendlyByteBuf buf){

    }

    public void toByte(FriendlyByteBuf buf){

    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getPersistentData().putInt("travelBlinkDistance", 0);
                player.displayClientMessage(Component.literal("Blink Distance is 0").withStyle(BeyonderUtil.getStyle(player)), true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
