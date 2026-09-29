/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 */
package night.modules.impl.visuals.stashfinder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashWebhook {
    private static volatile String webhookUrl = "";
    private static volatile String userId = "";
    private static volatile boolean loaded = false;
    private static final long PING_INTERVAL_MS = 5000L;
    private static volatile long lastSentAtMs = 0L;

    private static File file() {
        File dir = new File(FabricLoader.getInstance().getGameDir().toFile(), "night/stashfinder");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "webhook.json");
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        File f = StashWebhook.file();
        if (!f.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(f);){
            JsonObject obj = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            if (obj.has("url")) {
                webhookUrl = obj.get("url").getAsString();
            }
            if (obj.has("userId")) {
                userId = obj.get("userId").getAsString();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static synchronized void save() {
        try (FileWriter writer = new FileWriter(StashWebhook.file());){
            JsonObject obj = new JsonObject();
            obj.addProperty("url", webhookUrl);
            obj.addProperty("userId", userId);
            writer.write(obj.toString());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static String getWebhookUrl() {
        StashWebhook.ensureLoaded();
        return webhookUrl;
    }

    public static String getUserId() {
        StashWebhook.ensureLoaded();
        return userId;
    }

    public static void setWebhookUrl(String url) {
        StashWebhook.ensureLoaded();
        webhookUrl = url;
        StashWebhook.save();
    }

    public static void setUserId(String id) {
        StashWebhook.ensureLoaded();
        userId = id;
        StashWebhook.save();
    }

    private static synchronized boolean tryClaimSendSlot() {
        long now = System.currentTimeMillis();
        if (now - lastSentAtMs < 5000L) {
            return false;
        }
        lastSentAtMs = now;
        return true;
    }

    private static String serverLabel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() != null) {
            return mc.getCurrentServer().ip;
        }
        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            return "Singleplayer: " + mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        return "Unknown";
    }

    public static void send(int x, int z, String dimension, Map<String, Integer> countsInOrder, String customUrl, String customUserId) {
        String url;
        String string = url = customUrl != null && !customUrl.isBlank() ? customUrl : StashWebhook.getWebhookUrl();
        if (url == null || url.isBlank()) {
            return;
        }
        if (!StashWebhook.tryClaimSendSlot()) {
            return;
        }
        String server = StashWebhook.serverLabel();
        String user = customUserId != null && !customUserId.isBlank() ? customUserId : StashWebhook.getUserId();
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject embed = new JsonObject();
                embed.addProperty("title", "Stash Found!");
                embed.addProperty("description", "Coordinates: **X: " + x + " Z: " + z + "** (" + dimension + ")");
                embed.addProperty("color", (Number)3066993);
                JsonArray fields = new JsonArray();
                JsonObject serverField = new JsonObject();
                serverField.addProperty("name", "Server");
                serverField.addProperty("value", server);
                serverField.addProperty("inline", Boolean.valueOf(false));
                fields.add((JsonElement)serverField);
                for (Map.Entry e : countsInOrder.entrySet()) {
                    JsonObject field = new JsonObject();
                    field.addProperty("name", (String)e.getKey());
                    field.addProperty("value", String.valueOf(e.getValue()));
                    field.addProperty("inline", Boolean.valueOf(true));
                    fields.add((JsonElement)field);
                }
                embed.add("fields", (JsonElement)fields);
                JsonArray embeds = new JsonArray();
                embeds.add((JsonElement)embed);
                JsonObject payload = new JsonObject();
                payload.addProperty("content", (String)(user == null || user.isBlank() ? "" : "<@" + user + ">"));
                payload.add("embeds", (JsonElement)embeds);
                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
                URL targetUrl = URI.create(url).toURL();
                HttpURLConnection conn = (HttpURLConnection)targetUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                try (OutputStream os = conn.getOutputStream();){
                    os.write(body);
                    os.flush();
                }
                int responseCode = conn.getResponseCode();
                conn.disconnect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }
}

