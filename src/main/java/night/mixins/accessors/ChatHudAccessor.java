/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.ChatComponent
 *  net.minecraft.client.multiplayer.chat.GuiMessage
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ChatComponent.class})
public interface ChatHudAccessor {
    @Accessor(value="allMessages")
    public List<GuiMessage> getMessages();

    @Accessor(value="trimmedMessages")
    public List<GuiMessage.Line> getVisibleMessages();

    @Accessor(value="chatScrollbarPos")
    public int getScrolledLines();

    @Accessor(value="newMessageSinceScroll")
    public void setHasUnreadNewMessages(boolean var1);

    @Invoker(value="logChatMessage")
    public void invokeLogChatMessage(GuiMessage var1);

    @Invoker(value="addMessageToDisplayQueue")
    public void invokeAddVisibleMessage(GuiMessage var1);

    @Invoker(value="addMessageToQueue")
    public void invokeAddMessage(GuiMessage var1);

    @Invoker(value="getWidth")
    public int invokeGetWidth();

    @Invoker(value="getScale")
    public double invokeGetScale();
}

