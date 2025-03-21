package net.swimmingtuna.lotm.entity;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.UpdateEntityLocationS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class DivineHandRightEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(DivineHandRightEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(DivineHandRightEntity.class, EntityDataSerializers.FLOAT);


    public DivineHandRightEntity(EntityType<? extends DivineHandRightEntity> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    protected float getInertia() {
        return 0.99F;
    }

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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yaw", this.entityData.get(YAW));
        tag.putFloat("pitch", this.entityData.get(PITCH));
    }


    @Override
    public boolean isOnFire() {
        return false;
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

    public float getYaw() {
        return this.entityData.get(YAW);
    }

    public float getPitch() {
        return this.entityData.get(PITCH);
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

            float radius = ScaleTypes.BASE.getScaleData(this).getScale() * 0.6f;
            destroyBlocksAround((int) radius);
            Vec3 currentPos = this.position();
            for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(100))) {
                LOTMNetworkHandler.sendToPlayer(new UpdateEntityLocationS2C(currentPos.x(), currentPos.y(), currentPos.z(), this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z(), this.getId()), player);
            }
            if (this.tickCount >= 300) {
                this.discard();
            }
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(this);
            float scale = scaleData.getScale();
            if (this.getOwner() != null && this.getOwner() instanceof LivingEntity owner) {
                for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(scale * 0.8f))) {
                    if (livingEntity != owner && BeyonderUtil.isAllyOf(owner, livingEntity)) {
                        livingEntity.getPersistentData().putDouble("luck", livingEntity.getPersistentData().getDouble("luck") + 25);
                        livingEntity.getPersistentData().putUUID("divineHandUUID", owner.getUUID());
                        livingEntity.getPersistentData().putInt("divineHandGuarding", 1200);
                        if (livingEntity instanceof Player player) {
                            player.displayClientMessage(Component.literal("You are being guarded by " + owner.getName() + "for 1 minute").withStyle(ChatFormatting.GREEN), true);
                        }
                        owner.sendSystemMessage(Component.literal("Guarding Entity:" + livingEntity.getScoreboardName()).withStyle(ChatFormatting.YELLOW));
                    }
                    if (livingEntity == owner || BeyonderUtil.isAllyOf(owner ,livingEntity) && livingEntity.getPersistentData().getInt("divineHandLuckCooldown") == 0) {
                        livingEntity.getPersistentData().putDouble("corruption", livingEntity.getPersistentData().getDouble("corruption") - 25);
                        for (MobEffectInstance mobEffect : livingEntity.getActiveEffects()) {
                            MobEffect type = mobEffect.getEffect();
                            if (!type.isBeneficial()) {
                                livingEntity.removeEffect(type);
                            }
                        }
                        livingEntity.getPersistentData().putInt("divineHandLuckCooldown", 100);
                        if (livingEntity instanceof Player player) {
                            player.displayClientMessage(Component.literal("You were blessed with luck!").withStyle(ChatFormatting.GREEN), true);
                        }
                        livingEntity.getPersistentData().putDouble("luck", livingEntity.getPersistentData().getDouble("luck") + 25);
                    }
                }
            }
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public static void divineHandCooldownDecrease(LivingEntity living) {
        if (living.getPersistentData().getInt("divineHandLuckCooldown") >= 1) {
            living.getPersistentData().putInt("divineHandLuckCooldown", living.getPersistentData().getInt("divineHandLuckCooldown") -  1);
        }
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

    public void setPitch(float pitch) {
        this.entityData.set(PITCH, pitch);
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<DivineHandRightEntity> animationState) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
