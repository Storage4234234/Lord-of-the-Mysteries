package net.swimmingtuna.lotm.item.BeyonderAbilities;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ScribeRecording.CapabilityScribeAbilities;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class SimpleAbilityItem extends Item implements Ability {

    protected final Supplier<? extends BeyonderClass> requiredClass;
    protected final int requiredSequence;
    protected final int requiredSpirituality;
    protected final int cooldown;
    protected final double entityReach;
    protected final double blockReach;

    protected SimpleAbilityItem(Properties properties, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, int cooldown) {
        this(properties, () -> requiredClass, requiredSequence, requiredSpirituality, cooldown, 3.0, 4.5);
    }

    protected SimpleAbilityItem(Properties properties, Supplier<? extends BeyonderClass> requiredClass, int requiredSequence, int requiredSpirituality, int cooldown) {
        this(properties, requiredClass, requiredSequence, requiredSpirituality, cooldown, 3.0, 4.5);
    }

    protected SimpleAbilityItem(Properties properties, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, int cooldown, double entityReach, double blockReach) {
        this(properties, () -> requiredClass, requiredSequence, requiredSpirituality, cooldown, entityReach, blockReach);
    }

    protected SimpleAbilityItem(Properties properties, Supplier<? extends BeyonderClass> requiredClass, int requiredSequence, int requiredSpirituality, int cooldown, double entityReach, double blockReach) {
        super(properties);
        this.requiredClass = requiredClass;
        this.requiredSequence = requiredSequence;
        this.requiredSpirituality = requiredSpirituality;
        this.cooldown = cooldown;
        this.entityReach = entityReach;
        this.blockReach = blockReach;
    }

    @Override
    public double getBlockReach() {
        return blockReach;
    }

    @Override
    public double getEntityReach() {
        return entityReach;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        Item ability = player.getItemInHand(hand).getItem();
        if (!level.isClientSide() && checkIfCanUseAbility(player)) {
            if (player.getPersistentData().getInt("abilityStrengthened") >= 1) {
                player.getPersistentData().putInt("abilityStrengthened", player.getPersistentData().getInt("abilityStrengthened") - 1);
            }
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50))) {
                if (BeyonderUtil.isBeyonderCapable(entity) && entity != player) {
                    if (BeyonderUtil.scribeLookingAtYou(player, entity)) {
                        if (entity instanceof Player pPlayer) {
                            pPlayer.displayClientMessage(Component.literal("Scribed Ability: ").append(Component.literal(player.getItemInHand(hand).getItem().toString())), true);
                            pPlayer.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                                storage.copyScribeAbility(ability);
                            });
                        }
                    }
                }
            }
            InteractionResult interactionResult = useAbility(level, player, hand);
            return new InteractionResultHolder<>(interactionResult, player.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    protected boolean checkAll(Player player) {
        if (BeyonderUtil.getPathway(player) == BeyonderClassInit.APPRENTICE.get() && BeyonderUtil.getSequence(player) <= 6 && this.requiredClass != BeyonderClassInit.APPRENTICE.get()) {
            final SimpleAbilityItem ability;
            if (player.getMainHandItem().getItem() instanceof SimpleAbilityItem) {
                ability = (SimpleAbilityItem) player.getMainHandItem().getItem();
            } else if (player.getOffhandItem().getItem() instanceof SimpleAbilityItem) {
                ability = (SimpleAbilityItem) player.getOffhandItem().getItem();
            } else {
                ability = null;
            }

            if (ability != null) {
                return player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null)
                        .map(storage -> {
                            if (storage.hasScribedAbility(ability)) {
                                storage.useScribeAbility(ability);
                                return true;
                            } else {
                                return checkAll(player, this.requiredClass.get(), this.requiredSequence, this.requiredSpirituality);
                            }
                        })
                        .orElse(false);
            }
        }
        if (player.getPersistentData().getInt("inTwilight") >= 1) {
            int x = (int) player.getPersistentData().getInt("inTwilight") / 20;
            player.displayClientMessage(Component.literal("You are frozen by twilight for " + x + " seconds." ).withStyle(ChatFormatting.RED), true);
            return false;
        }
        return checkAll(player, this.requiredClass.get(), this.requiredSequence, this.requiredSpirituality);
    }


    public int getSpirituality() {
        return this.requiredSpirituality;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!level.isClientSide()) {
            if (player.getPersistentData().getInt("abilityStrengthened") >= 1) {
                player.getPersistentData().putInt("abilityStrengthened", player.getPersistentData().getInt("abilityStrengthened") - 1);
            }
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50))) {
                if (BeyonderUtil.isBeyonderCapable(entity) && entity != player) {
                    if (BeyonderUtil.scribeLookingAtYou(player, entity)) {
                        entity.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                            if (storage.hasScribedAbility(player.getItemInHand(context.getHand()).getItem())) {
                                storage.copyScribeAbility(player.getItemInHand(context.getHand()).getItem());
                            }
                        });
                    }
                }
            }
            return useAbilityOnBlock(context);
        }
        return InteractionResult.PASS;
    }


    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!player.level().isClientSide()) {
            Level level = player.level();
            if (player.getPersistentData().getInt("abilityStrengthened") >= 1) {
                player.getPersistentData().putInt("abilityStrengthened", player.getPersistentData().getInt("abilityStrengthened") - 1);
            }
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50))) {
                if (BeyonderUtil.isBeyonderCapable(entity) && entity != player) {
                    if (BeyonderUtil.scribeLookingAtYou(player, entity)) {
                        entity.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                            if (storage.hasScribedAbility(player.getItemInHand(usedHand).getItem())) {
                                storage.copyScribeAbility(player.getItemInHand(usedHand).getItem());
                            }
                        });
                    }
                }
            }
            return interactLivingEntity(stack, player, interactionTarget, usedHand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(getSpiritualityUsedText(this.requiredSpirituality));
        tooltipComponents.add(getCooldownText(this.cooldown));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    public static Component getSpiritualityUsedText(int requiredSpirituality) {
        return Component.literal("Spirituality Used: ").append(Component.literal(String.valueOf(requiredSpirituality)).withStyle(ChatFormatting.YELLOW));
    }

    public static Component getCooldownText(int cooldown) {
        return Component.literal("Cooldown: ").append(Component.literal(getTextForTicks(cooldown)).withStyle(ChatFormatting.YELLOW));
    }

    public static Component getPathwayText(BeyonderClass beyonderClass) {
        return Component.literal("Pathway: ").append(Component.literal(beyonderClass.sequenceNames().get(9)).withStyle(beyonderClass.getColorFormatting()));
    }

    public static Component getClassText(int requiredSequence, BeyonderClass beyonderClass) {
        return Component.literal("Sequence: ").append(Component.literal(requiredSequence + " - " + beyonderClass.sequenceNames().get(requiredSequence))
                .withStyle(beyonderClass.getColorFormatting()));
    }

    public static void addCooldown(Player player, Item item, int cooldown) {
        if (!player.isCreative()) {
            player.getCooldowns().addCooldown(item, cooldown);
        }
    }

    protected void addCooldown(Player player) {
        addCooldown(player, this, this.cooldown);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    protected void baseHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    private static String getTextForTicks(int ticks) {
        int min = ticks / 1200;
        double sec = (double) (ticks % 1200) / 20;
        StringBuilder stringBuilder = new StringBuilder();
        if (min > 0) {
            stringBuilder.append(min).append(" minute");
            if (min != 1) {
                stringBuilder.append("s");
            }
            if (sec > 0) {
                stringBuilder.append(" ");
            }
        }
        if (sec > 0) {
            stringBuilder.append(sec).append(" second");
            if (sec != 1) {
                stringBuilder.append("s");
            }
        }
        return stringBuilder.toString();
    }

    public static boolean checkRequiredClass(BeyonderHolder holder, Player player, BeyonderClass requiredClass) {
        if (!holder.currentClassMatches(requiredClass)) {
            String name = requiredClass.sequenceNames().get(9);

            player.displayClientMessage(
                    Component.literal("You are not of the ").withStyle(ChatFormatting.AQUA).append(
                            Component.literal(name).withStyle(requiredClass.getColorFormatting())).append(
                            Component.literal(" Pathway").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }

    public static boolean checkRequiredSequence(BeyonderHolder holder, Player player, int requiredSequence) {
        if (holder.getCurrentSequence() > requiredSequence) {
            player.displayClientMessage(
                    Component.literal("You need to be sequence ").withStyle(ChatFormatting.AQUA).append(
                            Component.literal(String.valueOf(requiredSequence)).withStyle(ChatFormatting.YELLOW)).append(
                            Component.literal(" or lower to use this").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }

    public static boolean checkSpirituality(BeyonderHolder holder, Player player, int requiredSpirituality) {
        if (holder.getSpirituality() < requiredSpirituality) {
            player.displayClientMessage(
                    Component.literal("You need ").withStyle(ChatFormatting.AQUA).append(
                            Component.literal(String.valueOf(requiredSpirituality)).withStyle(ChatFormatting.YELLOW)).append(
                            Component.literal(" spirituality to use this").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }

    public static boolean checkAll(Player player, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        return checkRequiredClass(holder, player, requiredClass) && checkRequiredSequence(holder, player, requiredSequence) && checkSpirituality(holder, player, requiredSpirituality);
    }

    public static boolean useSpirituality(Player player, int spirituality) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        return holder.useSpirituality(spirituality);
    }

    protected boolean useSpirituality(Player player) {
        return useSpirituality(player, this.requiredSpirituality);
    }

    public int getRequiredSequence() {
        return this.requiredSequence;
    }

    private boolean checkIfCanUseAbility(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            MisfortuneManipulation.livingUseAbilityMisfortuneManipulation(livingEntity);
            CompoundTag tag = livingEntity.getPersistentData();
            if (livingEntity.getMainHandItem().getItem() instanceof SimpleAbilityItem) {
                if (livingEntity.hasEffect(ModEffects.STUN.get())) {
                    if (livingEntity instanceof Player) {
                        livingEntity.sendSystemMessage(Component.literal("You are stunned and unable to use abilities for another " + (int) Objects.requireNonNull(livingEntity.getEffect(ModEffects.STUN.get())).getDuration() / 20 + " seconds.").withStyle(ChatFormatting.RED));
                    }
                    return false;
                } else if (tag.getInt("cantUseAbility") >= 1) {
                    tag.putInt("cantUseAbility", tag.getInt("cantUseAbility") - 1);
                    if (livingEntity instanceof Player) {
                        livingEntity.sendSystemMessage(Component.literal("How unlucky! You messed up and couldn't use your ability!").withStyle(ChatFormatting.RED));
                    }
                    return false;
                } else if (tag.getInt("unableToUseAbility") >= 1) {
                    tag.putInt("unableToUseAbility", tag.getInt("unableToUseAbility") - 1);
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("You are unable to use your ability").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
        }
        return true;
    }

    public interface scribeAbilitiesStorage {
        Map<Item, Integer> getScribedAbilities();

        void copyScribeAbility(Item ability);

        boolean hasScribedAbility(Item ability);

        void useScribeAbility(Item ability);
    }
}
