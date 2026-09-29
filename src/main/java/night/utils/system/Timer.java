/*
 * Decompiled with CFR 0.152.
 */
package night.utils.system;

public class Timer {
    private long startTime = System.currentTimeMillis();

    public boolean hasTimeElapsed(Number time) {
        return System.currentTimeMillis() - this.startTime >= time.longValue();
    }

    public long timeElapsed() {
        return System.currentTimeMillis() - this.startTime;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }
}

