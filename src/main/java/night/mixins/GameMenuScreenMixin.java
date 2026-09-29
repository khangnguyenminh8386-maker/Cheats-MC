/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.Night;
import night.modules.impl.core.PingBypassModule;
import night.pingbypass.PingBypassFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PauseScreen.class})
public abstract class GameMenuScreenMixin
extends Screen {
    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void init(CallbackInfo ci) {
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u00a7cDisconnect Proxy"), button -> {
            PingBypassModule pbModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
            if (pbModule != null && pbModule.isToggled()) {
                pbModule.setToggled(false);
            }
        }).bounds(this.width / 2 - 102, this.height - 40, 204, 20).build());
    }
}

