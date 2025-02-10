package net.swimmingtuna.lotm.entity.mob.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;
import java.util.function.Predicate;

public class GroupBeyondersBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = List.of(
            Pair.of(
                    MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                    MemoryStatus.VALUE_PRESENT
            )
    );

    protected Predicate<? extends LivingEntity> targetPredicate = entity -> true;
    protected Predicate<E> canTargetPredicate = entity -> true;

    public GroupBeyondersBehaviour<E> targetPredicate(Predicate<? extends LivingEntity> predicate) {
        this.targetPredicate = predicate;
        return this;
    }

    public GroupBeyondersBehaviour<E> canTargetPredicate(Predicate<E> predicate) {
        this.canTargetPredicate = predicate;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return this.canTargetPredicate.test(entity);
    }

}
