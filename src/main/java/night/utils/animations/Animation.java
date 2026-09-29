/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.Mth
 */
package night.utils.animations;

import lombok.Generated;
import net.minecraft.util.Mth;
import night.utils.animations.Easing;

public class Animation {
    private static final int MIN_DURATION_MS = 40;
    private float current;
    private float prev;
    private long startTime;
    private Easing.Method easing;
    private final int duration;
    private int activeDuration;

    public Animation(int duration, Easing.Method easing) {
        this(0.0f, 0.0f, duration, easing);
    }

    public Animation(float prev, float current, int duration, Easing.Method easing) {
        this.prev = prev;
        this.current = current;
        this.duration = duration;
        this.activeDuration = duration;
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
    }

    public float value() {
        if (this.activeDuration <= 0) {
            return this.current;
        }
        return Mth.lerp((float)Easing.ease(Easing.toDelta(this.startTime, this.activeDuration), this.easing), (float)this.prev, (float)this.current);
    }

    public boolean isFinished() {
        return this.activeDuration <= 0 || Easing.toDelta(this.startTime, this.activeDuration) >= 1.0f;
    }

    public float get() {
        return this.value();
    }

    public float get(float target) {
        float value = this.value();
        if (this.current != target) {
            float remaining = Math.abs(target - value);
            float previousLeg = Math.abs(this.current - this.prev);
            float reference = Math.max(previousLeg, remaining);
            int scaled = reference <= 1.0E-4f ? this.duration : Math.round((float)this.duration * Mth.clamp((float)(remaining / reference), (float)0.0f, (float)1.0f));
            this.prev = value;
            this.current = target;
            this.activeDuration = Math.max(40, scaled);
            this.startTime = System.currentTimeMillis();
        }
        return value;
    }

    public void setEasing(Easing.Method easing) {
        if (this.easing == easing) {
            return;
        }
        float value = this.value();
        int elapsed = (int)Math.min(Easing.toDelta(this.startTime), Integer.MAX_VALUE);
        int remaining = Math.max(0, this.activeDuration - elapsed);
        this.prev = value;
        this.activeDuration = remaining;
        this.startTime = System.currentTimeMillis();
        this.easing = easing;
    }

    @Generated
    public float getCurrent() {
        return this.current;
    }

    @Generated
    public float getPrev() {
        return this.prev;
    }

    @Generated
    public long getStartTime() {
        return this.startTime;
    }

    @Generated
    public Easing.Method getEasing() {
        return this.easing;
    }

    @Generated
    public int getDuration() {
        return this.duration;
    }

    @Generated
    public int getActiveDuration() {
        return this.activeDuration;
    }

    @Generated
    public void setCurrent(float current) {
        this.current = current;
    }

    @Generated
    public void setPrev(float prev) {
        this.prev = prev;
    }

    @Generated
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Generated
    public void setActiveDuration(int activeDuration) {
        this.activeDuration = activeDuration;
    }
}

