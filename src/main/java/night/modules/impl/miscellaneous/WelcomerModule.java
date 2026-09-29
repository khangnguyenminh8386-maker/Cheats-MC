/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.world.entity.player.Player
 */
package night.modules.impl.miscellaneous;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ChatInputEvent;
import night.events.impl.PlayerConnectEvent;
import night.events.impl.PlayerDisconnectEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;
import night.utils.system.MathUtils;
import night.utils.system.Timer;

@RegisterModule(name="Welcomer", description="Sends a message when a player joins or leaves the server.", category=Module.Category.MISCELLANEOUS)
public class WelcomerModule
extends Module {
    public BooleanSetting joins = new BooleanSetting("Joins", "Sends messages when a player joins the server", true);
    public BooleanSetting leaves = new BooleanSetting("Leaves", "Sends messages when a player leaves the server", true);
    public BooleanSetting clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
    public BooleanSetting unicode = new BooleanSetting("Unicode", "Uses prettier unicode icons instead of normal arrows.", false);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
    private final String[] JOIN_MESSAGES = new String[]{"Hello, ", "Welcome to the server, ", "Good to see you, ", "Greetings, ", "Good evening, ", "Hey, "};
    private final String[] LEAVE_MESSAGES = new String[]{"Goodbye, ", "See you later, ", "Bye bye, ", "I hope you had a good time, ", "Farewell, ", "See you next time, "};
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();
    private final Timer messageTimer = new Timer();

    @Override
    public void onEnable() {
        this.queue.clear();
        this.messageTimer.reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.getNull()) {
            return;
        }
        if (this.messageTimer.hasTimeElapsed(this.delay.getValue().intValue() * 1000) && !this.queue.isEmpty() && !this.clientside.getValue()) {
            String message = this.queue.poll();
            if (this.clientside.getValue()) {
                Night.CHAT_MANAGER.message(message);
            } else {
                WelcomerModule.mc.player.connection.sendChat((this.greenText.getValue() ? "> " : "") + message);
            }
            this.messageTimer.reset();
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent event) {
        if (this.getNull()) {
            return;
        }
        this.messageTimer.reset();
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent event) {
        if (this.getNull()) {
            return;
        }
        Player player = WelcomerModule.mc.level.getPlayerByUUID(event.getId());
        if (player != null && this.joins.getValue()) {
            if (this.clientside.getValue()) {
                Night.CHAT_MANAGER.message(String.valueOf(ChatFormatting.DARK_GRAY) + "[" + String.valueOf(ChatFormatting.GREEN) + (this.unicode.getValue() ? "\u00bb" : ">") + String.valueOf(ChatFormatting.DARK_GRAY) + "] " + String.valueOf(ChatFormatting.GRAY) + player.getName().getString());
            } else {
                this.queue.add(this.JOIN_MESSAGES[(int)MathUtils.random(this.JOIN_MESSAGES.length, 0.0)] + player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        if (this.getNull()) {
            return;
        }
        Player player = WelcomerModule.mc.level.getPlayerByUUID(event.getId());
        if (player != null && this.leaves.getValue()) {
            if (this.clientside.getValue()) {
                Night.CHAT_MANAGER.message(String.valueOf(ChatFormatting.DARK_GRAY) + "[" + String.valueOf(ChatFormatting.RED) + (this.unicode.getValue() ? "\u00ab" : "<") + String.valueOf(ChatFormatting.DARK_GRAY) + "] " + String.valueOf(ChatFormatting.GRAY) + player.getName().getString());
            } else {
                this.queue.add(this.LEAVE_MESSAGES[(int)MathUtils.random(this.LEAVE_MESSAGES.length, 0.0)] + player.getName().getString());
            }
        }
    }
}

