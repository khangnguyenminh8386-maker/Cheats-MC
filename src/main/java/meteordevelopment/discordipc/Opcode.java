/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.discordipc;

public enum Opcode {
    Handshake,
    Frame,
    Close,
    Ping,
    Pong;

    private static final Opcode[] VALUES;

    public static Opcode valueOf(int i) {
        return VALUES[i];
    }

    static {
        VALUES = Opcode.values();
    }
}

