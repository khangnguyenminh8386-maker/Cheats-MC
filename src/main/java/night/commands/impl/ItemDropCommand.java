/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package night.commands.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import night.commands.Command;
import night.commands.RegisterCommand;

@RegisterCommand(name="itemdrop", aliases={"drop"}, tag="ItemDrop", description="Drops items from your inventory.", syntax="<all|item name>")
public class ItemDropCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            ArrayList<String> options = new ArrayList<String>(List.of("all"));
            for (Item item : BuiltInRegistries.ITEM) {
                options.add(BuiltInRegistries.ITEM.getKey(item).getPath());
            }
            return options;
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            this.messageSyntax();
            return;
        }
        if (args[0].equalsIgnoreCase("all")) {
            this.dropAllItems();
            return;
        }
        String itemName = String.join((CharSequence)" ", args);
        this.dropSpecificItem(itemName);
    }

    private void dropAllItems() {
        if (ItemDropCommand.mc.player == null || ItemDropCommand.mc.gameMode == null) {
            return;
        }
        int containerId = ItemDropCommand.mc.player.containerMenu.containerId;
        for (int invSlot = 0; invSlot < 36; ++invSlot) {
            ItemStack stack = ItemDropCommand.mc.player.getInventory().getItem(invSlot);
            if (stack.isEmpty()) continue;
            ItemDropCommand.mc.gameMode.handleContainerInput(containerId, ItemDropCommand.invToHandlerSlot(invSlot), 1, ContainerInput.THROW, (Player)ItemDropCommand.mc.player);
        }
    }

    private void dropSpecificItem(String itemName) {
        if (ItemDropCommand.mc.player == null || ItemDropCommand.mc.gameMode == null) {
            return;
        }
        int containerId = ItemDropCommand.mc.player.containerMenu.containerId;
        for (int invSlot = 0; invSlot < 36; ++invSlot) {
            ItemStack stack = ItemDropCommand.mc.player.getInventory().getItem(invSlot);
            if (stack.isEmpty()) continue;
            String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String customName = stack.getHoverName().getString();
            if (!key.equalsIgnoreCase(itemName) && !key.substring(key.indexOf(58) + 1).equalsIgnoreCase(itemName) && !customName.equalsIgnoreCase(itemName)) continue;
            ItemDropCommand.mc.gameMode.handleContainerInput(containerId, ItemDropCommand.invToHandlerSlot(invSlot), 1, ContainerInput.THROW, (Player)ItemDropCommand.mc.player);
        }
    }

    private static int invToHandlerSlot(int invSlot) {
        return invSlot <= 8 ? 36 + invSlot : invSlot;
    }
}

