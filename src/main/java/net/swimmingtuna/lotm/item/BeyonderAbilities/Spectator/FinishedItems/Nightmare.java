package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Nightmare extends SimpleAbilityItem {

    public Nightmare(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 5, 100, 110, 35, 35);
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        Player player = pContext.getPlayer();

        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        nightmareNew(player, pContext.getClickedPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack pStack, LivingEntity player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        nightmare(player, player.level(), pInteractionTarget.getOnPos());
        return InteractionResult.SUCCESS;
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 35, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 35, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    public static void nightmareNew(LivingEntity livingEntity, BlockPos targetPos) {
        AttributeInstance dreamIntoReality = livingEntity.getAttribute(ModAttributes.DIR.get());
        int sequence = BeyonderUtil.getSequence(livingEntity);
        Level level = livingEntity.level();
        int dir = (int) dreamIntoReality.getValue();
        double radius = BeyonderUtil.getDamage(livingEntity).get(ItemInit.NIGHTMARE.get());
        float damagePlayer = ((float) (65.0 * dir) - (sequence * 2));
        int duration = 300 - (sequence * 20);
        AABB boundingBox = new AABB(targetPos).inflate(radius);
        level.getEntitiesOfClass(LivingEntity.class, boundingBox, LivingEntity::isAlive).forEach(living -> {
           String name = living.getDisplayName().getString();
           CompoundTag tag = living.getPersistentData();
           if (living != livingEntity && !BeyonderUtil.areAllies(livingEntity, living)) {
               living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, duration, 1, false, false));
               if (tag.getInt("NightmareTimer") < 300) {
                   int addToAmount = sequence < 3 ? 200 : 100;
                   tag.putInt("NightmareTimer", addToAmount);
                   livingEntity.sendSystemMessage(Component.literal(name + "'s nightmare value is " + tag.getInt("NightmareTimer") + " / 300"));
               } else {
                   tag.putInt("NightmareTimer", 0);
                   BeyonderUtil.applyMentalDamage(livingEntity, living, damagePlayer);
               }
           }
        });
    }

    private void nightmare(LivingEntity player, Level level, BlockPos targetPos) {
        if (!player.level().isClientSide()) {
            AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
            int sequence = BeyonderUtil.getSequence(player);
            int dir = (int) dreamIntoReality.getValue();
            double radius = BeyonderUtil.getDamage(player).get(ItemInit.NIGHTMARE.get());
            float damagePlayer = ((float) (65.0 * dir) - (sequence * 2));
            float damageMob = ((float) (20.0 * dir) - (sequence));
            int duration = 300 - (sequence * 20);
            AABB boundingBox = new AABB(targetPos).inflate(radius);
            level.getEntitiesOfClass(LivingEntity.class, boundingBox, entity -> entity.isAlive()).forEach(livingEntity -> {
                AttributeInstance nightmareAttribute = livingEntity.getAttribute(ModAttributes.NIGHTMARE.get());
                String playerName = livingEntity.getDisplayName().getString();
                if (livingEntity != player && !BeyonderUtil.areAllies(player, livingEntity)) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, duration, 1, false, false));
                    if (livingEntity instanceof Player) {
                        if (nightmareAttribute.getValue() < 3) {
                            if (sequence <= 2) {
                                nightmareAttribute.setBaseValue(nightmareAttribute.getValue() + 2);
                            } else {
                                nightmareAttribute.setBaseValue(nightmareAttribute.getValue() + 1);
                            }
                        }
                        if (nightmareAttribute.getValue() >= 3) {
                            BeyonderUtil.applyMentalDamage(player, livingEntity, damagePlayer);
                            nightmareAttribute.setBaseValue(0);
                        }
                        player.sendSystemMessage(Component.literal(playerName + "'s nightmare value is: " + (int) nightmareAttribute.getValue()).withStyle(BeyonderUtil.getStyle(player)));

                    } else {
                        BeyonderUtil.applyMentalDamage(player, livingEntity, damageMob);
                    }
                }
            });
        }
    }

    public static void nightmareTick(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            AttributeInstance nightmareAttribute = player.getAttribute(ModAttributes.NIGHTMARE.get());
            CompoundTag tag = player.getPersistentData();
            int nightmareTimer = tag.getInt("NightmareTimer");
            int matterAccelerationBlockTimer = player.getPersistentData().getInt("matterAccelerationBlockTimer");
            if (matterAccelerationBlockTimer >= 1) {
                player.getPersistentData().putInt("matterAccelerationBlockTimer", matterAccelerationBlockTimer - 1);
            }
            if (nightmareAttribute.getValue() >= 1) {
                nightmareTimer++;
                if (nightmareTimer >= 600) {
                    nightmareAttribute.setBaseValue(0);
                    nightmareTimer = 0;
                }
            } else {
                nightmareTimer = 0;
            }
            tag.putInt("NightmareTimer", nightmareTimer);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, makes all entities around the clicked block enter a nightmare, plunging them into darkness. If a player is hit by this three times in 30 seconds, they take immense damage. If a mob is hit, they take damage immediately, but less."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("45 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }
}
