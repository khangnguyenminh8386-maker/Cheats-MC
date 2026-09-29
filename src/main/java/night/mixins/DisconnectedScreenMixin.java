/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$Builder
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.layouts.LayoutElement
 *  net.minecraft.client.gui.layouts.LinearLayout
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.client.gui.screens.DisconnectedScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.TransferState
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;
import net.minecraft.client.gui.components.Button.Builder;
import org.spongepowered.asm.mixin.injection.At.Shift;


import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import night.Night;
import night.gui.components.AccountButton;
import night.modules.impl.miscellaneous.AutoReconnectModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DisconnectedScreen.class})
public class DisconnectedScreenMixin
extends Screen {
    @Shadow
    @Final
    private LinearLayout layout;
    @Unique
    private Button toggleButton;
    @Unique
    private Button button;
    @Unique
    private double time = 100.0;

    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift=At.Shift.BEFORE)})
    private void init(CallbackInfo info) {
        if (Night.SERVER_MANAGER.getLastConnection() != null) {
            this.button = new Button.Builder((Component)Component.literal((String)this.getText()), button -> this.tryConnecting()).width(200).build();
            this.toggleButton = new Button.Builder((Component)Component.literal((String)("Toggle " + String.valueOf(Night.MODULE_MANAGER.getModule(AutoReconnectModule.class).isToggled() ? ChatFormatting.GREEN : ChatFormatting.RED) + "AutoReconnect")), button -> {
                Night.MODULE_MANAGER.getModule(AutoReconnectModule.class).setToggled(!Night.MODULE_MANAGER.getModule(AutoReconnectModule.class).isToggled(), false);
                this.toggleButton.setMessage((Component)Component.literal((String)("Toggle " + String.valueOf(Night.MODULE_MANAGER.getModule(AutoReconnectModule.class).isToggled() ? ChatFormatting.GREEN : ChatFormatting.RED) + "AutoReconnect")));
                this.button.setMessage((Component)Component.literal((String)this.getText()));
                this.time = Night.MODULE_MANAGER.getModule(AutoReconnectModule.class).delay.getValue().intValue() * 20;
            }).width(200).build();
            this.layout.addChild((LayoutElement)this.button);
            this.layout.addChild((LayoutElement)this.toggleButton);
        }
        this.addRenderableWidget(AccountButton.create(8, 8, 75, 20, this));
    }

    public void tick() {
        if (!Night.MODULE_MANAGER.getModule(AutoReconnectModule.class).isToggled() || Night.SERVER_MANAGER.getLastConnection() == null) {
            return;
        }
        if (this.time <= 0.0) {
            this.tryConnecting();
        } else {
            this.time -= 1.0;
            if (this.button != null) {
                this.button.setMessage((Component)Component.literal((String)this.getText()));
            }
        }
    }

    @Unique
   private String getText() {
      String reconnectText = "Reconnect";
      if (Night.MODULE_MANAGER.getModule(AutoReconnectModule.class).isToggled()) {
         reconnectText = reconnectText + " " + String.format("(" + ChatFormatting.GREEN + "%.1fs" + ChatFormatting.RESET + ")", this.time / 20.0);
      }

      return reconnectText;
   }

    @Unique
    private void tryConnecting() {
        ConnectScreen.startConnecting((Screen)new TitleScreen(), (Minecraft)Minecraft.getInstance(), (ServerAddress)((ServerAddress)Night.SERVER_MANAGER.getLastConnection().left()), (ServerData)((ServerData)Night.SERVER_MANAGER.getLastConnection().right()), (boolean)false, (TransferState)new TransferState(Map.of(), Map.of(), false));
    }
}

