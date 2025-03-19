package net.swimmingtuna.lotm.entity;


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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
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

public class SpearOfDawnEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(SpearOfDawnEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(SpearOfDawnEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(SpearOfDawnEntity.class, EntityDataSerializers.BOOLEAN);

    public SpearOfDawnEntity(EntityType<? extends SpearOfDawnEntity> entityType, Level level) {
        super(entityType, level);
    }


    protected float getInertia() {
        return 1.0F;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (entity instanceof SpearOfDawnEntity) {
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
            if (this.tickCount >= 5) {
                BeyonderUtil.destroyBlocksInSphere(this, pResult.getBlockPos(), (int) ((int) ScaleTypes.BASE.getScaleData(this).getScale() * 1.01), 10 );
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
            Vec3 motion = this.getDeltaMovement();
            double horizontalDist = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float newYaw = (float) Math.toDegrees(Math.atan2(motion.z, motion.x));
            float newPitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontalDist));
            this.setYaw(newYaw);
            this.setPitch(newPitch);
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            float scale = scaleData.getScale();
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(scale * 0.6f))) {
                if (this.getOwner() instanceof LivingEntity owner && livingEntity != owner) {
                    livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner()), scale * 18);
                    this.discard();
                }
            }
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<SpearOfDawnEntity> animationState) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
