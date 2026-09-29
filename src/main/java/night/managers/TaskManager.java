/*
 * Decompiled with CFR 0.152.
 */
package night.managers;

import java.util.ArrayList;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.utils.IMinecraft;

public class TaskManager
implements IMinecraft {
    private final ArrayList<Runnable> tasks = new ArrayList();

    public TaskManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!this.tasks.isEmpty()) {
            this.tasks.getFirst().run();
            this.tasks.removeFirst();
        }
    }

    public void submit(Runnable runnable) {
        this.tasks.add(runnable);
    }
}

