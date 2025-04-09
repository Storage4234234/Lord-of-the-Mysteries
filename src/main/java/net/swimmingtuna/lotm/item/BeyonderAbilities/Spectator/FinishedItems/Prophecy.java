package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.MeteorEntity;
import net.swimmingtuna.lotm.entity.MeteorNoLevelEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake.isOnSurface;

public class Prophecy extends SimpleAbilityItem {

    public Prophecy(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 1, 750, 2400);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        prophecy(player);
        return InteractionResult.SUCCESS;
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            if (player.tickCount % 2 == 0 && !level.isClientSide()) {
                if (player.getMainHandItem().getItem() instanceof Prophecy) {
                    player.displayClientMessage(Component.literal("Current Prophecy is: " + prophecyString(player)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA), true);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    public static void prophecy (LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            int prophecy = tag.getInt("spectatorProphecyItem");
            if (prophecy == 1) {
                for (int i = 0; i < BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()); i++) {
                    MeteorEntity.summonMultipleMeteors(livingEntity);
                    if (livingEntity instanceof Player player) {
                        player.sendSystemMessage(Component.literal("You prophesized meteors into the world").withStyle(BeyonderUtil.getStyle(livingEntity)));
                    }
                }
            } else if (prophecy == 2) {
                for (int i = 0; i < BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()); i++) {
                    MeteorNoLevelEntity.summonMultipleMeteors(livingEntity);
                    if (livingEntity instanceof Player player) {
                        player.sendSystemMessage(Component.literal("You prophesized meteors that don't destroy blocks into the world").withStyle(BeyonderUtil.getStyle(livingEntity)));
                    }
                }
            } else if (prophecy == 3) {
                TornadoEntity tornado = new TornadoEntity(livingEntity.level(), livingEntity, 0, 0, 0);
                tornado.setTornadoHeight((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TORNADO.get()));
                tornado.setTornadoRadius((int) ((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TORNADO.get()) / 2.5));
                if (BeyonderUtil.getSequence(livingEntity) <= 0) {
                    tornado.setTornadoLightning(true);
                    tornado.setTornadoLifecount(400);
                } else {
                    tornado.setTornadoLifecount(200);
                }
                tornado.setTornadoPickup(false);
                tornado.setTornadoMov(livingEntity.getLookAngle().scale(0.5f).toVector3f());
                livingEntity.level().addFreshEntity(tornado);
                if (livingEntity instanceof Player player) {
                    player.sendSystemMessage(Component.literal("You prophesized a tornado into the world").withStyle(BeyonderUtil.getStyle(livingEntity)));
                }
            } else if (prophecy == 4) {
                tag.putInt("prophecyEarthquake", (int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 15));
                if (livingEntity instanceof Player player) {
                    player.sendSystemMessage(Component.literal("You prophesized an eartuquake into the world").withStyle(BeyonderUtil.getStyle(livingEntity)));
                }
            } else if (prophecy == 5) {
                tag.putInt("prophecyPlague",  (int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 10));
                if (livingEntity instanceof Player player) {
                    player.sendSystemMessage(Component.literal("You prophesized a plague around you into the world").withStyle(BeyonderUtil.getStyle(livingEntity)));
                }
            } else if (prophecy == 6) {
                tag.putInt("prophecySinkhole", 80);
                tag.putInt("prophecySinkholeX", (int) livingEntity.getX());
                tag.putInt("prophecySinkholeY", (int) livingEntity.getY());
                tag.putInt("prophecySinkholeZ", (int) livingEntity.getZ());
                int sinkholeRadius = (int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 3);
                sinkholeRadius = Math.max(5, sinkholeRadius);
                if (livingEntity instanceof Player player) {
                    player.sendSystemMessage(Component.literal("You prophesized a sinkhole into the world").withStyle(BeyonderUtil.getStyle(livingEntity)));
                }
                tag.putInt("sinkholeProphecyRadius", sinkholeRadius);
            } else if (prophecy == 7) {
                if (livingEntity instanceof Player player) {
                    player.sendSystemMessage(Component.literal("You prophesized fortune onto yourself").withStyle(BeyonderUtil.getStyle(livingEntity)));
                }
                tag.putDouble("luck", tag.getInt("luck") + BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 8);
            }
        }
    }

    public static void prophecyTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity livingEntity = event.getEntity();
            CompoundTag tag = livingEntity.getPersistentData();
            int x = tag.getInt("prophecyEarthquake");
            int y = tag.getInt("prophecyPlague");
            int z = tag.getInt("prophecySinkhole");
            if (x >= 1) {
                tag.putInt("prophecyEarthquake", x - 1);
                int sequence = BeyonderUtil.getSequence(livingEntity);
                int radius = (int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 8);
                if (x % 20 == 0) {
                    for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate((radius)))) {
                        if (entity != livingEntity && !BeyonderUtil.areAllies(livingEntity, entity)) {
                            if (entity.onGround()) {
                                entity.hurt(livingEntity.damageSources().fall(), 25 - (sequence * 5));
                            }
                        }
                    }
                }
                if (x % 2 == 0) {
                    AABB checkArea = livingEntity.getBoundingBox().inflate(radius);
                    Random random = new Random();
                    for (BlockPos blockPos : BlockPos.betweenClosed(
                            new BlockPos((int) checkArea.minX, (int) checkArea.minY, (int) checkArea.minZ),
                            new BlockPos((int) checkArea.maxX, (int) checkArea.maxY, (int) checkArea.maxZ))) {

                        if (!livingEntity.level().getBlockState(blockPos).isAir() && isOnSurface(livingEntity.level(), blockPos)) {
                            if (random.nextInt(20) == 1) {
                                BlockState blockState = livingEntity.level().getBlockState(blockPos);
                                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                                            blockPos.getX(),
                                            blockPos.getY() + 1,
                                            blockPos.getZ(),
                                            0, 0.0, 0.0, 0, 0);
                                }
                            }
                            if (random.nextInt(6000) == 1) {
                                livingEntity.level().destroyBlock(blockPos, false);
                            } else if (random.nextInt(12000) == 2) {
                                StoneEntity stoneEntity = new StoneEntity(livingEntity.level(), livingEntity);
                                ScaleData scaleData = ScaleTypes.BASE.getScaleData(stoneEntity);
                                stoneEntity.teleportTo(blockPos.getX(), blockPos.getY() + 3, blockPos.getZ());
                                stoneEntity.setDeltaMovement(0, (3 + (Math.random() * (6 - 3))), 0);
                                stoneEntity.setStoneYRot((int) (Math.random() * 18));
                                stoneEntity.setStoneXRot((int) (Math.random() * 18));
                                scaleData.setScale((float) (1 + (Math.random()) * 2.0f));
                                livingEntity.level().addFreshEntity(stoneEntity);
                            }
                        }
                    }
                }
            }
            if (y >= 1) {
                tag.putInt("prophecyPlague", y - 1);
                if (livingEntity.tickCount % 10 == 0) {
                    for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 5))) {
                        if (living != livingEntity && !BeyonderUtil.areAllies(livingEntity, living)) {
                            BeyonderUtil.applyMobEffect(living, MobEffects.WITHER, 200, 5, true, true);
                            BeyonderUtil.applyMobEffect(living, ModEffects.NOREGENERATION.get(), 100, 1, true, true);
                            BeyonderUtil.applyMobEffect(living, MobEffects.WEAKNESS, 200, 3, true, true);
                            BeyonderUtil.applyMobEffect(living, MobEffects.CONFUSION, 100, 1, true, true);
                        }
                    }
                }
            }
            if (z >= 1) {
                int livingX = tag.getInt("prophecySinkholeX");
                int livingY = tag.getInt("prophecySinkholeY");
                int livingZ = tag.getInt("prophecySinkholeZ");
                tag.putInt("prophecySinkhole", z - 1);
                int currentDepth = tag.getInt("sinkholeCurrentDepth");
                if (z == 80) {
                    currentDepth = 0;
                    tag.putInt("sinkholeCurrentDepth", 0);
                }
                int sinkholeRadius = (int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 1.5);
                sinkholeRadius = Math.max(5, sinkholeRadius);
                BlockPos center = new BlockPos(livingX, livingY, livingZ);
                if (z % 2 == 0 && currentDepth < 40) {
                    currentDepth++;
                    tag.putInt("sinkholeCurrentDepth", currentDepth);
                    for (int i = -sinkholeRadius; i <= sinkholeRadius; i++) {
                        for (int j = -sinkholeRadius; j <= sinkholeRadius; j++) {
                            if (i * i + j * j <= sinkholeRadius * sinkholeRadius) {
                                BlockPos pos = center.offset(i, 0, j);
                                BlockPos targetPos = pos.offset(0, -currentDepth, 0);
                                if (targetPos.getY() <= -50) {
                                    continue;
                                }
                                BlockState state = livingEntity.level().getBlockState(targetPos);
                                if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && !state.is(BlockTags.WITHER_IMMUNE)) {
                                    if (livingEntity.getRandom().nextInt(5) == 0 && livingEntity.level() instanceof ServerLevel serverLevel) {serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
                                    }
                                    livingEntity.level().destroyBlock(targetPos, false);
                                }
                            }
                        }
                    }
                }
                if (z % 10 == 0) {
                    for (int depth = 1; depth <= currentDepth; depth++) {
                        for (int i = -sinkholeRadius; i <= sinkholeRadius; i++) {
                            for (int j = -sinkholeRadius; j <= sinkholeRadius; j++) {
                                if (i * i + j * j <= sinkholeRadius * sinkholeRadius) {
                                    BlockPos pos = center.offset(i, 0, j);
                                    BlockPos targetPos = pos.offset(0, -depth, 0);
                                    if (targetPos.getY() <= -50) {
                                        continue;
                                    }
                                    BlockState state = livingEntity.level().getBlockState(targetPos);
                                    if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && !state.is(BlockTags.WITHER_IMMUNE)) {livingEntity.level().destroyBlock(targetPos, false);
                                    }
                                }
                            }
                        }
                    }
                }
                List<Entity> entities = livingEntity.level().getEntitiesOfClass(Entity.class, new AABB(center.getX() - sinkholeRadius - 5, center.getY() - currentDepth - 5, center.getZ() - sinkholeRadius - 5, center.getX() + sinkholeRadius + 5, center.getY() + 10, center.getZ() + sinkholeRadius + 5));
                for (Entity entity : entities) {
                    if (entity == livingEntity) {
                        continue;
                    }
                    double dx = center.getX() + 0.5 - entity.getX();
                    double dz = center.getZ() + 0.5 - entity.getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    if (distance <= sinkholeRadius * 0.8) {
                        if (entity.getY() > center.getY() - currentDepth - 1) {
                            entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.4, 0));
                            entity.hurtMarked = true;
                        }
                    }
                    else if (distance <= sinkholeRadius * 2) {
                        dx = dx / distance;
                        dz = dz / distance;
                        double pullStrength = 0.1 * (1 - distance / (sinkholeRadius * 2));
                        entity.setDeltaMovement(entity.getDeltaMovement().add(dx * (pullStrength * 3), -0.1, dz * (pullStrength * 3)));
                        entity.hurtMarked = true;
                        if (entity instanceof Player player && !player.isCreative()) {
                            player.setSprinting(false);
                            if (player.isCrouching()) {
                                player.setDeltaMovement(player.getDeltaMovement().scale(0.8));
                                player.hurtMarked = true;
                            }
                        }
                    }
                }
            }
        }
    }


    public static String prophecyString(Player pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        int luckManipulation = tag.getInt("spectatorProphecyItem");
        if (luckManipulation == 1) {
            return "Meteor Shower";
        } else if (luckManipulation == 2) {
            return "Meteor Shower (No Destruction)";
        } else if (luckManipulation == 3) {
            return "Tornado";
        } else if (luckManipulation == 4) {
            return "Earthquake";
        } else if (luckManipulation == 5) {
            return "Plague";
        } else if (luckManipulation == 6) {
            return "Sinkhole";
        } else if (luckManipulation == 7) {
            return "Luck";
        }
        return "None";
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("You can use Prophecy in two ways. Firstly, it can be used to instantly manifest a prophecy of your choice. It can also be used by holding it in your hand and typing in this way, (Player Name) will (Prophecy Event) in (Number) (Seconds/Minutes)"));
        tooltipComponents.add(Component.literal("Left Click to Cycle Between Prophecies"));
        tooltipComponents.add(Component.literal("Prophecy Events are ")
                .append(Component.literal("encounter a meteor ").withStyle(ChatFormatting.RED))
                .append(Component.literal("encounter a tornado ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("encounter an earthquake ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x8B4513))))
                .append(Component.literal("encounter a plague ").withStyle(ChatFormatting.DARK_GREEN))
                .append(Component.literal("encounter a sinhkole ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x8B4513))))
                .append(Component.literal("encounter weakness ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("(abilities included) "))
                .append(Component.literal("be healed ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal("be lucky ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("be unlucky ").withStyle(ChatFormatting.RED))
                .append(Component.literal("have potion success ").withStyle(ChatFormatting.GREEN))
        );


        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("750").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2 Minutes").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }


    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target == null && livingEntity.getMaxHealth() == livingEntity.getHealth() && livingEntity.getPersistentData().getDouble("luck") <= 200) {
            livingEntity.getPersistentData().putInt("spectatorProphecyItem", 7);
        } else if (target != null) {
            Random random = new Random();
            livingEntity.getPersistentData().putInt("spectatorProphecyItem", random.nextInt(8));
        }
        return 0;
    }
}
