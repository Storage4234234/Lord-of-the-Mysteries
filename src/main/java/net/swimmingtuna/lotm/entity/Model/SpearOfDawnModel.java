package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.SpearOfDawnEntity;
import software.bernie.geckolib.model.GeoModel;

public class SpearOfDawnModel extends GeoModel<SpearOfDawnEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SpearOfDawnEntity spearOfDawn) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/spear_of_dawn.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SpearOfDawnEntity spearOfDawn) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/spear_of_dawn.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpearOfDawnEntity spearOfDawn) {
        return null;
    }
}
