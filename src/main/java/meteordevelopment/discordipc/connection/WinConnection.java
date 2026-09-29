/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 */
package meteordevelopment.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import meteordevelopment.discordipc.Opcode;
import meteordevelopment.discordipc.Packet;
import meteordevelopment.discordipc.connection.Connection;

public class WinConnection
extends Connection {
    private final RandomAccessFile raf;
    private final Consumer<Packet> callback;

    WinConnection(String name, Consumer<Packet> callback) throws IOException {
        this.raf = new RandomAccessFile(name, "rw");
        this.callback = callback;
        Thread thread = new Thread(this::run);
        thread.setName("Discord IPC - Read thread");
        thread.start();
    }

    @Override
    protected void write(ByteBuffer buffer) {
        try {
            this.raf.write(buffer.array());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        ByteBuffer intB = ByteBuffer.allocate(4);
        try {
            while (true) {
                this.readFully(intB);
                Opcode opcode = Opcode.valueOf(Integer.reverseBytes(intB.getInt(0)));
                this.readFully(intB);
                int length = Integer.reverseBytes(intB.getInt(0));
                ByteBuffer dataB = ByteBuffer.allocate(length);
                this.readFully(dataB);
                String data = Charset.defaultCharset().decode(dataB.rewind()).toString();
                this.callback.accept(new Packet(opcode, JsonParser.parseString((String)data).getAsJsonObject()));
            }
        }
        catch (Exception exception) {
            return;
        }
    }

    private void readFully(ByteBuffer buffer) throws IOException {
        buffer.rewind();
        while (this.raf.length() < (long)buffer.remaining()) {
            Thread.onSpinWait();
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (buffer.hasRemaining()) {
            this.raf.getChannel().read(buffer);
        }
    }

    @Override
    public void close() {
        try {
            this.raf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

