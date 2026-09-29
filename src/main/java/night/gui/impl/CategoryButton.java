/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package night.gui.impl;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;

public class CategoryButton
extends Button {
    private final CategorySetting setting;
    private final BooleanSetting enableSetting;
    private final Animation fillAnim;

    public CategoryButton(CategorySetting setting, BooleanSetting enableSetting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
        this.enableSetting = enableSetting;
        float start = enableSetting != null && enableSetting.getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(start, start, 180, Easing.Method.EASE_OUT_CUBIC);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        float fFactor = this.fillAnim.get(this.enableSetting != null && this.enableSetting.getValue() ? 1.0f : 0.0f);
        if (fFactor > 0.001f) {
            int fillRight = this.getX() + this.getPadding() + 1 + Math.round((float)(this.getWidth() - (this.getPadding() + 1) * 2) * fFactor);
            Color accentColor = ClickGuiScreen.getButtonColor(this.getY(), Math.round(100.0f * fFactor));
            Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), fillRight, this.getY() + this.getHeight() - 1, accentColor);
        }
        Color textColor = this.enableSetting != null && this.enableSetting.getValue() ? Color.WHITE : ClickGuiScreen.getButtonColor(this.getY(), 180);
        Night.FONT_MANAGER.drawTextWithShadow(context, this.setting.getTag(), this.getX() + this.getTextPadding() + 4, this.getY() + 2, textColor);
        String indicator = this.setting.isOpen() ? "..." : "+";
        Night.FONT_MANAGER.drawTextWithShadow(context, indicator, this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth(indicator), this.getY() + 2, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isHovering(mouseX, mouseY)) {
            return;
        }
        if (this.enableSetting != null && button == 0) {
            this.enableSetting.setValue(!this.enableSetting.getValue());
            this.playClickSound();
        } else if (button == 1 || this.enableSetting == null && button == 0) {
            this.setting.setOpen(!this.setting.isOpen());
            this.playClickSound();
        }
    }
}

