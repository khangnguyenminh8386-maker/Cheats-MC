/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.Night;
import night.events.impl.ChatInputEvent;
import night.events.impl.CommandInputEvent;
import night.modules.impl.core.HUDModule;
import night.modules.impl.core.IRCModule;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer2D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin
extends Screen {
    @Shadow
    protected EditBox input;

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"handleChatInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V")}, cancellable=true)
    private void sendMessage(String chatText, boolean addToHistory, CallbackInfo info) {
        ChatInputEvent event = new ChatInputEvent(chatText);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"handleChatInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V")}, cancellable=true)
    private void sendCommand(String chatText, boolean addToHistory, CallbackInfo info) {
        CommandInputEvent event = new CommandInputEvent(chatText);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @WrapWithCondition(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V")})
    private boolean render(GuiGraphicsExtractor instance, int x1, int y1, int x2, int y2, int color) {
        return !Night.MODULE_MANAGER.getModule(HUDModule.class).isToggled();
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void night$renderIrcOutline(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.input == null) {
            return;
        }
        String text = this.input.getValue();
        IRCModule irc = Night.MODULE_MANAGER.getModule(IRCModule.class);
        if (irc == null || !irc.isToggled()) {
            return;
        }
        String prefix = irc.prefix.getValue();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "$";
        }
        if (text != null && text.startsWith(prefix)) {
            int x = this.input.getX() - 2;
            int y = this.input.getY() - 2;
            int right = this.input.getX() + this.input.getWidth() + 2;
            int bottom = this.input.getY() + this.input.getHeight() + 2;
            Color baseColor = ColorUtils.getGlobalColor();
            Renderer2D.renderOutline(graphics, (float)x - 1.0f, (float)y - 1.0f, (float)right + 1.0f, (float)bottom + 1.0f, new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 90));
            Renderer2D.renderOutline(graphics, x, y, right, bottom, baseColor);
            Renderer2D.renderQuad(graphics, x, y, right, bottom, new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 35));
        }
    }
}

