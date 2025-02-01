package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.SoundInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SirenSongHarm extends SimpleAbilityItem {

    public SirenSongHarm(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 5, 300, 1000);
    }


    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        sirenSongHarm(player, level);
        return InteractionResult.SUCCESS;
    }

    private static void sirenSongHarm(Player player, Level level) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            if (tag.getInt("sirenSongHarm") == 0) {
                tag.putInt("sirenSongHarm", 400);
            }
            if (tag.getInt("sirenSongHarm") > 1 && tag.getInt("sirenSongHarm") < 400) {
                tag.putInt("sirenSongHarm", 0);
            }
            if (tag.getInt("sirenSongWeaken") != 0) {
                tag.putInt("sirenSongWeaken", 0);
                tag.putInt("sirenSongHarm", 400);

            }
            if (tag.getInt("sirenSongStun") != 0) {
                tag.putInt("sirenSongStun", 0);
                tag.putInt("sirenSongHarm", 400);
            }
            if (tag.getInt("sirenSongStrengthen") != 0) {
                tag.putInt("sirenSongStrengthen", 0);
                tag.putInt("sirenSongHarm", 400);
            }
        }
    }
    public static void sirenSongs(CompoundTag playerPersistentData, BeyonderHolder holder, Player player, int sequence) {
        //SIREN SONGS
        int sirenSongHarm = playerPersistentData.getInt("sirenSongHarm");
        int sirenSongWeaken = playerPersistentData.getInt("sirenSongWeaken");
        int sirenSongStun = playerPersistentData.getInt("sirenSongStun");
        int sirenSongStrengthen = playerPersistentData.getInt("sirenSongStrengthen");
        if (!holder.currentClassMatches(BeyonderClassInit.SAILOR) || holder.getCurrentSequence() > 5) {
            return;
        }
        if (sirenSongHarm % 20 == 0 && sirenSongHarm != 0) {
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.SIREN_SONG_HARM.get())))) {
                if (entity != player && !BeyonderUtil.isAllyOf(player, entity)) {
                    BeyonderUtil.applyMentalDamage(player, entity, (float) ((double) 6 - (0.5 * sequence)));
                }
            }
        }
        SoundEvent harmSoundEvent = switch (sirenSongHarm) {
            case 400 -> SoundInit.SIREN_SONG_HARM_1.get();
            case 380 -> SoundInit.SIREN_SONG_HARM_2.get();
            case 360 -> SoundInit.SIREN_SONG_HARM_3.get();
            case 340 -> SoundInit.SIREN_SONG_HARM_4.get();
            case 320 -> SoundInit.SIREN_SONG_HARM_5.get();
            case 300 -> SoundInit.SIREN_SONG_HARM_6.get();
            case 280 -> SoundInit.SIREN_SONG_HARM_7.get();
            case 260 -> SoundInit.SIREN_SONG_HARM_8.get();
            case 240 -> SoundInit.SIREN_SONG_HARM_9.get();
            case 220 -> SoundInit.SIREN_SONG_HARM_10.get();
            case 200 -> SoundInit.SIREN_SONG_HARM_11.get();
            case 180 -> SoundInit.SIREN_SONG_HARM_12.get();
            case 160 -> SoundInit.SIREN_SONG_HARM_13.get();
            case 140 -> SoundInit.SIREN_SONG_HARM_14.get();
            case 120 -> SoundInit.SIREN_SONG_HARM_15.get();
            case 100 -> SoundInit.SIREN_SONG_HARM_16.get();
            case 80 -> SoundInit.SIREN_SONG_HARM_17.get();
            case 60 -> SoundInit.SIREN_SONG_HARM_18.get();
            case 40 -> SoundInit.SIREN_SONG_HARM_19.get();
            case 20 -> SoundInit.SIREN_SONG_HARM_20.get();
            default -> null;
        };
        if (harmSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), harmSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongHarm >= 1) {
            playerPersistentData.putInt("sirenSongHarm", sirenSongHarm - 1);
        }

        if (sirenSongWeaken % 20 == 0 && sirenSongWeaken != 0) { //make it for 380,360,430 etc.
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.SIREN_SONG_WEAKEN.get())))) {
                if (entity != player && !BeyonderUtil.isAllyOf(player, entity)) {

                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 21, 2, false, false));
                    entity.addEffect(new MobEffectInstance(ModEffects.ABILITY_WEAKNESS.get(), 21, 1, false, false));
                }
            }
        }

        SoundEvent weakenSoundEvent = switch (sirenSongWeaken) {
            case 400 -> SoundInit.SIREN_SONG_WEAKEN_1.get();
            case 380 -> SoundInit.SIREN_SONG_WEAKEN_2.get();
            case 360 -> SoundInit.SIREN_SONG_WEAKEN_3.get();
            case 340 -> SoundInit.SIREN_SONG_WEAKEN_4.get();
            case 320 -> SoundInit.SIREN_SONG_WEAKEN_5.get();
            case 300 -> SoundInit.SIREN_SONG_WEAKEN_6.get();
            case 280 -> SoundInit.SIREN_SONG_WEAKEN_7.get();
            case 260 -> SoundInit.SIREN_SONG_WEAKEN_8.get();
            case 240 -> SoundInit.SIREN_SONG_WEAKEN_9.get();
            case 220 -> SoundInit.SIREN_SONG_WEAKEN_10.get();
            case 200 -> SoundInit.SIREN_SONG_WEAKEN_11.get();
            case 180 -> SoundInit.SIREN_SONG_WEAKEN_12.get();
            case 160 -> SoundInit.SIREN_SONG_WEAKEN_13.get();
            case 140 -> SoundInit.SIREN_SONG_WEAKEN_14.get();
            case 120 -> SoundInit.SIREN_SONG_WEAKEN_15.get();
            case 100 -> SoundInit.SIREN_SONG_WEAKEN_16.get();
            case 80 -> SoundInit.SIREN_SONG_WEAKEN_17.get();
            case 60 -> SoundInit.SIREN_SONG_WEAKEN_18.get();
            case 40 -> SoundInit.SIREN_SONG_WEAKEN_19.get();
            case 20 -> SoundInit.SIREN_SONG_WEAKEN_20.get();
            default -> null;
        };
        if (weakenSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), weakenSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongWeaken >= 1) {
            playerPersistentData.putInt("sirenSongWeaken", sirenSongWeaken - 1);
        }

        if (sirenSongStun % 20 == 0 && sirenSongStun != 0) {
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.SIREN_SONG_STUN.get())))) {
                if (entity != player && !BeyonderUtil.isAllyOf(player, entity)) {
                    entity.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 19 - (sequence * 2), 2, false, false));
                }
            }
        }
        SoundEvent stunSoundEvent = switch (sirenSongStun) {
            case 400 -> SoundInit.SIREN_SONG_STUN_1.get();
            case 380 -> SoundInit.SIREN_SONG_STUN_2.get();
            case 360 -> SoundInit.SIREN_SONG_STUN_3.get();
            case 340 -> SoundInit.SIREN_SONG_STUN_4.get();
            case 320 -> SoundInit.SIREN_SONG_STUN_5.get();
            case 300 -> SoundInit.SIREN_SONG_STUN_6.get();
            case 280 -> SoundInit.SIREN_SONG_STUN_7.get();
            case 260 -> SoundInit.SIREN_SONG_STUN_8.get();
            case 240 -> SoundInit.SIREN_SONG_STUN_9.get();
            case 220 -> SoundInit.SIREN_SONG_STUN_10.get();
            case 200 -> SoundInit.SIREN_SONG_STUN_11.get();
            case 180 -> SoundInit.SIREN_SONG_STUN_12.get();
            case 160 -> SoundInit.SIREN_SONG_STUN_13.get();
            case 140 -> SoundInit.SIREN_SONG_STUN_14.get();
            case 120 -> SoundInit.SIREN_SONG_STUN_15.get();
            case 100 -> SoundInit.SIREN_SONG_STUN_16.get();
            case 80 -> SoundInit.SIREN_SONG_STUN_17.get();
            case 60 -> SoundInit.SIREN_SONG_STUN_18.get();
            case 40 -> SoundInit.SIREN_SONG_STUN_19.get();
            case 20 -> SoundInit.SIREN_SONG_STUN_20.get();
            default -> null;
        };

        if (stunSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), stunSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongStun >= 1) {
            playerPersistentData.putInt("sirenSongStun", sirenSongStun - 1);
        }
        if (sirenSongStrengthen % 20 == 0 && sirenSongStrengthen != 0) {
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.SIREN_SONG_STUN.get())))) {
                if (entity == player || BeyonderUtil.isAllyOf(player, entity)) {
                    if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) {
                        int strengthAmp = entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier();
                        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) (float) BeyonderUtil.getDamage(entity).get(ItemInit.SIREN_SONG_STRENGTHEN.get()), strengthAmp + 2));
                    } else if (!entity.hasEffect(MobEffects.DAMAGE_BOOST)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) (float) BeyonderUtil.getDamage(entity).get(ItemInit.SIREN_SONG_STRENGTHEN.get()), 2));
                    }
                    if (entity.hasEffect(MobEffects.REGENERATION)) {
                        int regenAmp = entity.getEffect(MobEffects.REGENERATION).getAmplifier();
                        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, (int) (float) BeyonderUtil.getDamage(entity).get(ItemInit.SIREN_SONG_STRENGTHEN.get()), regenAmp + 2));
                    } else if (!entity.hasEffect(MobEffects.REGENERATION)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, (int) (float) BeyonderUtil.getDamage(entity).get(ItemInit.SIREN_SONG_STRENGTHEN.get()), 2));
                    }
                }
            }
        }
        SoundEvent strengthenSoundEvent = switch (sirenSongStrengthen) {
            case 400 -> SoundInit.SIREN_SONG_STRENGTHEN_1.get();
            case 380 -> SoundInit.SIREN_SONG_STRENGTHEN_2.get();
            case 360 -> SoundInit.SIREN_SONG_STRENGTHEN_3.get();
            case 340 -> SoundInit.SIREN_SONG_STRENGTHEN_4.get();
            case 320 -> SoundInit.SIREN_SONG_STRENGTHEN_5.get();
            case 300 -> SoundInit.SIREN_SONG_STRENGTHEN_6.get();
            case 280 -> SoundInit.SIREN_SONG_STRENGTHEN_7.get();
            case 260 -> SoundInit.SIREN_SONG_STRENGTHEN_8.get();
            case 240 -> SoundInit.SIREN_SONG_STRENGTHEN_9.get();
            case 220 -> SoundInit.SIREN_SONG_STRENGTHEN_10.get();
            case 200 -> SoundInit.SIREN_SONG_STRENGTHEN_11.get();
            case 180 -> SoundInit.SIREN_SONG_STRENGTHEN_12.get();
            case 160 -> SoundInit.SIREN_SONG_STRENGTHEN_13.get();
            case 140 -> SoundInit.SIREN_SONG_STRENGTHEN_14.get();
            case 120 -> SoundInit.SIREN_SONG_STRENGTHEN_15.get();
            case 100 -> SoundInit.SIREN_SONG_STRENGTHEN_16.get();
            case 80 -> SoundInit.SIREN_SONG_STRENGTHEN_17.get();
            case 60 -> SoundInit.SIREN_SONG_STRENGTHEN_18.get();
            case 40 -> SoundInit.SIREN_SONG_STRENGTHEN_19.get();
            case 20 -> SoundInit.SIREN_SONG_STRENGTHEN_20.get();
            default -> null;
        };

        if (strengthenSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), strengthenSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongStrengthen >= 1) {
            playerPersistentData.putInt("sirenSongStrengthen", sirenSongStrengthen - 1);
        }
    }

    public static void sirenSongs(Player player) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        int sequence = holder.getCurrentSequence();
        CompoundTag playerPersistentData = player.getPersistentData();
        int harmCounter = 50 - (sequence * 6);
        int sirenSongWeaken = playerPersistentData.getInt("sirenSongWeaken");
        int sirenSongStrengthen = playerPersistentData.getInt("sirenSongStrengthen");
        int sirenSongHarm = playerPersistentData.getInt("sirenSongHarm");
        int sirenSongStun = playerPersistentData.getInt("sirenSongStun");
        if (sirenSongStrengthen >= 1 || sirenSongWeaken >= 1 || sirenSongStun >= 1 || sirenSongHarm >= 1) {
            SirenSongStrengthen.spawnParticlesInSphere(player, harmCounter);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, sings out a song which harms everyone around you if they're in it"));
        tooltipComponents.add(Component.literal("Left Click for Siren Song Strengthen"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("50 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }
}