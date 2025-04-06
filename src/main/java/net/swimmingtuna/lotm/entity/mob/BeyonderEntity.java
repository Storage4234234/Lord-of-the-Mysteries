package net.swimmingtuna.lotm.entity.mob;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.entity.mob.behaviour.GroupBeyondersBehaviour;
import net.swimmingtuna.lotm.init.MobInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BeyonderEntity extends Monster implements SmartBrainOwner<BeyonderEntity> {

    public BeyonderEntity(Level pLevel, BeyonderClass beyonderClass) {
        super(MobInit.MONSTER_BEYONDER_ENTITY.get(), pLevel);
        BeyonderUtil.setPathway(this, beyonderClass);
        BeyonderUtil.setSequence(this, 9);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 2f)
                .add(Attributes.MOVEMENT_SPEED, 0.250f);
    }

    @Override
    @NotNull
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    @NotNull
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }


    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        //todo add later
    }


    @Override
    @NotNull
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public List<ExtendedSensor<BeyonderEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyLivingEntitySensor<>(),
                new NearbyPlayersSensor<>()
        );
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }

    @Override
    public BrainActivityGroup<BeyonderEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new GroupBeyondersBehaviour<>(),
                new MoveToWalkTarget<>()

        );
    }

    @Override
    public BrainActivityGroup<BeyonderEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new TargetOrRetaliate<>(),
                        new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>(),
                        new SetRandomWalkTarget<>().dontAvoidWater().setRadius(8)
                )
        );
    }

/*    @Override
    public BrainActivityGroup<BeyonderEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>(),
                new SetWalkTargetToAttackTarget<>(),
                new AnimatableMeleeAttack<>(0));
    }*/
}