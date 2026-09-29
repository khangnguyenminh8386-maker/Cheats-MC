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

@RegisterCommand(name="chatnotify", tag="ChatNotify", description="Manages the toggle notification status of the client's modules.", syntax="<true|false|list|reset> | <[module]> <true|false|reset>")
public class ChatNotifyCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            ArrayList<String> names = new ArrayList<String>(this.moduleNames());
            names.add("true");
            names.add("false");
            names.add("list");
            names.add("reset");
            return names;
        }
        if (args.length == 1 && Night.MODULE_MANAGER.getModule(args[0]) != null) {
            return List.of("true", "false", "reset");
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
            switch (args[1]) {
                case "true": {
                    module.chatNotify.setValue(true);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    module.chatNotify.setValue(false);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "reset": {
                    module.chatNotify.setValue(module.chatNotify.getDefaultValue());
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to it's default value.", this.getTag(), this.getName());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (args.length == 1) {
            switch (args[0]) {
                case "true": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.chatNotify.setValue(true);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.chatNotify.setValue(false);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "list": {
                    ArrayList<Module> notifiableModules = new ArrayList<Module>(Night.MODULE_MANAGER.getModules().stream().filter(m -> m.chatNotify.getValue()).toList());
                    if (notifiableModules.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("There are currently no modules with their notification status set to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName() + "-list");
                        break;
                    }
                    StringBuilder modulesString = new StringBuilder();
                    int index = 0;
                    for (Module module : notifiableModules) {
                        modulesString.append(ChatUtils.getSecondary()).append(module.getName()).append(index + 1 == notifiableModules.size() ? "" : ", ");
                        ++index;
                    }
                    Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getSecondary()) + "Notifiable Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + notifiableModules.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(modulesString), this.getName() + "-list");
                    break;
                }
                case "reset": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.chatNotify.setValue(module.chatNotify.getDefaultValue());
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to it's default value.", this.getTag(), this.getName());
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

