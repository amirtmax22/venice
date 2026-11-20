package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {

    private final Setting range = add(new Setting(this, "Range", 40.0, 8.0, 128.0, 4.0));
    private final Setting playersOnly = add(new Setting(this, "Players Only", true));

    public ESP() { super("ESP", Category.RENDER); }

    @Override
    public void onEnable() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this); }

    @Override
    public void onDisable() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this); }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (Trestage.mc.theWorld == null) return;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(1.6F);

        for (Object o : Trestage.mc.theWorld.loadedEntityList) {
            Entity ent = (Entity) o;
            if (!(ent instanceof EntityLivingBase) || ent == Trestage.mc.thePlayer) continue;
            if (playersOnly.bool && !(ent instanceof EntityPlayer)) continue;
            if (ent.getDistanceToEntity(Trestage.mc.thePlayer) > range.num) continue;

            AxisAlignedBB bb = ent.getEntityBoundingBox();
            double x = ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * e.partialTicks;
            double y = ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * e.partialTicks;
            double z = ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * e.partialTicks;

            GlStateManager.pushMatrix();
            GlStateManager.translate(-x, -y, -z);
            RenderGlobal.drawSelectionBoundingBox(bb.expand(0.05, 0.05, 0.05));
            GL11.glColor4f(0.48F, 0.36F, 1.0F, 0.9F);
            GlStateManager.popMatrix();
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
