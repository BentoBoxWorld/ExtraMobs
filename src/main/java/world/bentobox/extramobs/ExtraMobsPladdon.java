package world.bentobox.extramobs;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class ExtraMobsPladdon extends Pladdon {
    private Addon addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new ExtraMobsAddon();
        }
        return addon;
    }
}
