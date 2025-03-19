package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.entity.MercuryEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.function.Supplier;

public class MercuryLiqueficationC2S {
    public MercuryLiqueficationC2S() {

    }

    public MercuryLiqueficationC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            CompoundTag tag = player.getPersistentData();
            boolean currentState = tag.getBoolean("mercuryLiquefication");
            float damage = BeyonderUtil.getDamage(player).get(ItemInit.MERCURYLIQUEFICATION.get());
            Level level = player.level();
            if (!level.isClientSide() && currentState && BeyonderUtil.getSequence(player) <= 2 && player.getMainHandItem().isEmpty()) {
                for (LivingEntity livingEntity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(damage * 5.0f))) {
                    if (livingEntity != player && !BeyonderUtil.isAllyOf(player, livingEntity)) {
                        MercuryEntity mercuryEntity = new MercuryEntity(EntityInit.MERCURY_ENTITY.get(), level);
                        mercuryEntity.setSpeed(Math.max(1, (int) damage / 4));
                        mercuryEntity.setHarmTime((int) damage * 25);
                        mercuryEntity.setLifetime((int) damage * 2);
                        mercuryEntity.setTarget(livingEntity);
                        mercuryEntity.teleportTo(player.getX(), player.getY(), player.getZ());
                        level.addFreshEntity(mercuryEntity);
                        player.setHealth(Math.max(1, player.getHealth() - 0.5f));
                    }
                }
            }
        });
        return true;
    }
}
