package net.swimmingtuna.lotm.item.Renderer;

import net.swimmingtuna.lotm.item.Model.SwordOfDawnModel;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfDawn;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SwordOfDawnRenderer extends GeoItemRenderer<SwordOfDawn> {

    public SwordOfDawnRenderer() {
        super(new SwordOfDawnModel());
    }
}
