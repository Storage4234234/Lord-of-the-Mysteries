package net.swimmingtuna.lotm.entity.mob;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;

public class MobModelLayers {
    public static final ModelLayerLocation monster_layer = new ModelLayerLocation(
            new ResourceLocation(LOTM.MOD_ID, "monster_layer"), "main");
}
