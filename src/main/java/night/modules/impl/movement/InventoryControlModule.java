/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.inventory.AnvilScreen
 *  net.minecraft.client.gui.screens.inventory.BookEditScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen
 *  net.minecraft.client.gui.screens.inventory.SignEditScreen
 *  net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen
 *  net.minecraft.world.item.CreativeModeTab$Type
 */
package night.modules.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.world.item.CreativeModeTab;
import night.events.SubscribeEvent;
import night.events.impl.TickEvent;
import night.mixins.accessors.CreativeInventoryScreenAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;

@RegisterModule(name="InventoryControl", description="Allows you to control things such as movement and your camera while a GUI is open.", category=Module.Category.MOVEMENT)
public class InventoryControlModule
extends Module {
    public BooleanSetting movement = new BooleanSetting("Movement", "Allows you to control movement.", true);
    public BooleanSetting portals = new BooleanSetting("Portals", "Allows you to interact with GUIs while inside of a portal.", true);
    public BooleanSetting dragClick = new BooleanSetting("DragClick", "Hold shift + left-click and drag over slots to move each item you pass over.", true);
    public BooleanSetting grimV2 = new BooleanSetting("GrimV2", "Bypasses inventory-while-moving checks on GrimV2/NCP servers.", false);

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (InventoryControlModule.mc.player == null) {
            return;
        }
        if (!(!this.movement.getValue() || InventoryControlModule.mc.gui.screen() == null || InventoryControlModule.mc.gui.screen() instanceof ChatScreen || InventoryControlModule.mc.gui.screen() instanceof BookEditScreen || InventoryControlModule.mc.gui.screen() instanceof SignEditScreen || InventoryControlModule.mc.gui.screen() instanceof JigsawBlockEditScreen || InventoryControlModule.mc.gui.screen() instanceof StructureBlockEditScreen || InventoryControlModule.mc.gui.screen() instanceof AnvilScreen || InventoryControlModule.mc.gui.screen() instanceof CreativeModeInventoryScreen && CreativeInventoryScreenAccessor.getSelectedTab().getType() == CreativeModeTab.Type.SEARCH)) {
            for (KeyMapping binding : new KeyMapping[]{InventoryControlModule.mc.options.keyUp, InventoryControlModule.mc.options.keyDown, InventoryControlModule.mc.options.keyRight, InventoryControlModule.mc.options.keyLeft, InventoryControlModule.mc.options.keySprint, InventoryControlModule.mc.options.keyShift, InventoryControlModule.mc.options.keyJump}) {
                binding.setDown(InputConstants.isKeyDown((Window)mc.getWindow(), (int)InputConstants.getKey((String)binding.saveString()).getValue()));
            }
        }
    }
}

