/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.sounds.SoundEvents
 */
package night.gui.api;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import night.Night;
import night.gui.api.Frame;
import night.modules.impl.core.ClickGuiModule;
import night.settings.Setting;
import night.utils.IMinecraft;

public class Button
implements IMinecraft {
    private final Setting setting;
    private final Frame parent;
    private int x;
    private int y;
    private int height;
    private int padding = 2;
    private int textPadding = 5;
    private final String description;
    private boolean visible = true;

    public Button(Frame parent, int height, String description) {
        this.setting = null;
        this.parent = parent;
        this.height = height;
        this.description = description;
    }

    public Button(Setting setting, Frame parent, int height, String description) {
        this.setting = setting;
        this.parent = parent;
        this.height = height;
        this.description = description;
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    }

    public void charTyped(char chr, int modifiers) {
    }

    public int getWidth() {
        return this.parent.getWidth();
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return (double)(this.x + this.padding) <= mouseX && (double)this.y <= mouseY && (double)(this.x + this.getWidth() - this.padding) > mouseX && (double)(this.y + this.getHeight()) > mouseY;
    }

    public void playClickSound() {
        if (Night.MODULE_MANAGER.getModule(ClickGuiModule.class).sounds.getValue()) {
            mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        }
    }

    @Generated
    public Setting getSetting() {
        return this.setting;
    }

    @Generated
    public Frame getParent() {
        return this.parent;
    }

    @Generated
    public int getX() {
        return this.x;
    }

    @Generated
    public int getY() {
        return this.y;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public int getPadding() {
        return this.padding;
    }

    @Generated
    public int getTextPadding() {
        return this.textPadding;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Generated
    public void setX(int x) {
        this.x = x;
    }

    @Generated
    public void setY(int y) {
        this.y = y;
    }

    @Generated
    public void setHeight(int height) {
        this.height = height;
    }

    @Generated
    public void setPadding(int padding) {
        this.padding = padding;
    }

    @Generated
    public void setTextPadding(int textPadding) {
        this.textPadding = textPadding;
    }

    @Generated
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

