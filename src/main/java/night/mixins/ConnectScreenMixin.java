/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.TransferState
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import night.Night;
import night.events.impl.ServerConnectEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ConnectScreen.class})
public class ConnectScreenMixin {
    @Inject(method={"startConnecting"}, at={@At(value="HEAD")})
    private static void connect(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data, boolean isQuickPlay, @Nullable TransferState transferState, CallbackInfo info) {
        Night.EVENT_HANDLER.post(new ServerConnectEvent(address, data));
    }
}

