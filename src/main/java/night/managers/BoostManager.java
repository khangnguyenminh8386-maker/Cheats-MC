/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundExplodePacket
 */
package night.managers;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.utils.IMinecraft;
import night.utils.minecraft.MovementUtils;
import night.utils.system.Timer;

public class BoostManager
implements IMinecraft {
    public static BoostManager INSTANCE;
    private final Timer explosionTimer = new Timer();
    private final Timer longjumpTimer = new Timer();
    double boostExplosionSpeed;
    boolean canLongjump = false;

    public BoostManager() {
        INSTANCE = this;
        Night.EVENT_HANDLER.subscribe(this);
        this.boostExplosionSpeed = 0.0;
    }

    @SubscribeEvent(priority=100)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (BoostManager.mc.player == null || BoostManager.mc.level == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundExplodePacket) {
            ClientboundExplodePacket packet2 = (ClientboundExplodePacket)packet;
            if (BoostManager.mc.player.position().distanceTo(packet2.center()) <= 6.0) {
                packet2.playerKnockback().ifPresent(kb -> {
                    if (kb.x != 0.0 || kb.z != 0.0) {
                        this.explosionTimer.reset();
                        this.boostExplosionSpeed = Math.hypot(kb.x, kb.z);
                        this.canLongjump = true;
                        this.longjumpTimer.reset();
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (BoostManager.mc.player == null || BoostManager.mc.level == null) {
            return;
        }
        if (!MovementUtils.isMoving()) {
            this.boostExplosionSpeed = 0.0;
            this.canLongjump = false;
        }
        if (this.explosionTimer.timeElapsed() >= 400L) {
            this.boostExplosionSpeed = 0.0;
        }
        if (this.longjumpTimer.timeElapsed() >= 500L) {
            this.canLongjump = false;
        }
    }

    public double getBoostSpeed(boolean slow) {
        return this.boostExplosionSpeed;
    }

    public boolean canDoLongjump() {
        return this.canLongjump;
    }
}

