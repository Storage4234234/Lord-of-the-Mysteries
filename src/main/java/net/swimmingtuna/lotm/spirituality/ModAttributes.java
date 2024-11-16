package net.swimmingtuna.lotm.spirituality;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, LOTM.MOD_ID);

    public static final RegistryObject<Attribute> SOUL_BODY = ATTRIBUTES.register("soul_body",
            ()-> new RangedAttribute("attribute.lotm.spirituality",100.0D,0.0D,10000000).setSyncable(true));
    public static final RegistryObject<Attribute> SANITY = ATTRIBUTES.register("sanity",
            ()-> new RangedAttribute("attribute.lotm.spirituality",100.0D,0.0D,100).setSyncable(true));
    public static final RegistryObject<Attribute> MISFORTUNE = ATTRIBUTES.register("misfortune",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,10000000).setSyncable(true));
    public static final RegistryObject<Attribute> NIGHTMARE = ATTRIBUTES.register("nightmare",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,10).setSyncable(true));
    public static final RegistryObject<Attribute> ARMORINVISIBLITY = ATTRIBUTES.register("armorinvisibility",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,10).setSyncable(true));
    public static final RegistryObject<Attribute> DIR = ATTRIBUTES.register("dreamintoreality",
            ()-> new RangedAttribute("attribute.lotm.spirituality",1.0D,0.0D,10).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER = ATTRIBUTES.register("particle_helper",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER1 = ATTRIBUTES.register("particle_helper1",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER2 = ATTRIBUTES.register("particle_helper2",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER3 = ATTRIBUTES.register("particle_helper3",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER4 = ATTRIBUTES.register("particle_helper4",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER5 = ATTRIBUTES.register("particle_helper5",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER6 = ATTRIBUTES.register("particle_helper6",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER7 = ATTRIBUTES.register("particle_helper7",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER8 = ATTRIBUTES.register("particle_helper8",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> PARTICLE_HELPER9 = ATTRIBUTES.register("particle_helper9",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,1000).setSyncable(true));
    public static final RegistryObject<Attribute> CORRUPTION = ATTRIBUTES.register("corruption",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,100).setSyncable(true));
    public static final RegistryObject<Attribute> LOTM_LUCK = ATTRIBUTES.register("luck",
            ()-> new RangedAttribute("attribute.lotm.spirituality",0.0D,0.0D,10000).setSyncable(true));
    public static final RegistryObject<Attribute> MOB_SPIRITUALITY = ATTRIBUTES.register("mob_spirituality",
            ()-> new RangedAttribute("attribute.lotm.mob_spirituality",0.0D,0.0D,10000).setSyncable(true));



    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

}