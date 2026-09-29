/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package night.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.settings.impl.StringSetting;
import night.utils.graphics.Renderer2D;

public class StringButton
extends Button {
    private final StringSetting setting;
    private String currentString = "";
    private boolean listening = false;
    private boolean selecting = false;
    private int cursorIndex = 0;

    public StringButton(StringSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 100));
        Object displayText = this.currentString;
        if (this.listening) {
            if (this.selecting) {
                displayText = this.currentString;
            } else {
                int clampedIndex = Math.max(0, Math.min(this.cursorIndex, this.currentString.length()));
                String before = this.currentString.substring(0, clampedIndex);
                String after = this.currentString.substring(clampedIndex);
                String cursorChar = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                displayText = before + cursorChar + after;
            }
        } else {
            displayText = this.setting.getTag() + " " + String.valueOf(ChatFormatting.GRAY) + this.setting.getValue();
        }
        Night.FONT_MANAGER.drawTextWithShadow(context, (String)displayText, this.getX() + this.getTextPadding() + 1, this.getY() + 2, this.selecting ? ClickGuiScreen.getButtonColor(this.getY(), 255) : Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isHovering(mouseX, mouseY) && !this.listening) {
                this.listening = true;
                this.currentString = this.setting.getValue();
                this.cursorIndex = this.currentString.length();
                this.selecting = false;
                this.playClickSound();
            } else if (!this.isHovering(mouseX, mouseY)) {
                this.listening = false;
                this.selecting = false;
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.listening) {
            return;
        }
        Window handle = mc.getWindow();
        boolean ctrl = InputConstants.isKeyDown((Window)handle, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (keyCode == 256) {
            this.selecting = false;
            return;
        }
        if (keyCode == 257) {
            this.setting.setValue(this.currentString);
            this.selecting = false;
            this.listening = false;
            return;
        }
        if (keyCode == 263 && !this.currentString.isEmpty()) {
            if (this.selecting) {
                this.selecting = false;
                this.cursorIndex = 0;
            } else if (this.cursorIndex > 0) {
                --this.cursorIndex;
            }
            return;
        }
        if (keyCode == 262 && !this.currentString.isEmpty()) {
            if (this.selecting) {
                this.selecting = false;
                this.cursorIndex = this.currentString.length();
            } else if (this.cursorIndex < this.currentString.length()) {
                ++this.cursorIndex;
            }
            return;
        }
        if (keyCode == 259) {
            if (this.selecting) {
                this.currentString = "";
                this.cursorIndex = 0;
                this.selecting = false;
            } else if (this.cursorIndex > 0) {
                this.currentString = this.currentString.substring(0, this.cursorIndex - 1) + this.currentString.substring(this.cursorIndex);
                --this.cursorIndex;
            }
            return;
        }
        if (keyCode == 261) {
            if (this.selecting) {
                this.currentString = "";
                this.cursorIndex = 0;
                this.selecting = false;
            } else if (this.cursorIndex < this.currentString.length()) {
                this.currentString = this.currentString.substring(0, this.cursorIndex) + this.currentString.substring(this.cursorIndex + 1);
            }
            return;
        }
        if (ctrl) {
            if (keyCode == 86) {
                try {
                    String clipboard = StringButton.mc.keyboardHandler.getClipboard();
                    if (this.selecting) {
                        this.currentString = clipboard;
                        this.cursorIndex = this.currentString.length();
                        this.selecting = false;
                    } else {
                        this.currentString = this.currentString.substring(0, this.cursorIndex) + clipboard + this.currentString.substring(this.cursorIndex);
                        this.cursorIndex += clipboard.length();
                    }
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard paste", (Object)exception.getClass().getName(), (Object)exception);
                }
                return;
            }
            if (keyCode == 67 && this.selecting) {
                try {
                    StringButton.mc.keyboardHandler.setClipboard(this.currentString);
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard change", (Object)exception.getClass().getName(), (Object)exception);
                }
                return;
            }
            if (keyCode == 65 && !this.currentString.isEmpty()) {
                this.selecting = true;
                this.cursorIndex = this.currentString.length();
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (!this.listening) {
            return;
        }
        if (Character.isISOControl(chr)) {
            return;
        }
        if (this.selecting) {
            this.currentString = String.valueOf(chr);
            this.cursorIndex = 1;
            this.selecting = false;
        } else {
            this.currentString = this.currentString.substring(0, this.cursorIndex) + chr + this.currentString.substring(this.cursorIndex);
            ++this.cursorIndex;
        }
    }
}

