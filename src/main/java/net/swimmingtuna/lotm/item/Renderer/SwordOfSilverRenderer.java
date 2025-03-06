package net.swimmingtuna.lotm.item.Renderer;


import net.swimmingtuna.lotm.item.Model.SwordOfSilverModel;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfSilver;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SwordOfSilverRenderer extends GeoItemRenderer<SwordOfSilver> {

    public SwordOfSilverRenderer() {
        super(new SwordOfSilverModel());
    }
}
