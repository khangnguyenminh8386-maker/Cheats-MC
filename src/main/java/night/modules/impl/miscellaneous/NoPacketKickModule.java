/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.modules.impl.miscellaneous;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import night.Night;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterModule(name="NoPacketKick", description="Prevents you from being kicked from the server due to Netty, packet decoding, or codec exceptions.", category=Module.Category.MISCELLANEOUS)
public class NoPacketKickModule
extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Night/NoPacketKick");
    public BooleanSetting logChat = new BooleanSetting("LogChat", "Notifies you in chat when a corrupted or invalid packet is caught and suppressed.", true);
    public BooleanSetting logConsole = new BooleanSetting("LogConsole", "Prints the full exception stacktrace to the console log.", true);
    public BooleanSetting onlyDecoder = new BooleanSetting("OnlyDecoder", "Only suppresses decoder and codec errors, allowing critical socket disconnects through.", false);
    private long lastChatNotification = 0L;
    private long lastConsoleLog = 0L;
    private int suppressedCountSinceLastLog = 0;

    public boolean shouldSuppress(Throwable throwable) {
        if (!this.onlyDecoder.getValue()) {
            return true;
        }
        return throwable instanceof DecoderException || throwable instanceof EncoderException || throwable instanceof IllegalArgumentException || throwable instanceof IndexOutOfBoundsException || throwable instanceof NullPointerException;
    }

    public void onExceptionCaught(Throwable throwable) {
        Object msg;
        long now = System.currentTimeMillis();
        ++this.suppressedCountSinceLastLog;
        if (this.logConsole.getValue() && now - this.lastConsoleLog > 1000L) {
            this.lastConsoleLog = now;
            Object object = msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
            if (msg == null || ((String)msg).isBlank()) {
                msg = throwable.getClass().getSimpleName();
            }
            LOGGER.warn("[NoPacketKick] Suppressed packet exception ({} occurrences in last sec): {}", (Object)this.suppressedCountSinceLastLog, msg);
            this.suppressedCountSinceLastLog = 0;
        }
        if (this.logChat.getValue() && NoPacketKickModule.mc.player != null && NoPacketKickModule.mc.level != null && now - this.lastChatNotification > 1000L) {
            this.lastChatNotification = now;
            Object object = msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
            if (msg == null || ((String)msg).isBlank()) {
                msg = throwable.getClass().getSimpleName();
            }
            if (((String)msg).length() > 100) {
                msg = ((String)msg).substring(0, 100) + "...";
            }
            Night.CHAT_MANAGER.tagged("Suppressed packet error: " + (String)msg, this.getName());
        }
    }
}

