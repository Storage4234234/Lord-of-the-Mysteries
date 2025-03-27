package net.swimmingtuna.lotm.init;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.armor.DawnArmorItem;
import net.swimmingtuna.lotm.armor.SilverArmorItem;
import net.swimmingtuna.lotm.item.AllyMaker;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.BeyonderAbilityUser;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.TwilightSword;
import net.swimmingtuna.lotm.item.BeyonderPotions.BeyonderCharacteristic;
import net.swimmingtuna.lotm.item.BeyonderPotions.BeyonderPotion;
import net.swimmingtuna.lotm.item.BeyonderPotions.BeyonderResetPotion;
import net.swimmingtuna.lotm.item.OtherItems.*;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.item.SealedArtifacts.SymphonyOfHatred;
import net.swimmingtuna.lotm.item.SealedArtifacts.WintryBlade;
import net.swimmingtuna.lotm.util.ModArmorMaterials;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LOTM.MOD_ID);




    //MONSTER
    public static final RegistryObject<Item> SPIRITVISION = ITEMS.register("spiritvision",
            () -> new SpiritVision(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MONSTERDANGERSENSE = ITEMS.register("monsterdangersense",
            () -> new MonsterDangerSense(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WHISPEROFCORRUPTION = ITEMS.register("whisperofcorruption",
            () -> new WhisperOfCorruption(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CALAMITYINCARNATION = ITEMS.register("calamityincarnation",
            () -> new MonsterCalamityIncarnation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENABLEDISABLERIPPLE = ITEMS.register("enabledisableripple",
            () -> new EnableDisableRipple(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AURAOFCHAOS = ITEMS.register("auraofchaos",
            () -> new AuraOfChaos(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHAOSWALKERCOMBAT = ITEMS.register("chaoswalkercombat",
            () -> new ChaosWalkerDisableEnable(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MISFORTUNEREDIRECTION = ITEMS.register("misfortuneredirection",
            () -> new MisfortuneRedirection(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FORTUNEAPPROPIATION = ITEMS.register("fortuneappropiation",
            () -> new FortuneAppropiation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FALSEPROPHECY = ITEMS.register("falseprophecy",
            () -> new FalseProphecy(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MONSTERREBOOT = ITEMS.register("monsterreboot",
            () -> new MonsterReboot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FATEREINCARNATION = ITEMS.register("fatereincarnation",
            () -> new FateReincarnation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CYCLEOFFATE = ITEMS.register("cycleoffate",
            () -> new CycleOfFate(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHAOSAMPLIFICATION = ITEMS.register("chaosamplification",
            () -> new ChaosAmplification(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FATEDCONNECTION = ITEMS.register("fatedconnection",
            () -> new FatedConnection(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REBOOTSELF = ITEMS.register("rebootself",
            () -> new RebootSelf(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYMISFORTUNEINCREASE = ITEMS.register("probabilitymanipulationworldmisfortune",
            () -> new ProbabilityManipulationWorldMisfortune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYFORTUNEINCREASE = ITEMS.register("probabilitymanipulationworldfortune",
            () -> new ProbabilityManipulationWorldFortune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYWIPE = ITEMS.register("probabilitywipe",
            () -> new ProbabilityManipulationWipe(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYEFFECT = ITEMS.register("probabilityeffect",
            () -> new ProbabilityManipulationImpulse(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYMISFORTUNE = ITEMS.register("probabilitymisfortune",
            () -> new ProbabilityManipulationMisfortune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYFORTUNE = ITEMS.register("probabilityfortune",
            () -> new ProbabilityManipulationFortune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYINFINITEMISFORTUNE = ITEMS.register("probabilityinfinitemisfortune",
            () -> new ProbabilityManipulationInfiniteMisfortune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROBABILITYINFINITEFORTUNE = ITEMS.register("probabilityinfinitefortune",
            () -> new ProbabilityManipulationInfiniteFortune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCKDENIAL = ITEMS.register("luckdenial",
            () -> new LuckDenial(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MISFORTUNEMANIPULATION = ITEMS.register("misfortunemanipulation",
            () -> new MisfortuneManipulation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCKCHANNELING = ITEMS.register("luckchanneling",
            () -> new LuckChanneling(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MONSTERDOMAINTELEPORATION = ITEMS.register("monsterdomainteleportation",
            () -> new MonsterDomainTeleporation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MONSTERCALAMITYATTRACTION = ITEMS.register("choiceofcalamity",
            () -> new MonsterDisableEnableCalamities(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROVIDENCEDOMAIN = ITEMS.register("providencedomain",
            () -> new DomainOfProvidence(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DECAYDOMAIN = ITEMS.register("misfortunedomain",
            () -> new DomainOfDecay(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCKPERCEPTION = ITEMS.register("luckperception",
            () -> new LuckPerception(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PSYCHESTORM = ITEMS.register("psychestorm",
            () -> new PsycheStorm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MONSTERPROJECTILECONTROL = ITEMS.register("monsterprojectilecontrol",
            () -> new MonsterProjectileControl(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCK_MANIPULATION = ITEMS.register("luckmanipulation",
            () -> new LuckManipulation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCKGIFTING = ITEMS.register("luckgifting",
            () -> new LuckGifting(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCKDEPRIVATION = ITEMS.register("luckdeprivation",
            () -> new LuckDeprivation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MISFORTUNEBESTOWAL = ITEMS.register("misfortunebestowal",
            () -> new MisfortuneBestowal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCKFUTURETELLING = ITEMS.register("luckfuturetelling",
            () -> new LuckFutureTelling(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MISFORTUNEIMPLOSION = ITEMS.register("misfortuneimplosion",
            () -> new MisfortuneImplosion(new Item.Properties().stacksTo(1)));

    //SAILOR
    public static final RegistryObject<Item> TSUNAMI = ITEMS.register("tsunami",
            () -> new Tsunami(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EARTHQUAKE = ITEMS.register("earthquake",
            () -> new Earthquake(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAILORPROJECTILECTONROL = ITEMS.register("sailorprojectilecontrol",
            () -> new SailorProjectileControl(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STAR_OF_LIGHTNING = ITEMS.register("staroflightning",
            () -> new StarOfLightning(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTNING_REDIRECTION = ITEMS.register("lightningredirection",
            () -> new LightningRedirection(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RAIN_EYES = ITEMS.register("raineyes",
            () -> new RainEyes(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SONIC_BOOM = ITEMS.register("sonicboom",
            () -> new SonicBoom(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTNING_STORM = ITEMS.register("sailorlightningstorm",
            () -> new LightningStorm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ROAR = ITEMS.register("roar",
            () -> new Roar(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CALAMITY_INCARNATION_TORNADO = ITEMS.register("calamityincarnationtornado",
            () -> new CalamityIncarnationTornado(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CALAMITY_INCARNATION_TSUNAMI = ITEMS.register("calamityincarnationtsunami",
            () -> new CalamityIncarnationTsunami(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTNING_BALL = ITEMS.register("lightningball",
            () -> new LightningBall(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTNING_BALL_ABSORB = ITEMS.register("lightningballabsorb",
            () -> new LightningBallAbsorb(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MATTER_ACCELERATION_BLOCKS = ITEMS.register("matteraccelerationblocks",
            () -> new MatterAccelerationBlocks(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MATTER_ACCELERATION_ENTITIES = ITEMS.register("matteraccelerationentities",
            () -> new MatterAccelerationEntities(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MATTER_ACCELERATION_SELF = ITEMS.register("matteraccelerationself",
            () -> new MatterAccelerationSelf(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAILOR_LIGHTNING_TRAVEL = ITEMS.register("sailorlightningtravel",
            () -> new SailorLightningTravel(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STORM_SEAL = ITEMS.register("stormseal",
            () -> new StormSeal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> VOLCANIC_ERUPTION = ITEMS.register("volcaniceruption",
            () -> new VolcanicEruption(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EXTREME_COLDNESS = ITEMS.register("extremecoldness",
            () -> new ExtremeColdness(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTNING_BRANCH = ITEMS.register("lightningbranch",
            () -> new LightningBranch(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAILOR_LIGHTNING = ITEMS.register("sailorlightning",
            () -> new SailorLightning(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WATER_SPHERE = ITEMS.register("watersphere",
            () -> new WaterSphere(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> THUNDER_CLAP = ITEMS.register("thunderclap",
            () -> new ThunderClap(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WATER_COLUMN = ITEMS.register("watercolumn",
            () -> new WaterColumn(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TYRANNY = ITEMS.register("tyranny",
            () -> new Tyranny(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HURRICANE = ITEMS.register("hurricane",
            () -> new Hurricane(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TORNADO = ITEMS.register("tornado",
            () -> new Tornado(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RAGING_BLOWS = ITEMS.register("ragingblows",
            () -> new RagingBlows(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AQUEOUS_LIGHT_DROWN = ITEMS.register("aqueouslightdrown",
            () -> new AqueousLightDrown(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AQUEOUS_LIGHT_PUSH = ITEMS.register("aqueouslightpush",
            () -> new AqueousLightPush(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AQUEOUS_LIGHT_PULL = ITEMS.register("aqueouslightpull",
            () -> new AqueousLightPull(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENABLE_OR_DISABLE_LIGHTNING = ITEMS.register("enableordisablelightning",
            () -> new EnableOrDisableLightning(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WIND_MANIPULATION_FLIGHT = ITEMS.register("windmanipulationflight",
            () -> new WindManipulationFlight(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WIND_MANIPULATION_BLADE = ITEMS.register("windmanipulationblade",
            () -> new WindManipulationBlade(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WIND_MANIPULATION_SENSE = ITEMS.register("windmanipulationsense",
            () -> new WindManipulationSense(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACIDIC_RAIN = ITEMS.register("acidicrain",
            () -> new AcidicRain(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AQUATIC_LIFE_MANIPULATION = ITEMS.register("aquaticlifemanipulation",
            () -> new AquaticLifeManipulation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TSUNAMI_SEAL = ITEMS.register("tsunamiseal",
            () -> new TsunamiSeal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIREN_SONG_HARM = ITEMS.register("siren_song_harm",
            () -> new SirenSongHarm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIREN_SONG_STRENGTHEN = ITEMS.register("siren_song_strengthen",
            () -> new SirenSongStrengthen(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIREN_SONG_STUN = ITEMS.register("siren_song_stun",
            () -> new SirenSongStun(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SIREN_SONG_WEAKEN = ITEMS.register("siren_song_weaken",
            () -> new SirenSongWeaken(new Item.Properties().stacksTo(1)));

    //SPECTATOR
    public static final RegistryObject<Item> MIND_READING = ITEMS.register("mindreading",
            () -> new MindReading(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AWE = ITEMS.register("awe",
            () -> new Awe(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FRENZY = ITEMS.register("frenzy",
            () -> new Frenzy(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PLACATE = ITEMS.register("placate",
            () -> new Placate(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BATTLE_HYPNOTISM = ITEMS.register("battlehypnotism",
            () -> new BattleHypnotism(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PSYCHOLOGICAL_INVISIBILITY = ITEMS.register("psychologicalinvisibility",
            () -> new PsychologicalInvisibility(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GUIDANCE = ITEMS.register("guidance",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DREAM_WALKING = ITEMS.register("dreamwalking",
            () -> new DreamWalking(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NIGHTMARE = ITEMS.register("nightmare",
            () -> new Nightmare(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANIPULATE_MOVEMENT = ITEMS.register("manipulatemovement",
            () -> new ManipulateMovement(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANIPULATE_EMOTION = ITEMS.register("manipulateemotion",
            () -> new ManipulateEmotion(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANIPULATE_FONDNESS = ITEMS.register("manipulatefondness",
            () -> new ManipulateFondness(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> APPLY_MANIPULATION = ITEMS.register("applymanipulation",
            () -> new ApplyManipulation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MENTAL_PLAGUE = ITEMS.register("mentalplague",
            () -> new MentalPlague(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MIND_STORM = ITEMS.register("mindstorm",
            () -> new MindStorm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CONSCIOUSNESS_STROLL = ITEMS.register("consciousnessstroll",
            () -> new ConsciousnessStroll(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DRAGON_BREATH = ITEMS.register("dragonbreath",
            () -> new DragonBreath(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PLAGUE_STORM = ITEMS.register("plaguestorm",
            () -> new PlagueStorm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DREAM_WEAVING = ITEMS.register("dreamweaving",
            () -> new DreamWeaving(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DISCERN = ITEMS.register("discern",
            () -> new Discern(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DREAM_INTO_REALITY = ITEMS.register("dreamintoreality",
            () -> new DreamIntoReality(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROPHESIZE_TELEPORT_BLOCK = ITEMS.register("prophesizeblock",
            () -> new ProphesizeTeleportBlock(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROPHESIZE_TELEPORT_PLAYER = ITEMS.register("prophesizeplayer",
            () -> new ProphesizeTeleportPlayer(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PROPHESIZE_DEMISE = ITEMS.register("prophesizedemise",
            () -> new ProphesizeDemise(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_LIFE = ITEMS.register("envisionlife",
            () -> new EnvisionLife(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> METEOR_SHOWER = ITEMS.register("meteorshower",
            () -> new MeteorShower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> METEOR_NO_LEVEL_SHOWER = ITEMS.register("meteorshowernoblockdestruction",
            () -> new MeteorNoLevelShower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_WEATHER = ITEMS.register("envisionweather",
            () -> new EnvisionWeather(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_BARRIER = ITEMS.register("envisionbarrier",
            () -> new EnvisionBarrier(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_DEATH = ITEMS.register("envisiondeath",
            () -> new EnvisionDeath(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_KINGDOM = ITEMS.register("envisionkingdom",
            () -> new EnvisionKingdom(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_LOCATION = ITEMS.register("envisionlocation",
            () -> new EnvisionLocation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_LOCATION_BLINK = ITEMS.register("envisionlocationblink",
            () -> new EnvisionLocationBlink(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ALTERATION = ITEMS.register("alteration",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENVISION_HEALTH = ITEMS.register("envisionhealth",
            () -> new EnvisionHealth(new Item.Properties().stacksTo(1)));

    //WARRIOR
    public static final RegistryObject<Item> GIGANTIFICATION = ITEMS.register("gigantification",
            () -> new Gigantification(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTOFDAWN = ITEMS.register("lightofdawn",
            () -> new LightOfDawn(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DAWNARMORY = ITEMS.register("dawnarmory",
            () -> new DawnArmory(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DAWNWEAPONRY = ITEMS.register("dawnweaponry",
            () -> new DawnWeaponry(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENABLEDISABLEPROTECTION = ITEMS.register("enabledisableprotection",
            () -> new EnableOrDisableProtection(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EYEOFDEMONHUNTING = ITEMS.register("eyeofdemonhunting",
            () -> new EyeOfDemonHunting(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WARRIORDANGERSENSE = ITEMS.register("warriordangersense",
            () -> new WarriorDangerSense(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MERCURYLIQUEFICATION = ITEMS.register("mercuryliquefication",
            () -> new MercuryLiquefication(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SILVERSWORDMANIFESTATION = ITEMS.register("silverswordmanifestation",
            () -> new SilverSwordManifestation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SILVERRAPIER = ITEMS.register("silverrapier",
            () -> new SilverRapier(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SILVERARMORY = ITEMS.register("silverarmory",
            () -> new SilverArmory(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTCONCEALMENT = ITEMS.register("lightconcealment",
            () -> new LightConcealment(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BEAMOFGLORY = ITEMS.register("beamofglory",
            () -> new BeamOfGlory(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AURAOFGLORY = ITEMS.register("auraofglory",
            () -> new AuraOfGlory(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TWILIGHTMANIFESTATION = ITEMS.register("twilightmanifestation",
            () -> new TwilightManifestation(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TWILIGHTSWORD = ITEMS.register("twilightsword",
            () -> new TwilightSword(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MERCURYCAGE = ITEMS.register("mercurycage",
            () -> new MercuryCage(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIVINEHANDRIGHT = ITEMS.register("divinehandright",
            () -> new DivineHandRight(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIVINEHANDLEFT = ITEMS.register("divinehandleft",
            () -> new DivineHandLeft(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AURAOFTWILIGHT = ITEMS.register("auraoftwilight",
            () -> new AuraOfTwilight(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TWILIGHTFREEZE = ITEMS.register("twilightfreeze",
            () -> new TwilightFreeze(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TWILIGHTLIGHT = ITEMS.register("twilightlight",
            () -> new TwilightLight(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TWILIGHTACCELERATE = ITEMS.register("twilightaccelerate",
            () -> new TwilightAccelerate(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GLOBEOFTWILIGHT = ITEMS.register("globeoftwilight",
            () -> new GlobeOfTwilight(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BEAMOFTWILIGHT = ITEMS.register("beamoftwilight",
            () -> new BeamOfTwilight(new Item.Properties().stacksTo(1)));

    //APPRENTICE
    public static final RegistryObject<Item> CREATEDOOR = ITEMS.register("createdoor",
            () -> new CreateDoor(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRICKBURN = ITEMS.register("trickburn",
            () -> new Burn(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRICKBOUNCE = ITEMS.register("trickbounce",
            () -> new BounceProjectiles(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRICKFREEZE = ITEMS.register("trickfreeze",
            () -> new Freeze(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRICKTUMBLE = ITEMS.register("tricktumble",
            () -> new Tumble(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRICKWINDPULL = ITEMS.register("trickwindpull",
            () -> new WindPullArea(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRICKWINDPUSH = ITEMS.register("trickwindpush",
            () -> new WindPushArea(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RECORDSCRIBE = ITEMS.register("recordscribe",
            () -> new ScribeRecord(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRAVELDOOR= ITEMS.register("traveldoor",
            () -> new TravelDoor(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRAVELDOORHOME= ITEMS.register("traveldoorhome",
            () -> new TravelDoorWaypoint(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> INVISIBLEHAND= ITEMS.register("invisiblehand",
            () -> new InvisibleHand(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SCRIBEABILITIES= ITEMS.register("scribeabilities.json",
            () -> new ScribeAbilities(new Item.Properties().stacksTo(1)));

    //INGREDIENTS
    public static final RegistryObject<Item> SPIRIT_EATER_STOMACH_POUCH = ITEMS.register("spirit_eater_stomach_pouch",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DEEP_SEA_MARLINS_BLOOD = ITEMS.register("deep_sea_marlins_blood",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> HORNBEAM_ESSENTIALS_OIL = ITEMS.register("hornbeam_essentials_oil",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> STRING_GRASS_POWDER = ITEMS.register("string_grass_powder",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> RED_CHESTNUT_FLOWER = ITEMS.register("red_chestnut_flower",
            () -> new Item(new Item.Properties().stacksTo(16)));


    //UTIL
    public static final RegistryObject<Item> ABILITYICONTAB = ITEMS.register("zqdsndnkawdnsalnkw",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> INGREDIENTSICONTAB = ITEMS.register("zywdyasdhwahdoioshd",
            () -> new Item(new Item.Properties().stacksTo(1)));


    //POTIONS
    public static final RegistryObject<Item> SPECTATOR_9_POTION = ITEMS.register("spectator_9_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 9));
    public static final RegistryObject<Item> SPECTATOR_8_POTION = ITEMS.register("spectator_8_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 8));
    public static final RegistryObject<Item> SPECTATOR_7_POTION = ITEMS.register("spectator_7_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 7));
    public static final RegistryObject<Item> SPECTATOR_6_POTION = ITEMS.register("spectator_6_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 6));
    public static final RegistryObject<Item> SPECTATOR_5_POTION = ITEMS.register("spectator_5_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 5));
    public static final RegistryObject<Item> SPECTATOR_4_POTION = ITEMS.register("spectator_4_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 4));
    public static final RegistryObject<Item> SPECTATOR_3_POTION = ITEMS.register("spectator_3_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 3));
    public static final RegistryObject<Item> SPECTATOR_2_POTION = ITEMS.register("spectator_2_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 2));
    public static final RegistryObject<Item> SPECTATOR_1_POTION = ITEMS.register("spectator_1_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 1));
    public static final RegistryObject<Item> SPECTATOR_0_POTION = ITEMS.register("spectator_0_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SPECTATOR, 0));
    public static final RegistryObject<Item> SAILOR_9_POTION = ITEMS.register("sailor_9_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 9));
    public static final RegistryObject<Item> SAILOR_8_POTION = ITEMS.register("sailor_8_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 8));
    public static final RegistryObject<Item> SAILOR_7_POTION = ITEMS.register("sailor_7_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 7));
    public static final RegistryObject<Item> SAILOR_6_POTION = ITEMS.register("sailor_6_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 6));
    public static final RegistryObject<Item> SAILOR_5_POTION = ITEMS.register("sailor_5_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 5));
    public static final RegistryObject<Item> SAILOR_4_POTION = ITEMS.register("sailor_4_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 4));
    public static final RegistryObject<Item> SAILOR_3_POTION = ITEMS.register("sailor_3_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 3));
    public static final RegistryObject<Item> SAILOR_2_POTION = ITEMS.register("sailor_2_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 2));
    public static final RegistryObject<Item> SAILOR_1_POTION = ITEMS.register("sailor_1_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 1));
    public static final RegistryObject<Item> SAILOR_0_POTION = ITEMS.register("sailor_0_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.SAILOR, 0));
    public static final RegistryObject<Item> MONSTER_9_POTION = ITEMS.register("monster_9_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 9));
    public static final RegistryObject<Item> MONSTER_8_POTION = ITEMS.register("monster_8_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 8));
    public static final RegistryObject<Item> MONSTER_7_POTION = ITEMS.register("monster_7_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 7));
    public static final RegistryObject<Item> MONSTER_6_POTION = ITEMS.register("monster_6_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 6));
    public static final RegistryObject<Item> MONSTER_5_POTION = ITEMS.register("monster_5_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 5));
    public static final RegistryObject<Item> MONSTER_4_POTION = ITEMS.register("monster_4_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 4));
    public static final RegistryObject<Item> MONSTER_3_POTION = ITEMS.register("monster_3_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 3));
    public static final RegistryObject<Item> MONSTER_2_POTION = ITEMS.register("monster_2_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 2));
    public static final RegistryObject<Item> MONSTER_1_POTION = ITEMS.register("monster_1_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 1));
    public static final RegistryObject<Item> MONSTER_0_POTION = ITEMS.register("monster_0_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.MONSTER, 0));
    public static final RegistryObject<Item> WARRIOR_9_POTION = ITEMS.register("warrior_9_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 9));
    public static final RegistryObject<Item> WARRIOR_8_POTION = ITEMS.register("warrior_8_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 8));
    public static final RegistryObject<Item> WARRIOR_7_POTION = ITEMS.register("warrior_7_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 7));
    public static final RegistryObject<Item> WARRIOR_6_POTION = ITEMS.register("warrior_6_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 6));
    public static final RegistryObject<Item> WARRIOR_5_POTION = ITEMS.register("warrior_5_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 5));
    public static final RegistryObject<Item> WARRIOR_4_POTION = ITEMS.register("warrior_4_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 4));
    public static final RegistryObject<Item> WARRIOR_3_POTION = ITEMS.register("warrior_3_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 3));
    public static final RegistryObject<Item> WARRIOR_2_POTION = ITEMS.register("warrior_2_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 2));
    public static final RegistryObject<Item> WARRIOR_1_POTION = ITEMS.register("warrior_1_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 1));
    public static final RegistryObject<Item> WARRIOR_0_POTION = ITEMS.register("warrior_0_potion",
            () -> new BeyonderPotion(new Item.Properties().stacksTo(1), BeyonderClassInit.WARRIOR, 0));
    public static final RegistryObject<Item> BEYONDER_RESET_POTION = ITEMS.register("beyonder_reset_potion",
            () -> new BeyonderResetPotion(new Item.Properties().stacksTo(1)));


    //SEALED ARTIFACTS
    public static final RegistryObject<Item> DEATHKNELL = ITEMS.register("deathknell",
            () -> new DeathKnell(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SYMPHONYOFHATRED = ITEMS.register("symphonyofhatred",
            () -> new SymphonyOfHatred(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WINTRYBLADE = ITEMS.register("wintryblade",
            () -> new WintryBlade(Tiers.NETHERITE, 4,-2,new Item.Properties()));


    //ARMOR
    public static final RegistryObject<Item> DAWN_HELMET = ITEMS.register("dawn_helmet",
            () -> new DawnArmorItem(ModArmorMaterials.DAWN, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> DAWN_CHESTPLATE = ITEMS.register("dawn_chestplate",
            () -> new DawnArmorItem(ModArmorMaterials.DAWN, ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> DAWN_LEGGINGS = ITEMS.register("dawn_leggings",
            () -> new DawnArmorItem(ModArmorMaterials.DAWN, ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> DAWN_BOOTS = ITEMS.register("dawn_boots",
            () -> new DawnArmorItem(ModArmorMaterials.DAWN, ArmorItem.Type.BOOTS,new Item.Properties()));
    public static final RegistryObject<Item> SILVER_HELMET = ITEMS.register("silver_helmet",
            () -> new SilverArmorItem(ModArmorMaterials.SILVER, ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> SILVER_CHESTPLATE = ITEMS.register("silver_chestplate",
            () -> new SilverArmorItem(ModArmorMaterials.SILVER, ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> SILVER_LEGGINGS = ITEMS.register("silver_leggings",
            () -> new SilverArmorItem(ModArmorMaterials.SILVER, ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> SILVER_BOOTS = ITEMS.register("silver_boots",
            () -> new SilverArmorItem(ModArmorMaterials.SILVER, ArmorItem.Type.BOOTS,new Item.Properties()));

    //TOOLS
    public static final RegistryObject<Item> SWORDOFDAWN = ITEMS.register("swordofdawn",
            () -> new SwordOfDawn(Tiers.NETHERITE, 10,-2.8f,new Item.Properties()));
    public static final RegistryObject<Item> SWORDOFTWILIGHT = ITEMS.register("swordoftwilight",
            () -> new SwordOfTwilight(Tiers.NETHERITE, 50,-2.8f,new Item.Properties()));
    public static final RegistryObject<Item> SWORDOFSILVER = ITEMS.register("swordofsilver",
            () -> new SwordOfSilver(Tiers.NETHERITE, 12,-2f,new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PICKAXEOFDAWN = ITEMS.register("pickaxeofdawn",
            () -> new PickaxeOfDawn(Tiers.NETHERITE, 0,0,new Item.Properties()));
    public static final RegistryObject<Item> SPEAROFDAWN = ITEMS.register("spearofdawn",
            () -> new SpearOfDawn(Tiers.NETHERITE, 4,-1.5f,new Item.Properties()));


    //MISC
    public static final RegistryObject<Item> LUCKBOTTLEITEM = ITEMS.register("luckbottleitem",
            () -> new LuckBottleItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUCKYGOLDCOIN = ITEMS.register("luckygoldcoin",
            () -> new LuckyGoldCoin(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTNINGRUNE = ITEMS.register("lightningstrikerune",
            () -> new LightningStrikeRune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLAMERUNE = ITEMS.register("flamerune",
            () -> new LightningStrikeRune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WITHERRUNE = ITEMS.register("witherrune",
            () -> new LightningStrikeRune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FREEZERUNE = ITEMS.register("freezerune",
            () -> new LightningStrikeRune(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CONFUSIONRUNE = ITEMS.register("confusionrune",
            () -> new LightningStrikeRune(new Item.Properties().stacksTo(1)));

    //OTHER
    public static final RegistryObject<Item> BEYONDER_CHARACTERISTIC = ITEMS.register("beyondercharacteristics",
            () -> new BeyonderCharacteristic(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BEYONDER_ABILITY_USER = ITEMS.register("beyonderabilityuser",
            () -> new BeyonderAbilityUser(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("testitem",
            () -> new TestItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ALLY_MAKER = ITEMS.register("ally_maker",
            () -> new AllyMaker(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
