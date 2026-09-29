/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.ClientInput
 *  net.minecraft.client.player.KeyboardInput
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.phys.Vec2
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import night.Night;
import night.events.impl.EventInput;
import night.events.impl.KeyboardTickEvent;
import night.modules.api.IAutoWalkModule;
import night.modules.api.IFreecamModule;
import night.modules.api.ISprintModule;
import night.utils.minecraft.BaritoneUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardInput.class})
public class KeyboardInputMixin
extends ClientInput {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void tick$TAIL(CallbackInfo info) {
        ISprintModule sprint;
        KeyboardTickEvent event = new KeyboardTickEvent(this.moveVector.y, this.moveVector.x);
        Night.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            this.moveVector = new Vec2(event.getMovementSideways(), event.getMovementForward());
        }
        if (Night.MODULE_MANAGER != null) {
            IFreecamModule freecam = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
            if (freecam != null && freecam.isToggled()) {
                this.keyPresses = new Input(false, false, false, false, false, false, false);
                this.moveVector = new Vec2(0.0f, 0.0f);
                return;
            }
            IAutoWalkModule autoWalk = (IAutoWalkModule)((Object)Night.MODULE_MANAGER.getModule("AutoWalk"));
            if (autoWalk != null && autoWalk.isToggled() && !BaritoneUtils.isActive()) {
                boolean backward = autoWalk.isBackward();
                boolean forward = !backward;
                this.keyPresses = new Input(forward, backward, this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
                this.moveVector = new Vec2(this.moveVector.x, backward ? -1.0f : 1.0f);
            }
            EventInput eventInput = new EventInput(this.keyPresses.forward(), this.keyPresses.backward(), this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
            Night.EVENT_HANDLER.post(eventInput);
            this.keyPresses = new Input(eventInput.forward, eventInput.backward, eventInput.left, eventInput.right, eventInput.jumping, eventInput.sneaking, eventInput.sprinting);
        }
        ISprintModule iSprintModule = sprint = Night.MODULE_MANAGER == null ? null : (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"));
        if (sprint != null && sprint.isGrimCompensating()) {
            int strafe = sprint.getGrimStrafe();
            this.keyPresses = new Input(true, false, strafe > 0, strafe < 0, this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
            this.moveVector = strafe == 0 ? new Vec2(0.0f, 1.0f) : new Vec2(strafe > 0 ? 1.0f : -1.0f, 1.0f);
            Minecraft.getInstance().player.setSprinting(true);
        }
    }
}

