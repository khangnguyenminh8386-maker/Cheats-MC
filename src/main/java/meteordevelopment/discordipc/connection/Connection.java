/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package meteordevelopment.discordipc.connection;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;
import meteordevelopment.discordipc.Opcode;
import meteordevelopment.discordipc.Packet;
import meteordevelopment.discordipc.connection.UnixConnection;
import meteordevelopment.discordipc.connection.WinConnection;

public abstract class Connection {
    private static final String[] UNIX_TEMP_PATHS = new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};

    public static Connection open(Consumer<Packet> callback) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            for (int i = 0; i < 10; ++i) {
                try {
                    return new WinConnection("\\\\?\\pipe\\discord-ipc-" + i, callback);
                }
                catch (IOException iOException) {
                    continue;
                }
            }
        } else {
            String tempPath;
            Object name = null;
            String[] stringArray = UNIX_TEMP_PATHS;
            int n = stringArray.length;
            for (int i = 0; i < n && (name = System.getenv(tempPath = stringArray[i])) == null; ++i) {
            }
            if (name == null) {
                name = "/tmp";
            }
            name = (String)name + "/discord-ipc-";
            for (int i = 0; i < 10; ++i) {
                try {
                    return new UnixConnection((String)name + i, callback);
                }
                catch (IOException iOException) {
                    continue;
                }
            }
        }
        return null;
    }

    public void write(Opcode opcode, JsonObject o) {
        o.addProperty("nonce", UUID.randomUUID().toString());
        byte[] d = o.toString().getBytes();
        ByteBuffer packet = ByteBuffer.allocate(d.length + 8);
        packet.putInt(Integer.reverseBytes(opcode.ordinal()));
        packet.putInt(Integer.reverseBytes(d.length));
        packet.put(d);
        packet.rewind();
        this.write(packet);
    }

    protected abstract void write(ByteBuffer var1);

    public abstract void close();
}

