package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.DivineHandLeftEntity;
import software.bernie.geckolib.model.GeoModel;

public class DivineHandLeftModel extends GeoModel<DivineHandLeftEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(DivineHandLeftEntity mercuryPortalEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/divine_hand_right.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(DivineHandLeftEntity mercuryPortalEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/divine_hand_right_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DivineHandLeftEntity mercuryPortalEntity) {
        return null;
    }
}
