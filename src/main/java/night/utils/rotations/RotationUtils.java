/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.rotations;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import night.utils.IMinecraft;

public class RotationUtils
implements IMinecraft {
    public static float[] getRotations(Entity entity) {
        return RotationUtils.getRotations(entity.getX(), entity.getY() + (double)entity.getEyeHeight(entity.getPose()) / 2.0, entity.getZ());
    }

    public static float[] getRotations(Vec3 vec3d) {
        return RotationUtils.getRotations(vec3d.x, vec3d.y, vec3d.z);
    }

    public static float[] getRotations(double x, double y, double z) {
        return RotationUtils.getRotations((Entity)RotationUtils.mc.player, x, y, z);
    }

    public static float[] getRotations(Entity entity, double x, double y, double z) {
        Vec3 vec3d = entity.position().add(0.0, (double)entity.getEyeHeight(entity.getPose()), 0.0);
        double deltaX = x - vec3d.x;
        double deltaY = (y - vec3d.y) * -1.0;
        double deltaZ = z - vec3d.z;
        double distance = Mth.sqrt((float)((float)(deltaX * deltaX + deltaZ * deltaZ)));
        float yaw = (float)Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0));
        float pitch = (float)Mth.clamp((double)Mth.wrapDegrees((double)Math.toDegrees(Math.atan2(deltaY, distance))), (double)-90.0, (double)90.0);
        return new float[]{yaw + ((float)Math.random() - 0.5f) * 4.0f, pitch + ((float)Math.random() - 0.5f) * 4.0f};
    }

    public static double getYRotToVec(Entity entity, Vec3 vec) {
        double dx = vec.x - entity.getX();
        double dz = vec.z - entity.getZ();
        return Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
    }

    public static double getXRotToVec(Entity entity, Vec3 vec) {
        double dx = vec.x - entity.getX();
        double dy = vec.y - (entity.getY() + (double)entity.getEyeHeight());
        double dz = vec.z - entity.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        return Mth.clamp((double)Mth.wrapDegrees((double)(-Math.toDegrees(Math.atan2(dy, dist)))), (double)-90.0, (double)90.0);
    }

    public static float[] getExactRotations(Entity entity, Vec3 target) {
        return new float[]{(float)RotationUtils.getYRotToVec(entity, target), (float)RotationUtils.getXRotToVec(entity, target)};
    }

    public static float getYRotToVec(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return (float)Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
    }

    public static float getXRotToVec(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        return (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    public static Vec3 getClosestPointToEye(Vec3 eyePos, AABB box) {
        double x = eyePos.x;
        double y = eyePos.y;
        double z = eyePos.z;
        double VEC = 0.0625;
        double EPS = 1.0E-9;
        if (eyePos.x < box.minX) {
            x = box.minX;
        } else if (eyePos.x > box.maxX) {
            x = box.maxX;
        }
        if (eyePos.y < box.minY) {
            y = box.minY;
        } else if (eyePos.y > box.maxY) {
            y = box.maxY;
        }
        if (eyePos.z < box.minZ) {
            z = box.minZ;
        } else if (eyePos.z > box.maxZ) {
            z = box.maxZ;
        }
        if (Math.abs(x - box.minX) < 1.0E-9) {
            x = Math.min(box.minX + 0.0625, box.maxX - 1.0E-9);
        } else if (Math.abs(x - box.maxX) < 1.0E-9) {
            x = Math.max(box.maxX - 0.0625, box.minX + 1.0E-9);
        }
        if (Math.abs(z - box.minZ) < 1.0E-9) {
            z = Math.min(box.minZ + 0.0625, box.maxZ - 1.0E-9);
        } else if (Math.abs(z - box.maxZ) < 1.0E-9) {
            z = Math.max(box.maxZ - 0.0625, box.minZ + 1.0E-9);
        }
        return new Vec3(x, y, z);
    }

    public static Vec3 getClampClosestPoint(Vec3 eyePos, AABB box) {
        double x = Mth.clamp((double)eyePos.x, (double)box.minX, (double)box.maxX);
        double y = Mth.clamp((double)eyePos.y, (double)box.minY, (double)box.maxY);
        double z = Mth.clamp((double)eyePos.z, (double)box.minZ, (double)box.maxZ);
        return new Vec3(x, y, z);
    }

    public static Vec3 getLookVectorFromYRotXRot(float yRot, float xRot) {
        float f = xRot * ((float)Math.PI / 180);
        float f1 = -yRot * ((float)Math.PI / 180);
        float f2 = Mth.cos((double)f1);
        float f3 = Mth.sin((double)f1);
        float f4 = Mth.cos((double)f);
        float f5 = Mth.sin((double)f);
        return new Vec3((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
    }

    public static EntityHitResult raycastTarget(Vec3 eyePos, Entity target, double reach, float yRot, float xRot) {
        Vec3 look = RotationUtils.getLookVectorFromYRotXRot(yRot, xRot);
        Vec3 reachEnd = eyePos.add(look.scale(reach));
        float pickRadius = target.getPickRadius();
        AABB targetBox = target.getBoundingBox().inflate((double)Math.max(pickRadius, 0.05f));
        if (targetBox.clip(eyePos, reachEnd).isPresent()) {
            return new EntityHitResult(target);
        }
        return null;
    }
}

