/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector2d
 */
package night.modules.impl.movement;

import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PlayerMoveEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.MovementUtils;
import org.joml.Vector2d;

@RegisterModule(name="FastSwim", description="Modifies your swim speed in water and lava.", category=Module.Category.MOVEMENT)
public class FastSwimModule
extends Module {
    private static final double DEFAULT_VERTICAL_SPEED = 0.04;
    public NumberSetting hWater = new NumberSetting("H-Water", "Horizontal swim speed multiplier in water.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting vWater = new NumberSetting("V-Water", "Vertical (up/down) swim speed multiplier in water.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting hLava = new NumberSetting("H-Lava", "Horizontal swim speed multiplier in lava.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting vLava = new NumberSetting("V-Lava", "Vertical (up/down) swim speed multiplier in lava.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        float v;
        float h;
        if (FastSwimModule.mc.player == null) {
            return;
        }
        if (FastSwimModule.mc.player.onGround()) {
            return;
        }
        ElytraFlyModule elytra = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytra.isToggled() && elytra.mode.getValue().equalsIgnoreCase("Control") && FastSwimModule.mc.player.isFallFlying()) {
            return;
        }
        if (FastSwimModule.mc.player.isInLava()) {
            h = this.hLava.getValue().floatValue();
            v = this.vLava.getValue().floatValue();
        } else if (FastSwimModule.mc.player.isInWater()) {
            h = this.hWater.getValue().floatValue();
            v = this.vWater.getValue().floatValue();
        } else {
            return;
        }
        Vector2d horizontal = MovementUtils.forward(MovementUtils.DEFAULT_SPEED * (double)h);
        double y = event.getMovement().y;
        if (FastSwimModule.mc.options.keyJump.isDown()) {
            y = 0.04 * (double)v;
        } else if (FastSwimModule.mc.options.keyShift.isDown()) {
            y = -0.04 * (double)v;
        }
        event.setMovement(new Vec3(horizontal.x, y, horizontal.y));
        event.setCancelled(true);
    }
}

