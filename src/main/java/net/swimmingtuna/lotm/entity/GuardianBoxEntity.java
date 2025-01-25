package net.swimmingtuna.lotm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.UpdateEntityLocationS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Optional;
import java.util.UUID;

public class GuardianBoxEntity extends Entity {
    private static final EntityDataAccessor<Integer> MAX_HEALTH = SynchedEntityData.defineId(GuardianBoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DAMAGE = SynchedEntityData.defineId(GuardianBoxEntity.class, EntityDataSerializers.INT);
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
        this.entityData.define(DAMAGE, 0);
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

    public int getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setDamage(int damage) {
        this.entityData.set(MAX_HEALTH, damage);
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
        if (compound.contains("damage")) {
            this.setDamage(compound.getInt("damage"));
        }
        if (compound.contains("ownerUUID")) {
            this.setUUID(compound.getUUID("ownedUUID"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("maxHealth", this.getMaxHealth());
        compound.putInt("damage", this.getDamage());
        compound.putInt("maxSize", this.getMaxSize());
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            float damage = getDamage();
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            this.setMaxSize((int) scaleData.getScale());
            UUID ownerUUID = this.getOwnerUUID().orElse(null);
            if (ownerUUID != null) {
                LivingEntity owner = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(150), entity -> entity.getUUID().equals(ownerUUID)).stream().findFirst().orElse(null);
                if (owner != null) {
                    for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(getMaxSize() + 5))) {
                        if (BeyonderUtil.isAllyOf(owner, livingEntity)) {
                            if (livingEntity != owner) {
                                livingEntity.getPersistentData().putUUID("guardianProtection", ownerUUID);
                                livingEntity.getPersistentData().putInt("guardianProtectionTimer", 10);
                            }
                        } else {
                            if (livingEntity != owner && this.tickCount % 10 == 0) {
                                int sequence = BeyonderUtil.getSequence(livingEntity);
                                double x = livingEntity.getX() - this.getX();
                                double y = livingEntity.getY() - this.getY();
                                double z = livingEntity.getZ() - this.getZ();
                                double magnitude = Math.sqrt(x * x + y * y + z * z);
                                livingEntity.setDeltaMovement(x / magnitude * 4, y / magnitude * 4, z / magnitude * 4);
                                livingEntity.hurtMarked = true;
                                int additionalDamage = (10 - sequence) * 2;
                                this.setDamage((int) (damage + additionalDamage));
                            }
                        }
                    }
                    for (Projectile projectile : this.level().getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(getMaxSize() + 5))) {
                        if (projectile.getOwner() == null || !projectile.getOwner().getUUID().equals(ownerUUID)) {
                            ScaleData pScaleData = ScaleTypes.BASE.getScaleData(projectile);
                            this.setDamage((int) (damage + (int) ((pScaleData.getScale() * 10) + projectile.getDeltaMovement().y() + projectile.getDeltaMovement().x() + projectile.getDeltaMovement().z())));
                            double x = projectile.getX() - this.getX();
                            double y = projectile.getY() - this.getY();
                            double z = projectile.getZ() - this.getZ();// Reversed direction for pushing away
                            double magnitude = Math.sqrt(x * x + y * y + z * z);
                            projectile.setDeltaMovement(x / magnitude * 4, y / magnitude * 4, z / magnitude * 4);
                            projectile.hurtMarked = true;
                        }
                    }
                } else if (this.tickCount >= 5) {
                    //this.discard();
                }
            }
            if (this.getDamage() >= this.getMaxHealth()) {
                this.discard();
            }
        }
    }
    public static void decrementGuardianTimer(LivingEntity entity) {
        if (entity.getPersistentData().getInt("guardianProtectionTimer") >= 1) {
            entity.getPersistentData().putInt("guardianProtectionTimer", entity.getPersistentData().getInt("guardianProtectionTimer") - 1);
        }
    }

    public static void guardianHurtEvent(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            if (entity.getPersistentData().getInt("guardianProtectionTimer") >= 1) {
                for (LivingEntity livingEntity : entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(100))) {
                    UUID uuid = entity.getPersistentData().getUUID("guardianProtection");
                    if (livingEntity.getUUID() == uuid) {
                        livingEntity.hurt(event.getSource(), event.getAmount() / 2);
                        event.setAmount(event.getAmount() / 2);
                    }
                }
            }
        }
    }
}