package net.swimmingtuna.lotm.init;


import net.minecraft.world.level.GameRules;

public class GameRuleInit {

    public static final GameRules.Key<GameRules.BooleanValue> NPC_SHOULD_SPAWN = GameRules.register("shouldSpawnNpc", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> SHOULD_DROP_CHARACTERISTIC = GameRules.register("shouldDropCharacteristic", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> RESET_SEQUENCE = GameRules.register("shouldResetSequence", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));


}

