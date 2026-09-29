/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 */
package night.commands.impl;

import java.util.List;
import net.minecraft.ChatFormatting;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.modules.Module;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="modules", tag="Modules", description="Shows you a list of all of the client's modules and their toggle status.", aliases={"mods"})
public class ModulesCommand
extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            List<Module> modules = Night.MODULE_MANAGER.getModules();
            if (modules.isEmpty()) {
                Night.CHAT_MANAGER.tagged("There are currently no registered modules.", this.getTag(), this.getName());
            } else {
                StringBuilder builder = new StringBuilder();
                int index = 0;
                for (Module module : modules) {
                    builder.append(ChatUtils.getSecondary()).append(module.getName()).append(ChatUtils.getPrimary()).append(" [").append(module.isToggled() ? String.valueOf(ChatFormatting.GREEN) + "ON" : String.valueOf(ChatFormatting.RED) + "OFF").append(ChatUtils.getPrimary()).append("]").append(++index == modules.size() ? "" : ", ");
                }
                Night.CHAT_MANAGER.message("Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + modules.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), this.getName());
            }
        } else {
            this.messageSyntax();
        }
    }
}

