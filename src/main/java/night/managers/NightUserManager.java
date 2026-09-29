/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  net.minecraft.world.entity.player.Player
 */
package night.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.entity.player.Player;
import night.utils.IMinecraft;

public class NightUserManager
implements IMinecraft {
    private static final long POLL_INTERVAL_SECONDS = 30L;
    private static final String BASE_URL = System.getProperty("night.license.baseUrl", "https://nightclient.info.vn");
    private static final String FALLBACK_BASE_URL = "http://nightclient.info.vn";
    private static final Gson GSON = new Gson();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
    private final Set<String> onlineUsernames = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Cheats MC-Cheats MCUserManager-Poll");
        t.setDaemon(true);
        return t;
    });

    public NightUserManager() {
        this.executor.scheduleWithFixedDelay(this::poll, 0L, 30L, TimeUnit.SECONDS);
    }

    public boolean isNightUser(Player player) {
        if (player == null) {
            return false;
        }
        if (NightUserManager.mc.player != null && player.getUUID().equals(NightUserManager.mc.player.getUUID())) {
            return true;
        }
        return this.isNightUser(player.getName().getString());
    }

    public boolean isNightUser(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (NightUserManager.mc.player != null && NightUserManager.mc.player.getName().getString().equalsIgnoreCase(name)) {
            return true;
        }
        return this.onlineUsernames.contains(name.toLowerCase());
    }

    private void poll() {
    }

    private static /* synthetic */ void lambda$tryPoll$0(Set fresh, JsonElement el) {
        fresh.add(el.getAsString().toLowerCase());
    }
}

