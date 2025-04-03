package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.GuardianBoxEntity;
import net.swimmingtuna.lotm.entity.HurricaneOfLightEntity;
import net.swimmingtuna.lotm.entity.SwordOfTwilightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.Renderer.SwordOfTwilightRenderer;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.SilverSwordManifestation.findClosestEmptySlot;

public class SwordOfTwilight extends SwordItem implements GeoItem {


    public SwordOfTwilight(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                if (!BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.WARRIOR.get(), 2)) {
                    removeItemFromSlot(livingEntity, stack);
                } else {
                    if (BeyonderUtil.getSpirituality(livingEntity) >= 150) {
                        BeyonderUtil.useSpirituality(livingEntity, 150);
                    } else {
                        removeItemFromSlot(livingEntity, stack);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pTarget.getPersistentData().putInt("age", pTarget.getPersistentData().getInt("age") + 900);
        if (pTarget instanceof Player player) {
            player.displayClientMessage(Component.literal("You were rapidly aged").withStyle(BeyonderUtil.ageStyle(pTarget)).withStyle(ChatFormatting.BOLD),true);
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }




    public static void twilightSwordTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide()) {
            if (tag.contains("twilightSwordOwnerUUID")) {
                int x = tag.getInt("twilightSwordSpawnTick");
                UUID uuid = tag.getUUID("twilightSwordOwnerUUID");
                if (x >= 1) {
                    tag.putInt("twilightSwordSpawnTick", x - 1);
                    LivingEntity swordOwner = BeyonderUtil.getEntityFromUUID(livingEntity.level(), uuid);
                    if (swordOwner.isAlive()) {
                        if (x == 21) {
                            SwordOfTwilightEntity swordOfTwilight = new SwordOfTwilightEntity(EntityInit.SWORD_OF_TWILIGHT_ENTITY.get(), livingEntity.level());
                            swordOfTwilight.setOwner(swordOwner);
                            BeyonderUtil.setScale(swordOfTwilight, 30);
                            int position = livingEntity.level().random.nextInt(4);
                            float yaw = 0;
                            double offsetX = 0;
                            double offsetZ = 0;
                            float swordYaw = 0;
                            float scaleSubtraction = BeyonderUtil.getScale(swordOfTwilight) / 2;
                            switch (position) {
                                case 0:
                                    swordOfTwilight.getPersistentData().putInt("swordOfTwilightCase", 0);
                                    offsetX = (Math.sin(Math.toRadians(yaw - 90)) * 2) - (scaleSubtraction + 2);
                                    offsetZ = Math.cos(Math.toRadians(yaw - 90)) * 2;
                                    swordYaw = yaw + 90;
                                    break;
                                case 1:
                                    swordOfTwilight.getPersistentData().putInt("swordOfTwilightCase", 1);
                                    offsetX = (Math.sin(Math.toRadians(yaw + 90)) * 2) + (scaleSubtraction + 2);
                                    offsetZ = Math.cos(Math.toRadians(yaw + 90)) * 2;
                                    swordYaw = yaw - 90;
                                    break;
                                case 2:
                                    swordOfTwilight.getPersistentData().putInt("swordOfTwilightCase", 2);
                                    offsetX = Math.sin(Math.toRadians(yaw)) * 2;
                                    offsetZ = (Math.cos(Math.toRadians(yaw)) * 2) + (scaleSubtraction + 2);
                                    swordYaw = yaw + 180;
                                    break;
                                case 3:
                                    swordOfTwilight.getPersistentData().putInt("swordOfTwilightCase", 3);
                                    offsetX = Math.sin(Math.toRadians(yaw + 180)) * 2;
                                    offsetZ = (Math.cos(Math.toRadians(yaw + 180)) * 2) - (scaleSubtraction + 2);
                                    swordYaw = yaw;
                                    break;
                            }
                            swordOfTwilight.teleportTo(livingEntity.getX() + offsetX, livingEntity.getY() - 30, livingEntity.getZ() + offsetZ);
                            swordOfTwilight.setYaw(swordYaw % 360);
                            swordOfTwilight.setYRot(swordYaw % 360);
                            livingEntity.level().addFreshEntity(swordOfTwilight);
                        }
                    }
                }
            }
            if (tag.getInt("returnSwordOfTwilight") >= 1) {
                tag.putInt("returnSwordOfTwilight", tag.getInt("returnSwordOfTwilight") - 1);
                if (tag.getInt("returnSwordOfTwilight") == 1) {
                    if (livingEntity instanceof Player player) {
                        int selectedSlot = findClosestEmptySlot(player);
                        Inventory inventory = player.getInventory();
                        inventory.setItem(selectedSlot, ItemInit.SWORDOFTWILIGHT.get().getDefaultInstance());
                    } else {
                        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemInit.SWORDOFTWILIGHT.get().getDefaultInstance());
                    }
                    tag.putInt("returnSwordOfTwilight", 0);
                }
            }
        }
    }

    public static void decrementTwilightSword(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getPersistentData().getInt("twilightSwordCooldown") >= 1) {
            event.getEntity().getPersistentData().putInt("twilightSwordCooldown", event.getEntity().getPersistentData().getInt("twilightSwordCooldown") - 1);
        }
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pPlayer.level().isClientSide()) {
            if (!pPlayer.isShiftKeyDown()) {
                Vec3 eyePosition = pPlayer.getEyePosition();
                Vec3 lookVector = pPlayer.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * 5, lookVector.y * 5, lookVector.z * 5);
                BlockHitResult blockHit = pPlayer.level().clip(new ClipContext(eyePosition, reachVector, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, pPlayer));
                if (blockHit.getType() == HitResult.Type.MISS) {
                    int sequence = BeyonderUtil.getSequence(pPlayer);
                    if (sequence >= 3) {
                        HurricaneOfLightEntity.summonHurricaneOfLightDawn(pPlayer);
                    } else {
                        HurricaneOfLightEntity.summonHurricaneOfLightDeity(pPlayer);
                    }
                    BeyonderUtil.useSpirituality(pPlayer, 1000);
                    pPlayer.getCooldowns().addCooldown(this, 400);
                }
            } else if (pPlayer.isShiftKeyDown()) {
                int boxCount = 0;
                for (GuardianBoxEntity guardianBox : pPlayer.level().getEntitiesOfClass(GuardianBoxEntity.class, pPlayer.getBoundingBox().inflate(200))) {
                    Optional<UUID> ownerUUID = Optional.of(pPlayer.getUUID());
                    if (Objects.equals(guardianBox.getOwnerUUID(), ownerUUID)) {
                        boxCount++;
                    }
                }
                if (boxCount == 0) {
                    GuardianBoxEntity guardianBoxEntity = new GuardianBoxEntity(EntityInit.GUARDIAN_BOX_ENTITY.get(), pPlayer.level());
                    int sequence = BeyonderUtil.getSequence(pPlayer);
                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(guardianBoxEntity);
                    scaleData.setTargetScale(18 - (sequence));
                    guardianBoxEntity.setOwnerUUID(pPlayer.getUUID());
                    guardianBoxEntity.teleportTo(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
                    guardianBoxEntity.setMaxHealth(500 - (sequence * 30));
                    pPlayer.level().addFreshEntity(guardianBoxEntity);

                    GuardianBoxEntity guardianBoxEntity2 = new GuardianBoxEntity(EntityInit.GUARDIAN_BOX_ENTITY.get(), pPlayer.level());
                    ScaleData scaleData2 = ScaleTypes.BASE.getScaleData(guardianBoxEntity);
                    scaleData2.setTargetScale(18 - (sequence));
                    guardianBoxEntity.setOwnerUUID(pPlayer.getUUID());
                    guardianBoxEntity.teleportTo(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
                    guardianBoxEntity.setMaxHealth(600 - (sequence * 30));
                    pPlayer.level().addFreshEntity(guardianBoxEntity2);
                    pPlayer.getCooldowns().addCooldown(this, 400);
                    BeyonderUtil.useSpirituality(pPlayer, 1000);

                }
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Activation", 0, state -> PlayState.STOP));
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SwordOfTwilightRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SwordOfTwilightRenderer();
                }
                return this.renderer;
            }
        });

    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, create a hurricane of dawn, destroying everything in it's path, aging and dealing damage to all those hit, and rapidly destroying all armor. If you're shifting, you can also create two boxes of light to protect you and your allies from enemies.").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("On swing, this will teleport to the entity you're looking at, becoming gigantic before swinging and dealing immense damage.").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("150 per second. 1000 to create the boxes of twilight and create the hurricanes.").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("20 Seconds for hurricane and box.").withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("DAWN_ITEM", ChatFormatting.GOLD);
    }


}
