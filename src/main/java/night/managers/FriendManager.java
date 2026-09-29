/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package night.managers;

import java.awt.Color;
import java.util.ArrayList;
import lombok.Generated;
import night.Night;
import night.modules.impl.core.FriendModule;
import night.modules.impl.core.PingBypassModule;
import night.pingbypass.PingBypassFlags;

public class FriendManager {
    private final ArrayList<String> friends = new ArrayList();

    public boolean contains(String name) {
        if (this.getFriendFire()) {
            return false;
        }
        return this.friends.stream().anyMatch(name::equalsIgnoreCase);
    }

    public void add(String name) {
        if (this.contains(name)) {
            return;
        }
        this.friends.add(name);
        this.syncToProxy();
    }

    public void remove(String name) {
        this.friends.removeIf(name::equalsIgnoreCase);
        this.syncToProxy();
    }

    public void clear() {
        this.friends.clear();
        this.syncToProxy();
    }

    private void syncToProxy() {
        PingBypassModule pingBypass;
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && !Night.PINGBYPASS_CONFIG.isServer() && (pingBypass = Night.MODULE_MANAGER.getModule(PingBypassModule.class)) != null) {
            pingBypass.syncFriendsToProxy();
        }
    }

    public boolean getFriendFire() {
        if (Night.MODULE_MANAGER == null) {
            return false;
        }
        FriendModule module = Night.MODULE_MANAGER.getModule(FriendModule.class);
        return module != null && module.friendlyFire != null && module.friendlyFire.getValue();
    }

    public void sendFriendMessage(String name) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        FriendModule module = Night.MODULE_MANAGER.getModule(FriendModule.class);
        if (module != null) {
            module.sendFriendMessage(name);
        }
    }

    public Color getDefaultFriendColor() {
        return this.getDefaultFriendColor(255);
    }

    public Color getDefaultFriendColor(int alpha) {
        return new Color(85, 255, 255, alpha);
    }

    @Generated
    public ArrayList<String> getFriends() {
        return this.friends;
    }
}

