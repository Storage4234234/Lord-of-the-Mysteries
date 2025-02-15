package net.swimmingtuna.lotm.entity;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HurricaneOfLightEntity extends AbstractHurtingProjectile {

    private static final EntityDataAccessor<Boolean> DESTROY_ARMOR = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_HURRICANE_RADIUS = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HURRICANE_HEIGHT = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFECOUNT = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> DATA_HURRICANE_MOV = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> RANDOM_MOVEMENT = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DESTROY_BLOCKS = SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int BLOCK_DESTROY_INTERVAL = 10;
    private static final int PARTICLE_UPDATE_INTERVAL = 2;
    private static final double PARTICLE_DENSITY_FACTOR = 0.75;

    public HurricaneOfLightEntity(EntityType<? extends HurricaneOfLightEntity> entityType, Level level) {
        super(entityType, level);
    }

    public HurricaneOfLightEntity(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.HURRICANE_OF_LIGHT_ENTITY.get(), shooter, offsetX, offsetY, offsetZ, level);
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DANGEROUS, false);
        this.entityData.define(DATA_LIFECOUNT, 300);
        this.entityData.define(DATA_HURRICANE_RADIUS, 4);
        this.entityData.define(DATA_HURRICANE_HEIGHT, 20);
        this.entityData.define(DATA_HURRICANE_MOV, new Vector3f());
        this.entityData.define(RANDOM_MOVEMENT, false);
        this.entityData.define(DESTROY_BLOCKS, false);
        this.entityData.define(DESTROY_ARMOR, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("HurricaneRandomMovement")) {
            this.setHurricaneRandom(compound.getBoolean("HurricaneRandomMovement"));
        }
        if (compound.contains("DestroyArmor")) {
            this.setDestroyArmor(compound.getBoolean("DestroyArmor"));
        }
        if (compound.contains("HurricanePickupBlocks")) {
            this.setHurricaneDestroy(compound.getBoolean("HurricanePickupBlocks"));
        }
        if (compound.contains("HurricaneRadius")) {
            this.setHurricaneRadius(compound.getInt("HurricaneRadius"));
        }
        if (compound.contains("HurricaneHeight")) {
            this.setHurricaneHeight(compound.getInt("HurricaneHeight"));
        }
        if (compound.contains("HurricaneLifeCount")) {
            this.setHurricaneLifecount(compound.getInt("HurricaneLifeCount"));
        }
        if (compound.contains("HurricaneMov")) {
            ExtraCodecs.VECTOR3F.decode(NbtOps.INSTANCE, compound.get("HurricaneMov")).result()
                    .ifPresent(HurricaneMovAndCompoundPair -> this.setHurricaneMov(HurricaneMovAndCompoundPair.getFirst()));
        }
    }

    public static void summonHurricaneOfLightWarrior(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            HurricaneOfLightEntity hurricaneOfLightEntity = new HurricaneOfLightEntity(livingEntity.level(), livingEntity, 0, 0, 0);
            hurricaneOfLightEntity.setHurricaneRadius((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFDAWN.get()));
            hurricaneOfLightEntity.setHurricaneHeight((int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFDAWN.get()) * 0.5f));
            hurricaneOfLightEntity.setHurricaneLifecount(300 - (sequence * 20));
            hurricaneOfLightEntity.setHurricaneDestroy(true);
            hurricaneOfLightEntity.setHurricaneMov(livingEntity.getLookAngle().scale(0.5f).toVector3f());
            livingEntity.level().addFreshEntity(hurricaneOfLightEntity);
        }
    }

    public static void summonHurricaneOfLightAngel(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            HurricaneOfLightEntity hurricaneOfLightEntity = new HurricaneOfLightEntity(livingEntity.level(), livingEntity, 0, 0, 0);
            hurricaneOfLightEntity.setHurricaneRadius((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFSILVER.get()));
            hurricaneOfLightEntity.setHurricaneHeight((int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SWORDOFSILVER.get()) * 0.5f));
            hurricaneOfLightEntity.setHurricaneLifecount(300 - (sequence * 20));
            hurricaneOfLightEntity.setHurricaneDestroy(true);
            hurricaneOfLightEntity.setHurricaneMov(livingEntity.getLookAngle().scale(0.75f).toVector3f());
            hurricaneOfLightEntity.setDestroyArmor(true);
            livingEntity.level().addFreshEntity(hurricaneOfLightEntity);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("HurricaneRadius", this.getHurricaneRadius());
        compound.putInt("HurricaneLifeCount", this.getHurricaneLifecount());
        compound.putInt("HurricaneHeight", this.getHurricaneHeight());
        compound.putBoolean("DestroyArmor", this.getDestroyArmor());
        DataResult<Tag> tagDataResult = ExtraCodecs.VECTOR3F.encodeStart(NbtOps.INSTANCE, this.getHurricaneMov());
        tagDataResult.result().ifPresent(tag -> compound.put("HurricaneMov", tag));
    }


    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {
            BlockPos hitPos = result.getBlockPos();
            BlockPos HurricanePos = this.blockPosition();
            if (hitPos.getY() == HurricanePos.getY() - 1 && hitPos.getX() == HurricanePos.getX() && hitPos.getZ() == HurricanePos.getZ()) {
                this.setPos(this.getX(), this.getY() + 1, this.getZ());
            }
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    public void tick() {
        super.tick();

        int hurricaneRadius = getHurricaneRadius();
        int hurricaneHeight = getHurricaneHeight();
        Vector3f HurricaneMov = this.getHurricaneMov();
        if (this.tickCount % 2 == 0) {
            this.setXRot(this.getXRot() + 2);
            this.setYRot(this.getYRot() + 2);
            this.setOldPosAndRot();
        }
        if (!this.level().isClientSide && getHurricaneDestroy() && this.tickCount % BLOCK_DESTROY_INTERVAL == 0) {
            destroyBlocksOptimized(hurricaneRadius, hurricaneHeight);
        }
        if (this.level().isClientSide && this.tickCount % PARTICLE_UPDATE_INTERVAL == 0) {
            spawnOptimizedParticles(hurricaneRadius, hurricaneHeight);
        }
        if (!this.level().isClientSide) {
            handleEntityCollisions(hurricaneRadius, hurricaneHeight);
        }
        updateMovementAndLifecycle(HurricaneMov);
    }

    private void destroyBlocksOptimized(int hurricaneRadius, int hurricaneHeight) {
        double minY = this.getY() - 10;
        double maxY = this.getY() + hurricaneHeight;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int currentY = (int) minY + (this.tickCount % 3);
        while (currentY <= maxY) {
            double heightRatio = (currentY - minY) / (maxY - minY);
            double effectiveRadius = hurricaneRadius * (0.5 + heightRatio * 0.5);
            double angleStep = 0.5;
            double radiusStep = 0.8;
            for (double angle = 0; angle < Math.PI * 2; angle += angleStep) {
                for (double r = 0; r < effectiveRadius; r += radiusStep) {
                    int x = (int) (this.getX() + Math.cos(angle) * r);
                    int z = (int) (this.getZ() + Math.sin(angle) * r);
                    mutablePos.set(x, currentY, z);
                    if (!this.level().getBlockState(mutablePos).isAir() &&
                            !this.level().getBlockState(mutablePos).liquid() &&
                            this.level().getBlockState(mutablePos).getDestroySpeed(this.level(), mutablePos) >= 0) {
                        this.level().removeBlock(mutablePos, false);
                    }
                }
            }
            currentY += 3;
        }
    }

    private void spawnOptimizedParticles(int hurricaneRadius, int hurricaneHeight) {
        double sizeFactor = (hurricaneRadius + 1) * (hurricaneHeight + 1) / 1000.0;
        int particleCount = Math.max(20, (int) (100 * sizeFactor * PARTICLE_DENSITY_FACTOR));
        double baseX = this.getX();
        double baseY = this.getY();
        double baseZ = this.getZ();
        double radius1 = (double) hurricaneRadius / 8;
        double riseSpeed = 0.2;
        double spinSpeed = 0.1;
        Random random = new Random();
        for (int i = 0; i < particleCount; i++) {
            double h = this.random.nextDouble() * hurricaneHeight;
            double radiusRatio = h / hurricaneHeight;
            double currentRadius = radius1 + (hurricaneRadius - radius1) * radiusRatio;
            double angle = this.random.nextDouble() * Math.PI * 2 + this.tickCount * spinSpeed;
            double offsetX = currentRadius * Math.cos(angle);
            double offsetZ = currentRadius * Math.sin(angle);
            ParticleOptions particle = switch (random.nextInt(3)) {
                case 0 -> ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_1.get();
                case 1 -> ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_2.get();
                default -> ParticleInit.HURRICANE_OF_LIGHT_PARTICLE_3.get();
            };
            this.level().addAlwaysVisibleParticle(particle, true, baseX + offsetX, baseY + h, baseZ + offsetZ, this.getDeltaMovement().x(), this.getDeltaMovement().y() + riseSpeed, this.getDeltaMovement().z());
        }
    }

    private void handleEntityCollisions(int hurricaneRadius, int hurricaneHeight) {
        AABB boundingBox = new AABB(
                this.getX() - hurricaneRadius,
                this.getY() - 10,
                this.getZ() - hurricaneRadius,
                this.getX() + hurricaneRadius,
                this.getY() + hurricaneHeight,
                this.getZ() + hurricaneRadius
        );
        List<Entity> entities = this.level().getEntities(this, boundingBox);
        if (entities.isEmpty()) return;
        double baseRadius = hurricaneRadius / 8.0;
        double radiusBuffer = hurricaneRadius * 0.2;
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity) || entity == this.getOwner()) {
                continue;
            }
            processEntityCollision(livingEntity, hurricaneHeight, baseRadius, radiusBuffer);
        }
    }

    private void processEntityCollision(LivingEntity entity, int hurricaneHeight, double baseRadius, double radiusBuffer) {
        if (this.tickCount % 5 != 0) return;
        double entityRelativeHeight = Math.max(0, Math.min(1, (entity.getY() - this.getY()) / hurricaneHeight));
        double heightBasedBuffer = radiusBuffer * entityRelativeHeight;
        double effectiveRadius = (baseRadius + (getHurricaneRadius() - baseRadius) * entityRelativeHeight) + heightBasedBuffer;
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < effectiveRadius + (getHurricaneRadius() * 0.1)) {
            applyDamageAndEffects(entity, distance, effectiveRadius, entityRelativeHeight);
        }
    }

    private void updateMovementAndLifecycle(Vector3f HurricaneMov) {
        this.setDeltaMovement(new Vec3(HurricaneMov));
        this.hurtMarked = true;

        if (getHurricaneRandomness() && this.tickCount % 60 == 0) {
            float newHurricaneX = (float) (Math.random() * 2 - 1);
            float newHurricaneZ = (float) (Math.random() * 2 - 1);
            this.setHurricaneMov(new Vector3f(newHurricaneX, HurricaneMov.y, newHurricaneZ));
        }

        if (this.tickCount >= getHurricaneLifecount()) {
            this.discard();
        }
    }

    private void applyDamageAndEffects(LivingEntity livingEntity, double distance, double effectiveRadius, double entityRelativeHeight) {
        if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
            int amplifier = 0;
            if (livingEntity.hasEffect(ModEffects.ARMOR_WEAKNESS.get())) {
                amplifier = livingEntity.getEffect(ModEffects.ARMOR_WEAKNESS.get()).getAmplifier();
            }
            double distanceRatio = distance / effectiveRadius;
            float damageMultiplier = (0.5f + (float) entityRelativeHeight * 0.5f) * (distanceRatio > 1.0 ? 0.7f : 1.0f);
            livingEntity.hurt(BeyonderUtil.genericSource(owner), (float) getHurricaneRadius() * damageMultiplier / 2);

            if (this.tickCount % 15 == 0) {
                if (getDestroyArmor()) {
                    for (ItemStack armor : livingEntity.getArmorSlots()) {
                        if (!armor.isEmpty()) {
                            armor.hurtAndBreak(30, livingEntity, (player) -> player.broadcastBreakEvent(Objects.requireNonNull(armor.getEquipmentSlot())));
                        }
                    }
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.ARMOR_WEAKNESS.get(), 200, amplifier, true, true));
                }

                if (BeyonderUtil.isPurifiable(livingEntity)) {
                    livingEntity.hurt(BeyonderUtil.magicSource(owner), (float) getHurricaneRadius() * damageMultiplier / 2);
                }
            }
        } else {
            int amplifier = 0;
            if (livingEntity.hasEffect(ModEffects.ARMOR_WEAKNESS.get())) {
                amplifier = livingEntity.getEffect(ModEffects.ARMOR_WEAKNESS.get()).getAmplifier();
            }
            double distanceRatio = distance / effectiveRadius;
            float damageMultiplier = (0.5f + (float) entityRelativeHeight * 0.5f) * (distanceRatio > 1.0 ? 0.7f : 1.0f);
            livingEntity.hurt(livingEntity.damageSources().generic(), (float) getHurricaneRadius() * damageMultiplier / 2);
            if (this.tickCount % 15 == 0) {
                livingEntity.addEffect(new MobEffectInstance(ModEffects.ARMOR_WEAKNESS.get(), 200, amplifier, true, true));
            }
            if (BeyonderUtil.isPurifiable(livingEntity)) {
                livingEntity.hurt(livingEntity.damageSources().magic(), (float) getHurricaneRadius() * damageMultiplier / 2);
            }
        }
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public int getHurricaneRadius() {
        return this.entityData.get(DATA_HURRICANE_RADIUS);
    }

    public void setHurricaneRadius(int radius) {
        this.entityData.set(DATA_HURRICANE_RADIUS, radius);
    }

    public int getHurricaneHeight() {
        return this.entityData.get(DATA_HURRICANE_HEIGHT);
    }

    public void setHurricaneHeight(int height) {
        this.entityData.set(DATA_HURRICANE_HEIGHT, height);
    }

    public int getHurricaneLifecount() {
        return this.entityData.get(DATA_LIFECOUNT);
    }

    public void setHurricaneLifecount(int lifeCount) {
        this.entityData.set(DATA_LIFECOUNT, lifeCount);
    }

    public Vector3f getHurricaneMov() {
        return this.entityData.get(DATA_HURRICANE_MOV);
    }

    public void setHurricaneMov(Vector3f HurricaneMov) {
        this.entityData.set(DATA_HURRICANE_MOV, HurricaneMov);
    }

    public void setHurricaneRandom(boolean random) {
        this.entityData.set(RANDOM_MOVEMENT, random);
    }

    public boolean getHurricaneRandomness() {
        return this.entityData.get(RANDOM_MOVEMENT);
    }

    public boolean getHurricaneDestroy() {
        return this.entityData.get(DESTROY_BLOCKS);
    }

    public void setHurricaneDestroy(boolean destroy) {
        this.entityData.set(DESTROY_BLOCKS, destroy);
    }

    public boolean getDestroyArmor() {
        return this.entityData.get(DESTROY_ARMOR);
    }

    public void setDestroyArmor(boolean destroy) {
        this.entityData.set(DESTROY_ARMOR, destroy);
    }

}
