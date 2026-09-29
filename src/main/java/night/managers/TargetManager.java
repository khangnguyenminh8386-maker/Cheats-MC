/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package night.managers;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.TargetDeathEvent;
import night.events.impl.TickEvent;
import night.modules.impl.combat.AutoCrystalModule;
import night.modules.impl.combat.KillAuraModule;
import night.utils.IMinecraft;

public class TargetManager
implements IMinecraft {
    private final ArrayList<Target> targets = new ArrayList();

    public TargetManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        this.targets.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (TargetManager.mc.player == null || TargetManager.mc.level == null) {
            return;
        }
        Player caTarget = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class).getTarget();
        Entity kaTarget = Night.MODULE_MANAGER.getModule(KillAuraModule.class).target;
        ArrayList<Target> arrayList = this.targets;
        synchronized (arrayList) {
            this.targets.removeIf(t -> System.currentTimeMillis() - t.time > 15000L);
            if (caTarget != null) {
                this.targets.add(new Target(this, caTarget));
            }
            if (kaTarget instanceof Player) {
                this.targets.add(new Target(this, (Player)kaTarget));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (TargetManager.mc.player == null || TargetManager.mc.level == null || !this.isTarget(event.getPlayer())) {
            return;
        }
        ArrayList<Target> arrayList = this.targets;
        synchronized (arrayList) {
            Night.EVENT_HANDLER.post(new TargetDeathEvent(event.getPlayer()));
            this.targets.remove(this.getTarget(event.getPlayer()));
        }
    }

    private Target getTarget(Player player) {
        for (Target target : this.targets) {
            if (target.player != player) continue;
            return target;
        }
        return null;
    }

    public boolean isTarget(Player player) {
        for (Target target : this.targets) {
            if (target.player != player) continue;
            return true;
        }
        return false;
    }

    private class Target {
        private final Player player;
        private final long time;

        public Target(TargetManager targetManager, Player player) {
            Objects.requireNonNull(targetManager);
            this.player = player;
            this.time = System.currentTimeMillis();
        }
    }
}

