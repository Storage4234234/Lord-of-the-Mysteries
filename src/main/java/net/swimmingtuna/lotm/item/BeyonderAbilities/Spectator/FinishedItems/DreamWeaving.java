package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DreamWeaving extends SimpleAbilityItem {

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public DreamWeaving(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 3, 250, 0, 150, 150);
    }

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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 150, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 150, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }


    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player, this, 500 / BeyonderUtil.getDreamIntoReality(player));
        useSpirituality(player);
        dreamWeave(player, interactionTarget);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on a living entity, brings their nightmares into reality, giving them darkness temporarily and summoning a random array of mobs around the target"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("250").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("25 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private static void spawnMobsAroundTarget(LivingEntity interactionTarget, EntityType<? extends Mob> mobEntityType, LivingEntity entity, Level level, double x, double y, double z, int numberOfMobs) {
        if (!level.isClientSide()) {
            for (int i = 0; i < numberOfMobs; i++) {
                Mob mob = mobEntityType.create(level);
                spawnEntityInRadius(mob, level, x, y, z);
                BeyonderHolder.updateMaxHealthModifier(mob, 551);
                mob.getPersistentData().putUUID("dreamWeavingUUID", interactionTarget.getUUID());
                mob.setTarget(entity);
            }
        }
    }

    public static void dreamWeaving(LivingEntity entity) {
        //DREAM WEAVING
        if (entity != null) {
            AttributeInstance maxHp = entity.getAttribute(Attributes.MAX_HEALTH);
            if (maxHp != null) {
                if (entity instanceof Player || maxHp.getBaseValue() != 551) {
                    return;
                }
                int deathTimer = entity.getPersistentData().getInt("DeathTimer");
                entity.getPersistentData().putInt("DeathTimer", deathTimer + 1);
                if (deathTimer >= 300) {
                    if (entity.getPersistentData().contains("dreamWeavingUUID")) {
                        UUID targetUUID = entity.getPersistentData().getUUID("dreamWeavingUUID");
                        LivingEntity livingEntity = BeyonderUtil.getEntityFromUUID(entity.level(), targetUUID);
                        if (livingEntity.isAlive() && entity instanceof Mob mob) {
                            mob.setTarget(livingEntity);
                        }
                    }
                    entity.remove(Entity.RemovalReason.KILLED);
                }
            }
        }
    }

    private static void spawnEntityInRadius(Mob entity, Level level, double x, double y, double z) {
        if (!level.isClientSide()) {
            Random random = new Random();
            double angle = random.nextDouble() * 2 * Math.PI;
            double xOffset = 10 * Math.cos(angle);
            double zOffset = 10 * Math.sin(angle);

            entity.moveTo(x + xOffset, y + 1, z + zOffset);
            level.addFreshEntity(entity);
        }
    }

    public void dreamWeave(LivingEntity player, LivingEntity interactionTarget) {
        Level level = player.level();
        if (!level.isClientSide()) {
            double x = interactionTarget.getX();
            double y = interactionTarget.getY();
            double z = interactionTarget.getZ();
            interactionTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 150, 1, false, false));
            RandomSource random = player.getRandom();
            int times = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.DREAM_WEAVING.get());
            for (int i = 0; i < times; i++) {
                int randomNumber = random.nextInt(10);
                EntityType<? extends Mob> entityType = MOB_TYPES.get(randomNumber);
                spawnMobsAroundTarget(interactionTarget, entityType, interactionTarget, level, x, y, z, BeyonderUtil.getDreamIntoReality(player) == 2 ? 2 : 1);
            }
        }
    }

    private static final List<EntityType<? extends Mob>> MOB_TYPES = List.of(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.CREEPER,
            EntityType.ENDERMAN,
            EntityType.RAVAGER,
            EntityType.VEX,
            EntityType.ENDERMITE,
            EntityType.SPIDER,
            EntityType.WITHER,
            EntityType.PHANTOM
    );

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 50;
        }
        return 0;
    }
}