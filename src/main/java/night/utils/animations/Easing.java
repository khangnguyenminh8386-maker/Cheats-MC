/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package night.utils.animations;

import net.minecraft.util.Mth;

public class Easing {
    public static float ease(float x, Method method) {
        return switch (method.ordinal()) {
            case 1 -> Easing.easeInSine(x);
            case 2 -> Easing.easeOutSine(x);
            case 3 -> Easing.easeInCubic(x);
            case 4 -> Easing.easeOutCubic(x);
            case 5 -> Easing.easeInQuad(x);
            case 6 -> Easing.easeOutQuad(x);
            case 7 -> Easing.easeInQuart(x);
            case 8 -> Easing.easeOutQuart(x);
            case 9 -> Easing.easeInOutSine(x);
            case 10 -> Easing.easeInOutQuad(x);
            case 11 -> Easing.easeInOutCubic(x);
            case 12 -> Easing.easeInOutQuart(x);
            case 13 -> Easing.easeInElastic(x);
            case 14 -> Easing.easeOutElastic(x);
            case 15 -> Easing.easeInOutElastic(x);
            default -> Easing.linear(x);
        };
    }

    public static float toDelta(long start, int length) {
        return Mth.clamp((float)((float)Easing.toDelta(start) / (float)length), (float)0.0f, (float)1.0f);
    }

    public static long toDelta(long start) {
        return System.currentTimeMillis() - start;
    }

    private static float linear(float x) {
        return x;
    }

    private static float easeInSine(float x) {
        return (float)(1.0 - Math.cos((double)x * Math.PI / 2.0));
    }

    private static float easeOutSine(float x) {
        return (float)Math.sin((double)x * Math.PI / 2.0);
    }

    private static float easeInCubic(float x) {
        return x * x * x;
    }

    private static float easeOutCubic(float x) {
        return (float)(1.0 - Math.pow(1.0f - x, 3.0));
    }

    private static float easeInQuad(float x) {
        return x * x;
    }

    private static float easeOutQuad(float x) {
        return 1.0f - (1.0f - x) * (1.0f - x);
    }

    private static float easeInQuart(float x) {
        return x * x * x * x;
    }

    private static float easeOutQuart(float x) {
        return (float)(1.0 - Math.pow(1.0f - x, 4.0));
    }

    private static float easeInOutSine(float x) {
        return (float)(-(Math.cos(Math.PI * (double)x) - 1.0) / 2.0);
    }

    private static float easeInOutQuad(float x) {
        return x < 0.5f ? 2.0f * x * x : (float)(1.0 - Math.pow(-2.0f * x + 2.0f, 2.0) / 2.0);
    }

    private static float easeInOutCubic(float x) {
        return x < 0.5f ? 4.0f * x * x * x : (float)(1.0 - Math.pow(-2.0f * x + 2.0f, 3.0) / 2.0);
    }

    private static float easeInOutQuart(float x) {
        return x < 0.5f ? 8.0f * x * x * x * x : (float)(1.0 - Math.pow(-2.0f * x + 2.0f, 4.0) / 2.0);
    }

    private static float easeInElastic(float x) {
        float c4 = 2.0943952f;
        return x == 0.0f ? 0.0f : (float)(x == 1.0f ? 1.0 : -Math.pow(2.0, 10.0f * x - 10.0f) * Math.sin(((double)(x * 10.0f) - 10.75) * (double)c4));
    }

    private static float easeOutElastic(float x) {
        float c4 = 2.0943952f;
        return x == 0.0f ? 0.0f : (float)(x == 1.0f ? 1.0 : Math.pow(2.0, -10.0f * x) * Math.sin(((double)(x * 10.0f) - 0.75) * (double)c4) + 1.0);
    }

    private static float easeInOutElastic(float x) {
        float c5 = 1.3962635f;
        return x == 0.0f ? 0.0f : (float)(x == 1.0f ? 1.0 : ((double)x < 0.5 ? -(Math.pow(2.0, 20.0f * x - 10.0f) * Math.sin(((double)(20.0f * x) - 11.125) * (double)c5)) / 2.0 : Math.pow(2.0, -20.0f * x + 10.0f) * Math.sin(((double)(20.0f * x) - 11.125) * (double)c5) / 2.0 + 1.0));
    }

    public static enum Method {
        LINEAR,
        EASE_IN_SINE,
        EASE_OUT_SINE,
        EASE_IN_CUBIC,
        EASE_OUT_CUBIC,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_QUART,
        EASE_OUT_QUART,
        EASE_IN_OUT_SINE,
        EASE_IN_OUT_QUAD,
        EASE_IN_OUT_CUBIC,
        EASE_IN_OUT_QUART,
        EASE_IN_ELASTIC,
        EASE_OUT_ELASTIC,
        EASE_IN_OUT_ELASTIC;

    }
}

