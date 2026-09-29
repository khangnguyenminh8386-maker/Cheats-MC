/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package night.mixins;

import net.minecraft.client.multiplayer.chat.GuiMessage;
import night.utils.mixins.IChatHudLineVisible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={GuiMessage.Line.class})
public class ChatHudLineVisibleMixin
implements IChatHudLineVisible {
    @Unique
    private boolean clientMessage = false;
    @Unique
    private String clientIdentifier = "";

    @Override
    public boolean night$isClientMessage() {
        return this.clientMessage;
    }

    @Override
    public void night$setClientMessage(boolean clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public String night$getClientIdentifier() {
        return this.clientIdentifier;
    }

    @Override
    public void night$setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}

