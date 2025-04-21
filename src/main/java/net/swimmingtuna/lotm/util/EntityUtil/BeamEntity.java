package net.swimmingtuna.lotm.util.EntityUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
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
                    if (getIsDragonBreath() && this.getOwner() != null && this.getOwner() instanceof LivingEntity livingOwner && entity instanceof LivingEntity livingEntity && !BeyonderUtil.areAllies(livingOwner, livingEntity)) {
                        BeyonderUtil.applyMentalDamage(livingOwner, livingEntity, this.getDamage());
                    } else if (getIsDragonBreath() && this.getOwner() == null && entity instanceof LivingEntity livingEntity) {
                        livingEntity.hurt(livingEntity.damageSources().magic(), getDamage());
                    }
                    if (getIsTwilight() && entity instanceof LivingEntity livingEntity && this.getOwner() instanceof LivingEntity pOwner && !BeyonderUtil.areAllies(pOwner, livingEntity)) {
                        int age = livingEntity.getPersistentData().getInt("age");
                        livingEntity.hurt(BeyonderUtil.genericSource(owner), 10);
                        int ageDivisibleAmount = 1;
                        if (pOwner instanceof Mob mob) {
                            ageDivisibleAmount = 3;
                        }
                        if (this.tickCount % 3 == 0) {
                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("You are getting rapidly aged").withStyle(BeyonderUtil.ageStyle(livingEntity)).withStyle(ChatFormatting.BOLD), true);
                            }
                            if (BeyonderUtil.getSequence(pOwner) != 0) {
                                livingEntity.getPersistentData().putInt("age", ((age + (30 - BeyonderUtil.getSequence(pOwner))) * 9) / ageDivisibleAmount);
                            } else {
                                livingEntity.getPersistentData().putInt("age", (age + (50)) / ageDivisibleAmount);
                            }
                        }
                    }
                    if (entity instanceof LivingEntity livingEntity && getIsDragonBreath() && this.getOwner() != null && this.getOwner() instanceof LivingEntity livingOwner && !BeyonderUtil.areAllies(livingOwner, livingEntity)) {
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
            direction = new Vec3(Math.cos(this.renderYaw) * Math.cos(this.renderPitch), Math.sin(this.renderPitch), Math.sin(this.renderYaw) * Math.cos(this.renderPitch)).normalize();
        } else {
            direction = new Vec3(Math.cos(this.getYaw()) * Math.cos(this.getPitch()), Math.sin(this.getPitch()), Math.sin(this.getYaw()) * Math.cos(this.getPitch())).normalize();
        }

        Vec3 end = new Vec3(this.getX(), this.getY(), this.getZ()).add(direction.scale(this.getRange()));
        this.endPosX = end.x;
        this.endPosY = end.y;
        this.endPosZ = end.z;
        this.endPos = end;
    }


    public List<Entity> checkCollisions(Vec3 from, Vec3 to) {
        if (!(this.getOwner() instanceof LivingEntity owner)) return List.of();
        BlockHitResult result = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
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
        Vec3 dir = to.subtract(from).normalize();
        double radius = this.getSize();
        AABB bounds = new AABB(
                Math.min(from.x, this.collidePosX) - radius,
                Math.min(from.y, this.collidePosY) - radius,
                Math.min(from.z, this.collidePosZ) - radius,
                Math.max(from.x, this.collidePosX) + radius,
                Math.max(from.y, this.collidePosY) + radius,
                Math.max(from.z, this.collidePosZ) + radius
        );
        if (!this.level().isClientSide) {
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int x = (int) Math.floor(bounds.minX); x <= Math.ceil(bounds.maxX); x++) {
                for (int y = (int) Math.floor(bounds.minY); y <= Math.ceil(bounds.maxY); y++) {
                    for (int z = (int) Math.floor(bounds.minZ); z <= Math.ceil(bounds.maxZ); z++) {
                        mutablePos.set(x, y, z);
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
                            } else if (this.tickCount % 5 == 0 && getIsTwilight() && this.level().getBlockState(mutablePos) != Blocks.BEDROCK.defaultBlockState()) {
                                if (this.level().getBlockState(mutablePos) != Blocks.DIRT.defaultBlockState() && this.level().getBlockState(mutablePos) != Blocks.AIR.defaultBlockState()) {
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
        double rayLength = from.distanceTo(new Vec3(this.collidePosX, this.collidePosY, this.collidePosZ));
        double entityDetectionRadius = radius * 1.5; // Wider radius for entity detection


        AABB entityBounds = new AABB(
                Math.min(from.x, this.collidePosX) - radius * 0.8,
                Math.min(from.y, this.collidePosY) - radius * 0.8,
                Math.min(from.z, this.collidePosZ) - radius * 0.8,
                Math.max(from.x, this.collidePosX) + radius * 0.8,
                Math.max(from.y, this.collidePosY) + radius,
                Math.max(from.z, this.collidePosZ) + radius * 0.8
        );
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : this.level().getEntitiesOfClass(Entity.class, entityBounds)) {
            if (entity == this.getOwner() || entity == this) continue;
            AABB entityBox = entity.getBoundingBox();
            if (rayIntersectsBox(from, to, entityBox)) {
                entities.add(entity);
                continue;
            }
            Vec3[] checkPoints = {
                    new Vec3(entity.getX(), entity.getY(), entity.getZ()),
                    new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.3, entity.getZ()),
                    new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ()),
                    new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.45, entity.getZ())
            };

            for (Vec3 point : checkPoints) {
                Vec3 nearestPoint = getNearestPointOnLine(from, to, point);
                double distance = point.distanceTo(nearestPoint);

                if (distance <= entityDetectionRadius) {
                    entities.add(entity);
                    break;
                }
            }
        }
        return entities;
    }

    private boolean rayIntersectsBox(Vec3 rayStart, Vec3 rayEnd, AABB box) {
        Vec3 rayDir = rayEnd.subtract(rayStart).normalize();

        // Calculate intersection with each face of the box
        double tMin = (box.minX - rayStart.x) / (rayDir.x == 0 ? 0.00001 : rayDir.x);
        double tMax = (box.maxX - rayStart.x) / (rayDir.x == 0 ? 0.00001 : rayDir.x);

        if (tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        double tyMin = (box.minY - rayStart.y) / (rayDir.y == 0 ? 0.00001 : rayDir.y);
        double tyMax = (box.maxY - rayStart.y) / (rayDir.y == 0 ? 0.00001 : rayDir.y);

        if (tyMin > tyMax) {
            double temp = tyMin;
            tyMin = tyMax;
            tyMax = temp;
        }

        if ((tMin > tyMax) || (tyMin > tMax)) {
            return false;
        }

        if (tyMin > tMin) {
            tMin = tyMin;
        }

        if (tyMax < tMax) {
            tMax = tyMax;
        }

        double tzMin = (box.minZ - rayStart.z) / (rayDir.z == 0 ? 0.00001 : rayDir.z);
        double tzMax = (box.maxZ - rayStart.z) / (rayDir.z == 0 ? 0.00001 : rayDir.z);

        if (tzMin > tzMax) {
            double temp = tzMin;
            tzMin = tzMax;
            tzMax = temp;
        }

        if ((tMin > tzMax) || (tzMin > tMax)) {
            return false;
        }

        // Check if intersection is within ray length
        double maxDistance = rayStart.distanceTo(rayEnd);
        return tMin >= 0 && tMin <= maxDistance;
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
            double yOffset = (this.getFrames() <= this.getCharge()) ? 0.5 : 0.0;
            this.setPos(spawn.x, spawn.y + yOffset, spawn.z);
        }
    }
}
