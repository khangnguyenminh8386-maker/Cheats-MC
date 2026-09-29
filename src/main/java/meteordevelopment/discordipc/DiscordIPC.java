/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package meteordevelopment.discordipc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import java.util.function.BiConsumer;
import meteordevelopment.discordipc.IPCUser;
import meteordevelopment.discordipc.Opcode;
import meteordevelopment.discordipc.Packet;
import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.discordipc.connection.Connection;

public class DiscordIPC {
    private static final Gson GSON = new Gson();
    private static BiConsumer<Integer, String> onError = DiscordIPC::defaultErrorCallback;
    private static Connection c;
    private static Runnable onReady;
    private static boolean receivedDispatch;
    private static JsonObject queuedActivity;
    private static IPCUser user;

    public static void setOnError(BiConsumer<Integer, String> onError) {
        DiscordIPC.onError = onError;
    }

    public static boolean start(long appId, Runnable onReady) {
        c = Connection.open(DiscordIPC::onPacket);
        if (c == null) {
            return false;
        }
        DiscordIPC.onReady = onReady;
        JsonObject o = new JsonObject();
        o.addProperty("v", (Number)1);
        o.addProperty("client_id", Long.toString(appId));
        c.write(Opcode.Handshake, o);
        return true;
    }

    public static boolean isConnected() {
        return c != null;
    }

    public static IPCUser getUser() {
        return user;
    }

    public static void setActivity(RichPresence presence) {
        if (c == null) {
            return;
        }
        queuedActivity = presence.toJson();
        if (receivedDispatch) {
            DiscordIPC.sendActivity();
        }
    }

    public static void stop() {
        if (c != null) {
            c.close();
            c = null;
            onReady = null;
            receivedDispatch = false;
            queuedActivity = null;
            user = null;
        }
    }

    private static void sendActivity() {
        JsonObject args = new JsonObject();
        args.addProperty("pid", (Number)DiscordIPC.getPID());
        args.add("activity", (JsonElement)queuedActivity);
        JsonObject o = new JsonObject();
        o.addProperty("cmd", "SET_ACTIVITY");
        o.add("args", (JsonElement)args);
        c.write(Opcode.Frame, o);
        queuedActivity = null;
    }

    private static void onPacket(Packet packet) {
        if (packet.opcode() == Opcode.Close) {
            if (onError != null) {
                onError.accept(packet.data().get("code").getAsInt(), packet.data().get("message").getAsString());
            }
            DiscordIPC.stop();
        } else if (packet.opcode() == Opcode.Frame) {
            if (packet.data().has("evt") && packet.data().get("evt").getAsString().equals("ERROR")) {
                JsonObject d = packet.data().getAsJsonObject("data");
                if (onError != null) {
                    onError.accept(d.get("code").getAsInt(), d.get("message").getAsString());
                }
            } else if (packet.data().has("cmd") && packet.data().get("cmd").getAsString().equals("DISPATCH")) {
                receivedDispatch = true;
                user = (IPCUser)GSON.fromJson((JsonElement)packet.data().getAsJsonObject("data").getAsJsonObject("user"), IPCUser.class);
                if (onReady != null) {
                    onReady.run();
                }
                if (queuedActivity != null) {
                    DiscordIPC.sendActivity();
                }
            }
        }
    }

    private static int getPID() {
        String pr = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(pr.substring(0, pr.indexOf(64)));
    }

    private static void defaultErrorCallback(int code, String message) {
        System.err.println("Discord IPC error " + code + " with message: " + message);
    }
}

