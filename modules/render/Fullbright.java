package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;

public class Fullbright extends Module {

    private float old;

    public Fullbright() { super("Fullbright", Category.RENDER); }

    @Override
    public void onEnable() {
        old = Trestage.mc.gameSettings.gammaSetting;
        Trestage.mc.gameSettings.gammaSetting = 10.0F;
    }

    @Override
    public void onDisable() {
        Trestage.mc.gameSettings.gammaSetting = old;
    }
}
