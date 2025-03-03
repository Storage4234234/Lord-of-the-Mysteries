package net.swimmingtuna.lotm.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.UpdateEntityLocationS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import virtuoel.pehkui.api.ScaleTypes;

public class DivineHandLeftEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public DivineHandLeftEntity(EntityType<? extends DivineHandLeftEntity> entityType, Level level) {
        super(entityType, level);
    }



    @Override
    protected float getInertia() {
        return 0.99F;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (!this.level().isClientSide()) {
            Entity entity = pResult.getEntity();
            if (entity instanceof LivingEntity livingEntity && this.getOwner() instanceof LivingEntity owner && this.tickCount % 10 == 0) {
                if (livingEntity != owner && BeyonderUtil.isAllyOf(owner, livingEntity)) {
                    CompoundTag tag = livingEntity.getPersistentData();
                    tag.putInt("age", tag.getInt("age") + 60);
                    tag.putInt("pressedDownByDivineHand", 200);
                }
            }
        }
    }

    public static void divineHandTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getPersistentData().getInt("pressedDownByDivineHand") >= 1) {
            Vec3 movement = event.getEntity().getDeltaMovement();
            event.getEntity().getPersistentData().putInt("pressedDownByDivineHand", event.getEntity().getPersistentData().getInt("pressedDownByDivineHand") - 1);
            event.getEntity().setDeltaMovement(movement.x(), movement.y() - 0.2f, movement.z());
            event.getEntity().hurtMarked = true;
            event.getEntity().getPersistentData().putInt("age", event.getEntity().getPersistentData().getInt("age") + 1);

        }
    }


    @Override
    public boolean isOnFire() {
        return false;
    }

    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(DivineHandLeftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(DivineHandLeftEntity.class, EntityDataSerializers.FLOAT);

    @Override
    public void defineSynchedData() {
        this.entityData.define(YAW, 0.0f);
        this.entityData.define(PITCH, 0.0f);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("yaw")) {
            this.entityData.set(YAW, tag.getFloat("yaw"));
        }
        if (tag.contains("pitch")) {
            this.entityData.set(PITCH, tag.getFloat("pitch"));
        }
    }

    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public void setPitch(float pitch) {
        this.entityData.set(PITCH, pitch);
    }

    public float getPitch() {
        return this.entityData.get(PITCH);
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("pitch", this.entityData.get(PITCH));
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
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (this.getOwner() != null) {
                Vec3 motion = this.getDeltaMovement();
                double horizontalDist = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                float newYaw = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
                float newPitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontalDist));
                this.setYaw(newYaw);
                this.setPitch(newPitch);
            }
            float radius = ScaleTypes.BASE.getScaleData(this).getScale();
            destroyBlocksAround((int) radius);
            Vec3 currentPos = this.position();
            for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(100))) {
                LOTMNetworkHandler.sendToPlayer(new UpdateEntityLocationS2C(currentPos.x(), currentPos.y(), currentPos.z(), this.getId()), player);
            }
            if (this.tickCount >= 300) {
                this.discard();
            }
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public void destroyBlocksAround(int radius) {
        BlockPos centerPos = this.blockPosition();
        BlockState obsidianState = Blocks.OBSIDIAN.defaultBlockState();
        float obsidianHardness = obsidianState.getDestroySpeed(level(), centerPos);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos targetPos = centerPos.offset(x, y, z);
                    BlockState blockState = level().getBlockState(targetPos);
                    if (blockState.getDestroySpeed(level(), targetPos) < obsidianHardness && !blockState.isAir() && !(blockState.getBlock() == Blocks.BEDROCK)) {
                        level().destroyBlock(targetPos, false);
                    }
                }
            }
        }
    }

    public void setYaw(float yaw) {
        this.entityData.set(YAW, yaw);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<DivineHandLeftEntity> animationState) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
