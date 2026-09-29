/*
 * Decompiled with CFR 0.152.
 */
package night.commands.impl;

import night.commands.Command;
import night.commands.RegisterCommand;

@RegisterCommand(name="say", description="Sends a message through commands, allowing you to bypass the client's prefix and minecraft's command prefix.", syntax="<[message]>")
public class SayCommand
extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length >= 1) {
            StringBuilder fullString = new StringBuilder();
            int index = 0;
            for (String str : args) {
                fullString.append(str).append(index + 1 == args.length ? "" : " ");
                ++index;
            }
            mc.getConnection().sendChat(fullString.toString());
        } else {
            this.messageSyntax();
        }
    }
}

