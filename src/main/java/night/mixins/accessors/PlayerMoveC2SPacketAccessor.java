/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ServerboundMovePlayerPacket.class})
public interface PlayerMoveC2SPacketAccessor {
    @Accessor(value="yRot")
    @Mutable
    public void setYaw(float var1);

    @Accessor(value="xRot")
    @Mutable
    public void setPitch(float var1);

    @Accessor(value="onGround")
    @Mutable
    public void setOnGround(boolean var1);

    @Accessor(value="hasRot")
    @Mutable
    public void setChangeLook(boolean var1);
}

