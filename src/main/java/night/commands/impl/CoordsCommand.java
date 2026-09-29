/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="coords", tag="Coords", description="Copies your position, or whispers it to a player.", syntax="| <[player]>")
public class CoordsCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0 && mc.getConnection() != null) {
            String myName = CoordsCommand.mc.player != null ? CoordsCommand.mc.player.getGameProfile().name() : "";
            return mc.getConnection().getOnlinePlayers().stream().map(info -> info.getProfile().name()).filter(name -> name != null && !name.isEmpty() && !name.equalsIgnoreCase(myName)).distinct().toList();
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (CoordsCommand.mc.player == null || CoordsCommand.mc.level == null) {
            return;
        }
        String dimension = CoordsCommand.mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
        String coords = (int)CoordsCommand.mc.player.getX() + " " + (int)CoordsCommand.mc.player.getY() + " " + (int)CoordsCommand.mc.player.getZ() + " (" + dimension + ")";
        if (args.length == 0) {
            CoordsCommand.mc.keyboardHandler.setClipboard(coords);
            Night.CHAT_MANAGER.tagged("Copied your position (" + String.valueOf(ChatUtils.getPrimary()) + coords + String.valueOf(ChatUtils.getSecondary()) + ") to the clipboard.", this.getTag(), this.getName());
            return;
        }
        if (args.length == 1) {
            String target = args[0];
            if (mc.getConnection() != null) {
                mc.getConnection().sendCommand("w " + target + " " + coords);
                Night.CHAT_MANAGER.tagged("Sent your position to " + String.valueOf(ChatUtils.getPrimary()) + target + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
            }
            return;
        }
        this.messageSyntax();
    }
}

