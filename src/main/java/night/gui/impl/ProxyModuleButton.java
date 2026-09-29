/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package night.gui.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.modules.impl.core.PingBypassModule;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.C2SModuleTogglePacket;
import night.utils.graphics.Renderer2D;

public class ProxyModuleButton
extends Button {
    private final String moduleName;

    public ProxyModuleButton(String moduleName, Frame parent, int height) {
        super(parent, height, "PingBypass proxy module: " + moduleName);
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    private boolean isEnabled() {
        PingBypassModule pbModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        if (pbModule == null) {
            return false;
        }
        return pbModule.isProxyModuleEnabled(this.moduleName);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        boolean enabled;
        Color bgColor;
        if (this.isHovering(mouseX, mouseY) && Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            Night.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());
        }
        Color color = bgColor = this.isHovering(mouseX, mouseY) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
        if (bgColor.getAlpha() > 0) {
            Renderer2D.renderQuad(context, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, bgColor);
        }
        if (enabled = this.isEnabled()) {
            Color accentColor = ClickGuiScreen.getButtonColor(this.getY(), 255);
            Renderer2D.renderQuad(context, this.getX() + this.getPadding(), this.getY() + 1, this.getX() + this.getPadding() + 2, this.getY() + this.getHeight() - 2, accentColor);
        }
        String displayName = this.moduleName;
        int textX = this.getX() + this.getTextPadding() + (enabled ? 1 : 0);
        int textY = this.getY() + 2;
        Night.FONT_MANAGER.drawTextWithShadow(context, String.valueOf(enabled ? "" : ChatFormatting.GRAY) + displayName, textX, textY, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            boolean newState = !this.isEnabled();
            this.sendModuleToggle(newState);
            this.playClickSound();
        }
    }

    private void sendModuleToggle(boolean enabled) {
        if (mc.getConnection() == null) {
            return;
        }
        C2SModuleTogglePacket packet = new C2SModuleTogglePacket(this.moduleName, enabled);
        mc.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(packet)));
    }
}

