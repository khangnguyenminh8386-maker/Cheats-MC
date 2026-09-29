/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package night.gui.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.settings.impl.BindSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;
import night.utils.input.KeyboardUtils;

public class BindButton
extends Button {
    private final BindSetting setting;
    private boolean listening = false;
    private final Animation hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private final Animation listenAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private static int listenersActive = 0;

    public static boolean isAnyListening() {
        return listenersActive > 0;
    }

    public BindButton(BindSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        float hFactor = this.hoverAnim.get(hovered ? 1.0f : 0.0f);
        float lFactor = this.listenAnim.get(this.listening ? 1.0f : 0.0f);
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        if (hFactor > 0.001f || lFactor > 0.001f) {
            int alpha = Math.min(255, (int)(20.0f * hFactor + 50.0f * lFactor));
            Color highlight = this.listening ? ClickGuiScreen.getButtonColor(this.getY(), alpha) : new Color(255, 255, 255, alpha);
            Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, highlight);
        }
        String tag = this.setting.getMode().equals("Bind") ? this.setting.getTag() : this.setting.getMode();
        float textX = (float)(this.getX() + this.getTextPadding() + 3) + hFactor * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(context, tag, (int)textX, this.getY() + 2, Color.WHITE);
        String bind = this.listening ? "..." : KeyboardUtils.getKeyName(this.setting.getValue());
        Color bindColor = this.listening ? Color.YELLOW : Color.WHITE;
        int bindX = this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth(bind) - (int)(lFactor * 2.0f);
        Night.FONT_MANAGER.drawTextWithShadow(context, String.valueOf(this.listening ? ChatFormatting.YELLOW : ChatFormatting.GRAY) + bind, bindX, this.getY() + 2, bindColor);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isHovering(mouseX, mouseY)) {
            return;
        }
        if (this.listening) {
            if (button >= 0 && button <= 4) {
                this.setting.setValue(-button - 1);
                this.stopListening();
            }
            return;
        }
        if (button == 0) {
            this.startListening();
            this.playClickSound();
        } else if (button == 1) {
            this.cycleMode();
            this.playClickSound();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.listening) {
            return;
        }
        if (keyCode == 261 || keyCode == 256) {
            this.setting.setValue(0);
            this.stopListening();
            return;
        }
        this.setting.setValue(keyCode);
        this.stopListening();
    }

    public void keyPressed(int key) {
        this.keyPressed(key, 0, 0);
    }

    private void startListening() {
        this.listening = true;
        ++listenersActive;
    }

    private void stopListening() {
        if (this.listening) {
            this.listening = false;
            listenersActive = Math.max(0, listenersActive - 1);
        }
    }

    private void cycleMode() {
        switch (this.setting.getMode()) {
            case "Bind": {
                this.setting.setMode("Hold");
                break;
            }
            case "Hold": {
                this.setting.setMode("ReverseHold");
                break;
            }
            case "ReverseHold": {
                this.setting.setMode("Bind");
            }
        }
    }
}

