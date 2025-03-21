package net.swimmingtuna.lotm.item.Renderer;

import net.swimmingtuna.lotm.item.Model.SwordOfTwilightModel;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfTwilight;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SwordOfTwilightRenderer extends GeoItemRenderer<SwordOfTwilight> {

    public SwordOfTwilightRenderer() {
        super(new SwordOfTwilightModel());
    }
}
