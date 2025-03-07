package net.swimmingtuna.lotm.util.ScribeRecording;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;

public class ScribeAbilityStorageProvider implements ICapabilitySerializable<CompoundTag> {
    private final SimpleAbilityItem.scribeAbilitiesStorage instance = new ScribeAbilityStorage();
    private final LazyOptional<SimpleAbilityItem.scribeAbilitiesStorage> holder = LazyOptional.of(() -> instance);


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityScribeAbilities.SCRIBE_CAPABILITY ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<Item, Integer> entry : instance.getScribedAbilities().entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("scribeAbility", BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
            entryTag.putInt("scribeCount", entry.getValue());
            list.add(entryTag);
        }
        tag.put("scribeAbilities", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.getScribedAbilities().clear();
        ListTag list = nbt.getList("scribeAbilities", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            String itemId = entryTag.getString("scribeAbility");
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
            int count = entryTag.getInt("scribeCount");
            if (item != null) {
                instance.getScribedAbilities().put(item, count);
            }
        }
    }
}