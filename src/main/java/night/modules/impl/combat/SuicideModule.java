/*
 * Decompiled with CFR 0.152.
 */
package night.modules.impl.combat;

import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PlayerDeathEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="Suicide", description="Makes all of the combat modules target you so that you die.", category=Module.Category.COMBAT)
public class SuicideModule
extends Module {
    public BooleanSetting offhandOverride = new BooleanSetting("OffhandOverride", "Changes the target offhand item in order to make it easier to die.", true);
    public BooleanSetting deathDisable = new BooleanSetting("DeathDisable", "Disables the module once you have died.", true);
    public BooleanSetting loginDisable = new BooleanSetting("LoginDisable", "Disables the module when you logged in.", true);

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!this.deathDisable.getValue()) {
            return;
        }
        if (event.getPlayer() != SuicideModule.mc.player) {
            return;
        }
        if (event.getPlayer().getId() != SuicideModule.mc.player.getId()) {
            return;
        }
        this.setToggled(false);
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientConnectEvent event) {
        if (this.loginDisable.getValue()) {
            this.setToggled(false);
        }
    }

    @Override
    public void onEnable() {
        if (SuicideModule.mc.player == null || SuicideModule.mc.level == null) {
            this.setToggled(false);
        }
    }
}

