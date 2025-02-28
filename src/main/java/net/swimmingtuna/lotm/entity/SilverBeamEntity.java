package net.swimmingtuna.lotm.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;

public class SilverBeamEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(SilverBeamEntity.class, EntityDataSerializers.BOOLEAN);

    public SilverBeamEntity(EntityType<? extends SilverBeamEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SilverBeamEntity(Level level, LivingEntity shooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityInit.SILVER_BEAM_ENTITY.get(), shooter, offsetX, offsetY, offsetZ, level);
    }


    protected float getInertia() {
        return 1.0F;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (entity instanceof SilverBeamEntity) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    public boolean isOnFire() {
        return false;
    }

    @Override
    public @NotNull ParticleOptions getTrailParticle() {
        return ParticleInit.NULL_PARTICLE.get();
    }

    @Override
    public void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide()) {
            Entity hitEntity = result.getEntity();
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            float scale = scaleData.getScale();
            if (hitEntity instanceof LivingEntity livingEntity && this.getOwner() != null) {
                livingEntity.hurt(BeyonderUtil.genericSource(this.getOwner()), scale * 20);
                this.discard();
            }
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
        ProjectileUtil.rotateTowardsMovement(this, 0.5f);
        this.xRotO = getXRot();
        this.yRotO = this.getYRot();

        if (!this.level().isClientSide() && this.getOwner() instanceof LivingEntity owner) {
            if (this.tickCount >= 200 && !this.getPersistentData().getBoolean("hasTeleported")) {
                // Set the hasTeleported tag to true
                this.getPersistentData().putBoolean("hasTeleported", true);

                // Find a target entity in the owner's sight
                Entity target = findTargetInSight(owner);

                if (target != null) {
                    Vec3 teleportPos = getRandomPositionAroundTarget(target);
                    this.setPos(teleportPos.x, teleportPos.y, teleportPos.z);
                    Vec3 targetPos = target.position();
                    Vec3 direction = targetPos.subtract(this.position()).normalize();
                    this.setDeltaMovement(direction.scale(2.0));

                    destroyBlocksAroundEntity();
                }
            }

            if (this.tickCount >= 400) {
                this.discard();
            }
        }
    }

    private Entity findTargetInSight(LivingEntity owner) {
        AABB area = new AABB(owner.getX() - 20, owner.getY() - 20, owner.getZ() - 20, owner.getX() + 20, owner.getY() + 20, owner.getZ() + 20);
        List<Entity> entities = owner.level().getEntities(owner, area);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && owner.hasLineOfSight(entity)) {
                return entity;
            }
        }

        return null;
    }

    private Vec3 getRandomPositionAroundTarget(Entity target) {
        double radius = 10.0;
        double theta = Math.random() * 2 * Math.PI;
        double phi = Math.random() * Math.PI;
        double x = target.getX() + radius * Math.sin(phi) * Math.cos(theta);
        double y = target.getY() + radius * Math.sin(phi) * Math.sin(theta);
        double z = target.getZ() + radius * Math.cos(phi);

        return new Vec3(x, y, z);
    }

    private void destroyBlocksAroundEntity() {
        int radius = 3;
        BlockPos pos = this.blockPosition();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = pos.offset(x, y, z);
                    if (this.level().getBlockState(blockPos).getBlock() != Blocks.AIR) {
                        this.level().destroyBlock(blockPos, true);
                    }
                }
            }
        }
    }
}
