package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.init.EntityInit;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TwilightLightEntity extends Entity {

    private static final EntityDataAccessor<Integer> MAX_LIFETIME = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CURRENT_X = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CURRENT_Z = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VELOCITY_X = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VELOCITY_Z = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_RADIUS = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BOTTOM_Y_OFFSET = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> NEXT_Y_UPDATE = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TWILIGHT = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DIVISION_AMOUNT = SynchedEntityData.defineId(TwilightLightEntity.class, EntityDataSerializers.INT);

    private static final Random random = new Random();
    private static final float SPEED = 1F;
    private static final float MIN_Y_OFFSET = -50.0F;
    private static final float MAX_Y_OFFSET = -10.0F;
    private static final float INITIAL_RADIUS = 5.0F;
    private double originX;
    private double originZ;

    public TwilightLightEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public TwilightLightEntity(Level level, double x, double y, double z, float maxRadius) {
        this(EntityInit.TWILIGHT_LIGHT.get(), level);
        this.setPos(x, y, z);
        this.originX = x;
        this.originZ = z;
        this.entityData.set(MAX_RADIUS, maxRadius);
        initializeMovement();
        updateBottomYOffset();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MAX_LIFETIME, 500);
        float initialX = (random.nextFloat() * 2 - 1) * INITIAL_RADIUS;
        float initialZ = (random.nextFloat() * 2 - 1) * INITIAL_RADIUS;
        this.entityData.define(CURRENT_X, initialX);
        this.entityData.define(CURRENT_Z, initialZ);
        this.entityData.define(VELOCITY_X, 0.0F);
        this.entityData.define(VELOCITY_Z, 0.0F);
        this.entityData.define(MAX_RADIUS, 50.0F);
        this.entityData.define(TWILIGHT, false);
        this.entityData.define(BOTTOM_Y_OFFSET, MIN_Y_OFFSET);
        this.entityData.define(NEXT_Y_UPDATE, 20);
        this.entityData.define(DIVISION_AMOUNT, 1);
    }

    private void initializeMovement() {
        float initialX = (random.nextFloat() * 2 - 1) * INITIAL_RADIUS;
        float initialZ = (random.nextFloat() * 2 - 1) * INITIAL_RADIUS;
        setCurrentPosition(initialX, initialZ);
        double angle = random.nextDouble() * 2 * Math.PI;
        float velocityX = (float) (Math.cos(angle) * SPEED);
        float velocityZ = (float) (Math.sin(angle) * SPEED);
        this.entityData.set(VELOCITY_X, velocityX);
        this.entityData.set(VELOCITY_Z, velocityZ);
    }

    private void updateBottomYOffset() {
        float newOffset = MIN_Y_OFFSET + random.nextFloat() * (MAX_Y_OFFSET - MIN_Y_OFFSET);
        this.entityData.set(BOTTOM_Y_OFFSET, newOffset);
        this.entityData.set(NEXT_Y_UPDATE, this.tickCount + (20 + random.nextInt(100)));
    }

    private void updateMovement() {
        float currentX = getCurrentX();
        float currentZ = getCurrentZ();
        float velocityX = getVelocityX();
        float velocityZ = getVelocityZ();
        float maxRadius = getMaxRadius();
        double distance = Math.sqrt(currentX * currentX + currentZ * currentZ);
        if (distance + SPEED > maxRadius / getDivisionAmount()) {
            double angle = Math.atan2(currentZ, currentX);
            double normalAngle = angle;
            double newVelocityAngle;
            float minDistance = maxRadius * 0.35f;
            int attempts = 0;
            float newX;
            float newZ;
            do {
                double velocityAngle = Math.atan2(velocityZ, velocityX);
                newVelocityAngle = 2 * normalAngle - velocityAngle + Math.PI;
                newVelocityAngle += (random.nextFloat() - 0.5f) * 0.5f;

                newX = currentX + (float) (Math.cos(newVelocityAngle) * minDistance);
                newZ = currentZ + (float) (Math.sin(newVelocityAngle) * minDistance);

                attempts++;
                if (attempts >= 10) break;

            } while (Math.sqrt(newX * newX + newZ * newZ) > maxRadius);
            velocityX = (float) (Math.cos(newVelocityAngle) * SPEED);
            velocityZ = (float) (Math.sin(newVelocityAngle) * SPEED);
            this.entityData.set(VELOCITY_X, velocityX);
            this.entityData.set(VELOCITY_Z, velocityZ);
        }

        setCurrentPosition(currentX + velocityX, currentZ + velocityZ);
    }

    @Override
    public void tick() {
        if (this.tickCount == 1) {
            this.setPos(this.getX(), this.getY(), this.getZ());
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

        super.tick();

        updateMovement();

        if (this.tickCount >= this.entityData.get(NEXT_Y_UPDATE)) {
            updateBottomYOffset();
        }

        if (!level().isClientSide) {
            if (this.tickCount >= getMaxLifetime()) {
                this.discard();
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return pDistance < 400000; // 128 blocks squared
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(
                this.getX() - 300,
                this.getY() - 300,
                this.getZ() - 300,
                this.getX() + 300,
                this.getY() + 300,
                this.getZ() + 300
        );
    }

    public float getMaxRadius() {
        return this.entityData.get(MAX_RADIUS);
    }

    public void setMaxRadius(float radius) {
        this.entityData.set(MAX_RADIUS, radius);
    }

    public float getCurrentX() {
        return this.entityData.get(CURRENT_X);
    }

    public float getCurrentZ() {
        return this.entityData.get(CURRENT_Z);
    }

    public float getVelocityX() {
        return this.entityData.get(VELOCITY_X);
    }

    public float getVelocityZ() {
        return this.entityData.get(VELOCITY_Z);
    }

    private void setCurrentPosition(float x, float z) {
        this.entityData.set(CURRENT_X, x);
        this.entityData.set(CURRENT_Z, z);
    }

    public int getMaxLifetime() {
        return this.entityData.get(MAX_LIFETIME);
    }

    public void setMaxLifetime(int lifetime) {
        this.entityData.set(MAX_LIFETIME, lifetime);
    }

    public int getDivisionAmount() {
        return this.entityData.get(DIVISION_AMOUNT);
    }

    public void setDivisionAmount(int divisionAmount) {
        this.entityData.set(DIVISION_AMOUNT, divisionAmount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("maxLifetime")) {
            this.setMaxLifetime(compound.getInt("maxLifetime"));
        }
        if (compound.contains("currentX")) {
            this.entityData.set(CURRENT_X, compound.getFloat("currentX"));
        }
        if (compound.contains("currentZ")) {
            this.entityData.set(CURRENT_Z, compound.getFloat("currentZ"));
        }
        if (compound.contains("velocityX")) {
            this.entityData.set(VELOCITY_X, compound.getFloat("velocityX"));
        }
        if (compound.contains("velocityZ")) {
            this.entityData.set(VELOCITY_Z, compound.getFloat("velocityZ"));
        }
        if (compound.contains("originX")) {
            this.originX = compound.getDouble("originX");
        }
        if (compound.contains("originZ")) {
            this.originZ = compound.getDouble("originZ");
        }
        if (compound.contains("bottomYOffset")) {
            this.entityData.set(BOTTOM_Y_OFFSET, compound.getFloat("bottomYOffset"));
        }
        if (compound.contains("nextYUpdate")) {
            this.entityData.set(NEXT_Y_UPDATE, compound.getInt("nextYUpdate"));
        }
        if (compound.contains("divisionAmount")) {
            this.entityData.set(DIVISION_AMOUNT, compound.getInt("divisionAmount"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("maxLifetime", this.getMaxLifetime());
        compound.putFloat("currentX", this.getCurrentX());
        compound.putFloat("currentZ", this.getCurrentZ());
        compound.putFloat("velocityX", this.getVelocityX());
        compound.putFloat("velocityZ", this.getVelocityZ());
        compound.putDouble("originX", this.originX);
        compound.putDouble("originZ", this.originZ);
        compound.putFloat("bottomYOffset", this.entityData.get(BOTTOM_Y_OFFSET));
        compound.putInt("nextYUpdate", this.entityData.get(NEXT_Y_UPDATE));
        compound.putInt("divisionAmount", this.entityData.get(DIVISION_AMOUNT));
    }
}