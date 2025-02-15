package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MidSequenceDoorEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> HAS_PLAYED_ANIMATION = SynchedEntityData.defineId(MidSequenceDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_DYING = SynchedEntityData.defineId(MidSequenceDoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(MidSequenceDoorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(MidSequenceDoorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TELEPORT_X = SynchedEntityData.defineId(MidSequenceDoorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TELEPORT_Y = SynchedEntityData.defineId(MidSequenceDoorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TELEPORT_Z = SynchedEntityData.defineId(MidSequenceDoorEntity.class, EntityDataSerializers.FLOAT);



    public MidSequenceDoorEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public MidSequenceDoorEntity(Level level, double x, double y, double z, float yaw, int life) {
        this(EntityInit.MID_SEQUENCE_DOOR_ENTITY.get(), level);
        this.entityData.set(LIFE, life);
        this.entityData.set(YAW, yaw);
        this.entityData.set(TELEPORT_X,(float) x);
        this.entityData.set(TELEPORT_Y,(float) y);
        this.entityData.set(TELEPORT_Z,(float) z);
    }

    @Override
    public void tick() {
        super.tick();

        int life = this.entityData.get(LIFE);
        if(life < 75){
            this.entityData.set(HAS_PLAYED_ANIMATION, true);
        }
        if(life > 0){
            if(life <= 15){
                this.entityData.set(IS_DYING, true);
            }
            this.entityData.set(LIFE, life - 1);
        }else{
            this.remove(RemovalReason.DISCARDED);
        }

        if (BeyonderUtil.isEntityColliding(this, this.level(), 0.5)) {
            if(life > 15 && life < 75){
                Entity entity = BeyonderUtil.checkEntityCollision(this, this.level(), 0.5);
                if (entity != null) {
                    double teleportX = this.entityData.get(TELEPORT_X);
                    double teleportY = this.entityData.get(TELEPORT_Y);
                    double teleportZ = this.entityData.get(TELEPORT_Z);
                    entity.teleportTo(teleportX, teleportY, teleportZ);
                }
            }
        }
    }

    public float getYaw() {
        return this.entityData.get(YAW);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(YAW, 0.0F);
        this.entityData.define(LIFE, 120);
        this.entityData.define(HAS_PLAYED_ANIMATION, false);
        this.entityData.define(IS_DYING, false);
        this.entityData.define(TELEPORT_X, 0.0F);
        this.entityData.define(TELEPORT_Y, 0.0F);
        this.entityData.define(TELEPORT_Z, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("entityCanPassThrough")) {
            this.entityData.set(LIFE, tag.getInt("life"));
        }
        if (tag.contains("isDying")) {
            this.entityData.set(IS_DYING, tag.getBoolean("isDying"));
        }
        if (tag.contains("teleportX")) {
            this.entityData.set(TELEPORT_X, tag.getFloat("teleportX"));
        }
        if (tag.contains("teleportY")) {
            this.entityData.set(TELEPORT_Y, tag.getFloat("teleportY"));
        }
        if (tag.contains("teleportZ")) {
            this.entityData.set(TELEPORT_Z, tag.getFloat("teleportZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putBoolean("hasPlayedAnimation", this.entityData.get(HAS_PLAYED_ANIMATION));
        tag.putInt("life", this.entityData.get(LIFE));
        tag.putBoolean("isDying", this.entityData.get(IS_DYING));
        tag.putFloat("teleportX", this.entityData.get(TELEPORT_X));
        tag.putFloat("teleportY", this.entityData.get(TELEPORT_Y));
        tag.putFloat("teleportZ", this.entityData.get(TELEPORT_Z));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<MidSequenceDoorEntity> animationState) {
        AnimationController<MidSequenceDoorEntity> controller = animationState.getController();

        if (!this.entityData.get(HAS_PLAYED_ANIMATION)) {
            controller.setAnimation(RawAnimation.begin().then("open", Animation.LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }

        if (this.entityData.get(IS_DYING)) {
            controller.setAnimation(RawAnimation.begin().then("close", Animation.LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }

        controller.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}