package net.swimmingtuna.lotm.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
import net.swimmingtuna.lotm.networking.packet.UpdateEntityLocationS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;

public class SilverLightEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(SilverLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(SilverLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(SilverLightEntity.class, EntityDataSerializers.BOOLEAN);

    public SilverLightEntity(EntityType<? extends SilverLightEntity> entityType, Level level) {
        super(entityType, level);
    }


    protected float getInertia() {
        return 1.0F;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (entity instanceof SilverLightEntity) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    public boolean isOnFire() {
        return false;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    public void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide()) {
            Entity hitEntity = result.getEntity();
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            float scale = scaleData.getScale();
            if (hitEntity instanceof LivingEntity livingEntity && this.getOwner() != null) {
                livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner()), scale * 20);
                this.discard();
            }
        }
    }


    public boolean isPickable() {
        return false;
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(YAW, 0.0f);
        this.entityData.define(PITCH, 0.0f);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("pitch")) {
            this.entityData.set(PITCH, tag.getFloat("pitch"));
        }
    }


    public float getYaw() { return this.entityData.get(YAW); }
    public void setYaw(float yaw) { this.entityData.set(YAW, yaw); }
    public float getPitch() { return this.entityData.get(PITCH); }
    public void setPitch(float pitch) { this.entityData.set(PITCH, pitch); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("pitch", this.entityData.get(PITCH));
    }
    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }


    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.getOwner() instanceof LivingEntity owner) {
            if (this.tickCount >= 100 && !this.getPersistentData().getBoolean("hasTeleported")) {
                this.getPersistentData().putBoolean("hasTeleported", true);
                LivingEntity target = findTargetInSight(owner);
                if (target != null) {
                    Vec3 teleportPos = getRandomPositionAroundTarget(target);
                    this.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
                    Vec3 targetPos = target.position();
                    Vec3 direction = targetPos.subtract(this.position()).normalize();
                    this.setDeltaMovement(direction.scale(5.0));
                    destroyBlocksAroundEntity();
                }
            }
            if (this.tickCount >= 200) {
                this.discard();
            }
            LOTMNetworkHandler.sendToAllPlayers(new UpdateEntityLocationS2C(this.getX(), this.getY(), this.getZ(),this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z(), this.getId()));
        }
        if (!level().isClientSide()) {
            LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleInit.FLASH_PARTICLE.get(), this.getX(), this.getY(), this.getZ(), 0,0,0));
            Vec3 motion = this.getDeltaMovement();
            double horizontalDist = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float newYaw = (float) Math.toDegrees(Math.atan2(motion.z, motion.x));
            float newPitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontalDist));
            this.setYaw(newYaw);
            this.setPitch(newPitch);
        }
    }

    public static LivingEntity findTargetInSight(LivingEntity owner) {
        List<LivingEntity> potentialTargets = owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(80));

        // Initialize variables for selecting the best target
        LivingEntity bestTarget = null;
        double bestScore = -1;

        for (LivingEntity target : potentialTargets) {
            if (target == owner || BeyonderUtil.isAllyOf(owner, target)) continue;
            Vec3 ownerPos = owner.position();
            Vec3 targetPos = target.position();
            double distanceSq = ownerPos.distanceToSqr(targetPos);
            if (distanceSq <= 80.0 * 80.0) {
                Vec3 lookVec = owner.getLookAngle().normalize();
                Vec3 toTargetVec = targetPos.subtract(ownerPos).normalize();
                double dotProduct = lookVec.dot(toTargetVec);
                double angle = Math.toDegrees(Math.acos(dotProduct));
                if (angle < 45.0) {
                    double score = 1.0 / distanceSq;
                    score *= (1.0 - (angle / 45.0));
                    if (score > bestScore) {
                        bestScore = score;
                        bestTarget = target;
                    }
                }
            }
        }
        return bestTarget;
    }


    private Vec3 getRandomPositionAroundTarget(LivingEntity target) {
        double radius = 10.0;
        double theta = Math.random() * 2 * Math.PI;
        double phi = Math.random() * Math.PI;
        double x = target.getX() + radius * Math.sin(phi) * Math.cos(theta);
        double y = target.getY() + radius * Math.sin(phi) * Math.sin(theta);
        double z = target.getZ() + radius * Math.cos(phi);

        return new Vec3(x, y, z);
    }

    private void destroyBlocksAroundEntity() {
        int radius = 5;
        BlockPos pos = this.blockPosition();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = pos.offset(x, y, z);
                    if (this.level().getBlockState(blockPos).getBlock() != Blocks.AIR) {
                        this.level().destroyBlock(blockPos, false);
                    }
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<SilverLightEntity> animationState) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
