/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundLoginPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import night.Night;
import night.events.impl.ClientConnectEvent;
import night.modules.impl.movement.ElytraFlyModule;
import night.pingbypass.PingBypassFlags;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ClientPlayNetworkHandlerMixin
implements IMinecraft {
    @Unique
    private float handleMovePlayer$prevYaw;
    @Unique
    private float handleMovePlayer$prevPitch;
    @Unique
    private boolean handleMovePlayer$guard;

    @Inject(method={"handleLogin"}, at={@At(value="TAIL")})
    private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo info) {
        Night.EVENT_HANDLER.post(new ClientConnectEvent());
    }

    @Inject(method={"handleMovePlayer"}, at={@At(value="HEAD")})
    private void handleMovePlayer$HEAD(ClientboundPlayerPositionPacket packet, CallbackInfo info) {
        boolean isBounce;
        this.handleMovePlayer$guard = false;
        if (ClientPlayNetworkHandlerMixin.mc.player == null) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        ElytraFlyModule ef = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ElytraFlyModule.class) : null;
        boolean bl = isBounce = ef != null && ef.isToggled() && ef.mode.getValue().equalsIgnoreCase("Bounce");
        if (Night.ROTATION_MANAGER != null && Night.ROTATION_MANAGER.isPacketRotateActive() || isBounce) {
            this.handleMovePlayer$prevYaw = ClientPlayNetworkHandlerMixin.mc.player.getYRot();
            this.handleMovePlayer$prevPitch = isBounce && ef.isBouncePitchOverrideActive() ? ef.getBounceSavedCameraPitch() : ClientPlayNetworkHandlerMixin.mc.player.getXRot();
            this.handleMovePlayer$guard = true;
        }
    }

    @Inject(method={"handleMovePlayer"}, at={@At(value="TAIL")})
    private void handleMovePlayer$TAIL(ClientboundPlayerPositionPacket packet, CallbackInfo info) {
        if (!this.handleMovePlayer$guard) {
            return;
        }
        this.handleMovePlayer$guard = false;
        ClientPlayNetworkHandlerMixin.mc.player.setYRot(this.handleMovePlayer$prevYaw);
        ClientPlayNetworkHandlerMixin.mc.player.setXRot(this.handleMovePlayer$prevPitch);
        ClientPlayNetworkHandlerMixin.mc.player.xRotO = this.handleMovePlayer$prevPitch;
    }
}

