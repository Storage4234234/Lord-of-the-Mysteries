package net.swimmingtuna.lotm.entity.mob.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;

public class GroupBeyondersBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = List.of(
            Pair.of(
                    MemoryModuleType.NEAREST_LIVING_ENTITIES,
                    MemoryStatus.VALUE_PRESENT
            )
    );

    protected Predicate<? extends LivingEntity> groupTarget = entity -> true;
    protected Predicate<E> canGroup = entity -> true;

    public GroupBeyondersBehaviour<E> targetPredicate(Predicate<? extends LivingEntity> predicate) {
        this.groupTarget = predicate;
        return this;
    }

    public GroupBeyondersBehaviour<E> canTargetPredicate(Predicate<E> predicate) {
        this.canGroup = predicate;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return this.canGroup.test(entity);
    }

    protected void tick(E entity) {
        BrainUtils.withMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, (target) -> {
            if (target.stream().findAny().isPresent()){
                entity.moveRelative(5, target.stream().min((o1, o2) -> (int) (o1.distanceTo(entity) - o2.distanceTo(entity))).get().position());
            }
        });
    }

    private void addAllies(List<LivingEntity> entities, E mainEntity){
        for (LivingEntity entity : entities) {
            PlayerAllyData allyData = entity.getCommandSenderWorld().getServer().getLevel(entity.getCommandSenderWorld().dimension()).getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            allyData.addAlly(mainEntity.getUUID(), entity.getUUID());
        }
    }

    @Override
    protected void start(E entity) {
        List<LivingEntity> nearbyEntities = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES);
        List<LivingEntity> groupTarget = nearbyEntities.stream().filter(
                target -> this.canGroup.test((E) target)
                        && BeyonderUtil.getSequence(target) != -1
                        && BeyonderUtil.getPathway(target) == BeyonderUtil.getPathway(entity)).toList();

        addAllies(groupTarget, entity);
        if (groupTarget.isEmpty()) {
            BrainUtils.clearMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES);
        }
        else {
            BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, groupTarget);
            BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        }
    }
}
