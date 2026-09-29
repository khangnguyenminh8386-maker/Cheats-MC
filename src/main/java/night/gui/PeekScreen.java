/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.ChestMenu
 */
package night.gui;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import night.gui.PeekMenu;

public class PeekScreen
extends ContainerScreen {
    public PeekScreen(PeekMenu menu, Inventory playerInventory, Component title) {
        super((ChestMenu)menu, playerInventory, title);
    }
}

