package net.swimmingtuna.lotm.item.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfSilver;
import software.bernie.geckolib.model.GeoModel;

public class SwordOfSilverModel extends GeoModel<SwordOfSilver> {

    public SwordOfSilverModel() {

    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SwordOfSilver swordOfSilver) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/silver_sword.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SwordOfSilver swordOfSilver) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/silver_sword_entity.png");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getAnimationResource(SwordOfSilver swordOfSilver) {
        return null;
    }
}
