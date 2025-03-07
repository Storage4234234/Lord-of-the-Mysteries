package net.swimmingtuna.lotm.entity;


import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendDustParticleS2C;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleTypes;

public class MercuryEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> HARM_TIME = SynchedEntityData.defineId(MercuryEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPEED = SynchedEntityData.defineId(MercuryEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(MercuryEntity.class, EntityDataSerializers.INT);

    private LivingEntity target = null;

    public MercuryEntity(EntityType<? extends MercuryEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SPEED, 1);
        this.entityData.define(HARM_TIME, 50);
        this.entityData.define(LIFETIME, 100);
    }

    public boolean isDangerous() {
        return true;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }


    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getPersistentData().putInt("mercuryLiqueficationTrapped", getHarmtime());
        }
    }


    @Override
    public void tick() {
        super.tick();

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        float scale = ScaleTypes.BASE.getScaleData(this).getScale();
        int particleCount = Math.max(5, (int) (5 * Math.sqrt(scale)));
        if (!this.level().isClientSide()) {
            for (int i = 0; i < particleCount; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.5 * scale;
                double offsetY = (random.nextDouble() - 0.5) * 0.5 * scale;
                double offsetZ = (random.nextDouble() - 0.5) * 0.5 * scale;
                double posX = this.getX() + offsetX;
                double posY = this.getY() + offsetY + (0.5 * scale);
                double posZ = this.getZ() + offsetZ;
                double motionX = (-this.getDeltaMovement().x * 0.1 + (random.nextDouble() - 0.5) * 0.02) * Math.sqrt(scale);
                double motionY = (0.05 + (random.nextDouble() - 0.5) * 0.02) * Math.sqrt(scale);
                double motionZ = (-this.getDeltaMovement().z * 0.1 + (random.nextDouble() - 0.5) * 0.02) * Math.sqrt(scale);
                LOTMNetworkHandler.sendToAllPlayers(new SendDustParticleS2C(0.75f, 0.75f, 0.75f, scale, posX, posY, posZ, motionX, motionY, motionZ));
            }
        }
        if (target != null) {
            double dx = target.getX() - this.getX();
            double dy = target.getY() - this.getY();
            double dz = target.getZ() - this.getZ();
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            this.setDeltaMovement((dx / length) * getSpeed(), (dy / length) * getSpeed(), (dz / length) * getSpeed());
            this.hurtMarked = true;
        }
        if (this.tickCount >= getLifetime()) {
            this.discard();
        }
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(LivingEntity targetToSet) {
        target = targetToSet;
    }

    public int getLifetime() {
        return this.entityData.get(LIFETIME);
    }

    public void setLifetime(int lifetime) {
        this.entityData.set(LIFETIME, lifetime);
    }

    public int getSpeed() {
        return this.entityData.get(SPEED);
    }

    public void setSpeed(int speed) {
        this.entityData.set(SPEED, speed);
    }

    public int getHarmtime() {
        return this.entityData.get(HARM_TIME);
    }

    public void setHarmTime(int harmTime) {
        this.entityData.set(HARM_TIME, harmTime);
    }
}
