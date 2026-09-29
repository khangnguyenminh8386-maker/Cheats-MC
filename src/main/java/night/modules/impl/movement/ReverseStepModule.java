/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 */
package night.modules.impl.movement;

import net.minecraft.world.entity.Entity;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerUpdateEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.NumberSetting;

@RegisterModule(name="ReverseStep", description="Makes it so that you fall down instantly at a specified height.", category=Module.Category.MOVEMENT)
public class ReverseStepModule
extends Module {
    public NumberSetting height = new NumberSetting("Height", "The maximum height at which instant falling will be applied to.", 3.0, 0.0, 12.0);

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(300L)) {
            return;
        }
        if (ReverseStepModule.mc.player.isPassenger() || ReverseStepModule.mc.player.isFallFlying() || ReverseStepModule.mc.player.onClimbable() || ReverseStepModule.mc.player.isInLava() || ReverseStepModule.mc.player.isInWater() || ReverseStepModule.mc.player.input.keyPresses.jump() || ReverseStepModule.mc.player.input.keyPresses.shift()) {
            return;
        }
        if (ReverseStepModule.mc.player.onGround() && this.nearBlock(this.height.getValue().doubleValue())) {
            ReverseStepModule.mc.player.setDeltaMovement(ReverseStepModule.mc.player.getDeltaMovement().x, -this.height.getValue().doubleValue(), ReverseStepModule.mc.player.getDeltaMovement().z);
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.height.getValue().floatValue());
    }

    private boolean nearBlock(double height) {
        for (double i = 0.0; i < height + 0.5; i += 0.01) {
            if (ReverseStepModule.mc.level.noCollision((Entity)ReverseStepModule.mc.player, ReverseStepModule.mc.player.getBoundingBox().move(0.0, -i, 0.0))) continue;
            return true;
        }
        return false;
    }
}

