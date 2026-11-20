package ai.venice.trestage.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class MathUtil {

    public static float angleTo(Entity target) {
        double dx = target.posX - net.minecraft.client.Minecraft.getMinecraft().thePlayer.posX;
        double dz = target.posZ - net.minecraft.client.Minecraft.getMinecraft().thePlayer.posZ;
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        return Math.abs(MathHelper.wrapAngleTo180_float(
            yaw - net.minecraft.client.Minecraft.getMinecraft().thePlayer.rotationYaw));
    }

    public static double rand(double min, double max) {
        return min + Math.random() * (max - min);
    }

    public static boolean isValid(EntityLivingBase e, double range) {
        return e != null && e != net.minecraft.client.Minecraft.getMinecraft().thePlayer
            && e.isEntityAlive() && e.getDistanceToEntity(net.minecraft.client.Minecraft.getMinecraft().thePlayer) <= range;
    }
}
