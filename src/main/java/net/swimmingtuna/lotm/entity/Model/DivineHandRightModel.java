package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.DivineHandRightEntity;
import software.bernie.geckolib.model.GeoModel;

public class DivineHandRightModel extends GeoModel<DivineHandRightEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(DivineHandRightEntity mercuryPortalEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/divine_hand_right.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(DivineHandRightEntity mercuryPortalEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/divine_hand_right_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DivineHandRightEntity mercuryPortalEntity) {
        return null;
    }
}
