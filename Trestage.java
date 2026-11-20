package ai.venice.trestage;

import ai.venice.trestage.core.Config;
import ai.venice.trestage.core.ModuleManager;
import ai.venice.trestage.core.RotationManager;
import ai.venice.trestage.gui.ClickGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "trestage", name = "Trestage", version = "1.0.0",
     acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class Trestage {

    public static final String NAME = "Trestage";
    public static final String VERSION = "1.0.0";
    public static final String PREFIX = "\u00A75[Trestage] \u00A7f";

    public static Minecraft mc = Minecraft.getMinecraft();
    public static ModuleManager modules;
    public static RotationManager rotations;
    public static ClickGUI gui;

    private static KeyBinding openGui;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        modules = new ModuleManager();
        rotations = new RotationManager();
        gui = new ClickGUI();
        Config.load();

        openGui = new KeyBinding("Open Trestage", Keyboard.KEY_RSHIFT, "Trestage");
        ClientRegistry.registerKeyBinding(openGui);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (openGui != null && openGui.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(gui);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        modules.onTick();
    }

    public static void chat(String msg) {
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(
            new net.minecraft.util.ChatComponentText(PREFIX + msg));
    }
}
