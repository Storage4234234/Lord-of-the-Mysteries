package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.SilverLightEntity;
import software.bernie.geckolib.model.GeoModel;

public class SilverLightModel extends GeoModel<SilverLightEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SilverLightEntity silverBeamEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/silver_sword.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SilverLightEntity silverBeamEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/silver_sword_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SilverLightEntity silverBeamEntity) {
        return null;
    }
}
