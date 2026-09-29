/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="grab", description="Lets you copy various things to your clipboard.", syntax="<ip|coords|name>")
public class GrabCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        return args.length == 0 ? List.of("ip", "coords", "name") : List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            switch (args[0]) {
                case "ip": {
                    this.copy(Night.SERVER_MANAGER.getServer());
                    break;
                }
                case "coords": {
                    this.copy("[" + (int)GrabCommand.mc.player.getX() + ", " + (int)GrabCommand.mc.player.getY() + ", " + (int)GrabCommand.mc.player.getZ() + "]");
                    break;
                }
                case "name": {
                    this.copy(GrabCommand.mc.player.getName().getString());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else {
            this.messageSyntax();
        }
    }

    private void copy(String text) {
        GrabCommand.mc.keyboardHandler.setClipboard(text);
        Night.CHAT_MANAGER.tagged("Successfully copied " + String.valueOf(ChatUtils.getPrimary()) + text + String.valueOf(ChatUtils.getSecondary()) + " to your clipboard.", this.getTag(), this.getName());
    }
}

