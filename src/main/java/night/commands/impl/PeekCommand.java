/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 */
package night.commands.impl;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import night.Night;
import night.commands.Command;
import night.commands.RegisterCommand;
import night.gui.PeekMenu;
import night.gui.PeekScreen;
import night.utils.minecraft.EChestMemory;

@RegisterCommand(name="peek", tag="Peek", description="Peeks a shulker box held in hand, or your last-opened ender chest, without opening it.", syntax="")
public class PeekCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (PeekCommand.mc.player == null) {
            return;
        }
        EChestMemory.init();
        ItemStack stack = PeekCommand.mc.player.getMainHandItem();
        ItemContainerContents contents = (ItemContainerContents)stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            stack = PeekCommand.mc.player.getOffhandItem();
            contents = (ItemContainerContents)stack.get(DataComponents.CONTAINER);
        }
        if (contents != null) {
            NonNullList items = NonNullList.withSize((int)27, (Object)ItemStack.EMPTY);
            contents.copyInto(items);
            this.openPreview((NonNullList<ItemStack>)items, stack.getHoverName());
            return;
        }
        if (EChestMemory.hasItems()) {
            this.openPreview(EChestMemory.getItems(), (Component)Component.literal((String)"Ender Chest (remembered)"));
            return;
        }
        Night.CHAT_MANAGER.tagged("Hold a shulker box to peek it, or open your ender chest once so it can be remembered.", this.getTag(), this.getName());
    }

    private void openPreview(NonNullList<ItemStack> items, Component title) {
        SimpleContainer preview = new SimpleContainer(27);
        for (int i = 0; i < 27 && i < items.size(); ++i) {
            preview.setItem(i, ((ItemStack)items.get(i)).copy());
        }
        mc.execute(() -> {
            PeekMenu menu = new PeekMenu(0, PeekCommand.mc.player.getInventory(), (Container)preview);
            PeekCommand.mc.player.containerMenu = menu;
            PeekCommand.mc.gui.setScreen((Screen)new PeekScreen(menu, PeekCommand.mc.player.getInventory(), title));
        });
    }
}

