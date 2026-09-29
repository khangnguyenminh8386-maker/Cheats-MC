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

@RegisterCommand(name="drawn", tag="Drawn", description="Manages the modules that will be shown on the HUD's module list.", syntax="<true|false|list|reset> | <[module]> <true|false|reset>")
public class DrawnCommand
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
                    module.drawn.setValue(true);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    module.drawn.setValue(false);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "reset": {
                    module.drawn.setValue(module.drawn.getDefaultValue());
                    Night.CHAT_MANAGER.tagged("Successfully set the module's drawn status to it's default value.", this.getTag(), this.getName());
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
                        module.drawn.setValue(true);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.drawn.setValue(false);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's drawn status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "list": {
                    ArrayList<Module> drawnModules = new ArrayList<Module>(Night.MODULE_MANAGER.getModules().stream().filter(m -> m.drawn.getValue()).toList());
                    if (drawnModules.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("There are currently no modules with their drawn status set to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName() + "-list");
                        break;
                    }
                    StringBuilder modulesString = new StringBuilder();
                    int index = 0;
                    for (Module module : drawnModules) {
                        modulesString.append(ChatUtils.getSecondary()).append(module.getName()).append(index + 1 == drawnModules.size() ? "" : ", ");
                        ++index;
                    }
                    Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getSecondary()) + "Drawn Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + drawnModules.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(modulesString), this.getName() + "-list");
                    break;
                }
                case "reset": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.drawn.setValue(module.drawn.getDefaultValue());
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's drawn status to it's default value.", this.getTag(), this.getName());
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

