package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class MercuryPortalEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(MercuryPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(MercuryPortalEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(MercuryPortalEntity.class, EntityDataSerializers.FLOAT);


    public MercuryPortalEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.tickCount >= getLife()) {
                this.discard();
            } else if (this.tickCount < 40 && this.getPersistentData().contains("mercuryPortalOwner")) {
                UUID uuid = this.getPersistentData().getUUID("mercuryPortalOwner");
                LivingEntity owner = BeyonderUtil.getEntityFromUUID(this.level(), uuid);
                this.setYaw(owner.getYRot());
                this.setPitch(owner.getXRot());
            } else if (this.tickCount == 40  && this.getPersistentData().contains("mercuryPortalOwner")) {
                if (this.tickCount == 40 && this.getPersistentData().contains("mercuryPortalOwner")) {
                    UUID uuid = this.getPersistentData().getUUID("mercuryPortalOwner");
                    LivingEntity owner = BeyonderUtil.getEntityFromUUID(this.level(), uuid);
                    Vec3 lookVec = owner.getLookAngle().normalize();
                    Vec3 targetPos = owner.position().add(lookVec.scale(80));
                    Vec3 direction = targetPos.subtract(this.position()).normalize();
                    SilverLightEntity silverLight = new SilverLightEntity(EntityInit.SILVER_LIGHT_ENTITY.get(), this.level());
                    silverLight.setOwner(owner);
                    silverLight.setDeltaMovement(direction.scale(5).scale(6.5));
                    silverLight.hurtMarked = true;
                    silverLight.teleportTo(this.getX(), this.getY(), this.getZ());
                    double horizontalDist = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
                    float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x));
                    float pitch = (float) Math.toDegrees(Math.atan2(direction.y, horizontalDist));
                    BeyonderUtil.setScale(silverLight, 1.5f);
                    silverLight.setYaw(yaw);
                    silverLight.setPitch(pitch);
                    this.level().addFreshEntity(silverLight);
                }

                this.discard();
            }
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(LIFE, 80);
        this.entityData.define(YAW, 0.0f);
        this.entityData.define(PITCH, 0.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("life")) {
            this.entityData.set(LIFE, tag.getInt("life"));
        }
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("pitch")) {
            this.entityData.set(PITCH, tag.getFloat("pitch"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("life", this.entityData.get(LIFE));
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("pitch", this.entityData.get(PITCH));
    }

    public int getLife() {
        return this.entityData.get(LIFE);
    }

    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public float getPitch() {
        return this.entityData.get(PITCH);
    }

    public float setPitch() {
        return this.entityData.get(YAW);
    }

    public void setLife(int life) {
        this.entityData.set(LIFE, life);
    }

    public void setPitch(float pitch) {
        this.entityData.set(PITCH, pitch);
    }

    public void setYaw(float yaw) {
        this.entityData.set(YAW, yaw);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<MercuryPortalEntity> animationState) {
        AnimationController<MercuryPortalEntity> controller = animationState.getController();
        controller.setAnimation(RawAnimation.begin().then("portal.rotating", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
