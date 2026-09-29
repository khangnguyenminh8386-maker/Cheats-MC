/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.player;

import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.NumberSetting;

@RegisterModule(name="Timer", description="Makes your game run at a faster tick speed.", category=Module.Category.PLAYER)
public class TimerModule
extends Module {
    public NumberSetting multiplier = new NumberSetting("Multiplier", "The multiplier that will be added to the game's speed.", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(20.0f));

    @SubscribeEvent(priority=-2147483648)
    public void onTick(TickEvent event) {
        if (TimerModule.mc.player == null || TimerModule.mc.level == null) {
            return;
        }
        Night.WORLD_MANAGER.setTimerMultiplier(this.multiplier.getValue().floatValue());
    }

    @Override
    public void onEnable() {
        Night.WORLD_MANAGER.setTimerMultiplier(this.multiplier.getValue().floatValue());
    }

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.multiplier.getValue().floatValue());
    }
}

