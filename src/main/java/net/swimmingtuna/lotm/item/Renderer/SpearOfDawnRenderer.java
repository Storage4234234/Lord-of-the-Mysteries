package net.swimmingtuna.lotm.item.Renderer;

import net.swimmingtuna.lotm.item.Model.SpearOfDawnModel;
import net.swimmingtuna.lotm.item.OtherItems.SpearOfDawn;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SpearOfDawnRenderer extends GeoItemRenderer<SpearOfDawn> {

    public SpearOfDawnRenderer() {
        super(new SpearOfDawnModel());
    }
}
