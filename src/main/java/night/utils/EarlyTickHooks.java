/*
 * Decompiled with CFR 0.152.
 */
package night.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EarlyTickHooks {
    private static final List<Runnable> callbacks = new CopyOnWriteArrayList<Runnable>();

    private EarlyTickHooks() {
    }

    public static void register(Runnable callback) {
        callbacks.add(callback);
    }

    public static void unregister(Runnable callback) {
        callbacks.remove(callback);
    }

    public static void dispatch() {
        for (Runnable callback : callbacks) {
            callback.run();
        }
    }
}

