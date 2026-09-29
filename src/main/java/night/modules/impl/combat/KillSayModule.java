/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.combat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ChatInputEvent;
import night.events.impl.CommandInputEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.PlayerPopEvent;
import night.events.impl.TargetDeathEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.combat.SuicideModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.utils.system.FileUtils;
import night.utils.system.MathUtils;
import night.utils.system.Timer;

@RegisterModule(name="KillSay", description="Automatically sends a message when you kill someone.", category=Module.Category.COMBAT)
public class KillSayModule
extends Module {
    private final String[] KILL_MESSAGES = new String[]{"Sit [username], Cheats MC owns me and all!", "[username] I killed you with the power of Cheats MC!", "Sorry [username], get good get Cheats MC!", "I just killed [username] thanks to Cheats MC!"};
    public BooleanSetting kills = new BooleanSetting("Kills", "Sends a message when you kill someone.", true);
    public BooleanSetting pops = new BooleanSetting("Pops", "Sends a message when you pop someone.", false);
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the messages.", "Default", new String[]{"Default", "Custom"});
    public StringSetting killFile = new StringSetting("KillFile", "The name of the file containing the kill messages.", "killfile.txt");
    public StringSetting popFile = new StringSetting("PopFile", "The name of the file containing the pop messages.", "popfile.txt");
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 10);
    public BooleanSetting clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public BooleanSetting killStreak = new BooleanSetting("KillStreak", "Keeps track of your kills.", false);
    public BooleanSetting suicideIgnore = new BooleanSetting("SuicideIgnore", "Doesn't reset kill streak when you suicide.", false);
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();
    private List<String> killMessages = new ArrayList<String>();
    private List<String> popMessages = new ArrayList<String>();
    private final Timer timer = new Timer();
    private String lastMessage;
    private int streak;
    private boolean suicide = false;

    @Override
    public void onEnable() {
        this.queue.clear();
        this.timer.reset();
        this.streak = 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.getNull() || this.queue.isEmpty()) {
            return;
        }
        this.killMessages = FileUtils.readLines(new File("Night/Client/" + this.killFile.getValue()));
        this.popMessages = FileUtils.readLines(new File("Night/Client/" + this.popFile.getValue()));
        ConcurrentLinkedQueue<String> concurrentLinkedQueue = this.queue;
        synchronized (concurrentLinkedQueue) {
            if (this.timer.hasTimeElapsed(this.delay.getValue().intValue() * 1000)) {
                String message = this.queue.poll();
                if (!message.isEmpty()) {
                    if (this.clientside.getValue()) {
                        Night.CHAT_MANAGER.message(message);
                    } else {
                        KillSayModule.mc.player.connection.sendChat((this.greenText.getValue() ? "> " : "") + message);
                    }
                    this.lastMessage = message;
                }
                this.timer.reset();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTargetDeath(TargetDeathEvent event) {
        if (this.getNull() || event.getPlayer() == KillSayModule.mc.player) {
            return;
        }
        ++this.streak;
        ConcurrentLinkedQueue<String> concurrentLinkedQueue = this.queue;
        synchronized (concurrentLinkedQueue) {
            if (this.kills.getValue()) {
                this.queue.clear();
                String message = this.getKillMessage(event.getPlayer().getName().getString());
                this.queue.add(message);
            }
        }
        if (this.killStreak.getValue() && this.streak > 1) {
            Night.CHAT_MANAGER.message("You are on a " + this.streak + " kill streak.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent event) {
        if (this.getNull() || event.getPlayer() == KillSayModule.mc.player || !Night.TARGET_MANAGER.isTarget(event.getPlayer()) || !this.pops.getValue()) {
            return;
        }
        ConcurrentLinkedQueue<String> concurrentLinkedQueue = this.queue;
        synchronized (concurrentLinkedQueue) {
            String message = this.getPopMessage(event.getPlayer().getName().getString(), event.getPops());
            this.queue.add(message);
        }
    }

    @SubscribeEvent
    public void onCommandInput(CommandInputEvent event) {
        if (this.getNull()) {
            return;
        }
        if (event.getMessage().contains("/kill")) {
            this.suicide = true;
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent event) {
        if (this.getNull()) {
            return;
        }
        this.timer.reset();
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(event.getPlayer() != KillSayModule.mc.player || this.suicideIgnore.getValue() && (this.suicide || Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()))) {
            this.streak = 0;
        }
        this.suicide = false;
    }

   private String getPopMessage(String username, int pops) {
      String message;
      if (this.mode.getValue().equals("Custom") && !this.popMessages.isEmpty()) {
         do {
            message = this.popMessages.get((int)MathUtils.random(this.popMessages.size(), 0.0)).replace("[username]", username);
         } while (message.equals(this.lastMessage) && this.popMessages.size() > 1);
      } else {
         message = pops + (pops > 1 ? " pops " : " pop ") + username + "!";
      }

      return message;
   }

    private String getKillMessage(String username) {
        String message;
        if (this.mode.getValue().equals("Custom") && !this.killMessages.isEmpty()) {
            while ((message = this.killMessages.get((int)MathUtils.random(this.killMessages.size(), 0.0)).replace("[username]", username)).equals(this.lastMessage) && this.killMessages.size() > 1) {
            }
        } else {
            while ((message = this.KILL_MESSAGES[(int)MathUtils.random(this.KILL_MESSAGES.length, 0.0)].replace("[username]", username)).equals(this.lastMessage)) {
            }
        }
        return message;
    }

    @Override
    public String getMetaData() {
        return "" + this.streak;
    }
}

