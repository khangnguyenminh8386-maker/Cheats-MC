/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.Unpooled
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 */
package night.pingbypass.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import night.pingbypass.protocol.PbPacket;

public record PbCustomPayload(byte[] data) implements CustomPacketPayload
{
    public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath((String)"night", (String)"pingbypass");
    public static final CustomPacketPayload.Type<PbCustomPayload> ID = new CustomPacketPayload.Type(CHANNEL);
    public static final int C2S_JOIN = 0;
    public static final int S2C_PASSWORD_REQUEST = 1;
    public static final int C2S_PASSWORD = 2;
    public static final int S2C_ERROR = 7;
    public static final StreamCodec<RegistryFriendlyByteBuf, PbCustomPayload> CODEC = new StreamCodec<RegistryFriendlyByteBuf, PbCustomPayload>(){

        public PbCustomPayload decode(RegistryFriendlyByteBuf buf) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return new PbCustomPayload(bytes);
        }

        public void encode(RegistryFriendlyByteBuf buf, PbCustomPayload payload) {
            buf.writeBytes(payload.data());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public FriendlyByteBuf toBuf() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.data));
    }

    public static PbCustomPayload fromPacket(PbPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(packet.getPacketId());
        packet.write(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return new PbCustomPayload(bytes);
    }

    public static ServerboundCustomPayloadPacket createC2SPacket(PbPacket packet) {
        return new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(packet));
    }

    public static PbCustomPayload passwordRequest() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(1);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return new PbCustomPayload(bytes);
    }

    public static PbCustomPayload error(String message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(7);
        buf.writeUtf(message);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return new PbCustomPayload(bytes);
    }
}

