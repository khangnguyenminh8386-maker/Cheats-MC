/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket
 *  net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket
 */
package night.modules.impl.miscellaneous;

import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;
import night.utils.chat.ChatUtils;
import night.utils.system.Timer;
import night.utils.system.ZeroTimer;

@RegisterModule(name="FastLatency", description="Makes ping resolving much faster.", category=Module.Category.MISCELLANEOUS)
public class FastLatencyModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "The amount of milliseconds that have to be waited for before resolving your ping again.", 100L, 0, 1000L);
    public BooleanSetting spikeNotifier = new BooleanSetting("SpikeNotifier", "Notifies you in chat whenever your ping spikes.", false);
    public NumberSetting threshold = new NumberSetting("Threshold", "The amount of milliseconds that your ping has to increase for before notifying you.", new BooleanSetting.Visibility(this.spikeNotifier, true), (Number)30, (Number)0, (Number)1000);
    private final Timer timer = new Timer();
    private final ZeroTimer receivedTimer = new ZeroTimer();
    private long time;
    private int latency;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (FastLatencyModule.mc.player == null) {
            return;
        }
        if (this.receivedTimer.hasTimeElapsed(1000L) && this.timer.hasTimeElapsed(this.delay.getValue().longValue())) {
            mc.getConnection().send((Packet)new ServerboundCommandSuggestionPacket(1000, "/w "));
            this.time = System.currentTimeMillis();
            this.receivedTimer.reset();
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        ClientboundCommandSuggestionsPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ClientboundCommandSuggestionsPacket && (packet = (ClientboundCommandSuggestionsPacket)packet2).id() == 1000) {
            int ping = (int)(System.currentTimeMillis() - this.time);
            if (this.spikeNotifier.getValue() && ping - this.latency > this.threshold.getValue().intValue()) {
                Night.CHAT_MANAGER.message("Your ping has spiked to " + String.valueOf(ChatUtils.getPrimary()) + ping + "ms" + String.valueOf(ChatUtils.getSecondary()) + " from " + String.valueOf(ChatUtils.getPrimary()) + this.latency + "ms" + String.valueOf(ChatUtils.getSecondary()) + "!", "module-" + this.getName().toLowerCase() + "-spike");
            }
            this.latency = ping;
            this.receivedTimer.zero();
        }
    }

    @Override
    public String getMetaData() {
        return this.latency + "ms";
    }

    @Generated
    public int getLatency() {
        return this.latency;
    }
}

