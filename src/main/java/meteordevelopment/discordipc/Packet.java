/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package meteordevelopment.discordipc;

import com.google.gson.JsonObject;
import meteordevelopment.discordipc.Opcode;

public record Packet(Opcode opcode, JsonObject data) {
}

