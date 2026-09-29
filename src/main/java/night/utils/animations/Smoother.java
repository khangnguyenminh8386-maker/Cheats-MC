/*
 * Decompiled with CFR 0.152.
 */
package night.utils.animations;

public class Smoother {
    private double smoothedValue;

    public double smooth(double original, double smoother, double partialTicks) {
        double alpha = 1.0 - Math.exp(-smoother * partialTicks);
        this.smoothedValue += (original - this.smoothedValue) * alpha;
        return this.smoothedValue;
    }

    public void clear() {
        this.smoothedValue = 0.0;
    }

    public double getSmoothedValue() {
        return this.smoothedValue;
    }
}

