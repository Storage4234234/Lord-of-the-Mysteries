package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.EntityUtil.BeamEntity;
import net.swimmingtuna.lotm.util.RotationUtil;
import org.jetbrains.annotations.NotNull;

public class DragonBreathEntity extends BeamEntity {
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(DragonBreathEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RANGE = SynchedEntityData.defineId(DragonBreathEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CAUSE_FIRE = SynchedEntityData.defineId(DragonBreathEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(DragonBreathEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(DragonBreathEntity.class, EntityDataSerializers.INT);


    public DragonBreathEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public DragonBreathEntity(LivingEntity owner, float power) {
        super(EntityInit.DRAGON_BREATH_ENTITY.get(), owner, power);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DURATION, 4);
        this.entityData.define(RANGE, 64);
        this.entityData.define(CHARGE, 20);
        this.entityData.define(CAUSE_FIRE, true);
        this.entityData.define(SIZE, 1);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("duration")) {
            this.setDuration(compound.getInt("duration"));
        }
        if (compound.contains("range")) {
            this.setRange(compound.getInt("range"));
        }
        if (compound.contains("charge")) {
            this.setCharge(compound.getInt("charge"));
        }
        if (compound.contains("cause_fire")) {
            this.setCausesFire(compound.getBoolean("cause_fire"));
        }
        if (compound.contains("size")) {
            this.setSize(compound.getInt("size"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("duration", this.getDuration());
        compound.putInt("range", (int) this.getRange());
        compound.putInt("charge", this.getCharge());
        compound.putBoolean("cause_fire", this.causesFire());
        compound.putInt("size", this.getSize());
    }

    @Override
    public int getFrames() {
        return 16;
    }

    @Override
    public int getCharge() {
        return this.entityData.get(CHARGE);
    }

    public void setCharge(int charge) {
        this.entityData.set(CHARGE, charge);
    }

    @Override
    public int getSize() {
        return this.entityData.get(SIZE);
    }

    public void setSize(int size) {
        this.entityData.set(SIZE, size);
    }

    @Override
    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION, duration);
    }

    @Override
    public double getRange() {
        return this.entityData.get(RANGE);
    }


    public void setRange(int range) {
        this.entityData.set(RANGE, range);
    }

    @Override
    public boolean causesFire() {
        return this.entityData.get(CAUSE_FIRE);
    }

    public void setCausesFire(boolean fire) {
        this.entityData.set(CAUSE_FIRE, fire);
    }

    @Override
    protected boolean breaksBlocks() {
        return true;
    }



    @Override
    protected Vec3 calculateSpawnPos(LivingEntity owner) {
        return new Vec3(owner.getX(), owner.getY() + (owner.getBbHeight() * 0.75F) - (this.getBbHeight() / 2.0F), owner.getZ())
                .add(RotationUtil.getTargetAdjustedLookAngle(owner));
    }

    public static void shootDragonBreath(LivingEntity player, int power, double x, double y, double z) {
        DragonBreathEntity dragonBreath = new DragonBreathEntity(player, power);
        dragonBreath.setDestroyBlocks(true);
        dragonBreath.teleportTo(x,y+1,z);
        dragonBreath.setDamage(power * 0.4f);
        dragonBreath.setSize(Math.max(1, 3 - (BeyonderUtil.getSequence(player) * 2)));
        dragonBreath.setIsDragonbreath(true);
        dragonBreath.setFrenzyTime((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.DRAGON_BREATH.get()));
        player.level().addFreshEntity(dragonBreath);
    }

}
