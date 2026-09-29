/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.world.item.ItemStack
 */
package night.modules.impl.core;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.IPatchModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="Patch", description="Server/anticheat-specific hotfixes.", category=Module.Category.CORE, persistent=true, drawn=false)
public class PatchModule
extends Module
implements IPatchModule {
    public BooleanSetting grimAttackVelocity = new BooleanSetting("GrimAttackVelocity", "Stops your own sprint and movement from being cut after landing extra knockback on a hit.", false);
    public BooleanSetting silentSwapFix = new BooleanSetting("SilentSwapFix", "Stops hotbar slot desync and ghost item duplication during silent switch.", true);

    @Override
    public boolean isGrimAttackVelocity() {
        return this.grimAttackVelocity.getValue();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (PatchModule.mc.player == null) {
            return;
        }
        if (!this.silentSwapFix.getValue()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ClientboundContainerSetSlotPacket)) {
            return;
        }
        ClientboundContainerSetSlotPacket packet2 = (ClientboundContainerSetSlotPacket)packet;
        if (packet2.getContainerId() != 0) {
            return;
        }
        int slot = packet2.getSlot();
        if (slot < 36 || slot > 44) {
            return;
        }
        int hotbarIndex = slot - 36;
        ItemStack packetStack = packet2.getItem();
        ItemStack currentSlotStack = PatchModule.mc.player.getInventory().getItem(hotbarIndex);
        ItemStack handStack = PatchModule.mc.player.getMainHandItem();
        if (!packetStack.isEmpty() && !currentSlotStack.isEmpty() && packetStack.getItem() == currentSlotStack.getItem() && packetStack.getCount() == currentSlotStack.getCount()) {
            event.setCancelled(true);
            return;
        }
        if (PatchModule.mc.gui.screen() == null && !currentSlotStack.isEmpty() && !packetStack.isEmpty() && currentSlotStack.getItem() != packetStack.getItem()) {
            int selectedSlot = PatchModule.mc.player.getInventory().getSelectedSlot();
            int serverSlot = Night.POSITION_MANAGER.getServerSlot();
            if (hotbarIndex != selectedSlot && (packetStack.getItem() == handStack.getItem() || serverSlot >= 0 && serverSlot <= 8 && packetStack.getItem() == PatchModule.mc.player.getInventory().getItem(serverSlot).getItem())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}

