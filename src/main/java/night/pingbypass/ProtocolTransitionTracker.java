/*
 * Decompiled with CFR 0.152.
 */
package night.pingbypass;

public class ProtocolTransitionTracker {
    private static volatile long lastTransitionAt = 0L;

    public static void mark() {
        lastTransitionAt = System.currentTimeMillis();
    }

    public static long lastTransitionAt() {
        return lastTransitionAt;
    }
}

