/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.gui.screens.social.SocialInteractionsScreen
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import night.Night;
import night.events.impl.RenderOverlayEvent;
import night.gui.ClickGuiScreen;
import night.gui.special.MainMenuScreen;
import night.modules.impl.core.MenuModule;
import night.modules.impl.miscellaneous.AutoRespawnModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.modules.impl.player.AutoShopModule;
import night.utils.minecraft.WorldUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public class InGameHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift=At.Shift.AFTER)})
    private void render(DeltaTracker tickCounter, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo info, @Local(ordinal=0) GuiGraphicsExtractor context) {
        if (this.minecraft.gui.hud.isHidden()) {
            return;
        }
        Night.EVENT_HANDLER.post(new RenderOverlayEvent(context, tickCounter.getGameTimeDeltaPartialTick(true)));
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$setScreen(Screen screen, CallbackInfo info) {
        ClickGuiScreen clickGui;
        Screen screen2;
        if (Night.MODULE_MANAGER != null) {
            AutoShopModule autoShop = Night.MODULE_MANAGER.getModule(AutoShopModule.class);
            if (autoShop != null) {
                AutoShopModule.ScreenDecision decision = autoShop.classifyScreen(screen);
                if (decision == AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED) {
                    info.cancel();
                    return;
                }
                if (decision == AutoShopModule.ScreenDecision.FAIL_OPEN_UNEXPECTED) {
                    // Let vanilla present this screen; bypass Night's 500 ms close branch only here.
                    return;
                }
            }
        }
        if (screen instanceof SocialInteractionsScreen) {
            info.cancel();
            return;
        }
        if ((screen == null || screen instanceof PauseScreen) && (screen2 = this.minecraft.gui.screen()) instanceof ClickGuiScreen && !(clickGui = (ClickGuiScreen)screen2).isReadyToRemove()) {
            clickGui.requestClose();
            info.cancel();
            return;
        }
        if (screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen) && System.currentTimeMillis() - WorldUtils.lastInteractablePlaceTime < 500L) {
            info.cancel();
            if (this.minecraft.getConnection() != null && this.minecraft.player != null && this.minecraft.player.containerMenu != null) {
                this.minecraft.getConnection().send((Packet)new ServerboundContainerClosePacket(this.minecraft.player.containerMenu.containerId));
            }
            return;
        }
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        if (screen instanceof DeathScreen && this.minecraft.player != null) {
            AutoRespawnModule autoRespawn;
            FakePlayerModule fakePlayer = Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
            if (fakePlayer != null && fakePlayer.isToggled()) {
                fakePlayer.setToggled(false);
            }
            if ((autoRespawn = Night.MODULE_MANAGER.getModule(AutoRespawnModule.class)) != null && autoRespawn.isToggled()) {
                this.minecraft.player.respawn();
                info.cancel();
                return;
            }
        }
        if (screen instanceof TitleScreen) {
            Night.checkForUpdates();
            MenuModule menu = Night.MODULE_MANAGER.getModule(MenuModule.class);
            if (menu != null && menu.isToggled() && menu.mainMenu.getValue()) {
                this.minecraft.gui.setScreen((Screen)new MainMenuScreen());
                info.cancel();
            }
        }
    }
}

