package net.swimmingtuna.lotm.item.BeyonderAbilities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class SimpleAbilityItem extends Item implements Ability {
    private boolean isProcessing = false;
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


    protected boolean checkAll(LivingEntity living) {
        if(living.getItemInHand(InteractionHand.MAIN_HAND).is(this)) {
            if(!checkAll(living, this.requiredClass.get(), this.requiredSequence, this.requiredSpirituality, false)) {
                if(BeyonderUtil.sequenceAbleCopy(living)) {
                    if(BeyonderUtil.checkAbilityIsCopied(living, this)) {
                        BeyonderUtil.useCopiedAbility(living, this);
                        return checkSpirituality(living, this.getSpirituality(), true);
                    }
                }
            }
            if(checkAll(living, this.requiredClass.get(), this.requiredSequence, this.requiredSpirituality, true)) {
                BeyonderUtil.copyAbilities(living.level(), living, this);
                return true;
            }
        }
        return false;
    }



    public int getSpirituality() {
        return this.requiredSpirituality;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && checkIfCanUseAbility(player)) {
            InteractionResult interactionResult = useAbility(level, player, hand);
            System.out.println("use ability used");
            return new InteractionResultHolder<>(interactionResult, player.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            System.out.println("useOn used");
            return useAbilityOnBlock(context);
        }
        return InteractionResult.PASS;
    }


    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity livingEntity, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!livingEntity.level().isClientSide()) {
            System.out.println("useOn used");
            return interactLivingEntityLivingEntity(stack, livingEntity, interactionTarget, usedHand);
        }
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntityLivingEntity(ItemStack pStack, LivingEntity pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
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

    public static void addCooldown(LivingEntity livingEntity, Item item, int cooldown) {
        if (!(livingEntity instanceof Player pPlayer && pPlayer.isCreative())) {
            if (livingEntity instanceof Player player) {
                player.getCooldowns().addCooldown(item, cooldown);
            } else {
                livingEntity.getPersistentData().putInt("abilityCooldownFor" + item.getDescription().getString(), cooldown);
            }
        }
    }

    protected void addCooldown(LivingEntity player) {
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

    public static boolean checkRequiredClass(LivingEntity living, BeyonderClass requiredClass, boolean message) {
        if (!BeyonderUtil.currentPathwayMatchesNoException(living, requiredClass)) {
            String name = requiredClass.sequenceNames().get(9);
            if(message && living instanceof Player player)
                player.displayClientMessage(
                        Component.literal("You are not of the ").withStyle(ChatFormatting.AQUA).append(
                                Component.literal(name).withStyle(requiredClass.getColorFormatting())).append(
                                Component.literal(" Pathway").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }

    public static boolean checkRequiredSequence(LivingEntity living, int requiredSequence, boolean message) {
        int sequence = BeyonderUtil.getSequence(living);
        if (sequence > requiredSequence) {
            if(message && living instanceof Player player)
                player.displayClientMessage(
                        Component.literal("You need to be sequence ").withStyle(ChatFormatting.AQUA).append(
                                Component.literal(String.valueOf(requiredSequence)).withStyle(ChatFormatting.YELLOW)).append(
                                Component.literal(" or lower to use this").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }

    public static boolean checkSpirituality(LivingEntity living, int requiredSpirituality, boolean message) {
        int spirituality = BeyonderUtil.getSpirituality(living);
        if (spirituality < requiredSpirituality) {
            if(message && living instanceof Player player)
                player.displayClientMessage(
                        Component.literal("You need ").withStyle(ChatFormatting.AQUA).append(
                                Component.literal(String.valueOf(requiredSpirituality)).withStyle(ChatFormatting.YELLOW)).append(
                                Component.literal(" spirituality to use this").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }



    public static boolean checkAll(LivingEntity living, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, boolean message) {
        return checkRequiredClass(living, requiredClass, message) && checkRequiredSequence(living, requiredSequence, message) && checkSpirituality(living, requiredSpirituality, message);
    }





    public static void useSpirituality(LivingEntity livingEntity, int spirituality) {
        BeyonderUtil.useSpirituality(livingEntity, spirituality);
    }

    protected boolean useSpirituality(LivingEntity living) {
        if (BeyonderUtil.getSpirituality(living) >= getRequiredSpirituality()) {
            useSpirituality(living, requiredSpirituality);
            return true;
        } else {
            return false;
        }
    }

    public int getRequiredSequence() {
        return this.requiredSequence;
    }

    public int getRequiredSpirituality() {
        return this.requiredSpirituality;
    }



    public BeyonderClass getRequiredPathway() {
        return this.requiredClass.get();
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
        int getRemainUses(Item ability);
        int getScribedAbilitiesCount();
    }
}