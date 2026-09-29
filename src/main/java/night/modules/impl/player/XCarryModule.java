/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 */
package night.modules.impl.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import night.events.SubscribeEvent;
import night.events.impl.PacketSendEvent;
import night.modules.Module;
import night.modules.RegisterModule;

@RegisterModule(name="XCarry", description="Allows you to carry items in your crafting slots.", category=Module.Category.PLAYER)
public class XCarryModule
extends Module {
    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (XCarryModule.mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof ServerboundContainerClosePacket) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onDisable() {
        if (XCarryModule.mc.player == null) {
            return;
        }
        mc.getConnection().send((Packet)new ServerboundContainerClosePacket(XCarryModule.mc.player.inventoryMenu.containerId));
    }
}

