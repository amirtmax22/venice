package ai.venice.trestage.modules.player;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {

    private final Setting horizontal = add(new Setting(this, "Horizontal", 0.0, 0.0, 100.0, 1.0));

    public Velocity() { super("Velocity", Category.PLAYER); }

    @Override
    public void onEnable() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        if (e.entity != Trestage.mc.thePlayer || Trestage.mc.thePlayer == null) return;
        double f = 1.0 - horizontal.num / 100.0;
        Trestage.mc.thePlayer.motionX *= f;
        Trestage.mc.thePlayer.motionZ *= f;
    }
}
