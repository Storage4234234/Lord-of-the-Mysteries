package net.swimmingtuna.lotm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.init.EntityInit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class GuardianBoxEntity extends Entity {
    private static final EntityDataAccessor<Integer> MAX_HEALTH = SynchedEntityData.defineId(GuardianBoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_SIZE = SynchedEntityData.defineId(GuardianBoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(GuardianBoxEntity.class, EntityDataSerializers.OPTIONAL_UUID);


    public GuardianBoxEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public GuardianBoxEntity(Level level, double x, double y, double z, float maxRadius) {
        this(EntityInit.GUARDIAN_BOX_ENTITY.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MAX_HEALTH, 100);
        this.entityData.define(MAX_SIZE, 10);
        this.entityData.define(OWNER_UUID, Optional.empty());

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

    public int getMaxHealth() {
        return this.entityData.get(MAX_HEALTH);
    }

    public void setMaxHealth(int maxHealth) {
        this.entityData.set(MAX_HEALTH, maxHealth);
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(ownerUUID));
    }

    public int getMaxSize() {
        return this.entityData.get(MAX_SIZE);
    }

    public void setMaxSize(int maxSize) {
        this.entityData.set(MAX_SIZE, maxSize);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("maxHealth")) {
            this.setMaxHealth(compound.getInt("maxHealth"));
        }
        if (compound.contains("maxSize")) {
            this.setMaxSize(compound.getInt("maxSize"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("maxHealth", this.getMaxHealth());

    }
}