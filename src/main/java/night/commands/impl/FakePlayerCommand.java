/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 */
package night.commands.impl;

import java.util.List;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.events.impl.PlayerPopEvent;
import night.modules.impl.miscellaneous.FakePlayerModule;

@RegisterCommand(name="fakeplayer", aliases={"fp"}, description="Spawns a client-side fake player to test on.", syntax="[remove|pop|swim|record|stop|play|<name>]")
public class FakePlayerCommand
extends Command {
    private static final byte TOTEM_POP_EVENT_ID = 35;

    @Override
    public void execute(String[] args) {
        FakePlayerModule fpModule = Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
        if (fpModule == null) {
            return;
        }
        if (args.length == 0) {
            fpModule.setToggled(!fpModule.isToggled());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "remove": {
                if (fpModule.isToggled()) {
                    fpModule.setToggled(false);
                    break;
                }
                Night.CHAT_MANAGER.warn("No fake player spawned.");
                break;
            }
            case "pop": {
                this.popBot(fpModule);
                break;
            }
            case "swim": {
                this.toggleSwim(fpModule);
                break;
            }
            case "record": {
                fpModule.startRecording();
                break;
            }
            case "stop": {
                fpModule.stopRecording();
                break;
            }
            case "play": {
                fpModule.startPlaying();
                break;
            }
            default: {
                fpModule.name.setValue(args[0]);
                if (!fpModule.isToggled()) {
                    fpModule.setToggled(true);
                    break;
                }
                fpModule.setToggled(false);
                fpModule.setToggled(true);
            }
        }
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("remove", "pop", "swim", "record", "stop", "play");
        }
        return List.of();
    }

    private boolean nullCheck() {
        return FakePlayerCommand.mc.level == null || FakePlayerCommand.mc.player == null;
    }

    private void popBot(FakePlayerModule fpModule) {
        if (this.nullCheck() || mc.getConnection() == null) {
            Night.CHAT_MANAGER.warn("You need to be in a world to do that.");
            return;
        }
        RemotePlayer fake = fpModule.getPlayer();
        if (fake == null || fake.level() != FakePlayerCommand.mc.level) {
            Night.CHAT_MANAGER.warn("No fake player spawned. Spawn one first.");
            return;
        }
        mc.getConnection().handleEntityEvent(new ClientboundEntityEventPacket((Entity)fake, (byte)35));
        Night.EVENT_HANDLER.post(new PlayerPopEvent((Player)fake, 1));
        Night.CHAT_MANAGER.tagged("Popped fake player.", "FakePlayer");
    }

    private void toggleSwim(FakePlayerModule fpModule) {
        if (this.nullCheck()) {
            Night.CHAT_MANAGER.warn("You need to be in a world to do that.");
            return;
        }
        RemotePlayer fake = fpModule.getPlayer();
        if (fake == null || fake.level() != FakePlayerCommand.mc.level) {
            Night.CHAT_MANAGER.warn("No fake player spawned. Spawn one first.");
            return;
        }
        Pose pose = fake.getPose() != Pose.SWIMMING ? Pose.SWIMMING : Pose.STANDING;
        fake.setPose(pose);
        fake.refreshDimensions();
        Night.CHAT_MANAGER.tagged(pose == Pose.SWIMMING ? "Fake player now in swim pose." : "Fake player now standing.", "FakePlayer");
    }
}

