package net.swimmingtuna.lotm.entity;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.client.Configs;
import net.swimmingtuna.lotm.entity.EntityGoals.PlayerMobGoals;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.GameRuleInit;
import net.swimmingtuna.lotm.init.SoundInit;
import net.swimmingtuna.lotm.util.PlayerMobs.ItemManager;
import net.swimmingtuna.lotm.util.PlayerMobs.NameManager;
import net.swimmingtuna.lotm.util.PlayerMobs.PlayerName;
import net.swimmingtuna.lotm.util.PlayerMobs.ProfileUpdater;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerMobEntity extends Monster implements RangedAttackMob, CrossbowAttackMob {

    @Nullable
    private GameProfile profile;
    @Nullable
    private ResourceLocation skin;
    @Nullable
    private ResourceLocation cape;
    private boolean skinAvailable;
    private boolean capeAvailable;

    protected Supplier<? extends BeyonderClass> requiredClass;


    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;
    private LivingEntity owner;

    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);

    private static final EntityDataAccessor<Boolean> IS_CHILD = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SEQUENCE = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPIRITUALITY = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAXSPIRITUALITY = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean canBreakDoors;
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, (difficulty) -> difficulty == Difficulty.HARD);
    private final RangedBowAttackGoal<PlayerMobEntity> bowAttackGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);
    private final RangedCrossbowAttackGoal<PlayerMobEntity> crossbowAttackGoal = new RangedCrossbowAttackGoal<>(this, 1.0D, 15.0F);

    public PlayerMobEntity(Level worldIn, Supplier<? extends BeyonderClass> requiredClass) {
        this(EntityInit.PLAYER_MOB_ENTITY.get(), worldIn, requiredClass);
    }

    public PlayerMobEntity(EntityType<? extends Monster> entityType, Level worldIn, Supplier<? extends BeyonderClass> requiredClass) {
        super(entityType, worldIn);
        this.requiredClass = requiredClass;
        setCombatTask();
    }

    public PlayerMobEntity(EntityType<PlayerMobEntity> playerMobEntityEntityType, Level level) {
        super(playerMobEntityEntityType,level);
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 35D)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.ATTACK_DAMAGE, 3.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D);
    }

    private boolean targetTwin(LivingEntity livingEntity) {
        return Configs.COMMON.attackTwin.get() || !(livingEntity instanceof Player && livingEntity.getName().getString().equals(getUsername().getDisplayName()));
    }

    protected void registerGoals() {
        goalSelector.addGoal(1, new PlayerMobGoals.PlayerMobSpawnCowGoals(this));
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        addBehaviourGoals();
    }

    private void addBehaviourGoals() {
        if (canOpenDoor()) {
            goalSelector.addGoal(1, new OpenDoorGoal(this, true));
            ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        }

        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        targetSelector.addGoal(1, new HurtByTargetGoal(this, ZombifiedPiglin.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::targetTwin));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    private boolean canOpenDoor() {
        return Configs.COMMON.openDoors.get() && level().getDifficulty().getId() >= Configs.COMMON.openDoorsDifficulty.get().getId();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(NAME, "");
        getEntityData().define(IS_CHILD, false);
        getEntityData().define(IS_CHARGING_CROSSBOW, false);
        getEntityData().define(SEQUENCE, 10);

    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (getVehicle() instanceof PathfinderMob mob) {
            yBodyRot = mob.yBodyRot;
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        boolean force = Configs.COMMON.forceSpawnItem.get();
        if (force || random.nextFloat() < (level().getDifficulty() == Difficulty.HARD ? 0.5F : 0.1F)) {
            ItemStack stack = ItemManager.INSTANCE.getRandomMainHand(random);
            setItemSlot(EquipmentSlot.MAINHAND, stack);

            if (level().getDifficulty().getId() >= Configs.COMMON.offhandDifficultyLimit.get().getId() && random.nextDouble() > Configs.COMMON.offhandSpawnChance.get()) {
                if (stack.getItem() instanceof ProjectileWeaponItem && Configs.COMMON.allowTippedArrows.get()) {
                    var potions = new ArrayList<>(ForgeRegistries.POTIONS.getKeys());
                    potions.removeAll(Configs.COMMON.tippedArrowBlocklist);
                    if (!potions.isEmpty()) {
                        var potion = ForgeRegistries.POTIONS.getValue(potions.get(random.nextInt(potions.size())));
                        setItemSlot(EquipmentSlot.OFFHAND, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion));
                    }
                } else {
                    setItemSlot(EquipmentSlot.OFFHAND, ItemManager.INSTANCE.getRandomOffHand(random));
                    getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Shield Bonus", random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if (!level().isClientSide) {
            setCombatTask();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (IS_CHILD.equals(key)) {
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    public int getExperienceReward() {
        if (isBaby()) {
            xpReward = (int) ((float) xpReward * 2.5F);
        }

        return super.getExperienceReward();
    }

    @Override
    public float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return isBaby() ? 0.93F : 1.62F;
    }

    @Override
    public boolean isFallFlying() {
        return false;
    }

    @Override
    public double getMyRidingOffset() {
        return isBaby() ? 0.0D : -0.45D;
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return (stack.getItem() != Items.EGG || !isBaby() || !isPassenger()) && super.canHoldItem(stack);
    }

    @Override
    public void tick() {
        CompoundTag tag = this.getPersistentData();
        if (!this.level().isClientSide()) {
            if (tag.getInt("CSlifetime") >= 1) {
                tag.putInt("CSlifetime", tag.getInt("CSlifetime") - 1);
            }
            if (tag.getInt("CSlifetime") == 1) {
                this.discard();
                this.getOwner().setHealth(this.getHealth());
            }
            if (!this.level().getLevelData().getGameRules().getBoolean(GameRuleInit.NPC_SHOULD_SPAWN)) {
                this.discard();
            }
        }
        super.tick();
        xCloakO = xCloak;
        yCloakO = yCloak;
        zCloakO = zCloak;
        double x = getX() - xCloak;
        double y = getY() - yCloak;
        double z = getZ() - zCloak;
        double maxCapeAngle = 10.0D;
        if (x > maxCapeAngle) {
            xCloak = getX();
            xCloakO = xCloak;
        }

        if (z > maxCapeAngle) {
            zCloak = getZ();
            zCloakO = zCloak;
        }

        if (y > maxCapeAngle) {
            yCloak = getY();
            yCloakO = yCloak;
        }

        if (x < -maxCapeAngle) {
            xCloak = getX();
            xCloakO = xCloak;
        }

        if (z < -maxCapeAngle) {
            zCloak = getZ();
            zCloakO = zCloak;
        }

        if (y < -maxCapeAngle) {
            yCloak = getY();
            yCloakO = yCloak;
        }

        xCloak += x * 0.25D;
        zCloak += z * 0.25D;
        yCloak += y * 0.25D;
    }

    @Override
    public boolean doHurtTarget(Entity entityIn) {
        boolean result = super.doHurtTarget(entityIn);
        if (result)
            swing(InteractionHand.MAIN_HAND);
        return result;
    }

    @Override
    public boolean isBaby() {
        return getEntityData().get(IS_CHILD);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setBaby(boolean isChild) {
        super.setBaby(isChild);
        getEntityData().set(IS_CHILD, isChild);
        if (!level().isClientSide) {
            AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(BABY_SPEED_BOOST);
            if (isChild) {
                attribute.addTransientModifier(BABY_SPEED_BOOST);
            }
        }
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player player, Mob child) {
        if (child instanceof PlayerMobEntity) {
            ((PlayerMobEntity) child).setUsername(getUsername());
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        RandomSource randomSource = level.getRandom();
        populateDefaultEquipmentSlots(randomSource, difficulty);
        populateDefaultEquipmentEnchantments(randomSource, difficulty);

        if (!hasUsername())
            setUsername(NameManager.INSTANCE.getRandomName());

        setCombatTask();
        float specialMultiplier = difficulty.getSpecialMultiplier();
        setCanPickUpLoot(randomSource.nextFloat() < Configs.COMMON.pickupItemsChance.get() * specialMultiplier);
        setCanBreakDoors(randomSource.nextFloat() < specialMultiplier * 0.1F);

        double rangeBonus = randomSource.nextDouble() * 1.5 * specialMultiplier;
        if (rangeBonus > 1.0)
            getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Range Bonus", rangeBonus, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.05F)
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Health Bonus", randomSource.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.15F)
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Damage Bonus", randomSource.nextDouble() + 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.2F)
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Speed Bonus", randomSource.nextDouble() * 2.0 * 0.24 + 0.01, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (randomSource.nextDouble() < Configs.COMMON.babySpawnChance.get())
            setBaby(true);

        return spawnData;
    }

    public void setCombatTask() {
        if (!level().isClientSide) {
            goalSelector.removeGoal(bowAttackGoal);
            goalSelector.removeGoal(crossbowAttackGoal);

            ItemStack itemstack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
            if (itemstack.getItem() instanceof CrossbowItem) {
                goalSelector.addGoal(2, crossbowAttackGoal);
            } else if (itemstack.getItem() instanceof BowItem) {
                bowAttackGoal.setMinAttackInterval(level().getDifficulty() != Difficulty.HARD ? 20 : 40);
                goalSelector.addGoal(2, bowAttackGoal);
            }
        }
    }

    public void setCanBreakDoors(boolean enabled) {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            if (canBreakDoors != enabled) {
                canBreakDoors = enabled;
                ((GroundPathNavigation) getNavigation()).setCanOpenDoors(enabled || canOpenDoor());
                if (enabled)
                    goalSelector.addGoal(1, breakDoorGoal);
                else
                    goalSelector.removeGoal(breakDoorGoal);
            }
        } else if (canBreakDoors) {
            goalSelector.removeGoal(breakDoorGoal);
            canBreakDoors = false;
        }
    }

    public boolean canFireProjectileWeapon(Item item) {
        return item instanceof ProjectileWeaponItem weaponItem && canFireProjectileWeapon(weaponItem);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem || item instanceof CrossbowItem;
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbow, Projectile projectile, float angle) {
        shootCrossbowProjectile(this, target, projectile, angle, 1.6F);
    }

    public boolean isChargingCrossbow() {
        return entityData.get(IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean isCharging) {
        entityData.set(IS_CHARGING_CROSSBOW, isCharging);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        noActionTime = 0;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack weaponStack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
        if (weaponStack.getItem() instanceof CrossbowItem) {
            performCrossbowAttack(this, 1.6F);
        } else {
            ItemStack itemstack = getProjectile(weaponStack);
            AbstractArrow mobArrow = ProjectileUtil.getMobArrow(this, itemstack, velocity);
            if (getMainHandItem().getItem() instanceof BowItem bowItem)
                mobArrow = bowItem.customArrow(mobArrow);
            double x = target.getX() - getX();
            double y = target.getY(1D / 3D) - mobArrow.getY();
            double z = target.getZ() - getZ();
            double d3 = Math.sqrt(x * x + z * z);
            mobArrow.shoot(x, y + d3 * 0.2F, z, 1.6F, 14 - level().getDifficulty().getId() * 4);
            playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
            level().addFreshEntity(mobArrow);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (getCustomName() != null && getCustomName().getString().isEmpty())
            compound.remove("CustomName");

        String username = getUsername().getCombinedNames();
        if (!StringUtil.isNullOrEmpty(username))
            compound.putString("Username", username);

        compound.putBoolean("CanBreakDoors", canBreakDoors);
        compound.putBoolean("IsBaby", isBaby());
        if (profile != null && profile.isComplete())
            compound.put("Profile", NbtUtils.writeGameProfile(new CompoundTag(), profile));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        String username = compound.getString("Username");
        if (!StringUtil.isNullOrEmpty(username)) {
            setUsername(username);
        } else {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        setBaby(compound.getBoolean("IsBaby"));
        setCanBreakDoors(compound.getBoolean("CanBreakDoors"));

        if (compound.contains("Profile", Tag.TAG_COMPOUND)) {
            profile = NbtUtils.readGameProfile(compound.getCompound("Profile"));
        }
        if (compound.contains("sequence")) {
            this.setSequence(compound.getInt("sequence"));
        }

        setCombatTask();
    }


    public void setPathway (Supplier<? extends BeyonderClass> classToBeSet) {
        this.requiredClass = classToBeSet;
    }
    public void setSpirituality(int spirituality) {
        this.entityData.set(SPIRITUALITY, spirituality);
    }
    public int getSpirituality() {
        return this.entityData.get(SPIRITUALITY);
    }
    public void setMaxSpirituality(int maxSpirituality) {
        this.entityData.set(MAXSPIRITUALITY, maxSpirituality);
    }
    public int getMaxSpirituality() {
        return this.entityData.get(MAXSPIRITUALITY);
    }
    public Supplier<? extends BeyonderClass> getCurrentPathway() {
        return this.requiredClass;
    }

    @Override
    public Component getCustomName() {
        Component customName = super.getCustomName();
        String displayName = getUsername().getDisplayName();
        return customName != null && !customName.getString().isEmpty() ? customName : !StringUtil.isNullOrEmpty(displayName) ? Component.literal(displayName) : null;
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName() || !StringUtil.isNullOrEmpty(getUsername().getDisplayName());
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.PLAYER_MOB_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundInit.PLAYER_MOB_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.PLAYER_MOB_AMBIENT.get();
    }

    @Nullable
    public GameProfile getProfile() {
        if (profile == null && hasUsername()) {
            profile = new GameProfile(null, getUsername().getSkinName());
            ProfileUpdater.updateProfile(this);
        }
        return profile;
    }

    public void setProfile(@Nullable GameProfile profile) {
        this.profile = profile;
    }

    public boolean hasUsername() {
        return !StringUtil.isNullOrEmpty(getEntityData().get(NAME));
    }

    public PlayerName getUsername() {
        if (!hasUsername() && !level().isClientSide()) {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        return new PlayerName(getEntityData().get(NAME));
    }
    public int getCurrentSequence() {
        return this.entityData.get(SEQUENCE);
    }
    public void setSequence(int sequence) {
        this.entityData.set(SEQUENCE, sequence);
    }


    public void setUsername(String username) {
        PlayerName playerName = new PlayerName(username);
        if (playerName.noDisplayName()) {
            Optional<PlayerName> name = NameManager.INSTANCE.findName(username);
            if (name.isPresent())
                playerName = name.get();
        }
        NameManager.INSTANCE.useName(playerName);
        setUsername(playerName);
    }

    public void setUsername(PlayerName name) {
        PlayerName oldName = hasUsername() ? getUsername(): null;
        getEntityData().set(NAME, name.getCombinedNames());

        if ("Herobrine".equals(name.getDisplayName())) {
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Herobrine Damage Bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Herobrine Speed Bonus", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (!Objects.equals(oldName, name)) {
            setProfile(null);
            getProfile();
        }
    }
    public boolean useSpirituality(int amount) {
        if (this.getSpirituality() - amount < 0) {
            return false;
        }
        this.setSpirituality(Mth.clamp(this.getSpirituality() - amount, 0, getMaxSpirituality()));
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public SkinManager.SkinTextureCallback getSkinCallback() {
        return (type, location, profileTexture) -> {
            switch (type) {
                case SKIN -> {
                    skin = location;
                    skinAvailable = true;
                }
                case CAPE -> {
                    cape = location;
                    capeAvailable = true;
                }
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isTextureAvailable(MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.SKIN)
            return skinAvailable;
        return capeAvailable;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture(MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.SKIN)
            return skin;
        return cape;
    }
    public LivingEntity getOwner() {
        return this.owner;
    }
    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

}

