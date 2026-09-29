/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import night.pingbypass.protocol.PbPacket;

public class C2SInputPacket
extends PbPacket {
    public static final int ID = 10;
    public static final int TYPE_MOUSE = 0;
    public static final int TYPE_KEY = 1;
    public static final int ACTION_PRESS = 1;
    public static final int ACTION_RELEASE = 0;
    private final int inputType;
    private final int button;
    private final int action;

    public C2SInputPacket(int inputType, int button, int action) {
        this.inputType = inputType;
        this.button = button;
        this.action = action;
    }

    public C2SInputPacket(FriendlyByteBuf buf) {
        this.inputType = buf.readVarInt();
        this.button = buf.readVarInt();
        this.action = buf.readVarInt();
    }

    @Override
    public int getPacketId() {
        return 10;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.inputType);
        buf.writeVarInt(this.button);
        buf.writeVarInt(this.action);
    }

    public int getInputType() {
        return this.inputType;
    }

    public int getButton() {
        return this.button;
    }

    public int getAction() {
        return this.action;
    }
}

