package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RagingBlows extends Item {
    public RagingBlows(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if (!holder.currentClassMatches(BeyonderClassInit.SAILOR)) {
                player.displayClientMessage(Component.literal("You are not of the Sailor pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
            }
            if (holder.getSpirituality() < 20) {
                player.displayClientMessage(Component.literal("You need 20 spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
            }
            if (holder.currentClassMatches(BeyonderClassInit.SAILOR) && holder.getCurrentSequence() <= 8 && holder.useSpirituality(20)) {
                useItem(player);
            }
            if (!player.getAbilities().instabuild)
                player.getCooldowns().addCooldown(this, 200);
        }
        return super.use(level, player, hand);
    }

    public static void useItem(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag persistentData = player.getPersistentData();
            int ragingBlows = persistentData.getInt("ragingBlows");
            persistentData.putInt("ragingBlows", 1);
            persistentData.putInt("rbParticleHelper", 0);
            player.getAttribute(ModAttributes.PARTICLE_HELPER1.get()).setBaseValue(1);
            ragingBlows = 1;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, causes the user to shoot punches powerfully all around the user, damaging everything around them\n" +
                "Spirituality Used: 20\n" +
                "Cooldown: 10 seconds").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            double ragingBlows = player.getAttributeBaseValue(ModAttributes.PARTICLE_HELPER1.get());
            if (ragingBlows >= 1) {
                spawnRagingBlowsParticles(player);
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    private static void spawnRagingBlowsParticles(Player player) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        Vec3 playerPos = player.position();
        Vec3 playerLookVector = player.getViewVector(1.0F);
        int radius = (25 - (holder.getCurrentSequence() * 3));
        CompoundTag persistentData = player.getPersistentData();
        int particleCounter = persistentData.getInt("ragingBlowsParticleCounter");

        if (particleCounter < 7) {
            double randomDistance = Math.random() * radius;
            Vec3 randomOffset = playerLookVector.scale(randomDistance);

            // Add random horizontal offset
            double randomHorizontalOffset = Math.random() * Math.PI * 2; // Random angle between 0 and 2π
            randomOffset = randomOffset.add(new Vec3(Math.cos(randomHorizontalOffset) * radius / 4, 0, Math.sin(randomHorizontalOffset) * radius / 4));

            // Add random vertical offset
            double randomVerticalOffset = Math.random() * Math.PI / 2 - Math.PI / 4; // Random angle between -π/4 and π/4
            randomOffset = randomOffset.add(new Vec3(0, Math.sin(randomVerticalOffset) * radius / 4, 0));

            double randomX = playerPos.x + randomOffset.x;
            double randomY = playerPos.y + randomOffset.y;
            double randomZ = playerPos.z + randomOffset.z;

            // Check if the random offset vector is in front of the player
            if (playerLookVector.dot(randomOffset) > 0) {
                player.level().addParticle(ParticleTypes.EXPLOSION, randomX, randomY, randomZ, 0, 0, 0);
            }

            particleCounter++;
            persistentData.putInt("ragingBlowsParticleCounter", particleCounter);
        } else {
            persistentData.putInt("ragingBlowsParticleCounter", 0);
        }
    }
}