package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import virtuoel.pehkui.api.ScaleTypes;

public class MercuryCageEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> HAS_PLAYED_ANIMATION = SynchedEntityData.defineId(MercuryCageEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(MercuryCageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(MercuryCageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(MercuryCageEntity.class, EntityDataSerializers.FLOAT);


    public MercuryCageEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.tickCount >= getMaxLife()) {
                this.discard();
            }

            float scale = ScaleTypes.BASE.getScaleData(this).getScale();
            CompoundTag tag = this.getPersistentData();
            double cageX = this.getX();
            double cageY = this.getY();
            double cageZ = this.getZ();

            if (tag.contains("cageOwnerUUID") && this.tickCount >= 20) {
                LivingEntity owner = BeyonderUtil.getEntityFromUUID(this.level(), tag.getUUID("cageOwnerUUID"));
                for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(scale + 10))) {
                    if (living != owner) {
                        if (BeyonderUtil.getSequence(living) >= BeyonderUtil.getSequence(owner)) {
                            if (ScaleTypes.BASE.getScaleData(living).getScale() >= scale) {
                                this.discard();
                            }
                        }
                        double entityX = living.getX();
                        double entityY = living.getY();
                        double entityZ = living.getZ();
                        double minX = cageX - (scale * 0.55);
                        double maxX = cageX + (scale * 0.55);
                        double minY = cageY - (scale * 0.05);
                        double maxY = cageY + (scale * 0.9);
                        double minZ = cageZ - (scale * 0.55);
                        double maxZ = cageZ + (scale * 0.55);
                        boolean isOutsideCage = entityX >= maxX || entityY >= maxY || entityZ >= maxZ || entityX <= minX || entityY <= minY || entityZ <= minZ;
                        if (isOutsideCage) {
                            living.teleportTo(this.getX(), this.getY(), this.getZ());
                            living.sendSystemMessage(Component.literal("outside"));
                            setLife((int) (getMaxLife() - (50 - (BeyonderUtil.getSequence(living) * 5))));
                        }
                    }
                }
            }
        }
    }

    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public float getPitch() {
        return this.entityData.get(PITCH);
    }
    public float getMaxLife() {
        return this.entityData.get(MAX_LIFE);
    }

    public void setYaw(float yaw) {
        this.entityData.set(YAW, yaw);
    }
    public void setPitch(float pitch) {
        this.entityData.set(PITCH, pitch);
    }
    public void setLife(int life) {
        this.entityData.set(MAX_LIFE, life);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(YAW, 0.0F);
        this.entityData.define(PITCH, 0.0F);
        this.entityData.define(MAX_LIFE, 400);
        this.entityData.define(HAS_PLAYED_ANIMATION, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("pitch")) {
            this.entityData.set(PITCH, tag.getFloat("pitch"));
        }
        if (tag.contains("life")) {
            this.entityData.set(MAX_LIFE, tag.getInt("life"));
        }
        if (tag.contains("hasPlayedAnimation")) {
            this.entityData.set(HAS_PLAYED_ANIMATION, tag.getBoolean("hasPlayedAnimation"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("pitch", this.entityData.get(PITCH));
        tag.putInt("life", this.entityData.get(MAX_LIFE));
        tag.putBoolean("hasPlayedAnimation", this.entityData.get(HAS_PLAYED_ANIMATION));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<MercuryCageEntity> animationState) {
        AnimationController<MercuryCageEntity> controller = animationState.getController();

        if (!this.entityData.get(HAS_PLAYED_ANIMATION)) {
            controller.setAnimation(RawAnimation.begin().then("open", Animation.LoopType.HOLD_ON_LAST_FRAME));
            this.entityData.set(HAS_PLAYED_ANIMATION, true);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
