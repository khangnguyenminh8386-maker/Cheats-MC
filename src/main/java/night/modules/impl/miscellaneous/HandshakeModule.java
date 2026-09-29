/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.BrandPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package night.modules.impl.miscellaneous;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import night.events.SubscribeEvent;
import night.events.impl.PacketSendEvent;
import night.mixins.accessors.CustomPayloadC2SPacketAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.StringSetting;

@RegisterModule(name="Handshake", description="Spoofs your client handshake to make the server think that you are playing on a different client.", category=Module.Category.MISCELLANEOUS)
public class HandshakeModule
extends Module {
    public StringSetting brand = new StringSetting("Brand", "The brand that the server will think you are playing on.", "vanilla");

    @SubscribeEvent
   public void onPacketSend(PacketSendEvent event) {
      if (mc.player != null && mc.level != null) {
         if (event.getPacket() instanceof ServerboundCustomPayloadPacket packet) {
            if (!packet.payload().type().id().equals(BrandPayload.TYPE.id())) {
               return;
            }

            ((CustomPayloadC2SPacketAccessor)(Object)packet).setPayload(new BrandPayload(this.brand.getValue()));
         }
      }
   }
}

