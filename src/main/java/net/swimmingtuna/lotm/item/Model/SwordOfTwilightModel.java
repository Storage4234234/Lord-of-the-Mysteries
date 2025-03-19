package net.swimmingtuna.lotm.item.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfDawn;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfTwilight;
import software.bernie.geckolib.model.GeoModel;

public class SwordOfTwilightModel extends GeoModel<SwordOfTwilight> {

    public SwordOfTwilightModel() {

    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SwordOfTwilight swordOfTwilight) {
        System.out.println("Loading model for SwordOfTwilight");
        return new ResourceLocation(LOTM.MOD_ID, "geo/sword_of_twilight.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SwordOfTwilight swordOfTwilight) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/item/sword_of_twilight.png");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getAnimationResource(SwordOfTwilight swordOfTwilight) {
        return null;
    }
}
