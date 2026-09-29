/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package night.modules;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import night.Night;
import night.events.impl.ToggleModuleEvent;
import night.modules.RegisterModule;
import night.pingbypass.PingBypassFlags;
import night.pingbypass.modules.PbModuleManager;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.protocol.packets.C2SModuleTogglePacket;
import night.pingbypass.protocol.packets.S2CModuleStatePacket;
import night.settings.Setting;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.PositionSetting;
import night.settings.impl.StringSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.IMinecraft;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.chat.ChatUtils;
import night.utils.minecraft.InventoryUtils;

public abstract class Module
implements IMinecraft {
    private final String name;
    private final String description;
    private final Category category;
    private final boolean persistent;
    private final boolean proxyEnhanced;
    private boolean toggled;
    private final List<Setting> settings;
    public BooleanSetting chatNotify;
    public BooleanSetting drawn;
    public BindSetting bind;
    public ModeSetting proxyMode;
    private final Animation animationOffset;

    public Module() {
        RegisterModule annotation = this.getClass().getAnnotation(RegisterModule.class);
        this.name = annotation.name();
        this.description = annotation.description();
        this.category = annotation.category();
        this.persistent = annotation.persistent();
        this.proxyEnhanced = annotation.proxyEnhanced();
        this.toggled = annotation.toggled();
        this.settings = new ArrayList<Setting>();
        this.animationOffset = new Animation(300, Easing.Method.EASE_IN_OUT_CUBIC);
        this.chatNotify = new BooleanSetting("ChatNotify", "Notifies you in chat whenever the module gets toggled on or off.", true);
        this.drawn = new BooleanSetting("Drawn", "Renders the module's name on the HUD's module list.", annotation.drawn());
        this.bind = new BindSetting("Bind", "The keybind that toggles the module on and off.", annotation.bind());
        if (this.proxyEnhanced) {
            this.proxyMode = new ModeSetting("ProxyMode", "Where this module executes when connected to a PingBypass proxy.", "Auto", new String[]{"Auto", "Proxy", "Local"});
        }
        if (this.persistent) {
            this.toggled = true;
        }
        if (this.toggled) {
            Night.EVENT_HANDLER.subscribe(this);
        }
    }

    public boolean getNull() {
        return Module.mc.player == null || Module.mc.level == null;
    }

    public boolean shouldRunOnProxy() {
        if (!this.proxyEnhanced || this.proxyMode == null) {
            return false;
        }
        if (!PingBypassFlags.proxyForwardingActive) {
            return false;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return false;
        }
        return switch (this.proxyMode.getValue()) {
            case "Auto", "Proxy" -> true;
            case "Local" -> false;
            default -> true;
        };
    }

    public boolean shouldSkipActions() {
        return this.shouldRunOnProxy();
    }

    public boolean isRunningOnProxy() {
        if (!this.proxyEnhanced) {
            return false;
        }
        if (!PingBypassFlags.proxyForwardingActive) {
            return false;
        }
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return false;
        }
        return this.proxyMode == null || !this.proxyMode.getValue().equals("Local");
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public String getMetaData() {
        return "";
    }

    public String getProxyIndicator() {
        if (this.shouldRunOnProxy() && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return " \u00a7d[PB]";
        }
        return "";
    }

    private boolean shouldSkipLocalExecution() {
        return this.shouldRunOnProxy();
    }

    public void setToggled(boolean toggled) {
        this.setToggled(toggled, true);
    }

    public void setToggled(boolean toggled, boolean notify) {
        if (this.persistent) {
            return;
        }
        if (toggled == this.toggled) {
            return;
        }
        if (toggled) {
        }
        this.toggled = toggled;
        Night.EVENT_HANDLER.post(new ToggleModuleEvent(this, this.toggled));
        if ((this.shouldRunOnProxy() || PbModuleManager.MIGRATED_MODULE_NAMES.contains(this.name)) && notify && Night.PINGBYPASS_CONFIG != null && !Night.PINGBYPASS_CONFIG.isServer()) {
            this.sendProxyToggle(this.toggled);
        }
        if (this.proxyEnhanced && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && PingBypassFlags.proxyForwardingActive && Night.PROXY_SERVER != null) {
            ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CModuleStatePacket(this.name, this.toggled)));
            for (Connection conn : Night.PROXY_SERVER.getConnections()) {
                if (!conn.isConnected()) continue;
                conn.send((Packet)packet);
            }
        }
        if (this.toggled) {
            this.animationOffset.setEasing(Easing.Method.EASE_IN_OUT_CUBIC);
            if (notify && this.chatNotify.getValue()) {
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + this.name + String.valueOf(ChatUtils.getSecondary()) + " = " + String.valueOf(ChatFormatting.GREEN) + "true" + String.valueOf(ChatUtils.getSecondary()) + ";", "toggle-" + this.getName().toLowerCase());
            }
            this.onEnable();
            if (this.toggled) {
                Night.EVENT_HANDLER.subscribe(this);
            }
        } else {
            this.animationOffset.setEasing(Easing.Method.EASE_IN_OUT_CUBIC);
            Night.EVENT_HANDLER.unsubscribe(this);
            this.onDisable();
            InventoryUtils.releaseAllBorrows();
            if (notify && this.chatNotify.getValue()) {
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + this.name + String.valueOf(ChatUtils.getSecondary()) + " = " + String.valueOf(ChatFormatting.RED) + "false" + String.valueOf(ChatUtils.getSecondary()) + ";", "toggle-" + this.getName().toLowerCase());
            }
        }
    }

    public int getBind() {
        return this.bind.getValue();
    }

    public void setBind(int bind) {
        this.bind.setValue(bind);
    }

    public void resetValues() {
        for (Setting uncastedSetting : this.settings) {
            Setting setting;
            if (uncastedSetting instanceof BooleanSetting) {
                setting = (BooleanSetting)uncastedSetting;
                ((BooleanSetting)setting).resetValue();
            }
            if (uncastedSetting instanceof NumberSetting) {
                setting = (NumberSetting)uncastedSetting;
                ((NumberSetting)setting).resetValue();
            }
            if (uncastedSetting instanceof ModeSetting) {
                setting = (ModeSetting)uncastedSetting;
                ((ModeSetting)setting).resetValue();
            }
            if (uncastedSetting instanceof StringSetting) {
                setting = (StringSetting)uncastedSetting;
                ((StringSetting)setting).resetValue();
            }
            if (uncastedSetting instanceof BindSetting) {
                setting = (BindSetting)uncastedSetting;
                ((BindSetting)setting).resetValue();
            }
            if (uncastedSetting instanceof WhitelistSetting) {
                setting = (WhitelistSetting)uncastedSetting;
                ((WhitelistSetting)setting).clear();
            }
            if (uncastedSetting instanceof ColorSetting) {
                setting = (ColorSetting)uncastedSetting;
                ((ColorSetting)setting).resetValue();
            }
            if (!(uncastedSetting instanceof PositionSetting)) continue;
            setting = (PositionSetting)uncastedSetting;
            ((PositionSetting)setting).resetValue();
        }
    }

    public Setting getSetting(String name) {
        return this.settings.stream().filter(s -> s.getName().equalsIgnoreCase(name) && !(s instanceof CategorySetting)).findFirst().orElse(null);
    }

    private void sendProxyToggle(boolean enabled) {
        try {
            if (mc.getConnection() != null) {
                PbCustomPayload payload = PbCustomPayload.fromPacket(new C2SModuleTogglePacket(this.name, enabled));
                mc.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)payload));
            }
        }
        catch (Exception e) {
            Night.LOGGER.warn("[PingBypass] Failed to send module toggle to proxy", (Throwable)e);
        }
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public Category getCategory() {
        return this.category;
    }

    @Generated
    public boolean isPersistent() {
        return this.persistent;
    }

    @Generated
    public boolean isProxyEnhanced() {
        return this.proxyEnhanced;
    }

    @Generated
    public boolean isToggled() {
        return this.toggled;
    }

    @Generated
    public List<Setting> getSettings() {
        return this.settings;
    }

    @Generated
    public BooleanSetting getChatNotify() {
        return this.chatNotify;
    }

    @Generated
    public BooleanSetting getDrawn() {
        return this.drawn;
    }

    @Generated
    public ModeSetting getProxyMode() {
        return this.proxyMode;
    }

    @Generated
    public Animation getAnimationOffset() {
        return this.animationOffset;
    }

    public static enum Category {
        COMBAT("Combat"),
        PLAYER("Player"),
        VISUALS("Visuals"),
        MOVEMENT("Movement"),
        MISCELLANEOUS("Miscellaneous"),
        CORE("Core");

        private final String name;

        private Category(String name) {
            this.name = name;
        }

        @Generated
        public String getName() {
            return this.name;
        }
    }
}

