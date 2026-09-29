/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.modules.impl.player.RekitModule;

@RegisterCommand(name="kit", tag="Kit", description="Manages Rekit's saved kits.", syntax="<save|load|delete> <name> | <list|active>")
public class KitCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("save", "load", "delete", "list", "active");
        }
        if (args.length == 1 && (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("delete"))) {
            return Night.MODULE_MANAGER.getModule(RekitModule.class).getKitNames();
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        RekitModule rekit = Night.MODULE_MANAGER.getModule(RekitModule.class);
        if (args.length == 0) {
            this.messageSyntax();
            return;
        }
        switch (args[0].toLowerCase()) {
            case "save": {
                if (args.length < 2) {
                    this.messageSyntax();
                    return;
                }
                rekit.saveKit(args[1]);
                break;
            }
            case "load": {
                if (args.length < 2) {
                    rekit.listKits();
                    break;
                }
                rekit.loadKit(args[1]);
                break;
            }
            case "delete": {
                if (args.length < 2) {
                    this.messageSyntax();
                    return;
                }
                rekit.deleteKit(args[1]);
                break;
            }
            case "active": {
                rekit.showActiveKit();
                break;
            }
            case "list": {
                rekit.listKits();
                break;
            }
            default: {
                this.messageSyntax();
            }
        }
    }
}

