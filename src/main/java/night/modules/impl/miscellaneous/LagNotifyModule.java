/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.miscellaneous;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.RenderOverlayEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ColorSetting;
import night.utils.color.ColorUtils;
import night.utils.system.MathUtils;

@RegisterModule(name="LagNotify", description="Notifies you when you lag.", category=Module.Category.MISCELLANEOUS)
public class LagNotifyModule
extends Module {
    BooleanSetting server = new BooleanSetting("Server", "Notifies you when the server stops responding.", true);
    BooleanSetting lagback = new BooleanSetting("Lagback", "Notifies you when you lagback.", true);
    ColorSetting color = new ColorSetting("Color", "The color of the notification text.", ColorUtils.getDefaultOutlineColor());
    Vec3 lagPos = null;
    double lagDistance;
    long lagTime = System.currentTimeMillis();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (this.getNull()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket packet2 = (ClientboundPlayerPositionPacket)packet;
            this.lagPos = new Vec3(packet2.change().position().x(), packet2.change().position().y(), packet2.change().position().z());
            this.lagDistance = LagNotifyModule.mc.player.position().distanceTo(this.lagPos);
            this.lagTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {
        String text;
        if (this.getNull()) {
            return;
        }
        int width = mc.getWindow().getGuiScaledWidth() / 2;
        int height = mc.getWindow().getGuiScaledHeight() / 4;
        boolean flag = false;
        if (this.server.getValue() && Night.SERVER_MANAGER.getResponseTimer().hasTimeElapsed(1000)) {
            text = "Detected server not responding for " + MathUtils.round((float)Night.SERVER_MANAGER.getResponseTimer().timeElapsed() / 1000.0f, 1) + "s.";
            Night.FONT_MANAGER.drawTextWithShadow(event.getContext(), text, width - Night.FONT_MANAGER.getWidth(text) / 2, height - Night.FONT_MANAGER.getHeight(), this.color.getColor());
            flag = true;
        }
        if (this.lagback.getValue() && System.currentTimeMillis() - this.lagTime < 3000L) {
            text = "Detected lagback of " + MathUtils.round(this.lagDistance, 1) + " blocks " + MathUtils.round((float)(System.currentTimeMillis() - this.lagTime) / 1000.0f, 1) + "s.";
            Night.FONT_MANAGER.drawTextWithShadow(event.getContext(), text, width - Night.FONT_MANAGER.getWidth(text) / 2, height - Night.FONT_MANAGER.getHeight() + (flag ? Night.FONT_MANAGER.getHeight() : 0), this.color.getColor());
        }
    }
}

