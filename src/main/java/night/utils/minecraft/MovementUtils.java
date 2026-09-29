/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector2d
 */
package night.utils.minecraft;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.impl.PlayerMoveEvent;
import night.mixins.accessors.Vec3dAccessor;
import night.modules.api.ISprintModule;
import night.utils.IMinecraft;
import night.utils.rotations.RotationUtils;
import org.joml.Vector2d;

public class MovementUtils
implements IMinecraft {
    public static double DEFAULT_SPEED = 0.2873;

    public static Vector2d forward(double speed) {
        ISprintModule sprint;
        float forward = MovementUtils.mc.player.input.getMoveVector().y;
        float sideways = MovementUtils.mc.player.input.getMoveVector().x;
        float yaw = MovementUtils.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (sprint = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && sprint.isGrimCompensating()) {
            yaw = sprint.getGrimYaw();
        }
        if (forward == 0.0f && sideways == 0.0f) {
            return new Vector2d(0.0, 0.0);
        }
        float magnitude = (float)Math.sqrt(forward * forward + sideways * sideways);
        if (magnitude > 1.0E-4f && Math.abs(magnitude - 1.0f) > 1.0E-4f) {
            forward /= magnitude;
            sideways /= magnitude;
        }
        double motionX = Math.cos(Math.toRadians(yaw + 90.0f));
        double motionZ = Math.sin(Math.toRadians(yaw + 90.0f));
        return new Vector2d((double)forward * speed * motionX + (double)sideways * speed * motionZ, (double)forward * speed * motionZ - (double)sideways * speed * motionX);
    }

    public static double[] straightForward(double speed) {
        ISprintModule sprint;
        float yaw = MovementUtils.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (sprint = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && sprint.isGrimCompensating()) {
            yaw = sprint.getGrimYaw();
        }
        return new double[]{speed * Math.cos(Math.toRadians(yaw + 90.0f)), speed * Math.sin(Math.toRadians(yaw + 90.0f))};
    }

    public static double getPotionSpeed(double speed) {
        if (MovementUtils.mc.player.hasEffect(MobEffects.SPEED)) {
            speed *= 1.0 + 0.2 * (double)(MovementUtils.mc.player.getEffect(MobEffects.SPEED).getAmplifier() + 1);
        }
        if (MovementUtils.mc.player.hasEffect(MobEffects.SLOWNESS)) {
            speed /= 1.0 + 0.2 * (double)(MovementUtils.mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1);
        }
        return speed;
    }

    public static double getPotionJump(double jump) {
        if (MovementUtils.mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
            jump += (double)((float)(MovementUtils.mc.player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f);
        }
        return jump;
    }

    public static boolean isMoving() {
        if (MovementUtils.mc.player == null) {
            return false;
        }
        return MovementUtils.mc.options.keyUp.isDown() || MovementUtils.mc.options.keyDown.isDown() || MovementUtils.mc.options.keyLeft.isDown() || MovementUtils.mc.options.keyRight.isDown() || MovementUtils.mc.player.input != null && (MovementUtils.mc.player.input.getMoveVector().x != 0.0f || MovementUtils.mc.player.input.getMoveVector().y != 0.0f) || MovementUtils.mc.player.xxa != 0.0f || MovementUtils.mc.player.zza != 0.0f;
    }

    public static void moveTowards(PlayerMoveEvent event, Vec3 vec3d, double speed) {
        double angle = Math.toRadians(RotationUtils.getRotations(vec3d)[0]);
        double x = -Math.sin(angle) * speed;
        double z = Math.cos(angle) * speed;
        double[] difference = new double[]{vec3d.x - MovementUtils.mc.player.getX(), vec3d.z - MovementUtils.mc.player.getZ()};
        event.setMovement(new Vec3(Math.abs(x) < Math.abs(difference[0]) ? x : difference[0], event.getMovement().y, event.getMovement().z));
        event.setMovement(new Vec3(event.getMovement().x, event.getMovement().y, Math.abs(z) < Math.abs(difference[1]) ? z : difference[1]));
        ((Vec3dAccessor)MovementUtils.mc.player.getDeltaMovement()).setX(0.0);
        ((Vec3dAccessor)MovementUtils.mc.player.getDeltaMovement()).setZ(0.0);
        event.setCancelled(true);
    }
}

