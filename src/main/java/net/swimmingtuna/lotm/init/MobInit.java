package net.swimmingtuna.lotm.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.MonsterClass;
import net.swimmingtuna.lotm.entity.mob.BeyonderEntity;

public class MobInit {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LOTM.MOD_ID);

    public static final RegistryObject<EntityType<BeyonderEntity>> MONSTER_BEYONDER_ENTITY = ENTITY_TYPE.register("monster_bey_entity",
            () -> EntityType.Builder.<BeyonderEntity>of((entityType, level) -> new BeyonderEntity(level, new MonsterClass()), MobCategory.MONSTER)
                    .sized(0.6f, 1.99f)
                    .build("monster_bey_entity"));

    public static void register(IEventBus iEventBus){
        ENTITY_TYPE.register(iEventBus);
    }
}
