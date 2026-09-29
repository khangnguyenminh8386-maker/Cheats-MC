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
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="toggle", tag="Toggle", description="Toggles a specified module or a setting on and off.", syntax="<[module]> | <[module]> <[setting]>", aliases={"t"})
public class ToggleCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return this.moduleNames();
        }
        if (args.length == 1) {
            Module module = Night.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                return List.of();
            }
            return module.getSettings().stream().filter(s -> s instanceof BooleanSetting).map(s -> s.getName().toLowerCase()).toList();
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1 || args.length == 2) {
            Module module = Night.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                Night.CHAT_MANAGER.tagged("Could not find the module specified.", this.getTag(), this.getName());
                return;
            }
            if (args.length == 1) {
                if (module.isPersistent()) {
                    Night.CHAT_MANAGER.tagged("Cannot toggle a persistent module.", this.getTag(), this.getName());
                    return;
                }
                module.setToggled(!module.isToggled(), false);
                Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + " has been toggled " + (module.isToggled() ? String.valueOf(ChatFormatting.GREEN) + "on" : String.valueOf(ChatFormatting.RED) + "off") + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName() + "-cmd-" + module.getName());
            }
            if (args.length == 2) {
                Setting setting = module.getSetting(args[1]);
                if (setting == null) {
                    Night.CHAT_MANAGER.tagged("Could not find the setting specified.", this.getTag(), this.getName());
                    return;
                }
                if (!(setting instanceof BooleanSetting)) {
                    Night.CHAT_MANAGER.tagged("This command only works for " + String.valueOf(ChatUtils.getPrimary()) + "boolean" + String.valueOf(ChatUtils.getSecondary()) + " settings.", this.getTag(), this.getName());
                    return;
                }
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                booleanSetting.setValue(!booleanSetting.getValue());
                Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + setting.getName() + String.valueOf(ChatUtils.getSecondary()) + " has been toggled " + (booleanSetting.getValue() ? String.valueOf(ChatFormatting.GREEN) + "on" : String.valueOf(ChatFormatting.RED) + "off") + String.valueOf(ChatUtils.getSecondary()) + " for " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName() + "-cmd-" + module.getName() + "-" + setting.getName());
            }
        } else {
            this.messageSyntax();
        }
    }
}

