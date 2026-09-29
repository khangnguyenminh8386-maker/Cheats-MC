/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package night.pingbypass.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.MouseInputEvent;
import night.events.impl.TickEvent;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.C2SInputPacket;
import night.pingbypass.protocol.packets.C2SOpenInventoryPacket;

public class ClientInputForwarder {
    private boolean inventoryOpen = false;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        InventoryScreen inv;
        boolean nowOpen;
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        Screen screen = mc.gui.screen();
        boolean bl = nowOpen = screen instanceof InventoryScreen && (inv = (InventoryScreen)screen).getMenu() == mc.player.inventoryMenu;
        if (nowOpen == this.inventoryOpen) {
            return;
        }
        this.inventoryOpen = nowOpen;
        try {
            PbCustomPayload payload = PbCustomPayload.fromPacket(new C2SOpenInventoryPacket(nowOpen));
            mc.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)payload));
        }
        catch (Exception e) {
            Night.LOGGER.warn("[PingBypass] Failed to send inventory-open state", (Throwable)e);
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return;
        }
        int button = event.getButton();
        if (button != 0 && button != 1) {
            return;
        }
        this.sendInput(mc, 0, button, 1);
    }

    private void sendInput(Minecraft mc, int type, int button, int action) {
        try {
            PbCustomPayload payload = PbCustomPayload.fromPacket(new C2SInputPacket(type, button, action));
            mc.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)payload));
        }
        catch (Exception e) {
            Night.LOGGER.warn("[PingBypass] Failed to send input", (Throwable)e);
        }
    }

    public void start() {
        Night.EVENT_HANDLER.subscribe(this);
        Night.LOGGER.info("[PingBypass] Client input forwarder started");
    }

    public void stop() {
        Night.EVENT_HANDLER.unsubscribe(this);
    }
}

