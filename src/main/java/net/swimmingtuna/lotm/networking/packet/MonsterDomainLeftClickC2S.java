package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfDecay;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfProvidence;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterDomainTeleporation;

import java.util.function.Supplier;

public class MonsterDomainLeftClickC2S {
    public MonsterDomainLeftClickC2S() {

    }

    public MonsterDomainLeftClickC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            ItemStack heldItem = player.getMainHandItem();
            int heldItemSlot = player.getInventory().selected;
            int sequence = BeyonderHolderAttacher.getHolderUnwrap(player).getCurrentSequence();
            if (sequence == 4) {
                if (heldItem.getItem() instanceof DomainOfDecay) {
                    player.getInventory().setItem(heldItemSlot, ItemInit.PROVIDENCEDOMAIN.get().getDefaultInstance());
                } else if (heldItem.getItem() instanceof DomainOfProvidence) {
                    player.getInventory().setItem(heldItemSlot, ItemInit.DECAYDOMAIN.get().getDefaultInstance());
                }
            } else if (sequence <= 3) {
                if (heldItem.getItem() instanceof DomainOfDecay) {
                    player.getInventory().setItem(heldItemSlot, ItemInit.PROVIDENCEDOMAIN.get().getDefaultInstance());
                } else if (heldItem.getItem() instanceof DomainOfProvidence) {
                    player.getInventory().setItem(heldItemSlot, ItemInit.MONSTERDOMAINTELEPORATION.get().getDefaultInstance());
                } else if (heldItem.getItem() instanceof MonsterDomainTeleporation) {
                    player.getInventory().setItem(heldItemSlot, ItemInit.DECAYDOMAIN.get().getDefaultInstance());
                }
            }
        });
        return true;
    }
}
