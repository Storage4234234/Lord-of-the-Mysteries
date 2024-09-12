package net.swimmingtuna.lotm;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.client.ClientConfigs;
import net.swimmingtuna.lotm.client.Configs;
import net.swimmingtuna.lotm.entity.Renderers.AqueousLightEntityPullRenderer;
import net.swimmingtuna.lotm.entity.Renderers.AqueousLightEntityPushRenderer;
import net.swimmingtuna.lotm.entity.Renderers.AqueousLightEntityRenderer;
import net.swimmingtuna.lotm.entity.Renderers.LightningEntityRenderer;
import net.swimmingtuna.lotm.events.ClientEvents;
import net.swimmingtuna.lotm.init.*;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.PlayerMobs.NameManager;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.worldgen.biome.BiomeModifierRegistry;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LOTM.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LOTM {

    public static final int NEW_STRUCTURE_SIZE = 512;
    public static Supplier<Boolean> fadeOut;
    public static Supplier<Integer> fadeTicks;
    public static Supplier<Double> maxBrightness;
    public static Supplier<Double> fadeRate = () -> maxBrightness.get() / fadeTicks.get();

    public static ResourceLocation modLoc(String name) {
        return new ResourceLocation(MOD_ID, name);
    }


    public static final String MOD_ID = "lotm";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation rl(String name) {
        return new ResourceLocation(LOTM.MOD_ID, name);
    }



    public LOTM() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
        BeyonderClassInit.BEYONDER_CLASS.register(modEventBus);
        BeyonderHolderAttacher.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.commonSpec);
        BlockEntityInit.BLOCK_ENTITIES.register(modEventBus);
        CreativeTabInit.register(modEventBus);
        ItemInit.register(modEventBus);
        BlockInit.register(modEventBus);
        ModEffects.register(modEventBus);
        ModAttributes.register(modEventBus);
        EntityInit.register(modEventBus);
        modEventBus.addListener(EntityInit::registerEntityAttributes);
        CommandInit.ARGUMENT_TYPES.register(modEventBus);
        ParticleInit.register(modEventBus);
        SoundInit.register(modEventBus);

        modEventBus.addListener(ClientEvents::onRegisterOverlays);
        BiomeModifierRegistry.BIOME_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s-client.toml", LOTM.MOD_ID));
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.addListener(CommandInit::onCommandRegistration);


    }
    private void serverAboutToStart(ServerAboutToStartEvent event) {
        NameManager.INSTANCE.init();
    }



    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.AQUEOUS_LIGHT_ENTITY.get(), AqueousLightEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.AQUEOUS_LIGHT_ENTITY_PUSH.get(), AqueousLightEntityPushRenderer::new);
        event.registerEntityRenderer(EntityInit.AQUEOUS_LIGHT_ENTITY_PULL.get(), AqueousLightEntityPullRenderer::new);
        event.registerEntityRenderer(EntityInit.LIGHTNING_ENTITY.get(), LightningEntityRenderer::new);


    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ItemInit.TestItem);
            event.accept(ItemInit.LightningStorm);
            event.accept(ItemInit.Roar);
            event.accept(ItemInit.CalamityIncarnationTsunami);
            event.accept(ItemInit.CalamityIncarnationTornado);
            event.accept(ItemInit.LightningBall);
            event.accept(ItemInit.LightningBallAbsorb);
            event.accept(ItemInit.MatterAccelerationBlocks);
            event.accept(ItemInit.MatterAccelerationEntities);
            event.accept(ItemInit.MatterAccelerationSelf);
            event.accept(ItemInit.StormSeal);
            event.accept(ItemInit.SailorLightningTravel);
            event.accept(ItemInit.StormSeal);
            event.accept(ItemInit.VolcanicEruption);
            event.accept(ItemInit.ExtremeColdness);
            event.accept(ItemInit.LightningBranch);
            event.accept(ItemInit.Earthquake);
            event.accept(ItemInit.StarOfLightning);
            event.accept(ItemInit.RainEyes);
            event.accept(ItemInit.SonicBoom);
            event.accept(ItemInit.SailorLightning);
            event.accept(ItemInit.WaterSphere);
            event.accept(ItemInit.ThunderClap);
            event.accept(ItemInit.Tyranny);
            event.accept(ItemInit.WaterColumn);
            event.accept(ItemInit.Hurricane);
            event.accept(ItemInit.Tornado);
            event.accept(ItemInit.MindReading);
            event.accept(ItemInit.Awe);
            event.accept(ItemInit.Frenzy);
            event.accept(ItemInit.Placate);
            event.accept(ItemInit.BattleHypnotism);
            event.accept(ItemInit.PsychologicalInvisibility);
            event.accept(ItemInit.Guidance);
            event.accept(ItemInit.BeyonderAbilityUser);
            event.accept(ItemInit.Alteration);
            event.accept(ItemInit.DreamWalking);
            event.accept(ItemInit.Nightmare);
            event.accept(ItemInit.ManipulateMovement);
            event.accept(ItemInit.ManipulateEmotion);
            event.accept(ItemInit.ApplyManipulation);
            event.accept(ItemInit.MentalPlague);
            event.accept(ItemInit.MindStorm);
            event.accept(ItemInit.ManipulateFondness);
            event.accept(ItemInit.ConsciousnessStroll);
            event.accept(ItemInit.DragonBreath);
            event.accept(ItemInit.PlagueStorm);
            event.accept(ItemInit.DreamWeaving);
            event.accept(ItemInit.Discern);
            event.accept(ItemInit.Tsunami);
            event.accept(ItemInit.DreamIntoReality);
            event.accept(ItemInit.ProphesizeTeleportBlock);
            event.accept(ItemInit.ProphesizeTeleportPlayer);
            event.accept(ItemInit.ProphesizeDemise);
            event.accept(ItemInit.EnvisionLife);
            event.accept(ItemInit.MeteorShower);
            event.accept(ItemInit.MeteorNoLevelShower);
            event.accept(ItemInit.EnvisionWeather);
            event.accept(ItemInit.EnvisionBarrier);
            event.accept(ItemInit.EnvisionDeath);
            event.accept(ItemInit.EnvisionKingdom);
            event.accept(ItemInit.EnvisionLocation);
            event.accept(ItemInit.EnvisionLocationBlink);
            event.accept(ItemInit.EnvisionHealth);
            event.accept(ItemInit.SPECTATOR_9_POTION);
            event.accept(ItemInit.SPECTATOR_8_POTION);
            event.accept(ItemInit.SPECTATOR_7_POTION);
            event.accept(ItemInit.SPECTATOR_6_POTION);
            event.accept(ItemInit.SPECTATOR_5_POTION);
            event.accept(ItemInit.SPECTATOR_4_POTION);
            event.accept(ItemInit.SPECTATOR_3_POTION);
            event.accept(ItemInit.SPECTATOR_2_POTION);
            event.accept(ItemInit.SPECTATOR_1_POTION);
            event.accept(ItemInit.SPECTATOR_0_POTION);
            event.accept(ItemInit.BEYONDER_RESET_POTION);
            event.accept(ItemInit.TYRANT_9_POTION);
            event.accept(ItemInit.TYRANT_8_POTION);
            event.accept(ItemInit.TYRANT_7_POTION);
            event.accept(ItemInit.TYRANT_6_POTION);
            event.accept(ItemInit.TYRANT_5_POTION);
            event.accept(ItemInit.TYRANT_4_POTION);
            event.accept(ItemInit.TYRANT_3_POTION);
            event.accept(ItemInit.TYRANT_2_POTION);
            event.accept(ItemInit.TYRANT_1_POTION);
            event.accept(ItemInit.TYRANT_0_POTION);
            event.accept(ItemInit.RagingBlows);
            event.accept(ItemInit.AqueousLightDrown);
            event.accept(ItemInit.EnableOrDisableLightning);
            event.accept(ItemInit.AqueousLightPull);
            event.accept(ItemInit.AqueousLightPush);
            event.accept(ItemInit.WindManipulationFlight);
            event.accept(ItemInit.WindManipulationBlade);
            event.accept(ItemInit.WindManipulationCushion);
            event.accept(ItemInit.WindManipulationSense);
            event.accept(ItemInit.AcidicRain);
            event.accept(ItemInit.AquaticLifeManipulation);
            event.accept(ItemInit.TsunamiSeal);
            event.accept(ItemInit.SirenSongHarm);
            event.accept(ItemInit.SirenSongWeaken);
            event.accept(ItemInit.SirenSongStun);
            event.accept(ItemInit.SirenSongStrengthen);
        }
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(BlockInit.VISIONARY_BARRIER_BLOCK);
            event.accept(BlockInit.VISIONARY_GLASS_PANE);
            event.accept(BlockInit.LOTM_LIGHT_BLUE_STAINED_GLASS);
            event.accept(BlockInit.LOTM_RED_NETHER_BRICKS);
            event.accept(BlockInit.LOTM_WHITE_STAINED_GLASS);
            event.accept(BlockInit.LOTM_BLUE_STAINED_GLASS);
            event.accept(BlockInit.CATHEDRAL_BLOCK);
            event.accept(BlockInit.MINDSCAPE_BLOCK);
            event.accept(BlockInit.MINDSCAPE_OUTSIDE);
            event.accept(BlockInit.LOTM_BOOKSHELF);
            event.accept(BlockInit.LOTM_DEEPSLATE_BRICKS);
            event.accept(BlockInit.LOTM_REDSTONE_BLOCK);
            event.accept(BlockInit.LOTM_SANDSTONE);
            event.accept(BlockInit.MINDSCAPE_OUTSIDE);
            event.accept(BlockInit.LOTM_POLISHED_DIORITE);
            event.accept(BlockInit.LOTM_DARK_OAK_PLANKS);
            event.accept(BlockInit.LOTM_QUARTZ);
            event.accept(BlockInit.LOTM_CHISELED_STONE_BRICKS);
            event.accept(BlockInit.LOTM_MANGROVE_PLANKS);
            event.accept(BlockInit.LOTM_SPRUCE_PLANKS);
            event.accept(BlockInit.LOTM_SPRUCE_LOG);
            event.accept(BlockInit.LOTM_OAK_PLANKS);
            event.accept(BlockInit.LOTM_BIRCH_PLANKS);
            event.accept(BlockInit.LOTM_BLACK_CONCRETE);
            event.accept(BlockInit.LOTM_STONE);
            event.accept(BlockInit.LOTM_STONE_BRICKS);
            event.accept(BlockInit.LOTM_CRACKED_STONE_BRICKS);
            event.accept(BlockInit.LOTM_LIGHT_BLUE_CONCRETE);
            event.accept(BlockInit.LOTM_BLUE_CONCRETE);
            event.accept(BlockInit.LOTM_BLACKSTONE);
            event.accept(BlockInit.LOTM_WHITE_CONCRETE);
            event.accept(BlockInit.LOTM_POLISHED_ANDESITE);
            event.accept(BlockInit.LOTM_POLISHED_BLACKSTONE);
            event.accept(BlockInit.LOTM_SEA_LANTERN);
            event.accept(BlockInit.LOTM_OAK_LOG);

            event.accept(BlockInit.VISIONARY_BLACK_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_WHITE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_GRAY_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_LIGHT_GRAY_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_BROWN_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_PURPLE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_CYAN_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_BLUE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_LIGHT_BLUE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_LIME_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_GREEN_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_YELLOW_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_RED_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_ORANGE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_PINK_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_MAGENTA_STAINED_GLASS_PANE);
            event.accept(BlockInit.LOTM_LIGHT_BLUE_CARPET);
            event.accept(BlockInit.LOTM_CHAIN);
            event.accept(BlockInit.LOTM_LANTERN);

            event.accept(BlockInit.LOTM_DARKOAK_SLAB);
            event.accept(BlockInit.LOTM_QUARTZ_SLAB);
            event.accept(BlockInit.LOTM_DARKOAK_STAIRS);
            event.accept(BlockInit.LOTM_OAK_STAIRS);
            event.accept(BlockInit.LOTM_QUARTZ_STAIRS);
            event.accept(BlockInit.LOTM_DEEPSLATEBRICK_STAIRS);
        }
    }
}
