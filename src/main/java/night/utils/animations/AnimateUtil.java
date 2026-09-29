/*
 * Decompiled with CFR 0.152.
 */
package night.utils.animations;

public class AnimateUtil {
    public static double animate(double current, double endPoint, double speed) {
        if (speed >= 1.0) {
            return endPoint;
        }
        if (speed <= 0.0) {
            return current;
        }
        boolean shouldContinueAnimation = endPoint > current;
        double dif = Math.abs(endPoint - current);
        if (dif <= 0.001) {
            return endPoint;
        }
        double factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    public static float animate(float current, float endPoint, float speed) {
        if (speed >= 1.0f) {
            return endPoint;
        }
        if (speed <= 0.0f) {
            return current;
        }
        boolean shouldContinueAnimation = endPoint > current;
        float dif = Math.abs(endPoint - current);
        if (dif <= 0.001f) {
            return endPoint;
        }
        float factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }
}

