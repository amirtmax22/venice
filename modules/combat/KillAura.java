package ai.venice.trestage.modules.combat;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import ai.venice.trestage.util.MathUtil;
import ai.venice.trestage.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {

    private final Setting range    = add(new Setting(this, "Range", 3.1, 2.5, 6.0, 0.05));
    private final Setting cpsMin   = add(new Setting(this, "CPS Min", 9.0, 1.0, 20.0, 0.5));
    private final Setting cpsMax   = add(new Setting(this, "CPS Max", 13.0, 1.0, 20.0, 0.5));
    private final Setting cpsJitter= add(new Setting(this, "CPS Jitter", 1.6, 0.0, 4.0, 0.1));
    private final Setting aimAngle = add(new Setting(this, "Aim Angle", 30.0, 5.0, 90.0, 1.0));
    private final Setting fov      = add(new Setting(this, "FOV", 120.0, 30.0, 360.0, 5.0));
    private final Setting speed    = add(new Setting(this, "Rot Speed", 34.0, 5.0, 180.0, 1.0));
    private final Setting sort     = add(new Setting(this, "Sort", "Angle", "Angle", "Distance", "Health"));
    private final Setting multi    = add(new Setting(this, "Multi Target", false));
    private final Setting raycast  = add(new Setting(this, "Raycast", true));
    private final Setting miss     = add(new Setting(this, "Miss Chance", 7.0, 0.0, 30.0, 1.0));
    private final Setting players  = add(new Setting(this, "Players", true));
    private final Setting mobs     = add(new Setting(this, "Mobs", false));
    private final Setting animals  = add(new Setting(this, "Animals", false));
    private final Setting invisible= add(new Setting(this, "Invisible", true));
    private final Setting bots     = add(new Setting(this, "Ignore Bots", true));
    private final Setting walls    = add(new Setting(this, "Through Walls", false));
    private final Setting autoBlock= add(new Setting(this, "Auto Block", true));
    private final Setting keepSprint=add(new Setting(this, "Keep Sprint", true));
    private final Setting swing    = add(new Setting(this, "Swing", true));
    private final Setting onlyKey  = add(new Setting(this, "Only On Attack", false));
    private final Setting switchCd = add(new Setting(this, "Switch Cooldown", 120.0, 0.0, 600.0, 10.0));

    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    private EntityLivingBase target;
    private long nextDelay = 100L;

    public KillAura() {
        super("KillAura", Category.COMBAT);
        attackTimer.reset();
        switchTimer.reset();
    }

    @Override
    public void onEnable() {
        target = null;
        nextDelay = 100L;
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onTick() {
        if (Trestage.mc.currentScreen != null) return;
        if (onlyKey.bool && !Trestage.mc.gameSettings.keyBindAttack.isKeyDown()) return;

        EntityLivingBase candidate = findTarget();

        if (target == null || !MathUtil.isValid(target, range.num) || angleDiffers(candidate)) {
            if (switchTimer.passed((long) switchCd.num)) {
                target = candidate;
                switchTimer.reset();
            }
        }
        if (target == null) return;

        boolean aimed = Trestage.rotations.step(target, (float) speed.num);
        if (!aimed) return;
        if (MathUtil.angleTo(target) > aimAngle.num) return;

        if (!attackTimer.passed(nextDelay)) return;
        if (Math.random() * 100.0 < miss.num) {
            attackTimer.reset();
            nextDelay = (long) MathUtil.rand(1000.0 / cpsMax.num, 1000.0 / cpsMin.num);
            return;
        }

        attack(target);
    }

    private boolean angleDiffers(EntityLivingBase candidate) {
        return candidate != null && candidate != target;
    }

    private void attack(EntityLivingBase e) {
        if (Trestage.mc.playerController == null) return;

        if (autoBlock.bool) {
            if (Trestage.mc.thePlayer.getHeldItem() != null
                && Trestage.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                Trestage.mc.thePlayer.setItemInUse(Trestage.mc.thePlayer.getHeldItem(), 72000);
            }
        }

        Trestage.mc.playerController.attackEntity(Trestage.mc.thePlayer, e);
        if (swing.bool) Trestage.mc.thePlayer.swingItem();
        Trestage.mc.thePlayer.setSprinting(keepSprint.bool);

        double base = 1000.0 / MathUtil.rand(cpsMin.num, cpsMax.num);
        double jitter = MathUtil.rand(-cpsJitter.num, cpsJitter.num);
        nextDelay = (long) Math.max(45.0, base + jitter);
        attackTimer.reset();
    }

    private EntityLivingBase findTarget() {
        Vec3 eyes = new Vec3(Trestage.mc.thePlayer.posX,
                             Trestage.mc.thePlayer.posY + Trestage.mc.thePlayer.getEyeHeight(),
                             Trestage.mc.thePlayer.posZ);
        AxisAlignedBB box = Trestage.mc.thePlayer.getEntityBoundingBox().expand(range.num, range.num, range.num);
        List<EntityLivingBase> found = new ArrayList<>();

        for (Object o : Trestage.mc.theWorld.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            EntityLivingBase e = (EntityLivingBase) o;
            if (!MathUtil.isValid(e, range.num)) continue;
            if (!invisible.bool && e.isInvisible()) continue;
            if (e instanceof EntityArmorStand) continue;
            if (e instanceof EntityPlayer) {
                if (!players.bool) continue;
                if (bots.bool && isBot(e)) continue;
            } else if (e instanceof IMob) {
                if (!mobs.bool) continue;
            } else if (e instanceof EntityAnimal) {
                if (!animals.bool) continue;
            } else {
                continue;
            }
            if (MathUtil.angleTo(e) > fov.num / 2.0F) continue;
            if (raycast.bool && !walls.bool && !canSee(eyes, e)) continue;
            found.add(e);
        }

        if (found.isEmpty()) return null;
        if (sort.mode.equals("Distance")) found.sort(Comparator.comparingDouble(
            e -> e.getDistanceToEntity(Trestage.mc.thePlayer)));
        else if (sort.mode.equals("Health")) found.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
        else found.sort(Comparator.comparingDouble(MathUtil::angleTo));

        if (multi.bool) return found.get(0);
        return found.get(0);
    }

    private boolean canSee(Vec3 eyes, Entity e) {
        Vec3 point = new Vec3(e.posX, e.posY + e.getEyeHeight() * 0.85, e.posZ);
        MovingObjectPosition hit = Trestage.mc.theWorld.rayTraceBlocks(eyes, point);
        return hit == null || hit.hitVec.distanceTo(eyes) >= eyes.distanceTo(point) - 0.05;
    }

    private boolean isBot(EntityLivingBase e) {
        String n = e.getName();
        return n.startsWith("[NPC]") || n.equals("Citizen") || n.matches(".*\\d{4,}.*");
    }
}
