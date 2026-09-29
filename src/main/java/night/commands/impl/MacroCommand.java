/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.Arrays;
import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.utils.chat.ChatUtils;
import night.utils.input.KeyboardUtils;

@RegisterCommand(name="macro", tag="Macro", description="Allows you to manage the client's macro system.", syntax="add <[key]> <[message]> | remove <[key]> | <clear|list>")
public class MacroCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("add", "remove", "clear", "list");
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("add")) {
                int key = KeyboardUtils.getKeyNumber(args[1]);
                if (key == 0) {
                    Night.CHAT_MANAGER.tagged("The keybind specified is not valid.", this.getTag());
                    return;
                }
                StringBuilder builder = new StringBuilder();
                String[] array = Arrays.copyOfRange(args, 2, args.length);
                int index = 0;
                for (String str : array) {
                    builder.append(str).append(++index == array.length ? "" : " ");
                }
                Night.MACRO_MANAGER.add(builder.toString(), key);
                Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + String.valueOf(builder) + String.valueOf(ChatUtils.getSecondary()) + " as a macro, bound to the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(key) + String.valueOf(ChatUtils.getSecondary()) + " key.", this.getTag(), "command-" + this.getName());
            } else {
                this.messageSyntax();
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                int key = KeyboardUtils.getKeyNumber(args[1]);
                if (key == 0) {
                    Night.CHAT_MANAGER.tagged("The keybind specified is not valid.", this.getTag());
                    return;
                }
                if (!Night.MACRO_MANAGER.containsValue(key)) {
                    Night.CHAT_MANAGER.tagged("There is no macro with the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(key) + String.valueOf(ChatUtils.getSecondary()) + " key.", this.getTag(), "command-" + this.getName());
                } else {
                    Night.CHAT_MANAGER.tagged("Successfully removed the " + String.valueOf(ChatUtils.getPrimary()) + Night.MACRO_MANAGER.getKey(key) + String.valueOf(ChatUtils.getSecondary()) + " macro.", this.getTag(), "command-" + this.getName());
                    Night.MACRO_MANAGER.remove(Night.MACRO_MANAGER.getKey(key));
                }
            } else {
                this.messageSyntax();
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                Night.MACRO_MANAGER.clear();
                Night.CHAT_MANAGER.tagged("Successfully removed all macros.", this.getTag(), "command-" + this.getName());
            } else if (args[0].equalsIgnoreCase("list")) {
                if (Night.MACRO_MANAGER.getMacros().isEmpty()) {
                    Night.CHAT_MANAGER.tagged("There are currently no macros added.", this.getTag(), "command-" + this.getName() + "-list");
                } else {
                    StringBuilder builder = new StringBuilder();
                    int index = 0;
                    for (String message : Night.MACRO_MANAGER.getMacros().keySet()) {
                        int key = Night.MACRO_MANAGER.getValue(message);
                        builder.append(ChatUtils.getSecondary()).append(message).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(key)).append(ChatUtils.getPrimary()).append("]").append(ChatUtils.getSecondary()).append(++index == Night.MACRO_MANAGER.getMacros().size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Macros " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + Night.MACRO_MANAGER.getMacros().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), "command-" + this.getName() + "-list");
                }
            } else {
                this.messageSyntax();
            }
        } else {
            this.messageSyntax();
        }
    }
}

