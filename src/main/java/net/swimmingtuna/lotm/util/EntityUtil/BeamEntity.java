package net.swimmingtuna.lotm.util.EntityUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.RotationUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class BeamEntity extends LOTMProjectile {
    public double endPosX;
    public double endPosY;
    public double endPosZ;
    public Vec3 endPos;
    public double collidePosX;
    public double collidePosY;
    public double collidePosZ;
    public Vec3 collidePos;
    public double prevCollidePosX;
    public double prevCollidePosY;
    public double prevCollidePosZ;
    public Vec3 prevCollidePos;
    public float renderYaw;
    public float renderPitch;

    public boolean on = true;

    public @Nullable Direction side = null;

    private static final EntityDataAccessor<Boolean> TWILIGHT = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DESTROY_BLOCKS = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DRAGON_BREATH = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRENZY_TIME = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(BeamEntity.class, EntityDataSerializers.FLOAT);

    public float prevYaw;
    public float prevPitch;

    public int animation;

    protected BeamEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);

        this.noCulling = true;

        this.update();
    }

    protected BeamEntity(EntityType<? extends Projectile> entityType, LivingEntity owner, float power) {
        this(entityType, owner.level());

        this.setOwner(owner);
        this.setPower(power);
    }


    public abstract int getFrames();

    protected abstract double getRange();

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public int getFrenzyTime() {
        return this.entityData.get(FRENZY_TIME);
    }

    public void setFrenzyTime(int frenzyTime) {
        this.entityData.set(FRENZY_TIME, frenzyTime);
    }

    public boolean getIsDragonBreath() {
        return this.entityData.get(DRAGON_BREATH);
    }

    public void setIsDragonbreath(boolean isDragonBreath) {
        this.entityData.set(DRAGON_BREATH, isDragonBreath);
    }

    public boolean getIsTwilight() {
        return this.entityData.get(TWILIGHT);
    }

    public void setIsTwilight(boolean isTwilight) {
        this.entityData.set(TWILIGHT, isTwilight);
    }

    public boolean getDestroyBlocks() {
        return this.entityData.get(DESTROY_BLOCKS);
    }

    public void setDestroyBlocks(boolean destroyBlocks) {
        this.entityData.set(DESTROY_BLOCKS, destroyBlocks);
    }

    public abstract int getDuration();


    public abstract int getCharge();

    protected boolean causesFire() {
        return false;
    }

    protected boolean breaksBlocks() {
        return true;
    }

    protected boolean isStill() {
        return false;
    }


    protected Vec3 calculateSpawnPos(LivingEntity owner) {
        return new Vec3(owner.getX(), owner.getEyeY() - (this.getBbHeight() / 2.0F) + 0.5, owner.getZ()).add(RotationUtil.getTargetAdjustedLookAngle(owner));
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.update();
        this.calculateEndPos();
    }

    @Override
    public void tick() {
        super.tick();

        // Update previous positions and rotations
        this.prevCollidePos = this.collidePos;
        this.prevYaw = this.renderYaw;
        this.prevPitch = this.renderPitch;
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (!this.isStill()) {
            this.update();
        }

        if (this.getOwner() instanceof LivingEntity owner) {
            if (!this.on && this.animation == 0) {
                this.discard();
            }

            if (this.getFrames() > 0) {
                if (this.on) {
                    if (this.animation < this.getFrames()) {
                        this.animation++;
                    }
                } else {
                    if (this.animation > 0) {
                        this.animation--;
                    }
                }
            }

            if (this.getTime() >= this.getCharge()) {
                if (!this.isStill()) {
                    this.calculateEndPos();
                }

                // Corrected collision detection start position
                List<Entity> entities = this.checkCollisions(
                        new Vec3(this.getX(), this.getY(), this.getZ()), // Corrected start position
                        new Vec3(this.endPosX, this.endPosY, this.endPosZ)
                );

                // Handle entity collisions and effects
                for (Entity entity : entities) {
                    if (entity == owner) continue;
                    if (getIsDragonBreath() && this.getOwner() != null && entity instanceof LivingEntity livingEntity) {
                        BeyonderUtil.applyMentalDamage((LivingEntity) this.getOwner(), livingEntity, this.getDamage());
                    } else if (getIsDragonBreath() && this.getOwner() == null && entity instanceof LivingEntity livingEntity){
                        livingEntity.hurt(livingEntity.damageSources().magic(), getDamage());
                    } if (getIsTwilight() && entity instanceof LivingEntity livingEntity && this.getOwner() instanceof LivingEntity pOwner) {
                        int age = livingEntity.getPersistentData().getInt("age");
                        livingEntity.getPersistentData().putInt("age", (age + (10 - BeyonderUtil.getSequence(pOwner))) * 3);
                    }

                    if (entity instanceof LivingEntity livingEntity && getIsDragonBreath()) {
                        livingEntity.addEffect(new MobEffectInstance(ModEffects.FRENZY.get(), getFrenzyTime(), 1, false, false));
                    }

                    if (this.causesFire()) {
                        entity.setSecondsOnFire(5);
                    }
                }

                // Handle block breaking and fire
                if (!this.level().isClientSide) {
                    double radius = this.getSize();
                    AABB bounds = new AABB(
                            this.collidePosX - radius, this.collidePosY - radius, this.collidePosZ - radius,
                            this.collidePosX + radius, this.collidePosY + radius, this.collidePosZ + radius
                    );

                    // Block breaking and fire logic here...
                }
            }

            if (this.getTime() - this.getCharge() >= this.getDuration()) {
                this.on = false;
            }
        }
    }

    private static final List<Block> EXCLUDED_BLOCKS = List.of(Blocks.BEDROCK, Blocks.OBSIDIAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 20.0F);
        this.entityData.define(DATA_YAW, 0.0F);
        this.entityData.define(DATA_PITCH, 0.0F);
        this.entityData.define(DRAGON_BREATH, false);
        this.entityData.define(FRENZY_TIME, 1);
        this.entityData.define(SIZE, 1);
        this.entityData.define(DESTROY_BLOCKS, true);
        this.entityData.define(TWILIGHT, false);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("damage")) {
            this.setDamage(compound.getFloat("damage"));
        }
        if (compound.contains("data_yaw")) {
            this.setYaw(compound.getFloat("data_yaw"));
        }
        if (compound.contains("data_pitch")) {
            this.setPitch(compound.getFloat("data_pitch"));
        }
        if (compound.contains("dragon_breath")) {
            this.setIsDragonbreath(compound.getBoolean("dragon_breath"));
        }
        if (compound.contains("frenzy_time")) {
            this.setFrenzyTime(compound.getInt("frenzy_time"));
        }
        if (compound.contains("size")) {
            this.setSize(compound.getInt("size"));
        }
        if (compound.contains("destroy_blocks")) {
            this.setSize(compound.getInt("destroy_blocks"));
        }
        if (compound.contains("twilight")) {
            this.setSize(compound.getInt("twilight"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("damage", this.getDamage());
        compound.putFloat("data_yaw", this.getYaw());
        compound.putFloat("data_pitch", this.getPitch());
        compound.putBoolean("dragon_breath", this.getIsDragonBreath());
        compound.putInt("frenzy_time", this.getFrenzyTime());
        compound.putInt("size", this.getSize());
        compound.putBoolean("destroy_blocks", this.getDestroyBlocks());
        compound.putBoolean("twilight", this.getIsTwilight());
    }


    public int getSize() {
        return this.entityData.get(SIZE);
    }

    public void setSize(int size) {
        this.entityData.set(SIZE, size);
    }

    public float getYaw() {
        return this.entityData.get(DATA_YAW);
    }

    public void setYaw(float yaw) {
        this.entityData.set(DATA_YAW, yaw);
    }

    public float getPitch() {
        return this.entityData.get(DATA_PITCH);
    }

    public void setPitch(float pitch) {
        this.entityData.set(DATA_PITCH, pitch);
    }

    private void calculateEndPos() {
        Vec3 direction;
        if (this.level().isClientSide) {
            direction = new Vec3(
                    Math.cos(this.renderYaw) * Math.cos(this.renderPitch),
                    Math.sin(this.renderPitch),
                    Math.sin(this.renderYaw) * Math.cos(this.renderPitch)
            ).normalize();
        } else {
            direction = new Vec3(
                    Math.cos(this.getYaw()) * Math.cos(this.getPitch()),
                    Math.sin(this.getPitch()),
                    Math.sin(this.getYaw()) * Math.cos(this.getPitch())
            ).normalize();
        }

        Vec3 end = new Vec3(this.getX(), this.getY(), this.getZ()).add(direction.scale(this.getRange()));
        this.endPosX = end.x;
        this.endPosY = end.y;
        this.endPosZ = end.z;
        this.endPos = end;
    }


    public List<Entity> checkCollisions(Vec3 from, Vec3 to) {
        if (!(this.getOwner() instanceof LivingEntity owner)) return List.of();

        // Get the collision result
        BlockHitResult result = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // Update collision position
        if (result.getType() != HitResult.Type.MISS) {
            Vec3 pos = result.getLocation();
            this.collidePosX = pos.x;
            this.collidePosY = pos.y;
            this.collidePosZ = pos.z;
            this.side = result.getDirection();
        } else {
            this.collidePosX = to.x;
            this.collidePosY = to.y;
            this.collidePosZ = to.z;
            this.side = null;
        }

        // Calculate the direction vector
        Vec3 dir = to.subtract(from).normalize();

        // Get size for radius
        double radius = this.getSize();

        // Create bounds centered on the beam path
        AABB bounds = new AABB(
                Math.min(from.x, this.collidePosX) - radius,
                Math.min(from.y, this.collidePosY) - radius,
                Math.min(from.z, this.collidePosZ) - radius,
                Math.max(from.x, this.collidePosX) + radius,
                Math.max(from.y, this.collidePosY) + radius,
                Math.max(from.z, this.collidePosZ) + radius
        );

        // For block breaking and fire
        if (!this.level().isClientSide) {
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            for (int x = (int) Math.floor(bounds.minX); x <= Math.ceil(bounds.maxX); x++) {
                for (int y = (int) Math.floor(bounds.minY); y <= Math.ceil(bounds.maxY); y++) {
                    for (int z = (int) Math.floor(bounds.minZ); z <= Math.ceil(bounds.maxZ); z++) {
                        mutablePos.set(x, y, z);

                        // Calculate distance from point to line (beam)
                        Vec3 point = new Vec3(x + 0.5, y + 0.5, z + 0.5);
                        Vec3 fromToPoint = point.subtract(from);
                        double dot = fromToPoint.dot(dir);
                        Vec3 projection = dir.scale(dot);
                        Vec3 distanceVec = fromToPoint.subtract(projection);
                        double distance = distanceVec.length();

                        if (distance <= radius) {
                            if (getDestroyBlocks()) {
                                if (this.breaksBlocks() && !EXCLUDED_BLOCKS.contains(this.level().getBlockState(mutablePos).getBlock())) {
                                    this.level().destroyBlock(mutablePos, false);
                                }
                            } else if (this.tickCount % 5 == 0 && getIsTwilight()) {
                                if (this.level().getBlockState(mutablePos) != Blocks.DIRT.defaultBlockState() && this.level().getBlockState(mutablePos) != Blocks.AIR.defaultBlockState() && this.level().getBlockState(mutablePos) != Blocks.BEDROCK.defaultBlockState()) {
                                    this.level().setBlock(mutablePos, Blocks.DIRT.defaultBlockState(), 11);
                                } else {
                                    this.level().destroyBlock(mutablePos, false);
                                }
                            }

                            if (this.causesFire()) {
                                if (this.random.nextInt(3) == 0 &&
                                        this.level().getBlockState(mutablePos).isAir() &&
                                        this.level().getBlockState(mutablePos.below()).isSolidRender(this.level(), mutablePos.below())) {
                                    this.level().setBlockAndUpdate(mutablePos, BaseFireBlock.getState(this.level(), mutablePos));
                                }
                            }
                        }
                    }
                }
            }
        }

        // Entity collision detection
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : this.level().getEntitiesOfClass(Entity.class, bounds)) {
            if (entity == this.getOwner()) continue;

            Vec3 nearestPoint = getNearestPointOnLine(from, to, entity.position());
            double distance = entity.position().subtract(nearestPoint).length();

            if (distance <= radius + entity.getBbWidth() / 2) {
                entities.add(entity);
            }
        }

        return entities;
    }

    private Vec3 getNearestPointOnLine(Vec3 lineStart, Vec3 lineEnd, Vec3 point) {
        Vec3 line = lineEnd.subtract(lineStart);
        double len = line.length();
        line = line.normalize();

        Vec3 v = point.subtract(lineStart);
        double d = v.dot(line);
        d = Math.max(0, Math.min(len, d));

        return lineStart.add(line.scale(d));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1024;
    }

    private void update() {
        if (this.getOwner() instanceof LivingEntity owner) {
            this.renderYaw = (float) ((RotationUtil.getTargetAdjustedYRot(owner) + 90.0D) * Math.PI / 180.0D);
            this.renderPitch = (float) (-RotationUtil.getTargetAdjustedXRot(owner) * Math.PI / 180.0D);
            this.setYaw((float) ((RotationUtil.getTargetAdjustedYRot(owner) + 90.0F) * Math.PI / 180.0D));
            this.setPitch((float) (-RotationUtil.getTargetAdjustedXRot(owner) * Math.PI / 180.0D));

            Vec3 spawn = this.calculateSpawnPos(owner);

            // Corrected positioning logic
            double yOffset = (this.getFrames() <= this.getCharge()) ? 0.5 : 0.0;
            this.setPos(spawn.x, spawn.y + yOffset, spawn.z);
        }
    }
}
