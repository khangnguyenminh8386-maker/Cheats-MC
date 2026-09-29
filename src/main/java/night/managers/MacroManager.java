/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.managers;

import java.util.HashMap;
import lombok.Generated;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.KeyInputEvent;
import night.events.impl.MouseInputEvent;
import night.utils.IMinecraft;
import night.utils.chat.ChatUtils;
import night.utils.input.KeyboardUtils;

public class MacroManager
implements IMinecraft {
    private final HashMap<String, Integer> macros = new HashMap();

    public MacroManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (MacroManager.mc.player == null || MacroManager.mc.level == null || MacroManager.mc.gui != null && MacroManager.mc.gui.screen() != null) {
            return;
        }
        for (String key : this.macros.keySet()) {
            String[] split;
            int value = this.macros.get(key);
            if (event.getKey() != value) continue;
            if (key == null) {
                Night.CHAT_MANAGER.error("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(value) + String.valueOf(ChatUtils.getSecondary()) + " macro.");
                continue;
            }
            for (String str : split = key.split(";")) {
                if (str.startsWith("/")) {
                    MacroManager.mc.player.connection.sendCommand(str.substring(1));
                    continue;
                }
                if (str.startsWith(Night.COMMAND_MANAGER.getPrefix())) {
                    Night.COMMAND_MANAGER.execute(str);
                    continue;
                }
                MacroManager.mc.player.connection.sendChat(str);
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (MacroManager.mc.player == null || MacroManager.mc.level == null || MacroManager.mc.gui != null && MacroManager.mc.gui.screen() != null) {
            return;
        }
        for (String key : this.macros.keySet()) {
            int value = this.macros.get(key);
            if (value != -event.getButton() - 1) continue;
            if (key == null) {
                Night.CHAT_MANAGER.error("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(value) + String.valueOf(ChatUtils.getSecondary()) + " macro.");
                continue;
            }
            if (key.startsWith("/")) {
                MacroManager.mc.player.connection.sendCommand(key.substring(1));
                continue;
            }
            MacroManager.mc.player.connection.sendChat(key);
        }
    }

    public String getKey(int value) {
        for (String key : this.macros.keySet()) {
            if (value != this.macros.get(key)) continue;
            return key;
        }
        return null;
    }

    public int getValue(String key) {
        return this.macros.get(key);
    }

    public boolean containsKey(String key) {
        return this.macros.containsKey(key);
    }

    public boolean containsValue(int key) {
        return this.macros.containsValue(key);
    }

    public void add(String key, int value) {
        this.macros.put(key, value);
    }

    public void remove(String key) {
        this.macros.remove(key);
    }

    public void clear() {
        this.macros.clear();
    }

    @Generated
    public HashMap<String, Integer> getMacros() {
        return this.macros;
    }
}

