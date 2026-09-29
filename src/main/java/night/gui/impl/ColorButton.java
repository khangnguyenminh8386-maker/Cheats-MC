/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package night.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.modules.impl.core.ColorModule;
import night.settings.impl.ColorSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer2D;

public class ColorButton
extends Button {
    private final ColorSetting setting;
    private boolean open = false;
    private boolean hoveringHex = false;
    private boolean hoveringHue = false;
    private boolean hoveringColor = false;
    private boolean hoveringAlpha = false;
    private boolean hoveringCopy = false;
    private boolean hoveringPaste = false;
    private boolean hoveringSync = false;
    private boolean hoveringRainbow = false;
    private boolean draggingHue = false;
    private boolean draggingColor = false;
    private boolean draggingAlpha = false;
    private boolean listeningHex = false;
    private String hexInput = "";
    private int hexCursor = 0;
    private int colorWidth = 84;
    private int totalHeight = 0;
    private float[] hsb;

    public ColorButton(ColorSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
        this.hsb = Color.RGBtoHSB(setting.getColor().getRed(), setting.getColor().getGreen(), setting.getColor().getBlue(), null);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!(this.draggingHue || this.draggingColor || this.listeningHex)) {
            Color cur = this.setting.getValue().getColor();
            this.hsb = Color.RGBtoHSB(cur.getRed(), cur.getGreen(), cur.getBlue(), null);
        }
        Color outlineColor = Color.BLACK;
        Color realColor = Color.getHSBColor(this.hsb[0], 1.0f, 1.0f);
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        Night.FONT_MANAGER.drawTextWithShadow(context, this.setting.getTag(), this.getX() + this.getTextPadding() + 3, this.getY() + 2, Color.WHITE);
        Renderer2D.renderQuad(context, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + 2, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 3, ColorUtils.getColor(this.setting.getColor(), 255));
        Renderer2D.renderOutline(context, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + 2, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 3, outlineColor);
        if (this.open) {
            Color updated;
            String cursor;
            int offset = this.getParent().getHeight();
            int hexBoxHeight = 13;
            int hexLeft = this.getX() + this.getPadding() + 1;
            int hexRight = this.getX() + this.getWidth() - this.getPadding() - 1;
            int hexTop = this.getY() + offset;
            int hexBottom = hexTop + hexBoxHeight;
            Renderer2D.renderCheckerboard(context, hexLeft, hexTop, hexRight, hexBottom, 4.0f, new Color(210, 210, 210), Color.WHITE);
            Renderer2D.renderOutline(context, hexLeft, hexTop, hexRight, hexBottom, outlineColor);
            String string = cursor = Night.CLICK_GUI.isShowLine() ? "|" : "";
            if (this.listeningHex) {
                if (this.hexInput.isEmpty()) {
                    String placeholder = this.toHex(this.setting.getValue().getColor(), this.setting.getAlpha());
                    Night.FONT_MANAGER.drawText(context, "Hex: " + cursor, hexLeft + 3, hexTop + 2, Color.BLACK);
                    int prefixW = Night.FONT_MANAGER.getWidth("Hex: ");
                    Night.FONT_MANAGER.drawText(context, placeholder, hexLeft + 3 + prefixW, hexTop + 2, new Color(130, 130, 130));
                } else {
                    int clamped = Math.max(0, Math.min(this.hexCursor, this.hexInput.length()));
                    String before = this.hexInput.substring(0, clamped);
                    String after = this.hexInput.substring(clamped);
                    String displayHex = "Hex: " + before + cursor + after;
                    Night.FONT_MANAGER.drawText(context, displayHex, hexLeft + 3, hexTop + 2, Color.BLACK);
                }
            } else {
                String displayHex = "Hex: " + this.toHex(this.setting.getValue().getColor(), this.setting.getAlpha());
                Night.FONT_MANAGER.drawText(context, displayHex, hexLeft + 3, hexTop + 2, Color.BLACK);
            }
            this.hoveringHex = this.isHoveringComponent(mouseX, mouseY, hexLeft, hexTop, hexRight, hexBottom);
            int dragX = Mth.clamp((int)(mouseX - this.getX() - this.getPadding() - 1), (int)0, (int)this.colorWidth);
            int dragY = Mth.clamp((int)(mouseY - this.getY() - (offset += hexBoxHeight + 2)), (int)0, (int)this.colorWidth);
            float dragHue = (float)this.colorWidth * this.hsb[0];
            float dragSaturation = (float)this.colorWidth * this.hsb[1];
            float dragBrightness = (float)this.colorWidth * (1.0f - this.hsb[2]);
            float dragAlpha = (float)this.colorWidth * ((float)this.setting.getAlpha() / 255.0f);
            for (float i = 0.0f; i < (float)this.colorWidth; i += 0.5f) {
                Renderer2D.renderQuad(context, this.getX() + this.getWidth() - this.getPadding() - 9, (float)(this.getY() + offset) + i, this.getX() + this.getWidth() - this.getPadding() - 1, (float)(this.getY() + offset) + i + 0.5f, Color.getHSBColor(i / (float)this.colorWidth, 1.0f, 1.0f));
            }
            Renderer2D.renderOutline(context, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.colorWidth, outlineColor);
            this.hoveringHue = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.colorWidth);
            Renderer2D.renderQuad(context, this.getX() + this.getWidth() - this.getPadding() - 10, (float)(this.getY() + offset) + dragHue - 1.5f, this.getX() + this.getWidth() - this.getPadding(), (float)(this.getY() + offset) + dragHue + 1.5f, outlineColor);
            Renderer2D.renderQuad(context, this.getX() + this.getWidth() - this.getPadding() - 9, (float)(this.getY() + offset) + dragHue - 0.5f, this.getX() + this.getWidth() - this.getPadding() - 1, (float)(this.getY() + offset) + dragHue + 0.5f, Color.WHITE);
            if (this.draggingHue) {
                this.setting.setSync(false);
                this.hsb[0] = Math.clamp((float)dragY / (float)this.colorWidth, 0.0f, 1.0f);
                Color rgb = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                updated = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), this.setting.getAlpha());
                this.setting.setColor(updated);
            }
            Renderer2D.renderSidewaysGradient(context, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offset + this.colorWidth, Color.WHITE, realColor);
            Renderer2D.renderGradient(context, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offset + this.colorWidth, new Color(0, 0, 0, 0), Color.BLACK);
            Renderer2D.renderOutline(context, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offset + this.colorWidth, outlineColor);
            this.hoveringColor = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offset + this.colorWidth);
            Renderer2D.renderQuad(context, (float)(this.getX() + this.getPadding() + 1) + dragSaturation - 1.5f, (float)(this.getY() + offset) + dragBrightness - 1.5f, (float)(this.getX() + this.getPadding() + 1) + dragSaturation + 1.5f, (float)(this.getY() + offset) + dragBrightness + 1.5f, outlineColor);
            Renderer2D.renderQuad(context, (float)(this.getX() + this.getPadding() + 1) + dragSaturation - 0.5f, (float)(this.getY() + offset) + dragBrightness - 0.5f, (float)(this.getX() + this.getPadding() + 1) + dragSaturation + 0.5f, (float)(this.getY() + offset) + dragBrightness + 0.5f, Color.WHITE);
            if (this.draggingColor) {
                this.setting.setSync(false);
                this.hsb[1] = Math.clamp((float)dragX / (float)this.colorWidth, 0.0f, 1.0f);
                this.hsb[2] = Math.clamp(1.0f - (float)dragY / (float)this.colorWidth, 0.0f, 1.0f);
                Color rgb = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                updated = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), this.setting.getAlpha());
                this.setting.setColor(updated);
            }
            Renderer2D.renderSidewaysGradient(context, this.getX() + this.getPadding() + 1, this.getY() + (offset += this.colorWidth + 2), this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offset + 8, Color.BLACK, ColorUtils.getColor(this.setting.getColor(), 255));
            Renderer2D.renderOutline(context, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offset + 8, outlineColor);
            this.hoveringAlpha = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offset + 8);
            Renderer2D.renderQuad(context, (float)(this.getX() + this.getPadding() + 1) + dragAlpha - 1.5f, this.getY() + offset - 1, (float)(this.getX() + this.getPadding() + 1) + dragAlpha + 1.5f, this.getY() + offset + 9, outlineColor);
            Renderer2D.renderQuad(context, (float)(this.getX() + this.getPadding() + 1) + dragAlpha - 0.5f, this.getY() + offset, (float)(this.getX() + this.getPadding() + 1) + dragAlpha + 0.5f, this.getY() + offset + 8, Color.WHITE);
            if (this.draggingAlpha) {
                int newAlpha = Math.clamp((long)((int)(255.0f * (float)dragX / (float)this.colorWidth)), 0, 255);
                Color cur = this.setting.getValue().getColor();
                Color updated2 = new Color(cur.getRed(), cur.getGreen(), cur.getBlue(), newAlpha);
                this.setting.setColor(updated2);
            }
            Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY() + (offset += 10), (float)this.getX() + (float)this.getWidth() / 2.0f - 0.5f, this.getY() + offset + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            Night.FONT_MANAGER.drawTextWithShadow(context, "Copy", this.getX() + this.getPadding() + this.getWidth() / 4 - 1 - Night.FONT_MANAGER.getWidth("Copy") / 2, this.getY() + offset + 2, Color.WHITE);
            this.hoveringCopy = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offset, (float)this.getX() + (float)this.getWidth() / 2.0f - 0.5f, this.getY() + offset + this.getParent().getHeight());
            Renderer2D.renderQuad(context, (float)this.getX() + (float)this.getWidth() / 2.0f + 0.5f, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            Night.FONT_MANAGER.drawTextWithShadow(context, "Paste", this.getX() + this.getWidth() / 2 + this.getWidth() / 4 - 1 - Night.FONT_MANAGER.getWidth("Paste") / 2, this.getY() + offset + 2, Color.WHITE);
            this.hoveringPaste = this.isHoveringComponent(mouseX, mouseY, (float)this.getX() + (float)this.getWidth() / 2.0f + 0.5f, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.getParent().getHeight());
            offset += this.getParent().getHeight() + 1;
            if (this.hasSync()) {
                if (this.setting.isSync()) {
                    Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
                }
                Night.FONT_MANAGER.drawTextWithShadow(context, "Sync", this.getX() + this.getWidth() / 2 - Night.FONT_MANAGER.getWidth("Sync") / 2, this.getY() + offset + 2, Color.WHITE);
                this.hoveringSync = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.getParent().getHeight());
                offset += this.getParent().getHeight() + 1;
            } else {
                this.hoveringSync = false;
            }
            if (this.setting.isRainbow()) {
                Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            }
            Night.FONT_MANAGER.drawTextWithShadow(context, "Rainbow", this.getX() + this.getWidth() / 2 - Night.FONT_MANAGER.getWidth("Rainbow") / 2, this.getY() + offset + 2, Color.WHITE);
            this.hoveringRainbow = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offset, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offset + this.getParent().getHeight());
        }
    }

    private String toHex(Color c, int alpha) {
        return String.format("%02X%02X%02X%02X", alpha, c.getRed(), c.getGreen(), c.getBlue());
    }

    private Color parseHex(String hex) {
        if (hex == null) {
            return null;
        }
        String clean = hex.trim().replace("#", "");
        try {
            if (clean.length() == 6) {
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                return new Color(r, g, b, this.setting.getAlpha());
            }
            if (clean.length() == 8) {
                int a = Integer.parseInt(clean.substring(0, 2), 16);
                int r = Integer.parseInt(clean.substring(2, 4), 16);
                int g = Integer.parseInt(clean.substring(4, 6), 16);
                int b = Integer.parseInt(clean.substring(6, 8), 16);
                return new Color(r, g, b, a);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private void applyHexLive() {
        if (this.hexInput == null || this.hexInput.isEmpty()) {
            return;
        }
        Color parsed = this.parseHex(this.hexInput);
        if (parsed != null) {
            this.setting.setSync(false);
            this.setting.setColor(parsed);
            this.hsb = Color.RGBtoHSB(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), null);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.listeningHex) {
            return;
        }
        Window handle = mc.getWindow();
        boolean ctrl = InputConstants.isKeyDown((Window)handle, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (keyCode == 256 || keyCode == 257) {
            this.applyHexLive();
            this.listeningHex = false;
            this.hexInput = "";
            return;
        }
        if (keyCode == 263) {
            if (this.hexCursor > 0) {
                --this.hexCursor;
            }
            return;
        }
        if (keyCode == 262) {
            if (this.hexCursor < this.hexInput.length()) {
                ++this.hexCursor;
            }
            return;
        }
        if (keyCode == 259) {
            if (this.hexCursor > 0 && !this.hexInput.isEmpty()) {
                this.hexInput = this.hexInput.substring(0, this.hexCursor - 1) + this.hexInput.substring(this.hexCursor);
                --this.hexCursor;
                this.applyHexLive();
            }
            return;
        }
        if (keyCode == 261) {
            if (this.hexCursor < this.hexInput.length()) {
                this.hexInput = this.hexInput.substring(0, this.hexCursor) + this.hexInput.substring(this.hexCursor + 1);
                this.applyHexLive();
            }
            return;
        }
        if (ctrl) {
            if (keyCode == 86) {
                try {
                    String clip = ColorButton.mc.keyboardHandler.getClipboard();
                    if (clip != null) {
                        clip = clip.trim().replace("#", "").toUpperCase();
                        StringBuilder valid = new StringBuilder();
                        for (char ch : clip.toCharArray()) {
                            if ((ch < '0' || ch > '9') && (ch < 'A' || ch > 'F')) continue;
                            valid.append(ch);
                        }
                        String toInsert = valid.toString();
                        if (this.hexInput.length() + toInsert.length() <= 8) {
                            this.hexInput = this.hexInput.substring(0, this.hexCursor) + toInsert + this.hexInput.substring(this.hexCursor);
                            this.hexCursor += toInsert.length();
                        } else {
                            this.hexInput = toInsert.substring(0, Math.min(8, toInsert.length()));
                            this.hexCursor = this.hexInput.length();
                        }
                        this.applyHexLive();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
            if (keyCode == 67) {
                try {
                    ColorButton.mc.keyboardHandler.setClipboard(this.hexInput);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (!this.listeningHex) {
            return;
        }
        char upper = Character.toUpperCase(chr);
        if ((upper >= '0' && upper <= '9' || upper >= 'A' && upper <= 'F') && this.hexInput.length() < 8) {
            this.hexInput = this.hexInput.substring(0, this.hexCursor) + upper + this.hexInput.substring(this.hexCursor);
            ++this.hexCursor;
            this.applyHexLive();
        }
    }

    private boolean hasSync() {
        ColorModule colorModule = Night.MODULE_MANAGER.getModule(ColorModule.class);
        return colorModule == null || this.setting != colorModule.color;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 1) {
            boolean bl = this.open = !this.open;
            if (this.listeningHex) {
                this.applyHexLive();
                this.listeningHex = false;
                this.hexInput = "";
            }
            this.playClickSound();
        }
        if (button == 0) {
            if (this.open && this.hoveringHex) {
                this.listeningHex = true;
                this.hexInput = "";
                this.hexCursor = 0;
                this.playClickSound();
                return;
            }
            if (this.listeningHex) {
                this.applyHexLive();
                this.listeningHex = false;
                this.hexInput = "";
            }
            if (this.hoveringHue) {
                this.draggingHue = true;
            }
            if (this.hoveringColor) {
                this.draggingColor = true;
            }
            if (this.hoveringAlpha) {
                this.draggingAlpha = true;
            }
            if (this.hoveringCopy) {
                Night.CLICK_GUI.setColorClipboard(this.setting.getValue().getColor());
                this.playClickSound();
            }
            if (this.hoveringPaste && Night.CLICK_GUI.getColorClipboard() != null) {
                Color clip = Night.CLICK_GUI.getColorClipboard();
                this.setting.setColor(clip);
                this.hsb = Color.RGBtoHSB(clip.getRed(), clip.getGreen(), clip.getBlue(), null);
                this.playClickSound();
            }
            if (this.hasSync() && this.hoveringSync) {
                this.setting.setSync(!this.setting.isSync());
                this.playClickSound();
            }
            if (this.hoveringRainbow) {
                this.setting.setRainbow(!this.setting.isRainbow());
                this.playClickSound();
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingHue = false;
        this.draggingColor = false;
        this.draggingAlpha = false;
    }

    @Override
    public int getHeight() {
        return this.open ? (this.hasSync() ? 166 : 152) : this.getParent().getHeight();
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return (double)(this.getX() + this.getPadding()) <= mouseX && (double)this.getY() <= mouseY && (double)(this.getX() + this.getWidth() - this.getPadding()) > mouseX && (double)(this.getY() + this.getParent().getHeight()) > mouseY;
    }

    private boolean isHoveringComponent(double mouseX, double mouseY, double left, double top, double right, double bottom) {
        return left <= mouseX && top <= mouseY && right > mouseX && bottom > mouseY;
    }
}

