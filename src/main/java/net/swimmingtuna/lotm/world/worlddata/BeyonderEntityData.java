package net.swimmingtuna.lotm.world.worlddata;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.*;
import java.util.stream.Collectors;

import static net.swimmingtuna.lotm.util.BeyonderUtil.getAbilities;
import static net.swimmingtuna.lotm.util.BeyonderUtil.useAvailableAbilityAsMob;

public class BeyonderEntityData extends SavedData {
    private static final String DATA_NAME = "EntityStringMapping";
    private final Map<EntityType<?>, String> entityToStringMap = new HashMap<>();
    private final Map<String, List<EntityType<?>>> stringToEntitiesMap = new HashMap<>();

    private BeyonderEntityData() {
    }

    public static BeyonderEntityData getInstance(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                BeyonderEntityData::load,
                BeyonderEntityData::create,
                DATA_NAME
        );
    }


    public boolean setEntityString(EntityType<?> entityType, String value) {
        // Check if already mapped to the same value
        if (value.equals(entityToStringMap.get(entityType))) {
            return false;
        }

        // Remove existing mapping if any
        removeEntity(entityType);

        // Add new mapping
        entityToStringMap.put(entityType, value);

        // Update reverse mapping
        stringToEntitiesMap.computeIfAbsent(value, k -> new ArrayList<>()).add(entityType);

        setDirty();
        return true;
    }


    public String getStringForEntity(EntityType<?> entityType) {
        return entityToStringMap.get(entityType);
    }

    public List<EntityType<?>> getEntitiesForString(String value) {
        return stringToEntitiesMap.getOrDefault(value, new ArrayList<>());
    }

    public boolean removeEntity(EntityType<?> entityType) {
        String value = entityToStringMap.remove(entityType);
        if (value != null) {
            List<EntityType<?>> entities = stringToEntitiesMap.get(value);
            if (entities != null) {
                entities.remove(entityType);
                if (entities.isEmpty()) {
                    stringToEntitiesMap.remove(value);
                }
            }
            setDirty();
            return true;
        }
        return false;
    }


    public boolean removeString(String value) {
        List<EntityType<?>> entities = stringToEntitiesMap.remove(value);
        if (entities != null) {
            for (EntityType<?> entityType : entities) {
                entityToStringMap.remove(entityType);
            }
            setDirty();
            return true;
        }
        return false;
    }


    public void clearMappings() {
        entityToStringMap.clear();
        stringToEntitiesMap.clear();
        setDirty();
    }

    public Map<EntityType<?>, String> getAllEntityMappings() {
        return new HashMap<>(entityToStringMap);
    }

    public Map<String, List<EntityType<?>>> getAllStringMappings() {
        Map<String, List<EntityType<?>>> copiedMap = new HashMap<>();
        for (Map.Entry<String, List<EntityType<?>>> entry : stringToEntitiesMap.entrySet()) {
            copiedMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copiedMap;
    }

    public void sendPlayerMappings(Player player) {
        if (entityToStringMap.isEmpty()) {
            player.sendSystemMessage(Component.literal("No entity-string mappings found.").withStyle(ChatFormatting.RED));
            return;
        }
        for (Map.Entry<String, List<EntityType<?>>> entry : stringToEntitiesMap.entrySet()) {
            String value = entry.getKey();
            List<String> entityNames = entry.getValue().stream().map(entityType -> ForgeRegistries.ENTITY_TYPES.getKey(entityType).toString()).collect(Collectors.toList());
            StringBuilder message = new StringBuilder();
            message.append("String '").append(value).append("' is associated with: ");
            message.append(String.join(", ", entityNames));
            player.sendSystemMessage(Component.literal(message.toString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag mappingList = new ListTag();
        for (Map.Entry<EntityType<?>, String> entry : entityToStringMap.entrySet()) {
            CompoundTag mappingTag = new CompoundTag();
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey());
            if (entityId != null) {
                mappingTag.putString("entityType", entityId.toString());
                mappingTag.putString("value", entry.getValue());
                mappingList.add(mappingTag);
            }
        }
        compoundTag.put("Mappings", mappingList);
        return compoundTag;
    }

    public static BeyonderEntityData load(CompoundTag compoundTag) {
        BeyonderEntityData data = new BeyonderEntityData();
        if (compoundTag.contains("Mappings")) {
            ListTag mappingList = compoundTag.getList("Mappings", Tag.TAG_COMPOUND);
            for (int i = 0; i < mappingList.size(); i++) {
                CompoundTag mappingTag = mappingList.getCompound(i);
                String entityTypeId = mappingTag.getString("entityType");
                String value = mappingTag.getString("value");
                ResourceLocation resourceLocation = new ResourceLocation(entityTypeId);
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
                if (entityType != null) {
                    data.entityToStringMap.put(entityType, value);
                    data.stringToEntitiesMap.computeIfAbsent(value, k -> new ArrayList<>()).add(entityType);
                }
            }
        }

        return data;
    }

    public static BeyonderEntityData create() {
        return new BeyonderEntityData();
    }

    public static void regenerateSpirituality(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().tickCount % 20 == 0) {
            LivingEntity living = event.getEntity();
            ServerLevel level = (ServerLevel) living.level();
            BeyonderEntityData mappingData = BeyonderEntityData.getInstance(level);
            String pathwayString = mappingData.getStringForEntity(living.getType());
            if (pathwayString != null) {
                BeyonderClass pathway = BeyonderUtil.getPathway(living);
                if (pathway != null) {
                    if (BeyonderUtil.getSpirituality(living) < BeyonderUtil.getMaxSpirituality(living)) {
                        int sequence = BeyonderUtil.getSequence(living);
                        BeyonderUtil.addSpirituality(living, pathway.spiritualityRegen().get(sequence) * 20);
                    }
                    CompoundTag persistentData = living.getPersistentData();
                    List<String> keysToDecrement = new ArrayList<>();
                    for (String key : persistentData.getAllKeys()) {
                        if (key.startsWith("abilityCooldownFor")) {
                            keysToDecrement.add(key);
                        }
                    }
                    for (String key : keysToDecrement) {
                        int currentCooldown = persistentData.getInt(key);
                        if (currentCooldown > 0) {
                            persistentData.putInt(key, Math.max(0, currentCooldown - 20));
                        }
                    }
                    if (living instanceof Mob mob && mob.tickCount % 60 == 0) {
                        selectAndUseAbility(mob);
                    }
                    int sequenceLevel = BeyonderUtil.getSequence(living);
                    pathway.tick(living, sequenceLevel);
                }
            }
        }
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity living = event.getEntity();
            if (!(living instanceof PlayerMobEntity) && living instanceof Mob mob) {
                ServerLevel level = (ServerLevel) living.level();
                BeyonderEntityData mappingData = BeyonderEntityData.getInstance(level);
                String pathwayString = mappingData.getStringForEntity(living.getType());
                if (pathwayString != null) {
                    BeyonderClass pathway = BeyonderUtil.getPathway(living);
                    if (pathway != null) {
                        if (mob.getTarget() == null && mob.getLastAttacker() != null) {
                            mob.setTarget(mob.getLastAttacker());
                        }
                    }
                }
            }
        }
    }
    private static void selectAndUseAbility(Mob mob) {
        List<Item> availableAbilities = getAbilities(mob);
        if (availableAbilities.isEmpty()) {
            return;
        }

        List<WeightedAbility> weightedAbilities = new ArrayList<>();
        int currentSpirituality = BeyonderUtil.getSpirituality(mob);
        for (Item item : availableAbilities) {
            if (item instanceof SimpleAbilityItem abilityItem) {
                String cooldownKey = "abilityCooldownFor" + abilityItem.getDescription().getString();
                int currentCooldown = mob.getPersistentData().getInt(cooldownKey);
                if (currentCooldown == 0 && currentSpirituality >= abilityItem.getRequiredSpirituality()) {
                    int priority = abilityItem.getPriority(mob, mob.getTarget());
                    if (priority > 0) {
                        weightedAbilities.add(new WeightedAbility(abilityItem, priority));
                    }
                }
            }
        }

        if (weightedAbilities.isEmpty()) {
            return;
        }

        SimpleAbilityItem selectedAbility = selectWeightedAbility(weightedAbilities);
        if (selectedAbility != null) {
            int totalPriority = 0;
            for (WeightedAbility ability : weightedAbilities) {
                totalPriority += ability.weight;
            }

            // Debug log message
            Level level = mob.level();
            if (level instanceof ServerLevel) {
                String entityName = mob.getName().getString();
                String abilityName = selectedAbility.getDescription().getString();
                int abilityPriority = 0;

                for (WeightedAbility ability : weightedAbilities) {
                    if (ability.abilityItem == selectedAbility) {
                        abilityPriority = ability.weight;
                        break;
                    }
                }
                LOTM.LOGGER.info("{} chose ability {} with a {}/{} probability",
                        entityName, abilityName, abilityPriority, totalPriority);
            }

            mob.setItemInHand(InteractionHand.MAIN_HAND, selectedAbility.getDefaultInstance());
            useAvailableAbilityAsMob(mob);
        }
    }

    private static SimpleAbilityItem selectWeightedAbility(List<WeightedAbility> weightedAbilities) {
        int totalWeight = 0;
        for (WeightedAbility ability : weightedAbilities) {
            totalWeight += ability.weight;
        }
        if (totalWeight <= 0) {
            return null;
        }

        Random random = new Random();
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (WeightedAbility ability : weightedAbilities) {
            currentWeight += ability.weight;
            if (randomValue < currentWeight) {
                return ability.abilityItem;
            }
        }
        return weightedAbilities.get(0).abilityItem;
    }

    private static class WeightedAbility {
        final SimpleAbilityItem abilityItem;
        final int weight;

        WeightedAbility(SimpleAbilityItem abilityItem, int weight) {
            this.abilityItem = abilityItem;
            this.weight = weight;
        }
    }
}