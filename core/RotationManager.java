package ai.venice.trestage.core;

import ai.venice.trestage.Trestage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;

public class RotationManager {

    private float lastYaw, lastPitch;

    public float gcd() {
        float f = Trestage.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float g = f * f * f * 1.2F;
        float gcd = g * 8.0F * 0.15F * 0.15F;
        return Math.max(gcd, 0.0005F);
    }

    public float[] rawTo(EntityLivingBase target) {
        double dx = target.posX - Trestage.mc.thePlayer.posX;
        double dy = (target.posY + target.getEyeHeight() * 0.9) - (Trestage.mc.thePlayer.posY + Trestage.mc.thePlayer.getEyeHeight());
        double dz = target.posZ - Trestage.mc.thePlayer.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{ yaw, pitch };
    }

    /** One smooth step toward the target. Returns true if the aim has converged. */
    public boolean step(EntityLivingBase target, float speed) {
        float[] want = rawTo(target);
        float curYaw = Trestage.mc.thePlayer.rotationYaw;
        float curPitch = Trestage.mc.thePlayer.rotationPitch;

        float dYaw = MathHelper.wrapAngleTo180_float(want[0] - curYaw);
        float dPitch = want[1] - curPitch;

        float gcd = gcd();
        float maxStep = Math.max(1.0F, speed);

        float sYaw = MathHelper.clamp_float(dYaw * 0.55F, -maxStep, maxStep);
        float sPitch = MathHelper.clamp_float(dPitch * 0.55F, -maxStep, maxStep);

        float nYaw = curYaw + sYaw;
        float nPitch = MathHelper.clamp_float(curPitch + sPitch, -90.0F, 90.0F);

        nYaw = Math.round(nYaw / gcd) * gcd;
        nPitch = Math.round(nPitch / gcd) * gcd;

        Trestage.mc.thePlayer.rotationYaw = nYaw;
        Trestage.mc.thePlayer.rotationPitch = nPitch;

        if (nYaw != lastYaw || nPitch != lastPitch) {
            Trestage.mc.getNetHandler().addToSendQueue(
                new C03PacketPlayer.C05PacketPlayerLook(nYaw, nPitch, Trestage.mc.thePlayer.onGround));
            lastYaw = nYaw;
            lastPitch = nPitch;
        }

        return Math.abs(MathHelper.wrapAngleTo180_float(want[0] - nYaw)) < 1.5F
            && Math.abs(want[1] - nPitch) < 1.5F;
    }
}
