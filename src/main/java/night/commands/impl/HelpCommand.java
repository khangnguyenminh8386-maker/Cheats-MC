/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.ArrayList;
import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="help", tag="Help", description="Shows you a list of all of the client's commands or information about a certain command.", syntax="empty | <[command]>", aliases={"cmds"})
public class HelpCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length != 0) {
            return List.of();
        }
        return Night.COMMAND_MANAGER.getCommands().stream().map(Command::getName).toList();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            Command command = Night.COMMAND_MANAGER.getCommand(args[0]);
            if (command == null) {
                Night.CHAT_MANAGER.tagged("Could not find the command specified.", this.getTag(), this.getName());
                return;
            }
            Night.CHAT_MANAGER.info(command.getTag() + String.valueOf(ChatUtils.getPrimary()) + " - " + String.valueOf(ChatUtils.getSecondary()) + command.getName() + " " + command.getSyntax());
            Night.CHAT_MANAGER.info(command.getDescription());
        } else if (args.length == 0) {
            ArrayList<Command> commands = Night.COMMAND_MANAGER.getCommands();
            if (commands.isEmpty()) {
                Night.CHAT_MANAGER.tagged("There are currently no registered commands.", this.getTag(), this.getName() + "-list");
            } else {
                StringBuilder builder = new StringBuilder();
                int index = 0;
                for (Command command : commands) {
                    builder.append(ChatUtils.getSecondary()).append(command.getName()).append(++index == commands.size() ? "" : ", ");
                }
                Night.CHAT_MANAGER.message("Commands " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + commands.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), this.getName() + "-list");
            }
        } else {
            this.messageSyntax();
        }
    }
}

