package ai.venice.trestage.gui;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Config;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import ai.venice.trestage.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends GuiScreen {

    private static final int BG        = 0xF00E0A18;
    private static final int PANEL     = 0xFF16112B;
    private static final int HEADER_A  = 0xFF7A5CFF;
    private static final int HEADER_B  = 0xFF3B2A8C;
    private static final int TEXT      = 0xFFEDE9FF;
    private static final int TEXT_DIM  = 0xFF8B82B8;
    private static final int ACCENT    = 0xFF7A5CFF;
    private static final int ROW       = 0xFF1E1838;
    private static final int ROW_HOVER = 0xFF2A2350;

    private final Module.Category[] cats = Module.Category.values();
    private final List<Module> open = new ArrayList<>();
    private Setting dragging;
    private int dragX;

    private int px, py;

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = 430;
        int h = 300;
        px = (sr.getScaledWidth() - w) / 2;
        py = (sr.getScaledHeight() - h) / 2;
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground();

        int w = 430, h = 300;
        RenderUtil.rounded(px - 2, py - 2, w + 4, h + 4, 10, 0x60000000);
        RenderUtil.rounded(px, py, w, h, 8, BG);

        RenderUtil.gradient(px, py, w, 30, HEADER_A, HEADER_B);
        RenderUtil.text("\u00A7lTRESTAGE", px + 14, py + 11, 0xFFFFFFFF);
        RenderUtil.text("v" + Trestage.VERSION, px + w - 14 - RenderUtil.width("v" + Trestage.VERSION), py + 11, 0xFFE4DCFF);
        RenderUtil.text("right shift to close", px + 14, py + 34, TEXT_DIM);

        int colW = (w - 24) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            Module.Category cat = cats[i];
            int cx = px + 12 + i * colW;
            int cy = py + 50;
            RenderUtil.rounded(cx, cy, colW - 6, 12, 4, 0x40FFFFFF);
            RenderUtil.text(cats[i].name(), cx + 6, cy + 2, TEXT);

            int ry = cy + 18;
            for (Module m : Trestage.modules.in(cat)) {
                boolean hover = in(mouseX, mouseY, cx, ry, colW - 6, 14);
                RenderUtil.rounded(cx, ry, colW - 6, 14, 4, m.enabled ? ACCENT : (hover ? ROW_HOVER : ROW));
                RenderUtil.text(m.name, cx + 6, ry + 3, m.enabled ? 0xFFFFFFFF : TEXT);

                if (m.expanded) {
                    int sy = ry + 18;
                    for (Setting s : m.settings) {
                        if (!s.isVisible()) continue;
                        if (s.type == Setting.Type.BOOLEAN) {
                            RenderUtil.text(s.name, cx + 6, sy, TEXT_DIM);
                            int bx = cx + colW - 18;
                            RenderUtil.rounded(bx, sy - 1, 10, 10, 3, s.bool ? ACCENT : ROW_HOVER);
                            sy += 13;
                        } else if (s.type == Setting.Type.MODE) {
                            RenderUtil.text(s.name + ": " + s.mode, cx + 6, sy, TEXT_DIM);
                            sy += 12;
                        } else {
                            RenderUtil.text(s.name + " " + s.display(), cx + 6, sy, TEXT_DIM);
                            RenderUtil.rect(cx + 6, sy + 10, colW - 18, 2, 0xFF2A2350);
                            float pct = (float) ((s.num - s.min) / (s.max - s.min));
                            RenderUtil.rect(cx + 6, sy + 10, (colW - 18) * pct, 2, ACCENT);
                            sy += 20;
                        }
                    }
                    ry = sy;
                }
                ry += 16;
            }
        }

        RenderUtil.rounded(px, py + h - 18, w, 18, 8, 0x40FFFFFF);
        RenderUtil.text("KillAura tuned for bypass  |  range 3.0-3.3  |  rot speed 25-45", px + 12, py + h - 13, TEXT_DIM);

        super.drawScreen(mouseX, mouseY, partial);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int w = 430;
        int colW = (w - 24) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            int cx = px + 12 + i * colW;
            int cy = py + 50;
            int ry = cy + 18;
            for (Module m : Trestage.modules.in(cats[i])) {
                if (in(mouseX, mouseY, cx, ry, colW - 6, 14)) {
                    if (button == 0) {
                        m.toggle();
                        Config.save();
                    } else if (button == 1) {
                        m.expanded = !m.expanded;
                        if (m.expanded) open.add(m); else open.remove(m);
                    }
                    return;
                }
                if (m.expanded) {
                    int sy = ry + 18;
                    for (Setting s : m.settings) {
                        if (!s.isVisible()) continue;
                        if (s.type == Setting.Type.BOOLEAN) {
                            if (in(mouseX, mouseY, cx + 6, sy - 2, colW - 12, 12)) {
                                s.toggle();
                                Config.save();
                                return;
                            }
                            sy += 13;
                        } else if (s.type == Setting.Type.MODE) {
                            if (in(mouseX, mouseY, cx + 6, sy - 2, colW - 12, 12)) {
                                s.cycle();
                                Config.save();
                                return;
                            }
                            sy += 12;
                        } else {
                            if (in(mouseX, mouseY, cx + 6, sy + 8, colW - 18, 6)) {
                                dragging = s;
                                dragX = cx + 6;
                                s.drag(mouseX, dragX, colW - 18);
                                return;
                            }
                            sy += 20;
                        }
                    }
                    ry = sy;
                }
                ry += 16;
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long time) {
        if (dragging != null) {
            int colW = (430 - 24) / cats.length;
            dragging.drag(mouseX, dragX, colW - 18);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging != null) {
            dragging = null;
            Config.save();
        }
    }

    @Override
    public void handleMouseInput() {
        int d = Mouse.getEventDWheel();
        if (d != 0) {
            // simple global scroll offset is unnecessary at this panel size
        }
        try { super.handleMouseInput(); } catch (Exception ignored) {}
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_RSHIFT) {
            mc.displayGuiScreen(null);
            Config.save();
        }
    }

    private boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
