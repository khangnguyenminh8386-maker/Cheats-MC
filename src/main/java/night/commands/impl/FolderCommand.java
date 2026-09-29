/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Util
 */
package night.commands.impl;

import java.io.File;
import net.minecraft.util.Util;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;

@RegisterCommand(name="folder", description="Opens the clients folder.")
public class FolderCommand
extends Command {
    @Override
    public void execute(String[] args) {
        File folder = new File("Cheats MC");
        if (folder.exists()) {
            Util.getPlatform().openFile(folder);
        } else {
            Night.CHAT_MANAGER.info("Could not find the client's configuration folder.");
        }
    }
}

