package net.swimmingtuna.lotm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber
public class CathedralBuilderUtil {
    private static final Queue<Runnable> tickTasks = new ConcurrentLinkedQueue<>();
    private static int tickCounter = 0; // Counter to track ticks

    // Method to schedule the building task
    public static void placeCorpseCathedral(ServerLevel serverLevel, int x, int y, int z) {
        for (int partIndex = 1; partIndex < 48; partIndex++) {
            int finalPartIndex = partIndex;
            tickTasks.add(() -> {
                String structureName = "corpse_cathedral_" + finalPartIndex;
                ResourceLocation structureLocation = new ResourceLocation("lotm", structureName);
                StructureTemplate part = serverLevel.getStructureManager().getOrCreate(structureLocation);
                BlockPos tagPos = new BlockPos(x, y + (finalPartIndex * 2), z);
                StructurePlaceSettings settings = BeyonderUtil.getStructurePlaceSettings(new BlockPos(x, y, z));
                part.placeInWorld(serverLevel, tagPos, tagPos, settings, null, Block.UPDATE_ALL);
            });
        }
    }

    // Tick event listener
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            // Only execute tasks on every second tick
            if (tickCounter % 5 == 0) {
                Runnable task = tickTasks.poll();
                if (task != null) {
                    task.run();
                }
            }
            if (tickCounter > 10000) tickCounter = 0;
        }
    }
}
