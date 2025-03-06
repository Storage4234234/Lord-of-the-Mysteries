package net.swimmingtuna.lotm.entity;


import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class GlobeOfTwilightEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(GlobeOfTwilightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(GlobeOfTwilightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RANDOM_YAW = SynchedEntityData.defineId(GlobeOfTwilightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RANDOM_PITCH = SynchedEntityData.defineId(GlobeOfTwilightEntity.class, EntityDataSerializers.FLOAT);

    public GlobeOfTwilightEntity(EntityType<? extends GlobeOfTwilightEntity> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    protected boolean canHitEntity(Entity p_36842_) {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        return;
    }

    public boolean isOnFire() {
        return false;
    }


    public boolean isPickable() {
        return false;
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(YAW, 0.0f);
        this.entityData.define(PITCH, 0.0f);
        this.entityData.define(RANDOM_PITCH, 0.0f);
        this.entityData.define(RANDOM_YAW, 0.0f);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("pitch")) {
            this.entityData.set(PITCH, tag.getFloat("pitch"));
        }
        if (tag.contains("random_yaw")) {
            this.entityData.set(YAW, tag.getFloat("random_yaw"));
        }
        if (tag.contains("random_pitch")) {
            this.entityData.set(PITCH, tag.getFloat("random_pitch"));
        }
    }


    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public void setYaw(float yaw) {
        this.entityData.set(YAW, yaw);
    }

    public float getPitch() {
        return this.entityData.get(PITCH);
    }

    public void setPitch(float pitch) {
        this.entityData.set(PITCH, pitch);
    }

    public float getRandomYaw() {
        return this.entityData.get(RANDOM_YAW);
    }

    public void setRandomYaw(float randomYaw) {
        this.entityData.set(RANDOM_YAW, randomYaw);
    }

    public float getRandomPitch() {
        return this.entityData.get(RANDOM_PITCH);
    }

    public void setRandomPitch(float randomPitch) {
        this.entityData.set(RANDOM_PITCH, randomPitch);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("pitch", this.entityData.get(PITCH));
        tag.putFloat("random_yaw", this.entityData.get(RANDOM_YAW));
        tag.putFloat("random_pitch", this.entityData.get(RANDOM_PITCH));
    }


    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            float scale = scaleData.getScale();
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(scale / 5))) {
                if (!BeyonderUtil.currentPathwayAndSequenceMatchesNoException(livingEntity, BeyonderClassInit.WARRIOR.get(), 0)) {
                    livingEntity.getPersistentData().putInt("inTwilight", 5);
                }
            }
        }
        if (!level().isClientSide()) {
            this.setYaw((this.getYaw() + getRandomPitch()));
            this.setPitch(getPitch() + getRandomPitch());
            if (this.tickCount >= 200) {
                this.discard();
            }
        }
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GlobeOfTwilightEntity> animationState) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
