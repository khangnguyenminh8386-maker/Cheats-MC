/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.inventory.ContainerInput
 */
package night.pingbypass.protocol.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerInput;
import night.pingbypass.protocol.PbPacket;

public class S2CWindowClickPacket
extends PbPacket {
    public static final int ID = 16;
    private final int containerId;
    private final int slotNum;
    private final int buttonNum;
    private final String containerInput;

    public S2CWindowClickPacket(int containerId, int slotNum, int buttonNum, ContainerInput containerInput) {
        this.containerId = containerId;
        this.slotNum = slotNum;
        this.buttonNum = buttonNum;
        this.containerInput = containerInput.name();
    }

    public S2CWindowClickPacket(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.slotNum = buf.readVarInt();
        this.buttonNum = buf.readVarInt();
        this.containerInput = buf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 16;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.slotNum);
        buf.writeVarInt(this.buttonNum);
        buf.writeUtf(this.containerInput);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public int getButtonNum() {
        return this.buttonNum;
    }

    public ContainerInput getContainerInput() {
        return ContainerInput.valueOf((String)this.containerInput);
    }
}

