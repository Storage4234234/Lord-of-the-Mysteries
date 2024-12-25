package net.swimmingtuna.lotm.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;
import java.util.Random;

public class LightningBallEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(LightningBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SUMMONED = SynchedEntityData.defineId(LightningBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> X_ROT = SynchedEntityData.defineId(LightningBallEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> Y_ROT = SynchedEntityData.defineId(LightningBallEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> ABSORB = SynchedEntityData.defineId(LightningBallEntity.class, EntityDataSerializers.BOOLEAN);


    public LightningBallEntity(EntityType<? extends LightningBallEntity> entityType, Level level, boolean absorb) {
        super(entityType, level);
        this.setAbsorbed(absorb);

    }

    public LightningBallEntity(EntityType<LightningBallEntity> lightningBallEntityEntityType, Level level) {
        super(lightningBallEntityEntityType, level);
    }

    public boolean isOnFire() {
        return false;
    }

    protected float getInertia() {
        return 0.99F;
    }


    @Override
    public void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {
            if (this.tickCount >= 50) {
                ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
                float scale = scaleData.getScale();
                this.setDeltaMovement(this.getDeltaMovement().x * 0.5, this.getDeltaMovement().y * 0.5f, this.getDeltaMovement().z * 0.5f);
                this.getPersistentData().putInt("lightningRadiusCounter", (int) (scale * 2));
                this.getPersistentData().putBoolean("isExploding", true);
            }
        }
    }

    @Override
    public void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide()) {
            if (this.tickCount >= 50) {
                Entity hitEntity = result.getEntity();
                ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
                float scale = scaleData.getScale();
                this.setDeltaMovement(this.getDeltaMovement().x * 0.2, this.getDeltaMovement().y * 0.2, this.getDeltaMovement().z * 0.2);
                this.getPersistentData().putInt("lightningRadiusCounter", (int) (scale * 2));
                this.getPersistentData().putBoolean("isExploding", true);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Summoned", this.getSummoned());
        compound.putBoolean("Absorbed", this.getAbsorb());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSummoned(compound.getBoolean("Summoned"));
        this.setAbsorbed(compound.getBoolean("Absorbed"));
        this.setBallXRot(compound.contains("xxRot") ? compound.getFloat("xxRot") : 0.0f);
        this.setBallYRot(compound.contains("yyRot") ? compound.getFloat("yyRot") : 0.0f);
    }


    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DANGEROUS, true);
        this.entityData.define(ABSORB, false);
        this.entityData.define(SUMMONED, false);
        this.entityData.define(X_ROT, 0.0f);
        this.entityData.define(Y_ROT, 0.0f);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }


    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (entity == this.getOwner()) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        boolean x = getSummoned();
        boolean y = getAbsorb();
        LivingEntity owner = (LivingEntity) this.getOwner();
        if (x) {
            this.setXRot(this.getXRot() + getBallXRot());
            this.setYRot(this.getYRot() + getBallYRot());
            if (!this.level().isClientSide()) {
                if (owner != null) {
                    if (this.tickCount <= 40) {
                        ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
                        this.teleportTo(owner.getX(), owner.getY() + scaleData.getScale() * 2, owner.getZ());
                    }
                    if (this.tickCount == 41) {
                        this.setDeltaMovement(owner.getLookAngle().scale(3.0f));
                        this.hurtMarked = true;
                    }
                }
            }
        }
        if (!this.level().isClientSide() && y && this.tickCount <= 40 && owner != null) {
            Vec3 playerPos = new Vec3(owner.getX(), owner.getY(), owner.getZ());
            for (Entity entity : this.level().getEntitiesOfClass(Entity.class, this.getBoundingBox().inflate(100))) {
                if (entity instanceof LightningEntity lightningEntity) {
                    if (lightningEntity.getSpeed() != 10.5f && lightningEntity.getLastPos() != null) {
                        Vec3 direction = this.position().subtract(lightningEntity.getLastPos());
                        lightningEntity.discard();
                        LightningEntity lightning = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), this.level());
                        BlockPos pos = BlockPos.containing(lightningEntity.getLastPos());
                        lightning.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                        direction = direction.normalize();
                        lightning.setDeltaMovement(direction);
                        lightning.hurtMarked = true;
                        lightning.setSpeed(10.5f);
                        lightning.setOwner(owner);
                        lightning.setOwner(owner);
                        lightning.setMaxLength(30);
                        this.level().addFreshEntity(lightning);
                    }
                    if (lightningEntity.getLastPos() != null && lightningEntity.getLastPos().distanceToSqr(this.getX(), this.getY(), this.getZ()) <= 250) {
                        lightningEntity.discard();
                        ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
                        if (scaleData.getScale() <= 50) {
                            scaleData.setScale(scaleData.getScale() + 0.2f);
                        }
                    }
                }
                if (entity instanceof LightningBolt lightningBolt) {
                    lightningBolt.discard();

                    LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, this.level());
                    lightning.teleportTo(this.getX(), this.getY(), this.getZ());
                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
                    if (scaleData.getScale() <= 50) {
                        scaleData.setScale(scaleData.getScale() + 0.3f);
                        scaleData.markForSync(true);
                    }
                }
            }
        }
        this.xRotO = getXRot();
        this.yRotO = this.getYRot();
        if (!this.level().isClientSide) {
            int lightningArea = this.getPersistentData().getInt("lightningRadiusCounter");
            boolean isExploding = this.getPersistentData().getBoolean("isExploding");
            float scale = ScaleTypes.BASE.getScaleData(this).getScale();
            if (isExploding) {
                if (lightningArea <= 1) {
                    explodeLightningBallBlock(this.getOnPos(), Math.min(50,2 * scale), scale); // Adjust as needed for final explosion
                    this.discard();
                }
                BlockPos centerPos = this.blockPosition();
                int currentRadius = lightningArea / 5; // Base radius
                spawnRandomLightning(centerPos, currentRadius, lightningArea);
                this.getPersistentData().putInt("lightningRadiusCounter", lightningArea - 1);
                System.out.println("Current lightning radius counter: " + lightningArea);
            }
        }
    }

    public boolean getSummoned() {
        return this.entityData.get(SUMMONED);
    }

    public float getBallXRot() {
        return this.entityData.get(X_ROT);
    }

    public float getBallYRot() {
        return this.entityData.get(Y_ROT);
    }

    public void setSummoned(boolean summoned) {
        this.entityData.set(SUMMONED, summoned);
    }

    public void setBallXRot(float xRot) {
        this.entityData.set(X_ROT, xRot);
    }

    public void setBallYRot(float yRot) {
        this.entityData.set(Y_ROT, yRot);
    }

    public boolean getAbsorb() {
        return this.entityData.get(ABSORB);
    }

    public void setAbsorbed(boolean absorbed) {
        this.entityData.set(ABSORB, absorbed);
    }

    public void explodeLightningBallEntity(LivingEntity hitEntity, float scale) {
        BlockPos hitPos = hitEntity.blockPosition();
        double radius = scale * 2;
        for (BlockPos pos : BlockPos.betweenClosed(
                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                hitPos.offset((int) radius, (int) radius, (int) radius))) {
            if (pos.distSqr(hitPos) <= radius * radius) {
                if (this.level().getBlockState(pos).getDestroySpeed(this.level(), pos) >= 0) {
                    this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        List<Entity> entities = this.level().getEntities(this,
                new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                        hitPos.offset((int) radius, (int) radius, (int) radius)));
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(BeyonderUtil.genericSource(this), 10 * scale);
            }
        }
    }

    public void explodeLightningBallBlock(BlockPos hitPos, double radius, float scale) {
        for (BlockPos pos : BlockPos.betweenClosed(
                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                hitPos.offset((int) radius, (int) radius, (int) radius))) {
            if (pos.distSqr(hitPos) <= radius * radius) {
                if (this.level().getBlockState(pos).getDestroySpeed(this.level(), pos) >= 0) {
                    this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        List<Entity> entities = this.level().getEntities(this,
                new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                        hitPos.offset((int) radius, (int) radius, (int) radius)));

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(BeyonderUtil.genericSource(this), 10 * scale);
            }
        }
    }


    private void spawnRandomLightning(BlockPos center, int radius, int lightningArea) {
        int adjustedRadius = radius + Math.max(0, 50 - lightningArea);
        for (int i = 0; i < 4; i++) {
            double angle = this.random.nextDouble() * 2 * Math.PI;
            double distance = this.random.nextDouble() * adjustedRadius;
            int x = center.getX() + (int) (Math.cos(angle) * distance);
            int z = center.getZ() + (int) (Math.sin(angle) * distance);
            BlockPos lightningPos = new BlockPos(x, center.getY(), z);
            while (this.level().isEmptyBlock(lightningPos) && lightningPos.getY() > this.level().getMinBuildHeight()) {
                lightningPos = lightningPos.below();
            }
            lightningPos = lightningPos.above();
            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, this.level());
            Random random = new Random();
            if (random.nextInt(20) == 0) {
                List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(adjustedRadius));
                if (!entities.isEmpty() && this.random.nextInt(20) == 0) {
                    LivingEntity randomEntity = entities.get(this.random.nextInt(entities.size()));
                    lightningBolt.moveTo(randomEntity.getOnPos().getCenter());
                } else {
                    lightningBolt.moveTo(Vec3.atBottomCenterOf(lightningPos));
                }
            }
            lightningBolt.moveTo(Vec3.atBottomCenterOf(lightningPos));
            lightningBolt.setVisualOnly(false);
            lightningBolt.setDamage(15);
            this.level().addFreshEntity(lightningBolt);
            if (this.level().getBlockState(lightningPos).getDestroySpeed(this.level(), lightningPos) >= 0) {
                this.level().setBlock(lightningPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
