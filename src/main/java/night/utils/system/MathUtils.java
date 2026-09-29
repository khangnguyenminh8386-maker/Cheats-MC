/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package night.utils.system;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MathUtils {
    public static double random(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static double round(double value, int precision) {
        int scale = (int)Math.pow(10.0, precision);
        return (double)Math.round(value * (double)scale) / (double)scale;
    }

    public static Vec3 getVec(BlockPos pos) {
        return new Vec3((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
    }

    public static AABB getBox(Vec3 vec3d) {
        return new AABB(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1.0, vec3d.y + 1.0, vec3d.z + 1.0);
    }

    public static Vec3 scale(Vec3 vec, float x) {
        return vec.scale((double)x);
    }

    public static double interpolate(double value, double newValue, double interpolation) {
        return value + (newValue - value) * interpolation;
    }

    public static boolean inRange(double x, double value, double range) {
        return x > value - range && x < value + range;
    }

    public static float wrapAngle(float x) {
        if ((x %= 360.0f) < 0.0f) {
            x += 360.0f;
        }
        return x;
    }
}

