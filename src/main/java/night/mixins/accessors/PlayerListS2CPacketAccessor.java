/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientboundPlayerInfoUpdatePacket.class})
public interface PlayerListS2CPacketAccessor {
    @Accessor(value="actions")
    @Mutable
    public void setActions(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> var1);

    @Accessor(value="entries")
    @Mutable
    public void setEntries(List<ClientboundPlayerInfoUpdatePacket.Entry> var1);
}

