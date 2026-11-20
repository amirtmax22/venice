package ai.venice.trestage.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    public static void rect(float x, float y, float w, float h, int color) {
        Gui.drawRect((int) x, (int) y, (int) (x + w), (int) (y + h), color);
    }

    public static void gradient(float x, float y, float w, float h, int top, int bottom) {
        float a1 = (top >> 24 & 255) / 255f, r1 = (top >> 16 & 255) / 255f, g1 = (top >> 8 & 255) / 255f, b1 = (top & 255) / 255f;
        float a2 = (bottom >> 24 & 255) / 255f, r2 = (bottom >> 16 & 255) / 255f, g2 = (bottom >> 8 & 255) / 255f, b2 = (bottom & 255) / 255f;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.startDrawingQuads();
        wr.setColorRGBA_F(r1, g1, b1, a1); wr.addVertex(x, y + h, 0);
        wr.addVertex(x + w, y + h, 0);
        wr.setColorRGBA_F(r2, g2, b2, a2); wr.addVertex(x + w, y, 0);
        wr.addVertex(x, y, 0);
        t.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void arc(float cx, float cy, float r, int color, int from, int to) {
        float a = (color >> 24 & 255) / 255f, rr = (color >> 16 & 255) / 255f,
              g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(rr, g, b, a);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.startDrawing(GL11.GL_TRIANGLE_FAN);
        wr.addVertex(cx, cy, 0);
        for (int i = from; i <= to; i += 6) {
            double rad = Math.toRadians(i);
            wr.addVertex(cx + Math.sin(rad) * r, cy + Math.cos(rad) * r, 0);
        }
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void rounded(float x, float y, float w, float h, float r, int color) {
        rect(x + r, y, w - 2 * r, h, color);
        rect(x, y + r, w, h - 2 * r, color);
        arc(x + r, y + r, r, color, 180, 270);
        arc(x + w - r, y + r, r, color, 270, 360);
        arc(x + w - r, y + h - r, r, color, 0, 90);
        arc(x + r, y + h - r, r, color, 90, 180);
    }

    public static void outline(float x, float y, float w, float h, int color) {
        rect(x, y, w, 1, color);
        rect(x, y + h - 1, w, 1, color);
        rect(x, y, 1, h, color);
        rect(x + w - 1, y, 1, h, color);
    }

    public static void text(String s, float x, float y, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s, x, y, color);
    }

    public static int width(String s) {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(s);
    }
}
