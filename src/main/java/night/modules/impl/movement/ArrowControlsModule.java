/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.inventory.AnvilScreen
 *  net.minecraft.client.gui.screens.inventory.BookEditScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen
 *  net.minecraft.client.gui.screens.inventory.SignEditScreen
 *  net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.CreativeModeTab$Type
 */
package night.modules.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import night.events.SubscribeEvent;
import night.events.impl.RenderWorldEvent;
import night.mixins.accessors.CreativeInventoryScreenAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.NumberSetting;

@RegisterModule(name="ArrowControls", description="Allows you to control the camera using your arrow keys.", category=Module.Category.MOVEMENT)
public class ArrowControlsModule
extends Module {
    public NumberSetting speed = new NumberSetting("Speed", "The speed at which the camera will be moving at.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f));
    public BooleanSetting inventory = new BooleanSetting("Inventory", "Allows you to control the camera when a screen is opened.", true);
    private long lastTime;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (ArrowControlsModule.mc.player == null || ArrowControlsModule.mc.level == null) {
            return;
        }
        if (ArrowControlsModule.mc.gui.screen() != null && (!this.inventory.getValue() || ArrowControlsModule.mc.gui.screen() instanceof ChatScreen || ArrowControlsModule.mc.gui.screen() instanceof BookEditScreen || ArrowControlsModule.mc.gui.screen() instanceof SignEditScreen || ArrowControlsModule.mc.gui.screen() instanceof JigsawBlockEditScreen || ArrowControlsModule.mc.gui.screen() instanceof StructureBlockEditScreen || ArrowControlsModule.mc.gui.screen() instanceof AnvilScreen || ArrowControlsModule.mc.gui.screen() instanceof CreativeModeInventoryScreen && CreativeInventoryScreenAccessor.getSelectedTab().getType() == CreativeModeTab.Type.SEARCH)) {
            return;
        }
        float yaw = 0.0f;
        float pitch = 0.0f;
        float amount = (float)(System.currentTimeMillis() - this.lastTime) / 10.0f * this.speed.getValue().floatValue();
        this.lastTime = System.currentTimeMillis();
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)263)) {
            yaw -= amount;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)262)) {
            yaw += amount;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)265)) {
            pitch -= amount;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)264)) {
            pitch += amount;
        }
        ArrowControlsModule.mc.player.setYRot(ArrowControlsModule.mc.player.getYRot() + yaw);
        ArrowControlsModule.mc.player.setXRot(Mth.clamp((float)(ArrowControlsModule.mc.player.getXRot() + pitch), (float)-90.0f, (float)90.0f));
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.speed.getValue().floatValue());
    }
}

