package net.swimmingtuna.lotm.util;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import static net.swimmingtuna.lotm.util.BeyonderUtil.getMentalStrength;

public class MentalDamageSource extends DamageSource {
    private final LivingEntity attacker;
    private final LivingEntity target;

    public MentalDamageSource(Holder<DamageType> type, LivingEntity attacker, LivingEntity target) {
        super(type, attacker, attacker);
        this.attacker = attacker;
        this.target = target;
    }


    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity pLivingEntity) {
        return Component.empty();
    }

    public float calculateDamage(float baseAmount) {
        if (attacker == null || target == null) return baseAmount;
        int attackerMental = getMentalStrength(attacker);
        int targetMental =getMentalStrength(target);
        if (targetMental <= 0) targetMental = 1;
        float multiplier = Math.min(2.0f, ((float)attackerMental) / targetMental);
        return baseAmount * multiplier;
    }
}
