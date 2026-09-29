/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.components.ComponentRenderUtils
 *  net.minecraft.client.multiplayer.chat.GuiMessage
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  net.minecraft.client.multiplayer.chat.GuiMessageSource
 *  net.minecraft.client.multiplayer.chat.GuiMessageTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Mth
 */
package night.managers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.ChatHudAccessor;
import night.modules.impl.core.CommandsModule;
import night.modules.impl.miscellaneous.BetterChatModule;
import night.utils.IMinecraft;
import night.utils.chat.ChatUtils;
import night.utils.mixins.IChatHudLine;
import night.utils.mixins.IChatHudLineVisible;
import night.utils.text.FormattingUtils;

public class ChatManager
implements IMinecraft {
    public static boolean isInsideHudExtractChat = false;
    private final List<String> awaitMessages = new ArrayList<String>();

    public ChatManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        if (this.awaitMessages.isEmpty()) {
            return;
        }
        for (String message : new ArrayList<String>(this.awaitMessages)) {
            this.addMessage(message);
            this.awaitMessages.remove(message);
        }
    }

    public void message(String message) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void message(String message, String identifier) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        ChatManager.deleteMessage(identifier);
        this.addMessage(this.getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + message, identifier);
    }

    public void tagged(String message, String tag) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + tag + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void tagged(String message, String tag, String identifier) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        ChatManager.deleteMessage(identifier);
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + tag + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + message, identifier);
    }

    public void info$await(String message) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.DARK_BLUE) + " [" + String.valueOf(ChatFormatting.BLUE) + "?" + String.valueOf(ChatFormatting.DARK_BLUE) + "] " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void info(String message) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_BLUE) + " [" + String.valueOf(ChatFormatting.BLUE) + "?" + String.valueOf(ChatFormatting.DARK_BLUE) + "] " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void warn$await(String message) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.GOLD) + " [" + String.valueOf(ChatFormatting.YELLOW) + "!" + String.valueOf(ChatFormatting.GOLD) + "] " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void warn(String message) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.GOLD) + " [" + String.valueOf(ChatFormatting.YELLOW) + "!" + String.valueOf(ChatFormatting.GOLD) + "] " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void error$await(String message) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.DARK_RED) + " [" + String.valueOf(ChatFormatting.RED) + "!!" + String.valueOf(ChatFormatting.DARK_RED) + "] " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void error(String message) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_RED) + " [" + String.valueOf(ChatFormatting.RED) + "!!" + String.valueOf(ChatFormatting.DARK_RED) + "] " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void await(String message) {
        this.awaitMessages.add(this.getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void await(String message, String tag) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + tag + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + message);
    }

    public void addMessage(String message) {
        this.addTextMessage((Component)Component.literal((String)message), "");
    }

    public void addMessage(String message, String identifier) {
        this.addTextMessage((Component)Component.literal((String)message), identifier);
    }

    public void addMessage(Component message) {
        this.addTextMessage(message, "");
    }

    public void addMessage(Component message, String identifier) {
        this.addTextMessage(message, identifier);
    }

    private void addTextMessage(Component message, String identifier) {
        if (!mc.isSameThread()) {
            mc.execute(() -> this.addTextMessage(message, identifier));
            return;
        }
        GuiMessage line = new GuiMessage(ChatManager.mc.gui.hud.getGuiTicks(), message, null, GuiMessageSource.SYSTEM_CLIENT, GuiMessageTag.system());
        ((IChatHudLine)(Object)line).night$setClientMessage(true);
        ((IChatHudLine)(Object)line).night$setClientIdentifier(identifier);
        ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeLogChatMessage(line);
        ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeAddMessage(line);
        List list = ComponentRenderUtils.wrapComponents((FormattedText)line.content(), (int)Mth.floor((double)((double)((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeGetWidth() / ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeGetScale())), (Font)ChatManager.mc.font);
        for (int j = 0; j < list.size(); ++j) {
            FormattedCharSequence orderedText = (FormattedCharSequence)list.get(j);
            if (ChatManager.mc.gui.hud.getChat().isChatFocused() && ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getScrolledLines() > 0) {
                ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).setHasUnreadNewMessages(true);
                ChatManager.mc.gui.hud.getChat().scrollChat(1);
            }
            boolean bl2 = j == list.size() - 1;
            GuiMessage.Line visible = new GuiMessage.Line(line, orderedText, bl2);
            ((IChatHudLineVisible)(Object)visible).night$setClientMessage(true);
            ((IChatHudLineVisible)(Object)visible).night$setClientIdentifier(identifier);
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().addFirst(visible);
            if (!Night.MODULE_MANAGER.getModule(BetterChatModule.class).isToggled() || !Night.MODULE_MANAGER.getModule(BetterChatModule.class).animation.getValue()) continue;
            Night.MODULE_MANAGER.getModule(BetterChatModule.class).getAnimationMap().put(visible, System.currentTimeMillis());
        }
        while (((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().size() > 100) {
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().removeLast();
        }
    }

    public static void deleteMessage(String identifier) {
        try {
            ArrayList<GuiMessage> removedLines = new ArrayList<GuiMessage>();
            for (GuiMessage message : ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getMessages()) {
                if (!((IChatHudLine)(Object)(Object)(Object)message).night$isClientMessage() || ((IChatHudLine)(Object)(Object)(Object)message).night$getClientIdentifier().isEmpty() || !((IChatHudLine)(Object)(Object)(Object)message).night$getClientIdentifier().equals(identifier)) continue;
                removedLines.add(message);
            }
            ArrayList<GuiMessage.Line> removedVisibleLines = new ArrayList<GuiMessage.Line>();
            for (GuiMessage.Line message : ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages()) {
                if (!((IChatHudLineVisible)(Object)(Object)(Object)message).night$isClientMessage() || ((IChatHudLineVisible)(Object)(Object)(Object)message).night$getClientIdentifier().isEmpty() || !((IChatHudLineVisible)(Object)(Object)(Object)message).night$getClientIdentifier().equals(identifier)) continue;
                removedVisibleLines.add(message);
            }
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getMessages().removeAll(removedLines);
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().removeAll(removedVisibleLines);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String getWatermark() {
        return this.getWatermark(null);
    }

    private String getWatermark(String text) {
        if (!Night.MODULE_MANAGER.getModule(CommandsModule.class).watermark.getValue()) {
            return "";
        }
        return String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())) + Night.MODULE_MANAGER.getModule(CommandsModule.class).opening.getValue() + String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).primaryWatermarkColor.getValue())) + (text == null ? Night.MODULE_MANAGER.getModule(CommandsModule.class).watermarkText.getValue() : text) + String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())) + Night.MODULE_MANAGER.getModule(CommandsModule.class).closing.getValue();
    }
}

