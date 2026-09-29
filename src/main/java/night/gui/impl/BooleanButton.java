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
import night.modules.impl.core.ClickGuiModule;
import night.settings.impl.BooleanSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;

public class BooleanButton
extends Button {
    private final BooleanSetting setting;
    private final Animation fillAnim;
    private final Animation hoverAnim;
    private final Animation textAnim;
    private final Animation slideAnim;
    private boolean prevVisible = false;

    public BooleanButton(BooleanSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
        float start = setting.getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(start, start, 180, Easing.Method.EASE_OUT_CUBIC);
        this.hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.textAnim = new Animation(start, start, 150, Easing.Method.EASE_OUT_CUBIC);
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean prevVisible) {
        this.prevVisible = prevVisible;
    }

    public void resetSlide() {
        this.slideAnim.setCurrent(0.0f);
        this.slideAnim.setPrev(0.0f);
        this.slideAnim.setActiveDuration(220);
        this.slideAnim.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        boolean hovered;
        if (!this.prevVisible) {
            this.resetSlide();
            this.prevVisible = true;
        }
        float hFactor = this.hoverAnim.get((hovered = this.isHovering(mouseX, mouseY)) ? 1.0f : 0.0f);
        float fFactor = this.fillAnim.get(this.setting.getValue() ? 1.0f : 0.0f);
        float tFactor = this.textAnim.get(this.setting.getValue() || hovered ? 1.0f : 0.0f);
        float sFactor = this.slideAnim.get(1.0f);
        int slideOffset = Math.round((1.0f - sFactor) * -20.0f);
        int left = this.getX() + this.getPadding() + 1 + slideOffset;
        int right = this.getX() + this.getWidth() - this.getPadding() - 1 + slideOffset;
        Renderer2D.renderQuad(context, left, this.getY(), right, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        if (hFactor > 0.001f) {
            Color hoverColor = new Color(255, 255, 255, (int)(20.0f * hFactor));
            Renderer2D.renderQuad(context, left, this.getY(), right, this.getY() + this.getHeight() - 1, hoverColor);
        }
        if (fFactor > 0.001f) {
            int fillRight = left + Math.round((float)(right - left) * fFactor);
            Color accentColor = ClickGuiScreen.getButtonColor(this.getY(), Math.round(100.0f * fFactor));
            Renderer2D.renderQuad(context, left, this.getY(), fillRight, this.getY() + this.getHeight() - 1, accentColor);
        }
        float textX = (float)(this.getX() + this.getTextPadding() + 3) + tFactor * 1.5f + (float)slideOffset;
        Night.FONT_MANAGER.drawTextWithShadow(context, String.valueOf(this.setting.getValue() ? "" : ChatFormatting.GRAY) + this.setting.getTag(), (int)textX, this.getY() + 2, Color.WHITE);
        ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGui != null && clickGui.switches.getValue()) {
            int trackW = 14;
            int trackH = 7;
            int trackX = right - trackW - 1;
            int trackY = this.getY() + (this.getHeight() - trackH) / 2;
            Color trackBg = this.setting.getValue() ? ClickGuiScreen.getButtonColor(this.getY(), Math.round(180.0f * fFactor)) : new Color(40, 40, 50, 160);
            Renderer2D.renderQuad(context, trackX, trackY, trackX + trackW, trackY + trackH, trackBg);
            Renderer2D.renderOutline(context, trackX, trackY, trackX + trackW, trackY + trackH, new Color(0, 0, 0, 80));
            int thumbW = 5;
            int thumbX = trackX + Math.round(fFactor * (float)(trackW - thumbW));
            Color thumbColor = this.setting.getValue() ? Color.WHITE : new Color(180, 180, 190);
            Renderer2D.renderQuad(context, thumbX, trackY - 1, thumbX + thumbW, trackY + trackH + 1, thumbColor);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            this.setting.setValue(!this.setting.getValue());
            this.playClickSound();
        }
    }
}

