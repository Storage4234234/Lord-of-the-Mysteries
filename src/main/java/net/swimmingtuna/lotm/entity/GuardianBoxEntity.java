package net.swimmingtuna.lotm.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.init.EntityInit;
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
        this.noCulling = true;
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
        return true;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(this.getX() - 600, this.getY() - 600, this.getZ() - 600, this.getX() + 600, this.getY() + 600, this.getZ() + 600);
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
        this.entityData.set(DAMAGE, damage);
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
                LivingEntity owner = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(getMaxSize() + 3), entity -> entity.getUUID().equals(ownerUUID)).stream().findFirst().orElse(null);
                if (owner != null) {
                    if (owner.tickCount % 5 == 0 && owner instanceof Player pPlayer) {
                        pPlayer.displayClientMessage(Component.literal(this.getDamage() + " / " + this.getMaxHealth()).withStyle(ChatFormatting.YELLOW), true);
                    }
                    for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(getMaxSize() + 3))) {
                        if (BeyonderUtil.areAllies(owner, livingEntity)) {
                            //possible logic in the future
                        } else {
                            if (livingEntity != owner && this.tickCount % 10 == 0) {
                                int entitySequence = BeyonderUtil.getSequence(livingEntity);
                                if (entitySequence == -1) {
                                    entitySequence = 9;
                                }
                                int ownerSequence = BeyonderUtil.getSequence(owner);
                                int amountToSubtract = 50 - (entitySequence * 5);
                                if (this.tickCount >= 60 && this.tickCount % 20 == 0) {
                                    this.setDamage((this.getDamage() + amountToSubtract));
                                }
                                if (entitySequence >= ownerSequence) {
                                    double x = livingEntity.getX() - this.getX();
                                    double y = livingEntity.getY() - this.getY();
                                    double z = livingEntity.getZ() - this.getZ();
                                    double magnitude = Math.sqrt(x * x + y * y + z * z);
                                    livingEntity.setDeltaMovement(x / magnitude * 4, y / magnitude * 4, z / magnitude * 4);
                                    livingEntity.hurtMarked = true;
                                }
                            }
                        }
                        if (livingEntity == owner) {
                            if (owner.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                                owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, Math.min(4, owner.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1), true, true));
                            }
                            if (owner.hasEffect(MobEffects.REGENERATION)) {
                                owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, Math.min(5, owner.getEffect(MobEffects.REGENERATION).getAmplifier() + 1), true, true));
                            }
                        }
                    }
                    for (Projectile projectile : this.level().getEntitiesOfClass(Projectile.class, this.getBoundingBox().inflate(getMaxSize() + 3))) {
                        if (projectile.getOwner() == null || (projectile.getOwner() instanceof LivingEntity living && !BeyonderUtil.areAllies(owner, living) && !projectile.getOwner().getUUID().equals(ownerUUID))) {
                            int sequence = 900;
                            if (projectile.getOwner() instanceof LivingEntity living) {
                                sequence = BeyonderUtil.getSequence(living);
                            }
                            int ownerSequence = BeyonderUtil.getSequence(owner);
                            if (sequence >= ownerSequence + 2) {
                                ScaleData pScaleData = ScaleTypes.BASE.getScaleData(projectile);
                                if (this.tickCount >= 60) {
                                    this.setDamage((int) (damage + (int) ((pScaleData.getScale() * 10) + projectile.getDeltaMovement().y() + projectile.getDeltaMovement().x() + projectile.getDeltaMovement().z())));
                                }
                                double x = projectile.getX() - this.getX();
                                double y = projectile.getY() - this.getY();
                                double z = projectile.getZ() - this.getZ();// Reversed direction for pushing away
                                double magnitude = Math.sqrt(x * x + y * y + z * z);
                                projectile.setDeltaMovement(x / magnitude * 4, y / magnitude * 4, z / magnitude * 4);
                                projectile.hurtMarked = true;
                            }
                        }
                    }
                    if (this.tickCount == 100) {
                        double centerX = this.getX();
                        double centerY = this.getY();
                        double centerZ = this.getZ();
                        double teleportDistance = getMaxSize() + 10;
                        for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(getMaxSize() + 3))) {
                            if (!BeyonderUtil.areAllies(owner, livingEntity) && livingEntity != owner) {
                                double dx = livingEntity.getX() - centerX;
                                double dy = livingEntity.getY() - centerY;
                                double dz = livingEntity.getZ() - centerZ;
                                double magnitude = Math.sqrt(dx * dx + dy * dy + dz * dz);
                                double scale = (magnitude + teleportDistance) / magnitude;
                                double newX = centerX + dx * scale;
                                double newY = centerY + dy * scale;
                                double newZ = centerZ + dz * scale;
                                livingEntity.teleportTo(newX, newY, newZ);
                            }
                        }
                    }
                } else if (this.tickCount >= 5) {
                    this.discard();
                }
            }
            if (this.getDamage() >= this.getMaxHealth()) {
                this.discard();
            }
        }
    }


    public static void decrementGuardianTimer(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            if (entity.getPersistentData().getInt("guardianProtectionTimer") >= 1) {
                entity.getPersistentData().putInt("guardianProtectionTimer", entity.getPersistentData().getInt("guardianProtectionTimer") - 1);
            }
            if (entity.getPersistentData().getInt("divineHandGuarding") >= 1) {
                entity.getPersistentData().putInt("divineHandGuarding", entity.getPersistentData().getInt("divineHandGuarding") - 1);
            }
        }
    }

    public static void guardianHurtEvent(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.isCanceled() || entity.level().isClientSide()) {
            return;
        }
        if (entity.getPersistentData().getInt("guardianProtectionTimer") >= 1) {
            if (entity.getPersistentData().contains("guardianProtection")) {
                UUID guardianUUID = entity.getPersistentData().getUUID("guardianProtection");
                for (LivingEntity livingEntity : entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(100))) {
                    if (livingEntity.getUUID().equals(guardianUUID) && livingEntity != entity) {
                        livingEntity.hurt(event.getSource(), event.getAmount() / 2);
                        event.setAmount(event.getAmount() / 2);
                        break;
                    }
                }
            }
        }
        if (entity.getPersistentData().getInt("divineHandGuarding") >= 1) {
            if (entity.getPersistentData().contains("divineHandUUID")) {
                UUID divineHandUUID = entity.getPersistentData().getUUID("divineHandUUID");
                LivingEntity divineEntity = BeyonderUtil.getEntityFromUUID(entity.level(), divineHandUUID);
                if (divineEntity != null && divineEntity.isAlive() && divineEntity != entity && BeyonderUtil.areAllies(divineEntity, entity)) {
                    divineEntity.hurt(event.getSource(), event.getAmount());
                    event.setAmount(0);
                }
            }
        }
    }
}