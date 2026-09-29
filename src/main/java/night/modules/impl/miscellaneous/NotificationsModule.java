/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 */
package night.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ClientConnectEvent;
import night.events.impl.PlayerDeathEvent;
import night.events.impl.PlayerPopEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.modules.impl.miscellaneous.FakePlayerModule;
import night.settings.impl.BooleanSetting;
import night.utils.chat.ChatUtils;
import night.utils.minecraft.EntityUtils;

@RegisterModule(name="Notifications", description="Notifies you in chat whenever something significant happens.", category=Module.Category.MISCELLANEOUS)
public class NotificationsModule
extends Module {
    public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Notifies you in chat whenever a player pops a totem.", true);
    public BooleanSetting visualRange = new BooleanSetting("VisualRange", "Notifies you in chat whenever a player enters your render distance.", false);
    public BooleanSetting pearlThrows = new BooleanSetting("PearlThrows", "Notifies you in chat whenever a player throws a pearl.", true);
    private final Map<UUID, String> loadedPlayers = new LinkedHashMap<UUID, String>();
    private final ArrayList<Integer> thrownPearls = new ArrayList();

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.getNull()) {
            return;
        }
        if (this.visualRange.getValue()) {
            HashSet<UUID> currentlyVisible = new HashSet<UUID>();
            for (Entity entity : NotificationsModule.mc.level.entitiesForRendering()) {
                FakePlayerModule fakePlayer;
                if (!(entity instanceof Player)) continue;
                Player player = (Player)entity;
                if (entity == NotificationsModule.mc.player || EntityUtils.isGhost((Entity)player)) continue;
                FakePlayerModule fakePlayerModule = fakePlayer = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
                if (fakePlayer != null && fakePlayer.isToggled() && entity == fakePlayer.getPlayer()) continue;
                currentlyVisible.add(player.getUUID());
                if (this.loadedPlayers.containsKey(player.getUUID())) continue;
                this.loadedPlayers.put(player.getUUID(), player.getName().getString());
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + player.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has entered your visual range.", "visual-range-" + player.getName().getString());
            }
            if (!this.loadedPlayers.isEmpty()) {
                Iterator<Map.Entry<UUID, String>> it = this.loadedPlayers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, String> entry = it.next();
                    if (currentlyVisible.contains(entry.getKey())) continue;
                    it.remove();
                    Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + entry.getValue() + String.valueOf(ChatUtils.getSecondary()) + " has left your visual range.", "visual-range-" + entry.getValue());
                }
            }
        }
        if (this.pearlThrows.getValue()) {
            for (Entity e : NotificationsModule.mc.level.entitiesForRendering()) {
                ThrownEnderpearl pearl;
                if (!(e instanceof ThrownEnderpearl) || (pearl = (ThrownEnderpearl)e).getOwner() == null || this.thrownPearls.contains(pearl.getId())) continue;
                String name = pearl.getOwner().getName().getString();
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + name + String.valueOf(ChatUtils.getSecondary()) + " threw a pearl towards " + EntityUtils.getPearlDirection(pearl).toString() + ".", "pearl-throws-" + name);
                this.thrownPearls.add(pearl.getId());
            }
            this.thrownPearls.removeIf(id -> !(NotificationsModule.mc.level.getEntity(id.intValue()) instanceof ThrownEnderpearl));
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        this.loadedPlayers.clear();
    }

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent event) {
        if (this.totemPops.getValue()) {
            Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + event.getPlayer().getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has popped " + String.valueOf(ChatUtils.getPrimary()) + event.getPops() + String.valueOf(ChatUtils.getSecondary()) + " totem" + (event.getPops() > 1 ? "s" : "") + ".", "totem-pop-" + event.getPlayer().getName().getString());
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        int pops = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(event.getPlayer().getUUID(), 0);
        if (this.totemPops.getValue() && pops > 0) {
            Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + event.getPlayer().getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has died after popping " + String.valueOf(ChatUtils.getPrimary()) + pops + String.valueOf(ChatUtils.getSecondary()) + " totem" + (pops > 1 ? "s" : "") + ".", "totem-pop-" + event.getPlayer().getName().getString());
        }
    }
}

