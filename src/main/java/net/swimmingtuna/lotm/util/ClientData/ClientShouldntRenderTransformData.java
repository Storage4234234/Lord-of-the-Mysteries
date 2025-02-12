package net.swimmingtuna.lotm.util.ClientData;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;

public final class ClientShouldntRenderTransformData {

    private static final ClientShouldntRenderTransformData INSTANCE = new ClientShouldntRenderTransformData();
    private boolean isTransformed = false;
    private Mob cachedMob;

    public static ClientShouldntRenderTransformData getInstance() {
        return INSTANCE;
    }

    private ClientShouldntRenderTransformData() {
    }

    public void setCachedMob(Mob mob) {
        this.cachedMob = mob;
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public Mob getCachedMob() {
        return cachedMob;
    }

    public int transform(String mob, ServerPlayer player) {
        this.isTransformed = true;
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mob));
        Entity entity = entityType.create(player.level());
        if (entity instanceof Mob tmpMob){
            cachedMob = tmpMob;
            cachedMob.setNoAi(true);
        }
        return 1;
    }

    public int removeTransform(ServerPlayer player){
        if (!isTransformed){
            player.sendSystemMessage(Component.literal("You are currently not transformed"));
            return 0;
        }
        cachedMob.discard();
        cachedMob = null;
        this.isTransformed = false;
        return 1;
    }
}
