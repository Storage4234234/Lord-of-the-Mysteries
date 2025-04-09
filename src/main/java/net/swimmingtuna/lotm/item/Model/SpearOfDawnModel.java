package net.swimmingtuna.lotm.item.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.item.OtherItems.SpearOfDawn;
import software.bernie.geckolib.model.GeoModel;

public class SpearOfDawnModel extends GeoModel<SpearOfDawn> {

    public SpearOfDawnModel() {

    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SpearOfDawn spearOfDawn) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/spear_of_dawn.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SpearOfDawn spearOfDawn) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/spear_of_dawn.png");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getAnimationResource(SpearOfDawn spearOfDawn) {
        return null;
    }
}
