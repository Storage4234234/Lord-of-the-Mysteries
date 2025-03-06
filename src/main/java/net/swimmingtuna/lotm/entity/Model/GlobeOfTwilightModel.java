package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.DivineHandLeftEntity;
import net.swimmingtuna.lotm.entity.GlobeOfTwilightEntity;
import software.bernie.geckolib.model.GeoModel;

public class GlobeOfTwilightModel extends GeoModel<GlobeOfTwilightEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(GlobeOfTwilightEntity globeOfTwilightEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/globe_of_twilight.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(GlobeOfTwilightEntity globeOfTwilightEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/globe_of_twilight_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GlobeOfTwilightEntity globeOfTwilightEntity) {
        return null;
    }
}
