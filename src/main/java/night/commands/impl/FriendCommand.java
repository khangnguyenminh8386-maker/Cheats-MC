/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import java.util.ArrayList;
import java.util.List;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.modules.impl.core.FriendModule;
import night.utils.chat.ChatUtils;

@RegisterCommand(name="friend", tag="Friend", description="Allows you to manage the client's friend list.", syntax="<add|del> <[player]> | <clear|list>", aliases={"f", "friends"})
public class FriendCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("add", "del", "clear", "list");
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("del")) {
            return Night.FRIEND_MANAGER.getFriends();
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("add") && FriendCommand.mc.level != null && FriendCommand.mc.player != null) {
            return FriendCommand.mc.level.players().stream().filter(player -> player != FriendCommand.mc.player).map(player -> player.getName().getString()).filter(name -> !Night.FRIEND_MANAGER.contains((String)name)).toList();
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add": {
                    if (!Night.FRIEND_MANAGER.contains(args[1])) {
                        if (Night.MODULE_MANAGER.getModule(FriendModule.class).friendMessage.getValue()) {
                            Night.FRIEND_MANAGER.sendFriendMessage(args[1]);
                        }
                        Night.FRIEND_MANAGER.add(args[1]);
                        Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " to your friends list.", this.getTag(), this.getName());
                        break;
                    }
                    Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " is already on your friends list.", this.getTag(), this.getName());
                    break;
                }
                case "del": {
                    if (Night.FRIEND_MANAGER.contains(args[1])) {
                        Night.FRIEND_MANAGER.remove(args[1]);
                        Night.CHAT_MANAGER.tagged("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " from your friends list.", this.getTag(), this.getName());
                        break;
                    }
                    Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " is not on your friends list.", this.getTag(), this.getName());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "clear": {
                    Night.FRIEND_MANAGER.clear();
                    Night.CHAT_MANAGER.tagged("Successfully cleared your friends list.", this.getTag(), this.getName() + "-list");
                    break;
                }
                case "list": {
                    ArrayList<String> friends = Night.FRIEND_MANAGER.getFriends();
                    if (friends.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("You currently have no friends.", this.getTag());
                        break;
                    }
                    StringBuilder builder = new StringBuilder();
                    int index = 0;
                    for (String name : friends) {
                        builder.append(ChatUtils.getSecondary()).append(name).append(++index == friends.size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Friends " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + friends.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), this.getName() + "-list");
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

