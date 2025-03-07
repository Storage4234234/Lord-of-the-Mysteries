package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.GlobeOfTwilightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.joml.Random;
import org.joml.Vector3f;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class GlobeOfTwilight extends SimpleAbilityItem {


    public GlobeOfTwilight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        globeOfTwilight(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        globeOfTwilightTarget(player, interactionTarget);
        return InteractionResult.SUCCESS;
    }

    public static void globeOfTwilight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(100))) {
                if (living != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, living)) {
                    living.getPersistentData().putInt("globeOfTwilightX", (int) living.getX());
                    living.getPersistentData().putInt("globeOfTwilightY", (int) living.getY());
                    living.getPersistentData().putInt("globeOfTwilightZ", (int) living.getZ());
                    living.getPersistentData().putInt("globeOfTwilight", 50 - (BeyonderUtil.getSequence(living) * 5));
                    living.getPersistentData().putInt("globeOfTwilightSize", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.GLOBEOFTWILIGHT.get()));
                }
            }
        }
    }

    public static void globeOfTwilightTarget(LivingEntity livingEntity, LivingEntity target) {
        if (!livingEntity.level().isClientSide()) {
            target.getPersistentData().putInt("globeOfTwilightX", (int) target.getX());
            target.getPersistentData().putInt("globeOfTwilightY", (int) target.getY());
            target.getPersistentData().putInt("globeOfTwilightZ", (int) target.getZ());
            target.getPersistentData().putInt("globeOfTwilight", 50 - (BeyonderUtil.getSequence(target) * 5));
            target.getPersistentData().putInt("globeOfTwilightSize", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.GLOBEOFTWILIGHT.get()) * 5);
        }
    }

    public static void globeOfTwilightTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide()) {
            int counter = tag.getInt("globeOfTwilight");
            int size = tag.getInt("globeOfTwilightSize");
            int x = tag.getInt("globeOfTwilightX");
            int y = tag.getInt("globeOfTwilightY");
            int z = tag.getInt("globeOfTwilightZ");
            if (counter >= 1) {
                tag.putInt("globeOfTwilight", counter - 1);
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    double halfSize = size / 10.0;
                    Vector3f orangeColor = new Vector3f(1.0F, 0.5F, 0.0F);
                    for (int i = 0; i < 12; i++) {
                        double startX, startY, startZ, endX, endY, endZ;

                        if (i < 4) {
                            // Bottom face edges
                            startX = x + (i == 0 || i == 3 ? -halfSize : halfSize);
                            startZ = z + (i == 0 || i == 1 ? -halfSize : halfSize);
                            startY = y - halfSize;
                            endX = x + (i == 1 || i == 2 ? halfSize : -halfSize);
                            endZ = z + (i == 2 || i == 3 ? halfSize : -halfSize);
                            endY = y - halfSize;
                        } else if (i < 8) {
                            // Top face edges
                            startX = x + (i == 4 || i == 7 ? -halfSize : halfSize);
                            startZ = z + (i == 4 || i == 5 ? -halfSize : halfSize);
                            startY = y + halfSize;
                            endX = x + (i == 5 || i == 6 ? halfSize : -halfSize);
                            endZ = z + (i == 6 || i == 7 ? halfSize : -halfSize);
                            endY = y + halfSize;
                        } else {
                            // Vertical edges connecting top and bottom faces
                            int j = i - 8;
                            startX = x + (j == 0 || j == 2 ? -halfSize : halfSize);
                            startZ = z + (j == 0 || j == 1 ? -halfSize : halfSize);
                            startY = y - halfSize;
                            endX = startX;
                            endZ = startZ;
                            endY = y + halfSize;
                        }
                        double stepSize = 0.5;
                        double distX = endX - startX;
                        double distY = endY - startY;
                        double distZ = endZ - startZ;
                        double dist = Math.sqrt(distX * distX + distY * distY + distZ * distZ);
                        int steps = (int) Math.ceil(dist / stepSize);
                        for (int step = 0; step <= steps; step++) {
                            double factor = steps > 0 ? (double) step / steps : 0;
                            double particleX = startX + distX * factor;
                            double particleY = startY + distY * factor;
                            double particleZ = startZ + distZ * factor;
                            serverLevel.sendParticles(new DustParticleOptions(orangeColor, 1), particleX, particleY, particleZ, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
            if (counter == 1) {
                float random = BeyonderUtil.getRandomInRange(5);
                float random2 = BeyonderUtil.getRandomInRange(5);
                GlobeOfTwilightEntity globeOfTwilight = new GlobeOfTwilightEntity(EntityInit.GLOBE_OF_TWILIGHT_ENTITY.get(), livingEntity.level());
                ScaleTypes.BASE.getScaleData(globeOfTwilight).setTargetScale(Math.max(1,size / 5));
                globeOfTwilight.setRandomPitch(random);
                globeOfTwilight.setRandomYaw(random2);
                globeOfTwilight.teleportTo(x,y,z);
                tag.putInt("globeOfTwilight", 0);
                livingEntity.level().addFreshEntity(globeOfTwilight);
            }
        }
    }
}

