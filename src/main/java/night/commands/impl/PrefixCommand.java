/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="prefix", tag="Prefix", description="Allows you to change the client's command prefix.", syntax="<new_prefix> | reset", aliases={"setprefix", "p"})
public class PrefixCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        return args.length == 0 ? List.of("reset") : List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reset")) {
                Night.COMMAND_MANAGER.setPrefix(".");
                try {
                    if (Night.CONFIG_MANAGER != null) {
                        Night.CONFIG_MANAGER.saveGeneral();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                Night.CHAT_MANAGER.tagged("Successfully reset the command's prefix back to it's default (.).", this.getTag(), this.getName());
            } else {
                if (args[0].length() > 3) {
                    Night.CHAT_MANAGER.tagged("The specified prefix is longer than the limit of 3 characters.", this.getTag(), this.getName());
                    return;
                }
                if (args[0].equalsIgnoreCase("/")) {
                    Night.CHAT_MANAGER.tagged("The specified prefix would interfere with vanilla Minecraft commands.", this.getTag(), this.getName());
                    return;
                }
                Night.COMMAND_MANAGER.setPrefix(args[0]);
                try {
                    if (Night.CONFIG_MANAGER != null) {
                        Night.CONFIG_MANAGER.saveGeneral();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                Night.CHAT_MANAGER.tagged("Successfully set the client's command prefix to " + String.valueOf(ChatUtils.getPrimary()) + args[0] + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
            }
        } else {
            this.messageSyntax();
        }
    }
}

