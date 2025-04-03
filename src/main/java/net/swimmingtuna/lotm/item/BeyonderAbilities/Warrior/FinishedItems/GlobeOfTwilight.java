package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.GlobeOfTwilightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class GlobeOfTwilight extends SimpleAbilityItem {


    public GlobeOfTwilight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 2500, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        globeOfTwilight(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        globeOfTwilightTarget(player, interactionTarget);
        return InteractionResult.SUCCESS;
    }

    public static void globeOfTwilight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(100))) {
                if (living != livingEntity && !BeyonderUtil.areAllies(livingEntity, living)) {
                    living.getPersistentData().putInt("globeOfTwilightX", (int) living.getX());
                    living.getPersistentData().putInt("globeOfTwilightY", (int) living.getY());
                    living.getPersistentData().putInt("globeOfTwilightZ", (int) living.getZ());
                    living.getPersistentData().putInt("globeOfTwilight", 50 - (BeyonderUtil.getSequence(living) * 5));
                    living.getPersistentData().putInt("globeOfTwilightSize", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.GLOBEOFTWILIGHT.get()));
                }
            }
        }
    }

    public static void globeOfTwilightTarget(LivingEntity livingEntity, LivingEntity target) {
        if (!livingEntity.level().isClientSide()) {
            target.getPersistentData().putInt("globeOfTwilightX", (int) target.getX());
            target.getPersistentData().putInt("globeOfTwilightY", (int) target.getY());
            target.getPersistentData().putInt("globeOfTwilightZ", (int) target.getZ());
            target.getPersistentData().putInt("globeOfTwilight", 50 - (BeyonderUtil.getSequence(target) * 5));
            target.getPersistentData().putInt("globeOfTwilightSize", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.GLOBEOFTWILIGHT.get()) * 5);
        }
    }

    public static void globeOfTwilightTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide()) {
            int counter = tag.getInt("globeOfTwilight");
            int size = tag.getInt("globeOfTwilightSize");
            int x = tag.getInt("globeOfTwilightX");
            int y = tag.getInt("globeOfTwilightY");
            int z = tag.getInt("globeOfTwilightZ");
            if (counter >= 1) {
                tag.putInt("globeOfTwilight", counter - 1);
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    double halfSize = size / 10.0;
                    Vector3f orangeColor = new Vector3f(1.0F, 0.5F, 0.0F);
                    for (int i = 0; i < 12; i++) {
                        double startX, startY, startZ, endX, endY, endZ;

                        if (i < 4) {
                            startX = x + (i == 0 || i == 3 ? -halfSize : halfSize);
                            startZ = z + (i == 0 || i == 1 ? -halfSize : halfSize);
                            startY = y - halfSize;
                            endX = x + (i == 1 || i == 2 ? halfSize : -halfSize);
                            endZ = z + (i == 2 || i == 3 ? halfSize : -halfSize);
                            endY = y - halfSize;
                        } else if (i < 8) {
                            startX = x + (i == 4 || i == 7 ? -halfSize : halfSize);
                            startZ = z + (i == 4 || i == 5 ? -halfSize : halfSize);
                            startY = y + halfSize;
                            endX = x + (i == 5 || i == 6 ? halfSize : -halfSize);
                            endZ = z + (i == 6 || i == 7 ? halfSize : -halfSize);
                            endY = y + halfSize;
                        } else {
                            int j = i - 8;
                            startX = x + (j == 0 || j == 2 ? -halfSize : halfSize);
                            startZ = z + (j == 0 || j == 1 ? -halfSize : halfSize);
                            startY = y - halfSize;
                            endX = startX;
                            endZ = startZ;
                            endY = y + halfSize;
                        }
                        double stepSize = 0.5;
                        double distX = endX - startX;
                        double distY = endY - startY;
                        double distZ = endZ - startZ;
                        double dist = Math.sqrt(distX * distX + distY * distY + distZ * distZ);
                        int steps = (int) Math.ceil(dist / stepSize);
                        for (int step = 0; step <= steps; step++) {
                            double factor = steps > 0 ? (double) step / steps : 0;
                            double particleX = startX + distX * factor;
                            double particleY = startY + distY * factor;
                            double particleZ = startZ + distZ * factor;
                            serverLevel.sendParticles(new DustParticleOptions(orangeColor, 1), particleX, particleY, particleZ, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
            if (counter == 1) {
                float random = BeyonderUtil.getRandomInRange(5);
                float random2 = BeyonderUtil.getRandomInRange(5);
                GlobeOfTwilightEntity globeOfTwilight = new GlobeOfTwilightEntity(EntityInit.GLOBE_OF_TWILIGHT_ENTITY.get(), livingEntity.level());
                ScaleTypes.BASE.getScaleData(globeOfTwilight).setTargetScale(Math.max(1,size / 5));
                globeOfTwilight.setRandomPitch(random);
                globeOfTwilight.setRandomYaw(random2);
                globeOfTwilight.teleportTo(x,y,z);
                tag.putInt("globeOfTwilight", 0);
                livingEntity.level().addFreshEntity(globeOfTwilight);
            }
        }
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, create a globe of twilight around each entity around you, or a giant one on an entity if you click on one. Those in a globe of twliight will be completely frozen."));
        tooltipComponents.add(Component.literal("Left Click for Twilight: Beam"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("2500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

}

