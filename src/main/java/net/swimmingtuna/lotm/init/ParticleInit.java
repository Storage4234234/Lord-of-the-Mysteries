package net.swimmingtuna.lotm.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;

public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, LOTM.MOD_ID);

    public static final RegistryObject<SimpleParticleType> NULL_PARTICLE =
            PARTICLE_TYPES.register("null_particle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> METEOR_PARTICLE =
            PARTICLE_TYPES.register("meteor_particle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ACIDRAIN_PARTICLE =
            PARTICLE_TYPES.register("acidrain_particle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> TORNADO_PARTICLE =
            PARTICLE_TYPES.register("tornado_particle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SONIC_BOOM_PARTICLE =
            PARTICLE_TYPES.register("sonic_boom_particle", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}

