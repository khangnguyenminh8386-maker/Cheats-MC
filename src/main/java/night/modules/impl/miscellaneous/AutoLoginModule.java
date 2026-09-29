/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSystemChatPacket
 */
package night.modules.impl.miscellaneous;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.StringSetting;
import night.utils.system.Timer;

@RegisterModule(name="AutoLogin", description="Automatically logs in on cracked servers.", category=Module.Category.MISCELLANEOUS)
public class AutoLoginModule
extends Module {
    public StringSetting password = new StringSetting("Password", "The password to use when logging in.", "password");
    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (this.getNull() || !this.timer.hasTimeElapsed(10000)) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket packet2 = (ClientboundSystemChatPacket)packet;
            String s = packet2.content().getString().toLowerCase();
            if (s.contains("/register")) {
                mc.getConnection().sendCommand("register " + this.password.getValue() + " " + this.password.getValue());
                Night.CHAT_MANAGER.tagged("Registered successfully.", this.getName());
                this.timer.reset();
            } else if (s.contains("/login")) {
                mc.getConnection().sendCommand("login " + this.password.getValue());
                Night.CHAT_MANAGER.tagged("Logged in as " + mc.getUser().getName() + ".", this.getName());
                this.timer.reset();
            }
        }
    }
}

