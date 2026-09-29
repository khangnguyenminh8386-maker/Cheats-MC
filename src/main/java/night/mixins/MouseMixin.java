/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.input.MouseButtonInfo
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import night.Night;
import night.events.impl.MouseInputEvent;
import night.events.impl.UnfilteredMouseInputEvent;
import night.modules.impl.combat.MainhandModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"onButton"}, at={@At(value="HEAD")})
    private void onMouseButton(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo info) {
        int button = buttonInfo.button();
        int mods = buttonInfo.modifiers();
        Night.EVENT_HANDLER.post(new UnfilteredMouseInputEvent(button, action, mods));
        if (window == this.minecraft.getWindow().handle() && action == 1 && this.minecraft.gui.screen() == null) {
            Night.EVENT_HANDLER.post(new MouseInputEvent(button));
        }
    }

    @Inject(method={"onScroll"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onScroll(long window, double xoffset, double yoffset, CallbackInfo info) {
        MainhandModule mainhand;
        if (this.minecraft.gui.screen() == null && window == this.minecraft.getWindow().handle() && Night.MODULE_MANAGER != null && (mainhand = Night.MODULE_MANAGER.getModule(MainhandModule.class)) != null && mainhand.shouldLockSlot()) {
            info.cancel();
        }
    }
}

