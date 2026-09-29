/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.ArrayList;
import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.modules.Module;
import night.utils.chat.ChatUtils;
import night.utils.input.KeyboardUtils;

@RegisterCommand(name="bind", tag="Bind", description="Changes the toggle keybind of a module.", syntax="<[module]> <[key]|reset> | <reset|list> ", aliases={"b", "key", "keybind"})
public class BindCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            ArrayList<String> names = new ArrayList<String>(this.moduleNames());
            names.add("reset");
            names.add("list");
            return names;
        }
        if (args.length == 1 && Night.MODULE_MANAGER.getModule(args[0]) != null) {
            return List.of("<bind>");
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            Module module = Night.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                Night.CHAT_MANAGER.tagged("Could not find the module specified.", this.getTag(), this.getName());
                return;
            }
            if (args[1].equalsIgnoreCase("reset")) {
                module.setBind(0);
                Night.CHAT_MANAGER.tagged("Successfully reset the toggle keybind of the " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + " module.", this.getTag(), this.getName());
            } else {
                module.setBind(KeyboardUtils.getKeyNumber(args[1]));
                Night.CHAT_MANAGER.tagged("Successfully bound the " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + " module to the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(module.getBind()) + String.valueOf(ChatUtils.getSecondary()) + " key.", this.getTag(), this.getName());
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reset": {
                    Night.MODULE_MANAGER.getModules().forEach(m -> m.bind.resetValue());
                    Night.CHAT_MANAGER.tagged("Successfully reset every module's toggle keybind.", this.getTag(), this.getName());
                    break;
                }
                case "list": {
                    List<Module> modules = Night.MODULE_MANAGER.getModules().stream().filter(m -> m.getBind() != 0).toList();
                    if (modules.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("There are currently no bound modules.", this.getTag(), this.getName() + "-list");
                        break;
                    }
                    StringBuilder builder = new StringBuilder();
                    int index = 0;
                    for (Module module : modules) {
                        builder.append(ChatUtils.getSecondary()).append(module.getName()).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(module.getBind()).toUpperCase()).append(ChatUtils.getPrimary()).append("]").append(++index == modules.size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Bound Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + modules.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), this.getName() + "-list");
                    break;
                }
                default: {
                    this.messageSyntax();
                }
            }
        } else {
            this.messageSyntax();
        }
    }
}

