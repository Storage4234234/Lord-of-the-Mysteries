package net.swimmingtuna.lotm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class StormSealEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(StormSealEntity.class, EntityDataSerializers.BOOLEAN);

    public StormSealEntity(EntityType<? extends StormSealEntity> entityType, Level level) {
        super(entityType, level);
    }

    public StormSealEntity(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.ROAR_ENTITY.get(), shooter, offsetX, offsetY, offsetZ, level);
    }

    @Override
    protected float getInertia() {
        return this.isDangerous() ? 1.0F : super.getInertia();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide()) {
            Entity entity = result.getEntity();
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);

            if (entity instanceof Projectile projectile) {
                float explosionRadius = 3 * scaleData.getScale();
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), explosionRadius, Level.ExplosionInteraction.TNT);
                projectile.discard();
            }
            if (entity instanceof LivingEntity livingEntity) {
                if (this.getOwner() != null && livingEntity != this.getOwner()) {
                    livingEntity.getPersistentData().putInt("inStormSeal", 3600);
                    livingEntity.getPersistentData().putInt("stormSealX", (int) livingEntity.getX());
                    livingEntity.getPersistentData().putInt("stormSealY", (int) livingEntity.getY());
                    livingEntity.getPersistentData().putInt("stormSealZ", (int) livingEntity.getZ());
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            float explosionRadius = 5 * scaleData.getScale();
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), explosionRadius, Level.ExplosionInteraction.TNT);
            this.discard();
        }
    }

    public boolean isPickable() {
        return false;
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_DANGEROUS, false);
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
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
        float radius = 3 * scaleData.getScale();
        if (!this.level().isClientSide()) {
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius * 0.75))) {
                if (livingEntity != null && livingEntity.getMaxHealth() >= 50 && livingEntity != this.getOwner() && (this.getOwner() instanceof LivingEntity living && !BeyonderUtil.isAllyOf(living, livingEntity))) {
                    livingEntity.getPersistentData().putInt("inStormSeal", 3600);
                    livingEntity.getPersistentData().putInt("stormSealX", (int) livingEntity.getX());
                    livingEntity.getPersistentData().putInt("stormSealY", (int) livingEntity.getY());
                    livingEntity.getPersistentData().putInt("stormSealZ", (int) livingEntity.getZ());
                    this.discard();
                }
            }
            for (int i = 0; i < 36; i++) {
                double angle = i * 10 * Math.PI / 180;
                double x = this.getX() + radius * Math.cos(angle);
                double z = this.getZ() + radius * Math.sin(angle);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, this.getY(),z, 0, 0,0,0, 1);
                }
            }

            for (int i = 0; i < 4; i++) {
                double angle = i * 90 * Math.PI / 180;
                double x = this.getX() + radius * Math.cos(angle);
                double z = this.getZ() + radius * Math.sin(angle);
                BlockPos strikePos = new BlockPos((int) x, (int) this.getY(), (int) z);
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this.level());
                if (lightningBolt != null) {
                    lightningBolt.moveTo(Vec3.atBottomCenterOf(strikePos));
                    lightningBolt.setVisualOnly(false);
                    lightningBolt.setCause(null);
                    this.level().addFreshEntity(lightningBolt);
                }
            }
            if (this.tickCount >= 300) {
                this.discard();
            }
        }
    }
}
