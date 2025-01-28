package net.swimmingtuna.lotm.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.swimmingtuna.lotm.LOTM;

public class DamageTypeInit {

    public static final DeferredRegister<DamageType> DAMAGE_TYPES =
            DeferredRegister.create(Registries.DAMAGE_TYPE, LOTM.MOD_ID);

    public static final ResourceKey<DamageType>
            MENTAL_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(LOTM.MOD_ID, "mental_damage"));

    public static DamageSource source(Level level, ResourceKey<DamageType> id) {
        final Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        final Holder.Reference<DamageType> damage = registry.getHolderOrThrow(id);
        return new DamageSource(damage);
    }

}