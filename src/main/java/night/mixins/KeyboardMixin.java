/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyboardHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.input.KeyEvent
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import night.Night;
import night.events.impl.KeyInputEvent;
import night.events.impl.UnfilteredKeyInputEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardHandler.class})
public class KeyboardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"keyPress"}, at={@At(value="HEAD")})
    private void keyPress(long handle, int action, KeyEvent event, CallbackInfo info) {
        Night.EVENT_HANDLER.post(new UnfilteredKeyInputEvent(event.key(), event.scancode(), action, event.modifiers()));
        if (handle == this.minecraft.getWindow().handle() && action == 1 && this.minecraft.gui.screen() == null) {
            Night.EVENT_HANDLER.post(new KeyInputEvent(event.key(), event.modifiers()));
        }
    }
}

