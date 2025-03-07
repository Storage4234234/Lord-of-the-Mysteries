package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnWeaponry.hasFullDawnArmor;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnWeaponry.hasFullSilverArmor;
import static net.swimmingtuna.lotm.util.BeyonderUtil.applyMobEffect;

public class WarriorClass implements BeyonderClass {
    private int speed;
    public static int resistance;
    private int strength;
    private int regen;

    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Twilight Giant",
                "Hand of God",
                "Glory",
                "Silver Knight",
                "Demon Hunter",
                "Guardian",
                "Dawn Paladin",
                "Weapon Master",
                "Pugilist",
                "Warrior"
        );
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(10000, 6000, 3500, 2800, 1500, 780, 475, 375, 200, 150);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(450, 320, 210, 175, 150, 110, 80, 70, 50, 33);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(34, 22, 16, 12, 10, 8, 6, 5, 3, 2);
    }

    @Override
    public List<Double> maxHealth() {
        return List.of(70.0, 68.0, 65.0, 60.0, 50.0, 42.0, 32.0, 28.0, 28.0, 25.0);
    }

    @Override
    public void tick(Player player, int sequenceLevel) {
        if (!player.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);
            boolean isGiant = player.getPersistentData().getBoolean("warriorGiant");
            boolean isHoGGiant = player.getPersistentData().getBoolean("handOfGodGiant");
            boolean isTwilightGiant = player.getPersistentData().getBoolean("twilightGiant");
            boolean x = !isGiant && !isHoGGiant && !isTwilightGiant;
            if (player.level().getGameTime() % 10 == 0) {
                if (sequenceLevel == 8) {
                    if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof SwordItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, speed + 1, true, true);
                    }
                } else if (sequenceLevel <= 7 && sequenceLevel >= 6) {
                    if (player.getMainHandItem().getItem() instanceof SwordItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, speed + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof AxeItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_BOOST, 20, strength + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                        applyMobEffect(player, MobEffects.DIG_SPEED, 20, 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, speed + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
                    }
                } else if (sequenceLevel <= 5) {
                    if (player.getMainHandItem().getItem() instanceof SwordItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, speed + 2, true, true);
                        applyMobEffect(player, MobEffects.DIG_SPEED, 20, 0, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof AxeItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_BOOST, 20, strength + 1, true, true);
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                        applyMobEffect(player, MobEffects.DIG_SPEED, 20, 3, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                        applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 20, speed + 2, true, true);
                        applyMobEffect(player, MobEffects.REGENERATION, 20, regen + 1, true, true);
                    }
                    if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                        applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
                    }
                }
            }
            if (player.level().getGameTime() % 50 == 0) {
                if (sequenceLevel == 9) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                    speed = 1;
                    strength = 0;
                    resistance = 0;
                    regen = -1;
                } else if (sequenceLevel == 8) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 0, false, false);
                    speed = 1;
                    strength = 0;
                    resistance = 0;
                    regen = -1;
                } else if (sequenceLevel == 7) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 0, false, false);
                    speed = 1;
                    strength = 1;
                    resistance = 0;
                    regen = 0;
                } else if (sequenceLevel == 6) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 0, false, false);
                    speed = 1;
                    strength = 2;
                    resistance = 0;
                    regen = 0;
                } else if (sequenceLevel == 5) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 0, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 1, false, false);
                    speed = 1;
                    strength = 2;
                    resistance = 1;
                    regen = 1;
                } else if (sequenceLevel == 4) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 2, false, false);
                    speed = 2;
                    strength = 3;
                    resistance = 1;
                    regen = 2;
                } else if (sequenceLevel == 3) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 2, false, false);
                    speed = 2;
                    strength = 3;
                    resistance = 1;
                    regen = 2;
                } else if (sequenceLevel == 2) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 2, false, false);
                    speed = 2;
                    strength = 3;
                    resistance = 2;
                    regen = 2;
                } else if (sequenceLevel == 1) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 2, false, false);
                    speed = 2;
                    strength = 3;
                    resistance = 2;
                    regen = 2;
                } else if (sequenceLevel == 0) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                    applyMobEffect(player, MobEffects.JUMP, 300, 2, false, false);
                    applyMobEffect(player, MobEffects.REGENERATION, 300, 2, false, false);
                    speed = 2;
                    strength = 3;
                    resistance = 2;
                    regen = 2;
                }
            }
        }
    }

    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(9, ItemInit.BEYONDER_ABILITY_USER.get());
        items.put(9, ItemInit.ALLY_MAKER.get());
        items.put(6, ItemInit.GIGANTIFICATION.get());
        items.put(6, ItemInit.LIGHTOFDAWN.get());
        items.put(6, ItemInit.DAWNARMORY.get());
        items.put(6, ItemInit.DAWNWEAPONRY.get());
        items.put(5, ItemInit.ENABLEDISABLEPROTECTION.get());
        items.put(4, ItemInit.EYEOFDEMONHUNTING.get());
        items.put(4, ItemInit.WARRIORDANGERSENSE.get());
        items.put(3, ItemInit.MERCURYLIQUEFICATION.get());
        items.put(3, ItemInit.SILVERSWORDMANIFESTATION.get());
        items.put(3, ItemInit.SILVERRAPIER.get());
        items.put(3, ItemInit.SILVERARMORY.get());
        items.put(3, ItemInit.LIGHTCONCEALMENT.get());
        items.put(2, ItemInit.BEAMOFGLORY.get());
        items.put(2, ItemInit.AURAOFGLORY.get());
        items.put(2, ItemInit.TWILIGHTSWORD.get());
        items.put(2, ItemInit.MERCURYCAGE.get());
        items.put(1, ItemInit.DIVINEHANDLEFT.get());
        items.put(1, ItemInit.DIVINEHANDRIGHT.get());
        items.put(1, ItemInit.TWILIGHTMANIFESTATION.get());
        items.put(0, ItemInit.AURAOFTWILIGHT.get());
        items.put(0, ItemInit.TWILIGHTFREEZE.get());
        items.put(0, ItemInit.TWILIGHTACCELERATE.get());
        items.put(0, ItemInit.TWILIGHTLIGHT.get());
        items.put(0, ItemInit.GLOBEOFTWILIGHT.get());
        items.put(0, ItemInit.BEAMOFTWILIGHT.get());
        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.DARK_RED;
    }

    public static void warriorDamageNegation(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        if (!livingEntity.level().isClientSide()) {
            // boolean isWearingDawnArmor =
            //boolean isWearingSilverArmor =
            boolean isGiant = livingEntity.getPersistentData().getBoolean("warriorGiant");
            boolean isHoGGiant = livingEntity.getPersistentData().getBoolean("handOfGodGiant");
            boolean isTwilightGiant = livingEntity.getPersistentData().getBoolean("twilightGiant");
            boolean isPhysical = BeyonderUtil.isPhysicalDamage(source);
            boolean isSupernatural = BeyonderUtil.isSupernaturalDamage(source);
            float amount = event.getAmount();
            int sequence = -1;


            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                sequence = holder.getSequence();
            } else if (livingEntity instanceof PlayerMobEntity player) {
                sequence = player.getCurrentSequence();
            }
            //if wearing silver armor, if damage is under 25 HP, it's damage is reduced by 80%
            boolean isWarrior = BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.WARRIOR.get());
            if (hasFullSilverArmor(livingEntity) || hasFullDawnArmor(livingEntity)) {
                if (hasFullSilverArmor(livingEntity)) {
                    if (event.getAmount() <= 25) {
                        event.setAmount(0);
                    } else {
                        event.setAmount(amount * 0.33f);
                    }
                } else if (hasFullDawnArmor(livingEntity)) {
                    if (livingEntity.tickCount % 2 == 0) {
                        BeyonderUtil.useSpirituality(livingEntity, 2);
                    }
                    if (event.getAmount() <= 10) {
                        event.setAmount(0);
                    } else if (isSupernatural) {
                        event.setAmount(amount / 2);
                    }
                }
            }
            if (isWarrior) {
                if (sequence == 8) {
                    if (isSupernatural) {
                        event.setAmount((float) (amount * 0.75));
                    }
                } else if (sequence == 7) {
                    if (isSupernatural) {
                        event.setAmount((float) (amount * 0.75));
                    } else if (isPhysical) {
                        event.setAmount((float) (amount * 0.7));
                    }
                } else if (sequence == 6) {
                    if (isSupernatural) {
                        event.setAmount((float) (amount * 0.7));
                    } else if (isPhysical) {
                        if (!isGiant) {
                            event.setAmount((float) (amount * 0.6));
                        } else {
                            if (event.getAmount() <= 5) {
                                event.setAmount(0);
                            } else {
                                event.setAmount((float) (amount * 0.45));
                            }
                        }
                    }
                } else if (sequence == 5) {
                    if (isSupernatural) {
                        event.setAmount((float) (amount * 0.7));
                    } else if (isPhysical) {
                        if (!isGiant) {
                            event.setAmount((float) (amount * 0.5));
                        } else {
                            if (event.getAmount() <= 7) {
                                event.setAmount(0);
                            } else {
                                event.setAmount((float) (amount * 0.35));
                            }
                        }
                    }
                } else if (sequence == 4) {
                    if (isSupernatural) {
                        event.setAmount((float) (amount * 0.6));
                    } else if (isPhysical) {
                        if (!isGiant) {
                            event.setAmount((float) (amount * 0.45));
                        } else {
                            if (event.getAmount() <= 10) {
                                event.setAmount(0);
                            } else {
                                event.setAmount((float) (amount * 0.35));
                            }
                        }
                    }
                } else if (sequence == 3 || sequence == 2) {
                    if (isSupernatural) {
                        event.setAmount((float) (amount * 0.6));
                    } else if (isPhysical) {
                        if (!isGiant) {
                            event.setAmount((float) (amount * 0.4));
                        } else {
                            if (amount <= 15) {
                                event.setCanceled(true);
                            } else {
                                event.setAmount((float) (amount * 0.35));
                            }
                        }
                    }
                } else if (sequence == 1) {
                    if (isSupernatural) {
                        if (isHoGGiant) {
                            if (event.getAmount() <= 20) {
                                event.setAmount(0);
                            } else {
                                event.setAmount((float) (amount * 0.3));
                            }
                        }
                    } else if (isPhysical) {
                        if (isHoGGiant) {
                            if (event.getAmount() <= 20) {
                                event.setAmount(0);
                            } else {
                                event.setAmount((float) (amount * 0.275));
                            }
                        } else {
                            event.setAmount((float) (amount * 0.35));
                        }
                    }
                } else if (sequence == 0) {
                    if (isSupernatural) {
                        if (isTwilightGiant) {
                            if (event.getAmount() <= 25) {
                                event.setAmount(0);
                            } else {
                                event.setAmount((float) (amount * 0.2));
                            }
                        } else {
                            event.setAmount((float) (amount * 0.5));
                        }
                    } else if (isPhysical) {
                        if (isHoGGiant) {
                            if (event.getAmount() <= 25) {
                                event.setAmount(0);
                            } else {
                                event.setAmount((float) (amount * 0.2));
                            }
                        } else {
                            event.setAmount((float) (amount * 0.35));
                        }
                    }
                }
            }
        }
    }
    public static void newWarriorDamageNegation(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        if (!livingEntity.level().isClientSide()) {
            boolean isGiant = livingEntity.getPersistentData().getBoolean("warriorGiant");
            boolean isHoGGiant = livingEntity.getPersistentData().getBoolean("handOfGodGiant");
            boolean isTwilightGiant = livingEntity.getPersistentData().getBoolean("twilightGiant");
            boolean isPhysical = BeyonderUtil.isPhysicalDamage(source);
            boolean isSupernatural = BeyonderUtil.isSupernaturalDamage(source);
            float originalAmount = event.getAmount();
            float amount = originalAmount;
            int sequence = -1;

            // Track damage reduction multipliers
            float physicalReduction = 0.0f;
            float supernaturalReduction = 0.0f;

            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                sequence = holder.getSequence();
            } else if (livingEntity instanceof PlayerMobEntity player) {
                sequence = player.getCurrentSequence();
            }
            boolean isWarrior = BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.WARRIOR.get());
            if (hasFullSilverArmor(livingEntity) && originalAmount <= 25) {
                event.setAmount(0);
                return;
            }

            if (hasFullDawnArmor(livingEntity)) {
                if (livingEntity.tickCount % 2 == 0) {
                    BeyonderUtil.useSpirituality(livingEntity, 2);
                }
                if (originalAmount <= 10) {
                    event.setAmount(0);
                    return;
                }
            }
            if (isWarrior && isPhysical) {
                if (sequence == 6 && isGiant && originalAmount <= 5) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 5 && isGiant && originalAmount <= 7) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 4 && isGiant && originalAmount <= 10) {
                    event.setAmount(0);
                    return;
                } else if ((sequence == 3 || sequence == 2) && isGiant && originalAmount <= 15) {
                    event.setCanceled(true);
                    return;
                } else if (sequence == 1 && isHoGGiant && originalAmount <= 20) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 0 && isHoGGiant && originalAmount <= 25) {
                    event.setAmount(0);
                    return;
                }
            }
            if (isWarrior && isSupernatural) {
                if (sequence == 1 && isHoGGiant && originalAmount <= 20) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 0 && isTwilightGiant && originalAmount <= 25) {
                    event.setAmount(0);
                    return;
                }
            }
            if (hasFullSilverArmor(livingEntity)) {
                physicalReduction += 0.67f;
                supernaturalReduction += 0.67f;
            } else if (hasFullDawnArmor(livingEntity) && isSupernatural) {
                supernaturalReduction += 0.5f;
            }
            if (isWarrior) {
                if (sequence == 8) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.25f;
                    }
                } else if (sequence == 7) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.25f;
                    } else if (isPhysical) {
                        physicalReduction += 0.3f;
                    }
                } else if (sequence == 6) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.3f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.4f;
                        } else {
                            physicalReduction += 0.55f;
                        }
                    }
                } else if (sequence == 5) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.3f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.5f;
                        } else {
                            physicalReduction += 0.65f;
                        }
                    }
                } else if (sequence == 4) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.4f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.55f;
                        } else {
                            physicalReduction += 0.65f;
                        }
                    }
                } else if (sequence == 3 || sequence == 2) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.4f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.6f;
                        } else {
                            physicalReduction += 0.65f;
                        }
                    }
                } else if (sequence == 1) {
                    if (isSupernatural) {
                        if (isHoGGiant) {
                            supernaturalReduction += 0.7f;
                        }
                    } else if (isPhysical) {
                        if (isHoGGiant) {
                            physicalReduction += 0.725f;
                        } else {
                            physicalReduction += 0.65f;
                        }
                    }
                } else if (sequence == 0) {
                    if (isSupernatural) {
                        if (isTwilightGiant) {
                            supernaturalReduction += 0.8f;
                        } else {
                            supernaturalReduction += 0.5f;
                        }
                    } else if (isPhysical) {
                        if (isHoGGiant) {
                            physicalReduction += 0.8f;
                        } else {
                            physicalReduction += 0.65f;
                        }
                    }
                }
            }
            if (isPhysical) {
                float finalReduction = Math.min(physicalReduction, 0.8f);
                event.setAmount(amount * (1.0f - finalReduction));
            } else if (isSupernatural) {
                float finalReduction = Math.min(supernaturalReduction, 0.8f);
                event.setAmount(amount * (1.0f - finalReduction));
            }
        }
    }
}
