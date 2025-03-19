package net.swimmingtuna.lotm.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
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
    private static final EntityDataAccessor<Boolean> SHOULD_TELEPORT = SynchedEntityData.defineId(SilverLightEntity.class, EntityDataSerializers.BOOLEAN);
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
                livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner()), scale * 25);
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (!this.level().isClientSide()) {
            if (!getShouldTeleport() && this.tickCount >= 5) {
                BeyonderUtil.destroyBlocksInSphere(this,pResult.getBlockPos(), (int) ((int) ScaleTypes.BASE.getScaleData(this).getScale() * 1.2), 15 );
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
        this.entityData.define(SHOULD_TELEPORT, true);
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
        if (tag.contains("should_teleport)")) {
            this.entityData.set(SHOULD_TELEPORT, tag.getBoolean("should_teleport"));
        }
    }


    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public void setYaw(float yaw) {
        this.entityData.set(YAW, yaw);
    }

    public boolean getShouldTeleport() {
        return this.entityData.get(SHOULD_TELEPORT);
    }

    public void setShouldTeleport(boolean shouldTeleport) {
        this.entityData.set(SHOULD_TELEPORT, shouldTeleport);
    }

    public float getPitch() {
        return this.entityData.get(PITCH);
    }

    public void setPitch(float pitch) {
        this.entityData.set(PITCH, pitch);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("pitch", this.entityData.get(PITCH));
        tag.putBoolean("should_teleport", this.entityData.get(SHOULD_TELEPORT));
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
            if (this.tickCount >= 30 && !this.getPersistentData().getBoolean("hasTeleported") && getShouldTeleport()) {
                this.getPersistentData().putBoolean("hasTeleported", true);
                LivingEntity target;
                Vec3 eyePosition = owner.getEyePosition();
                Vec3 lookVector = owner.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * 80, lookVector.y * 80, lookVector.z * 80);
                AABB searchBox = owner.getBoundingBox().inflate(80);
                EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(owner.level(), owner, eyePosition, reachVector, searchBox, entity -> !entity.isSpectator() && entity.isPickable(), 0.0f);
                if (entityHit != null && entityHit.getEntity() instanceof LivingEntity living) {
                    target = living;
                } else {
                    target = findTargetInSight(owner);
                }
                if (target != null) {
                    Vec3 teleportPos = getRandomPositionInSphere(target, 20.0);
                    this.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
                    Vec3 targetPos = target.position();
                    Vec3 direction = targetPos.subtract(teleportPos).normalize();
                    double speed = 6.0;
                    this.setDeltaMovement(direction.scale(speed));
                }
            }
            if (this.tickCount >= 80) {
                this.discard();
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                int chunkRadius = 5;
                ChunkPos centerChunk = new ChunkPos(this.blockPosition());
                for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                    for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                        ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                        serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, chunkPos, 3, chunkPos);
                    }
                }
            }
            LOTMNetworkHandler.sendToAllPlayers(new UpdateEntityLocationS2C(this.getX(), this.getY(), this.getZ(), this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z(), this.getId()));
        }
        if (!level().isClientSide()) {
            LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleInit.FLASH_PARTICLE.get(), this.getX(), this.getY(), this.getZ(), 0, 0, 0));
            Vec3 motion = this.getDeltaMovement();
            double horizontalDist = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float newYaw = (float) Math.toDegrees(Math.atan2(motion.z, motion.x));
            float newPitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontalDist));
            this.setYaw(newYaw);
            this.setPitch(newPitch);
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            float scale = scaleData.getScale();
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(scale * 0.8f))) {
                if (this.getOwner() instanceof LivingEntity owner && livingEntity != owner) {
                    livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner()), scale * 25);
                    this.discard();
                }
            }
        }
    }

    public static LivingEntity findTargetInSight(LivingEntity owner) {
        List<LivingEntity> potentialTargets = owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(80));
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

    private Vec3 getRandomPositionInSphere(LivingEntity target, double radius) {
        Level level = target.level();
        Vec3 targetPos = target.position();
        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) targetPos.x, (int) targetPos.z);
        Vec3 surfacePos = new Vec3(targetPos.x, surfaceHeight, targetPos.z);
        double theta = Math.random() * 2 * Math.PI;
        double phi = Math.acos(2 * Math.random() - 1);
        double r = radius * Math.cbrt(Math.random());
        double x = surfacePos.x + r * Math.sin(phi) * Math.cos(theta);
        double y = surfacePos.y + r * Math.sin(phi) * Math.sin(theta);
        double z = surfacePos.z + r * Math.cos(phi);
        int newSurfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z);
        if (Math.abs(newSurfaceHeight - target.getY()) > 20) {
            return new Vec3(x, y, z);
        } else {
            return new Vec3(x, newSurfaceHeight, z);
        }
    }


    private void destroyBlocksAroundEntityDropBlocks(int radius) {
        BlockPos pos = this.blockPosition();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = pos.offset(x, y, z);
                    if (this.level().getBlockState(blockPos).getBlock() != Blocks.AIR) {
                        this.level().destroyBlock(blockPos, true);
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
