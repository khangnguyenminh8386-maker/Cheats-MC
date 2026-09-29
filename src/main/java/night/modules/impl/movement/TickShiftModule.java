/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.world.entity.Entity
 */
package night.modules.impl.movement;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.api.ISprintModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.movement.SpeedModule;
import night.settings.impl.NumberSetting;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="TickShift", description="Manipulates minecraft timer to give you a small speed boost.", category=Module.Category.MOVEMENT)
public class TickShiftModule
extends Module {
    public NumberSetting maxTicks = new NumberSetting("MaxTicks", "The maximum amount of ticks that the boost will be charging for.", 1, 1, 40);
    public NumberSetting delay = new NumberSetting("Delay", "The delay between each tick.", 4, 1, 10);
    public NumberSetting speed = new NumberSetting("Speed", "The speed of charging each tick.", Float.valueOf(2.0f), Float.valueOf(1.0f), Float.valueOf(10.0f));
    int ticks = 0;
    int wait = 0;

    @Override
    public void onEnable() {
        if (this.getNull()) {
            return;
        }
        this.reset();
    }

    @Override
    public void onDisable() {
        if (this.getNull()) {
            return;
        }
        this.reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        boolean charging;
        if (this.getNull()) {
            return;
        }
        if (TickShiftModule.mc.player.fallDistance >= 5.0) {
            return;
        }
        ElytraFlyModule elytra = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytra.isToggled() && elytra.mode.getValue().equalsIgnoreCase("Control") && TickShiftModule.mc.player.isFallFlying()) {
            return;
        }
        boolean instant = ((ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))).isInstantMode();
        boolean bl = charging = TickShiftModule.mc.player.xxa == 0.0f && TickShiftModule.mc.player.zza == 0.0f && TickShiftModule.mc.player.fallDistance == 0.0;
        if (!instant) {
            charging |= EntityUtils.getSpeed((Entity)TickShiftModule.mc.player, EntityUtils.SpeedUnit.KILOMETERS) <= 5.0;
        }
        if (charging) {
            Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
            if (this.wait >= this.delay.getValue().intValue()) {
                if (this.ticks < this.maxTicks.getValue().intValue()) {
                    ++this.ticks;
                }
                this.wait = 0;
            }
            ++this.wait;
        } else if (this.ticks > 0) {
            if (!Night.MODULE_MANAGER.getModule(SpeedModule.class).isDrivingTimer() && !TickShiftModule.mc.options.keyJump.isDown()) {
                Night.WORLD_MANAGER.setTimerMultiplier(this.speed.getValue().floatValue());
            }
            --this.ticks;
        } else {
            this.reset();
        }
    }

    public int getTicks() {
        return this.ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public boolean isShifting() {
        return this.isToggled() && this.ticks > 0 && Night.WORLD_MANAGER.getTimerMultiplier() > 1.0f;
    }

    public void reset() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
        this.ticks = 0;
        this.wait = 0;
    }

    @Override
    public String getMetaData() {
        return (String)(this.ticks >= this.maxTicks.getValue().intValue() ? String.valueOf(ChatFormatting.GREEN) : "") + this.ticks + String.valueOf(ChatFormatting.RESET);
    }
}

