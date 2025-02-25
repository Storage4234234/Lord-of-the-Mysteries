package net.swimmingtuna.lotm.networking;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.networking.packet.*;
import net.swimmingtuna.lotm.util.AllyInformation.SyncAlliesPacket;
import net.swimmingtuna.lotm.util.CapabilitySyncer.network.SimpleEntityCapabilityStatusPacket;

import java.util.List;
import java.util.function.BiConsumer;

public class LOTMNetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LOTM.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int nextId = 0;
    private static int id() {
        return nextId++;
    }

    public static void register() {
        List<BiConsumer<SimpleChannel, Integer>> packets = ImmutableList.<BiConsumer<SimpleChannel, Integer>>builder()
                .add(SimpleEntityCapabilityStatusPacket::register)
                .add(SpiritualityC2S::register)
                .build();
        packets.forEach(consumer -> consumer.accept(INSTANCE, id()));

        INSTANCE.messageBuilder(LuckManipulationLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(LuckManipulationLeftClickC2S::new)
                .encoder(LuckManipulationLeftClickC2S::toByte)
                .consumerMainThread(LuckManipulationLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(MercuryLiqueficationC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MercuryLiqueficationC2S::new)
                .encoder(MercuryLiqueficationC2S::toByte)
                .consumerMainThread(MercuryLiqueficationC2S::handle)
                .add();
        INSTANCE.messageBuilder(MonsterDomainLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonsterDomainLeftClickC2S::new)
                .encoder(MonsterDomainLeftClickC2S::toByte)
                .consumerMainThread(MonsterDomainLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(DeathKnellLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DeathKnellLeftClickC2S::new)
                .encoder(DeathKnellLeftClickC2S::toByte)
                .consumerMainThread(DeathKnellLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(SwordOfSilverC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SwordOfSilverC2S::new)
                .encoder(SwordOfSilverC2S::toByte)
                .consumerMainThread(SwordOfSilverC2S::handle)
                .add();
        INSTANCE.messageBuilder(SyncAlliesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAlliesPacket::decode)
                .encoder(SyncAlliesPacket::encode)
                .consumerMainThread(SyncAlliesPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncShouldntRenderInvisibilityPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncShouldntRenderInvisibilityPacketS2C::new)
                .encoder(SyncShouldntRenderInvisibilityPacketS2C::encode)
                .consumerMainThread(SyncShouldntRenderInvisibilityPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncAntiConcealmentPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAntiConcealmentPacketS2C::new)
                .encoder(SyncAntiConcealmentPacketS2C::encode)
                .consumerMainThread(SyncAntiConcealmentPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(ScribeCopyAbilityC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ScribeCopyAbilityC2S::new)
                .encoder(ScribeCopyAbilityC2S::toByte)
                .consumerMainThread(ScribeCopyAbilityC2S::handle)
                .add();
        INSTANCE.messageBuilder(RemoveInvisibiltyS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RemoveInvisibiltyS2C::new)
                .encoder(RemoveInvisibiltyS2C::encode)
                .consumerMainThread(RemoveInvisibiltyS2C::handle)
                .add();
        INSTANCE.messageBuilder(MonsterLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonsterLeftClickC2S::new)
                .encoder(MonsterLeftClickC2S::toByte)
                .consumerMainThread(MonsterLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(GigantificationC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(GigantificationC2S::new)
                .encoder(GigantificationC2S::toByte)
                .consumerMainThread(GigantificationC2S::handle)
                .add();
        INSTANCE.messageBuilder(AddItemInInventoryC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AddItemInInventoryC2S::new)
                .encoder(AddItemInInventoryC2S::toByte)
                .consumerMainThread(AddItemInInventoryC2S::handle)
                .add();
        INSTANCE.messageBuilder(LeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(LeftClickC2S::new)
                .encoder(LeftClickC2S::toByte)
                .consumerMainThread(LeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(UpdateItemInHandC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateItemInHandC2S::new)
                .encoder(UpdateItemInHandC2S::toByte)
                .consumerMainThread(UpdateItemInHandC2S::handle)
                .add();
        INSTANCE.messageBuilder(MatterAccelerationBlockC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MatterAccelerationBlockC2S::new)
                .encoder(MatterAccelerationBlockC2S::toByte)
                .consumerMainThread(MatterAccelerationBlockC2S::handle)
                .add();
        INSTANCE.messageBuilder(SpiritVisionC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpiritVisionC2S::new)
                .encoder(SpiritVisionC2S::toByte)
                .consumerMainThread(SpiritVisionC2S::handle)
                .add();
        INSTANCE.messageBuilder(SpiritWorldTraversalC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpiritWorldTraversalC2S::new)
                .encoder(SpiritWorldTraversalC2S::toByte)
                .consumerMainThread(SpiritWorldTraversalC2S::handle)
                .add();
        INSTANCE.messageBuilder(MisfortuneManipulationLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MisfortuneManipulationLeftClickC2S::new)
                .encoder(MisfortuneManipulationLeftClickC2S::toByte)
                .consumerMainThread(MisfortuneManipulationLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(DawnWeaponryLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DawnWeaponryLeftClickC2S::new)
                .encoder(DawnWeaponryLeftClickC2S::toByte)
                .consumerMainThread(DawnWeaponryLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(MonsterCalamityIncarnationLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonsterCalamityIncarnationLeftClickC2S::new)
                .encoder(MonsterCalamityIncarnationLeftClickC2S::toByte)
                .consumerMainThread(MonsterCalamityIncarnationLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(FalseProphecyLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FalseProphecyLeftClickC2S::new)
                .encoder(FalseProphecyLeftClickC2S::toByte)
                .consumerMainThread(FalseProphecyLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(CalamityEnhancementLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(CalamityEnhancementLeftClickC2S::new)
                .encoder(CalamityEnhancementLeftClickC2S::toByte)
                .consumerMainThread(CalamityEnhancementLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(NonVisibleS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NonVisibleS2C::decode)
                .encoder(NonVisibleS2C::encode)
                .consumerMainThread(NonVisibleS2C::handle)
                .add();
        INSTANCE.messageBuilder(UpdateEntityLocationS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateEntityLocationS2C::decode)
                .encoder(UpdateEntityLocationS2C::encode)
                .consumerMainThread(UpdateEntityLocationS2C::handle)
                .add();
        INSTANCE.messageBuilder(UpdateDragonBreathS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateDragonBreathS2C::decode)
                .encoder(UpdateDragonBreathS2C::encode)
                .consumerMainThread(UpdateDragonBreathS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncSequencePacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncSequencePacketS2C::new)
                .encoder(SyncSequencePacketS2C::encode)
                .consumerMainThread(SyncSequencePacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncLeftClickCooldownS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncLeftClickCooldownS2C::new)
                .encoder(SyncLeftClickCooldownS2C::encode)
                .consumerMainThread(SyncLeftClickCooldownS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncShouldntRenderSpiritWorldPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncShouldntRenderSpiritWorldPacketS2C::new)
                .encoder(SyncShouldntRenderSpiritWorldPacketS2C::encode)
                .consumerMainThread(SyncShouldntRenderSpiritWorldPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(BatchedSpiritWorldUpdatePacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BatchedSpiritWorldUpdatePacketS2C::new)
                .encoder(BatchedSpiritWorldUpdatePacketS2C::encode)
                .consumerMainThread(BatchedSpiritWorldUpdatePacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(RequestCooldownSetC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestCooldownSetC2S::new)
                .encoder(RequestCooldownSetC2S::toByte)
                .consumerMainThread(RequestCooldownSetC2S::handle)
                .add();
        INSTANCE.messageBuilder(SendParticleS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendParticleS2C::new)
                .encoder(SendParticleS2C::encode)
                .consumerMainThread(SendParticleS2C::handle)
                .add();
        INSTANCE.messageBuilder(SendDustParticleS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendDustParticleS2C::new)
                .encoder(SendDustParticleS2C::encode)
                .consumerMainThread(SendDustParticleS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncAbilityCooldownsS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAbilityCooldownsS2C::decode)
                .encoder(SyncAbilityCooldownsS2C::encode)
                .consumerMainThread(SyncAbilityCooldownsS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncAbilitiesS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAbilitiesS2C::new)
                .encoder(SyncAbilitiesS2C::encode)
                .consumerMainThread(SyncAbilitiesS2C::handle)
                .add();
        INSTANCE.messageBuilder(ClearAbilitiesS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClearAbilitiesS2C::new)
                .encoder(ClearAbilitiesS2C::toByte)
                .consumerMainThread(ClearAbilitiesS2C::handle)
                .add();
        INSTANCE.messageBuilder(ToggleDistanceC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleDistanceC2S::new)
                .encoder(ToggleDistanceC2S::toByte)
                .consumerMainThread(ToggleDistanceC2S::handle)
                .add();
        INSTANCE.messageBuilder(TravelerWaypointC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(TravelerWaypointC2S::new)
                .encoder(TravelerWaypointC2S::toByte)
                .consumerMainThread(TravelerWaypointC2S::handle)
                .add();
    }


    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}

