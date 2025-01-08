package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.ItemInit;

import static net.swimmingtuna.lotm.util.BeyonderUtil.applyMobEffect;
import java.util.List;

public class WarriorClass implements BeyonderClass {
    private int speed;
    private int resistance;
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
        if (player.level().getGameTime() % 50 == 0) {
            if (sequenceLevel == 8) {
                if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60,resistance + 1, true,true);
                }
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60,speed + 1, true,true);
                }
            } else if (sequenceLevel <= 7 && sequenceLevel >= 6) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60,speed + 1, true,true);
                }
                if (player.getMainHandItem().getItem() instanceof AxeItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60,strength + 1, true,true);
                }
                if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60,1, true,true);
                }
                if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60,speed + 1, true,true);
                }
                if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60,resistance + 1, true,true);
                }
            }  else if (sequenceLevel <= 5) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60, 0, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof AxeItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60, 3, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
                    applyMobEffect(player, MobEffects.REGENERATION, 60, regen + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                }
            }
            if (sequenceLevel == 9) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false,false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 0, false,false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false,false);
                speed = 1;
                strength = 1;
                resistance = 1;
            }
            if (sequenceLevel == 8) {

            }
            if (sequenceLevel == 7) {

            }
            if (sequenceLevel == 6) {

            }
            if (sequenceLevel == 5) {

            }
            if (sequenceLevel == 4) {

            }
            if (sequenceLevel == 3) {

            }
            if (sequenceLevel == 2) {

            }
            if (sequenceLevel == 1) {

            }
            if (sequenceLevel == 0) {

            }
        }
    }

    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(0, ItemInit.PLACATE.get());
        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.DARK_RED;
    }


}
