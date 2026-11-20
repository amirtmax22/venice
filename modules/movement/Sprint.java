package ai.venice.trestage.modules.movement;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (Trestage.mc.thePlayer == null) return;
        if (Trestage.mc.thePlayer.moveForward > 0 && !Trestage.mc.thePlayer.isCollidedHorizontally) {
            Trestage.mc.thePlayer.setSprinting(true);
        }
    }
}
