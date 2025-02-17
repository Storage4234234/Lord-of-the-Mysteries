package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.MercuryLiqueficationC2S;
import net.swimmingtuna.lotm.networking.packet.SendDustParticleS2C;
import net.swimmingtuna.lotm.networking.packet.SyncShouldntRenderInvisibilityPacketS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ModArmorMaterials;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.*;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.SilverArmory.createEnchantedArmor;

public class MercuryLiquefication extends SimpleAbilityItem {


    public MercuryLiquefication(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 3, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        mercuryLiquefication(player);
        return InteractionResult.SUCCESS;
    }

    public static void mercuryLiquefication(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            boolean x = livingEntity.getPersistentData().getBoolean("mercuryLiquefication");
            livingEntity.getPersistentData().putBoolean("mercuryLiquefication", !x);
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Liquefied: " + (x ? "off" : "on")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.BOLD), true);

            }
        }
    }

    public static final Map<UUID, Boolean> lastSentStates = new HashMap<>();

    public static void mercuryLiqueficationTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        Level level = livingEntity.level();
        boolean currentState = tag.getBoolean("mercuryLiquefication");
        int trapped = tag.getInt("mercuryLiqueficationTrapped");
        int y = tag.getInt("mercuryArmorForm");
        if (!level.isClientSide() && currentState) {
            if (y == 0) {
                BeyonderUtil.useSpirituality(livingEntity, 10);
            }
            UUID playerId = livingEntity.getUUID();
            Boolean lastState = lastSentStates.get(playerId);
            if (lastState == null || lastState != currentState) {
                LOTMNetworkHandler.sendToAllPlayers(new SyncShouldntRenderInvisibilityPacketS2C(currentState, playerId));
                lastSentStates.put(playerId, currentState);
            }
            Vec3 lookVec = livingEntity.getLookAngle();
            Random random = new Random();
            float scale = ScaleTypes.BASE.getScaleData(livingEntity).getScale();
            int particleCount = Math.max(20, (int) (20 * Math.sqrt(scale)));
            if (y == 0) {
                for (int i = 0; i < particleCount; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 0.5 * scale;
                    double offsetY = (random.nextDouble() - 0.5) * 0.5 * scale;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.5 * scale;
                    double posX = livingEntity.getX() + offsetX - (lookVec.x * 0.5 * scale);
                    double posY = livingEntity.getY() + offsetY + (0.5 * scale);
                    double posZ = livingEntity.getZ() + offsetZ - (lookVec.z * 0.5 * scale);
                    double motionX = (-lookVec.x * 0.1 + (random.nextDouble() - 0.5) * 0.02) * Math.sqrt(scale);
                    double motionY = (0.05 + (random.nextDouble() - 0.5) * 0.02) * Math.sqrt(scale);
                    double motionZ = (-lookVec.z * 0.1 + (random.nextDouble() - 0.5) * 0.02) * Math.sqrt(scale);
                    LOTMNetworkHandler.sendToAllPlayers(new SendDustParticleS2C(0.75f, 0.75f, 0.75f, scale, posX, posY, posZ, motionX, motionY, motionZ));
                }
            }
            if (livingEntity.tickCount % 20 == 0) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 1, false, false));
            }
            if (livingEntity instanceof Player player) {
                Abilities playerAbilites = player.getAbilities();
                playerAbilites.mayfly = true;
                playerAbilites.setFlyingSpeed(0.1F);
                player.onUpdateAbilities();
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));
                }
            }
        }
        if (!livingEntity.level().isClientSide()) {
            if (tag.getInt("mercuryLiqueficationCooldown") >= 1) {
                tag.putInt("mercuryLiqueficationCooldown", tag.getInt("mercuryLiqueficationCooldown") - 1);
            }
            if (livingEntity instanceof Player player && !currentState) {
                Abilities playerAbilites = player.getAbilities();
                playerAbilites.setFlyingSpeed(0.05F);
                playerAbilites.mayfly = false;
                playerAbilites.flying = false;
                player.onUpdateAbilities();
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));
                }
            }
        }
        if (!livingEntity.level().isClientSide() && trapped >= 1) {
            tag.putInt("mercuryLiqueficationTrapped", trapped - 1);
            livingEntity.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            livingEntity.addEffect(new MobEffectInstance(ModEffects.ABILITY_WEAKNESS.get(), 5, 1, true, true));
            livingEntity.setDeltaMovement(0, 0, 0);
            livingEntity.hurtMarked = true;
            float scale = ScaleTypes.BASE.getScaleData(livingEntity).getScale();
            Random random = new Random();
            double offsetX = (random.nextDouble() - 0.5) * 0.5 * scale;
            double offsetY = (random.nextDouble() - 0.5) * 0.5 * scale;
            double offsetZ = (random.nextDouble() - 0.5) * 0.5 * scale;
            double posX = livingEntity.getX() + offsetX;
            double posY = livingEntity.getY() + offsetY;
            double posZ = livingEntity.getZ() + offsetZ;
            int particleCount = Math.max(10, (int) (10 * Math.sqrt(scale)));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 1, true, true));
            for (int i = 0; i < particleCount; i++) {
                LOTMNetworkHandler.sendToAllPlayers(new SendDustParticleS2C(0.75f, 0.75f, 0.75f, scale, posX, posY, posZ, 0, 0, 0));
            }
        }
    }

    private static long lastActivationTime = 0;
    private static final long COOLDOWN_MS = 250;

    public static void mercuryRightClick(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getEntity().level().isClientSide()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastActivationTime >= COOLDOWN_MS) {
                lastActivationTime = currentTime;
                LOTMNetworkHandler.sendToServer(new MercuryLiqueficationC2S());
            }
        }
    }

    public static void mercuryArmorRightClick(PlayerInteractEvent.EntityInteract event) {
        LivingEntity interactor = event.getEntity();
        Entity target = event.getTarget();
        if (target instanceof LivingEntity livingTarget) {
            equipSilverArmor(interactor, livingTarget);
        }
    }

    public static void equipSilverArmor(LivingEntity user, LivingEntity targetEntity) {
        if (targetEntity.level().isClientSide()) return;
        if (user.getPersistentData().getBoolean("mercuryLiquefication")) {
            if (BeyonderUtil.isAllyOf(user, targetEntity)) {
                if (user != targetEntity) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastActivationTime >= COOLDOWN_MS) {
                        CompoundTag tag = targetEntity.getPersistentData();
                        user.getPersistentData().putUUID("mercuryArmor", targetEntity.getUUID());
                        user.getPersistentData().putInt("mercuryArmorForm", 10);
                        targetEntity.getPersistentData().putInt("mercuryArmorEquipped", 10);
                        CompoundTag armorData = new CompoundTag();
                        ListTag armorItems = new ListTag();
                        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                            ItemStack armorStack = targetEntity.getItemBySlot(slot);
                            if (!armorStack.isEmpty()) {
                                CompoundTag slotTag = new CompoundTag();
                                slotTag.putInt("mercurySlot", slot.getIndex());
                                armorStack.save(slotTag);
                                armorItems.add(slotTag);
                            }
                            targetEntity.setItemSlot(slot, ItemStack.EMPTY);
                        }
                        armorData.put("mercuryStoredArmor", armorItems);
                        tag.put("mercuryArmorStorage", armorData);
                        targetEntity.setItemSlot(EquipmentSlot.HEAD, createEnchantedArmor(ItemInit.SILVER_HELMET.get().getDefaultInstance()));
                        targetEntity.setItemSlot(EquipmentSlot.CHEST, createEnchantedArmor(ItemInit.SILVER_CHESTPLATE.get().getDefaultInstance()));
                        targetEntity.setItemSlot(EquipmentSlot.LEGS, createEnchantedArmor(ItemInit.SILVER_LEGGINGS.get().getDefaultInstance()));
                        targetEntity.setItemSlot(EquipmentSlot.FEET, createEnchantedArmor(ItemInit.SILVER_BOOTS.get().getDefaultInstance()));
                    }
                }
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastActivationTime >= COOLDOWN_MS) {
                lastActivationTime = currentTime;
                CompoundTag tag = user.getPersistentData();
                CompoundTag armorData = new CompoundTag();
                ListTag armorItems = new ListTag();
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack armorStack = user.getItemBySlot(slot);
                    if (!armorStack.isEmpty()) {
                        CompoundTag slotTag = new CompoundTag();
                        slotTag.putInt("mercurySlot", slot.getIndex());
                        armorStack.save(slotTag);
                        armorItems.add(slotTag);
                    }
                    user.setItemSlot(slot, ItemStack.EMPTY);
                }
                armorData.put("mercuryStoredArmor", armorItems);
                tag.put("mercuryArmorStorage", armorData);
            }
        }
    }


    public static void mercuryArmorTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("mercuryArmorEquipped");
        int y = tag.getInt("mercuryArmorForm");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            tag.putInt("mercuryArmorEquipped", x - 1);
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(150))) {
                if (living.getPersistentData().contains("mercuryArmor")) {
                    if (living.getPersistentData().getUUID("mercuryArmor").equals(livingEntity.getUUID())) {
                        living.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                        living.getPersistentData().putInt("mercuryArmorForm", 10);
                    }
                    if (!hasFullSilverArmor(livingEntity)) {
                        if (tag.contains("mercuryArmorStorage")) {
                            CompoundTag armorData = tag.getCompound("mercuryArmorStorage");
                            ListTag armorItems = armorData.getList("mercuryStoredArmor", 10);
                            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                                livingEntity.setItemSlot(slot, ItemStack.EMPTY);
                            }
                            for (int i = 0; i < armorItems.size(); i++) {
                                CompoundTag slotTag = armorItems.getCompound(i);
                                int slotIndex = slotTag.getInt("mercurySlot");
                                ItemStack armorStack = ItemStack.of(slotTag);
                                EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slotIndex);
                                if (slot != null) {
                                    livingEntity.setItemSlot(slot, armorStack);
                                }
                            }
                            tag.remove("mercuryArmorStorage");
                        }

                        tag.putInt("mercuryArmorEquipped", 0);
                        living.getPersistentData().putInt("mercuryArmorForm", 0);
                    } else {
                        tag.putInt("mercuryArmorEquipped", 10);
                    }
                }
            }
        }
        if (!livingEntity.level().isClientSide() && y >= 1) {
            BeyonderUtil.useSpirituality(livingEntity, 5);
            tag.putInt("mercuryArmorForm", y - 1);
            if (!livingEntity.isShiftKeyDown()) {
                if (livingEntity instanceof Player player && player.tickCount % 10 == 0) {
                    player.displayClientMessage(Component.literal("Shift to unequip yourself").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD), true);
                }
            } else {
                tag.putInt("mercuryArmorForm", 0);
                for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(10))) {
                    living.getPersistentData().putInt("mercuryArmorEquipped", 0);
                    CompoundTag pTag = living.getPersistentData();
                    if (pTag.contains("mercuryArmorStorage")) {
                        System.out.println("detected one for " + living.getName());
                        CompoundTag armorData = pTag.getCompound("mercuryArmorStorage");
                        ListTag armorItems = armorData.getList("mercuryStoredArmor", 10);
                        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                            living.setItemSlot(slot, ItemStack.EMPTY);
                        }
                        for (int i = 0; i < armorItems.size(); i++) {
                            CompoundTag slotTag = armorItems.getCompound(i);
                            int slotIndex = slotTag.getInt("mercurySlot");
                            ItemStack armorStack = ItemStack.of(slotTag);
                            EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slotIndex);
                            if (slot != null) {
                                living.setItemSlot(slot, armorStack);
                            }
                        }
                        pTag.remove("mercuryArmorStorage");
                    }
                }
            }
        }
        if (!livingEntity.level().isClientSide() && y == 0) {
            tag.remove("mercuryArmor");

        }
    }

    public static void mercuryArmorHurt(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("mercuryArmorEquipped");
        int y = tag.getInt("mercuryArmorForm");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(15))) {
                if (living.getPersistentData().contains("mercuryArmor")) {
                    if (living.getPersistentData().getUUID("mercuryArmor") == livingEntity.getUUID()) {
                        living.hurt(event.getSource(), event.getAmount());
                        event.setAmount(0);
                    }
                }
            }
        }
    }

    public static boolean hasFullSilverArmor(LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack itemStack = entity.getItemBySlot(slot);
                if (!(itemStack.getItem() instanceof ArmorItem armor) || armor.getMaterial() != ModArmorMaterials.DAWN) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, disable or enable your liquefied state. If enabled, you will transform into a liquid mercury, able to fly around at high speeds and right click the air to separate parts of yourself to restrain nearby enemies"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}

