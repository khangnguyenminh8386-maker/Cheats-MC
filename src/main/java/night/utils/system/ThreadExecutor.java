/*
 * Decompiled with CFR 0.152.
 */
package night.utils.system;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadExecutor {
    private static final Map<Class<?>, Thread> RUNNING = new ConcurrentHashMap();

    public static void execute(Runnable runnable) {
        Thread running;
        Class<?> caller = runnable.getClass().getEnclosingClass();
        if (caller == null) {
            caller = runnable.getClass();
        }
        if ((running = RUNNING.get(caller)) != null && running.isAlive()) {
            return;
        }
        Class<?> key = caller;
        OneTimeThread thread = new OneTimeThread(() -> {
            try {
                runnable.run();
            }
            finally {
                RUNNING.remove(key);
            }
        });
        RUNNING.put(key, thread);
        thread.start();
    }

    private static class OneTimeThread
    extends Thread {
        private final Runnable runnable;

        public OneTimeThread(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }
    }
}

