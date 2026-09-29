/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  net.minecraft.network.Connection
 *  net.minecraft.network.PacketListener
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Connection.class})
public interface ConnectionAccessor {
    @Accessor(value="channel")
    public Channel getChannel();

    @Accessor(value="packetListener")
    public void setPacketListener(PacketListener var1);

    @Accessor(value="sendLoginDisconnect")
    public void setSendLoginDisconnect(boolean var1);
}

