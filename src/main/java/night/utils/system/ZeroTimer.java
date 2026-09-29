/*
 * Decompiled with CFR 0.152.
 */
package night.utils.system;

public class ZeroTimer {
    private long startTime = System.currentTimeMillis();

    public boolean hasTimeElapsed(long time) {
        if (this.startTime == 0L) {
            return true;
        }
        return System.currentTimeMillis() - this.startTime >= time;
    }

    public void zero() {
        this.startTime = 0L;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }
}

