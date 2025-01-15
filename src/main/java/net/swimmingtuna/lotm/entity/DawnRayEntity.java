package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;

import java.util.List;

public class DawnRayEntity extends Entity {
    private static final EntityDataAccessor<Integer> MAX_LIFETIME = SynchedEntityData.defineId(DawnRayEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ANGLE = SynchedEntityData.defineId(DawnRayEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> ROTATION_SPEED = SynchedEntityData.defineId(DawnRayEntity.class, EntityDataSerializers.INT);
    private double originX;
    private double originZ;

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MAX_LIFETIME, 500);
        this.entityData.define(ANGLE, 0.0F);
        this.entityData.define(ROTATION_SPEED, 5); // Default rotation speed
    }

    public DawnRayEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public DawnRayEntity(Level level, double x, double y, double z) {
        this(EntityInit.DAWN_RAY_ENTITY.get(), level);
        this.setPos(x, y, z);
        this.originX = x;
        this.originZ = z;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(
                this.getX() - 50, this.getY() - 50, this.getZ() - 50,
                this.getX() + 50, this.getY() + 50, this.getZ() + 50
        );
    }

    private AABB getAngledBoundingBox() {
        // Calculate the ray's direction vector based on current angle
        double rayDirX = Math.cos(Math.toRadians(getAngle()));
        double rayDirZ = Math.sin(Math.toRadians(getAngle()));

        double length = 50.0; // Length of the beam
        double width = 0.25;

        // The bottom point moves in an arc while the top stays fixed
        double bottomX = this.getX() + rayDirX * length;
        double bottomZ = this.getZ() + rayDirZ * length;

        return new AABB(
                Math.min(this.getX() - width, bottomX - width),
                this.getY() - length, // Bottom point is length units below
                Math.min(this.getZ() - width, bottomZ - width),
                Math.max(this.getX() + width, bottomX + width),
                this.getY(), // Top point stays at entity's Y
                Math.max(this.getZ() + width, bottomZ + width)
        );
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("maxLifetime")) {
            this.setMaxLifetime(compound.getInt("maxLifetime"));
        }
        if (compound.contains("angle")) {
            this.setAngle(compound.getFloat("angle"));
        }
        if (compound.contains("rotationSpeed")) {
            this.setRotationSpeed(compound.getInt("rotationSpeed"));
        }
        if (compound.contains("originX")) {
            this.originX = compound.getDouble("originX");
        }
        if (compound.contains("originZ")) {
            this.originZ = compound.getDouble("originZ");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("maxLifetime", this.getMaxLifetime());
        compound.putFloat("angle", this.getAngle());
        compound.putInt("rotationSpeed", this.getRotationSpeed());
        compound.putDouble("originX", this.originX);
        compound.putDouble("originZ", this.originZ);
    }
    @Override
    public void tick() {
        super.tick();

        // Update rotation on both client and server
        float currentAngle = getAngle(); // Use the stored angle
        currentAngle += getRotationSpeed(); // Increment angle by rotation speed
        if (currentAngle >= 360) {
            currentAngle -= 360; // Keep angle within 0-360
        }

        setAngle(currentAngle); // Store the updated angle

        // Ensure visual rotation reflects the current angle
        setYRot(currentAngle);

        if (!level().isClientSide) {
            if (this.tickCount >= getMaxLifetime()) {
                this.discard();
                return;
            }

            // Debug: Print angle and rotation for verification
            System.out.println("Current Angle: " + currentAngle);
            System.out.println("Y-Rotation: " + this.getYRot());

            // Calculate the bottom point of the beam (arc movement)
            double rayDirX = Math.cos(Math.toRadians(currentAngle));
            double rayDirZ = Math.sin(Math.toRadians(currentAngle));
            double length = 50.0;

            double bottomX = this.originX + rayDirX * length;
            double bottomZ = this.originZ + rayDirZ * length;

            // Define hitbox for the beam
            double width = 0.25;
            AABB hitBox = new AABB(
                    Math.min(this.originX - width, bottomX - width),
                    this.getY() - length,
                    Math.min(this.originZ - width, bottomZ - width),
                    Math.max(this.originX + width, bottomX + width),
                    this.getY(),
                    Math.max(this.originZ + width, bottomZ + width)
            );

            // Find entities within the hitbox
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, hitBox);
            Vec3 beamStart = this.position();
            Vec3 beamEnd = new Vec3(bottomX, this.getY() - length, bottomZ);
            Vec3 beamDir = beamEnd.subtract(beamStart).normalize();

            for (LivingEntity entity : entities) {
                Vec3 toEntity = entity.position().subtract(beamStart);
                double dot = toEntity.dot(beamDir);
                if (dot > 0 && dot < length) {
                    Vec3 projection = beamStart.add(beamDir.scale(dot));
                    if (projection.distanceTo(entity.position()) < width * 2) {
                        entity.hurt(this.damageSources().magic(), 2.0F);
                    }
                }
            }
        }
    }

    public int getMaxLifetime() {
        return this.entityData.get(MAX_LIFETIME);
    }

    public void setMaxLifetime(int lifetime) {
        this.entityData.set(MAX_LIFETIME, lifetime);
    }

    public float getAngle() {
        return this.entityData.get(ANGLE);
    }

    public void setAngle(float angle) {
        this.entityData.set(ANGLE, angle);
    }

    public int getRotationSpeed() {
        return this.entityData.get(ROTATION_SPEED);
    }

    public void setRotationSpeed(int speed) {
        this.entityData.set(ROTATION_SPEED, speed);
    }
}