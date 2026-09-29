/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 */
package night.gui.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.settings.impl.ModeSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;

public class ModeButton
extends Button {
    private final ModeSetting setting;
    private boolean open = false;
    private final Animation openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);
    private final Animation hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private final Animation textAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private static final int VALUE_SLIDE_MS = 180;
    private static final int VALUE_SLIDE_DISTANCE = 10;
    private String lastValue;
    private String previousValue;
    private long valueChangeTime;

    public ModeButton(ModeSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
        this.lastValue = setting.getValue();
    }

    private float getOpenAmount() {
        return this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        float hFactor = this.hoverAnim.get(hovered ? 1.0f : 0.0f);
        float tFactor = this.textAnim.get(hovered ? 1.0f : 0.0f);
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        if (hFactor > 0.001f) {
            Color hoverColor = new Color(255, 255, 255, (int)(20.0f * hFactor));
            Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, hoverColor);
        }
        float textX = (float)(this.getX() + this.getTextPadding() + 3) + tFactor * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(context, this.setting.getTag(), (int)textX, this.getY() + 2, Color.WHITE);
        String currentValue = this.setting.getValue();
        if (!currentValue.equals(this.lastValue)) {
            this.previousValue = this.lastValue;
            this.lastValue = currentValue;
            this.valueChangeTime = System.currentTimeMillis();
        }
        float slideT = this.valueChangeTime == 0L ? 1.0f : Easing.ease(Easing.toDelta(this.valueChangeTime, 180), Easing.Method.EASE_OUT_CUBIC);
        int valueRight = this.getX() + this.getWidth() - this.getTextPadding() - 1;
        if (slideT < 1.0f && this.previousValue != null) {
            int outOffset = Math.round(slideT * 10.0f);
            int outAlpha = Math.round(255.0f * (1.0f - slideT));
            String outText = String.valueOf(ChatFormatting.GRAY) + this.previousValue;
            Night.FONT_MANAGER.drawTextWithShadow(context, outText, valueRight - Night.FONT_MANAGER.getWidth(outText) - outOffset, this.getY() + 2, new Color(255, 255, 255, outAlpha));
        }
        int inOffset = Math.round((1.0f - slideT) * 10.0f);
        int inAlpha = Math.round(255.0f * slideT);
        String inText = String.valueOf(ChatFormatting.GRAY) + currentValue;
        Night.FONT_MANAGER.drawTextWithShadow(context, inText, valueRight - Night.FONT_MANAGER.getWidth(inText) + inOffset, this.getY() + 2, new Color(255, 255, 255, inAlpha));
        float openAmount = this.getOpenAmount();
        if (openAmount > 0.001f) {
            int visibleRows = Mth.clamp((int)Math.round((float)this.setting.getModes().size() * openAmount), (int)0, (int)this.setting.getModes().size());
            int dropdownHeight = visibleRows * this.getParent().getHeight();
            int dropdownY = this.getY() + this.getParent().getHeight();
            Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 2, dropdownY, this.getX() + this.getWidth() - this.getPadding() - 2, dropdownY + dropdownHeight, new Color(10, 10, 15, 210));
            Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 2, dropdownY, this.getX() + this.getPadding() + 4, dropdownY + dropdownHeight, ClickGuiScreen.getButtonColor(this.getY(), 160));
            for (int i = 0; i < visibleRows; ++i) {
                boolean rowHover;
                String s = this.setting.getModes().get(i);
                int rowY = dropdownY + i * this.getParent().getHeight();
                boolean isSelected = this.setting.getValue().equals(s);
                boolean bl = rowHover = mouseX >= this.getX() + this.getPadding() && mouseX <= this.getX() + this.getWidth() - this.getPadding() && mouseY >= rowY && mouseY < rowY + this.getParent().getHeight();
                if (rowHover) {
                    Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 4, rowY, this.getX() + this.getWidth() - this.getPadding() - 2, rowY + this.getParent().getHeight() - 1, new Color(255, 255, 255, 18));
                }
                if (isSelected) {
                    Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 4, rowY, this.getX() + this.getWidth() - this.getPadding() - 2, rowY + this.getParent().getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 45));
                }
                Color textCol = isSelected ? Color.WHITE : (rowHover ? new Color(220, 220, 230) : new Color(150, 150, 160));
                Night.FONT_MANAGER.drawTextWithShadow(context, (isSelected ? "\u2022 " : "  ") + s, this.getX() + this.getTextPadding() + 5, rowY + 2, textCol);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                int index = this.setting.getModes().indexOf(this.setting.getValue());
                if (index < this.setting.getModes().size() - 1) {
                    this.setting.setValue(this.setting.getModes().get(index + 1));
                } else {
                    this.setting.setValue(this.setting.getModes().get(0));
                }
                this.playClickSound();
            } else if (button == 1) {
                this.open = !this.open;
                this.playClickSound();
            }
        }
        if (this.open) {
            float openAmount = this.getOpenAmount();
            int visibleRows = Mth.clamp((int)Math.round((float)this.setting.getModes().size() * openAmount), (int)0, (int)this.setting.getModes().size());
            for (int i = 0; i < visibleRows; ++i) {
                boolean hovered;
                String s = this.setting.getModes().get(i);
                int rowY = this.getY() + this.getParent().getHeight() + i * this.getParent().getHeight();
                boolean bl = hovered = mouseX >= (double)(this.getX() + this.getPadding()) && mouseX <= (double)(this.getX() + this.getWidth() - this.getPadding()) && mouseY >= (double)rowY && mouseY < (double)(rowY + this.getParent().getHeight());
                if (!hovered || button != 0) continue;
                this.setting.setValue(s);
                this.playClickSound();
            }
        }
    }

    @Override
    public int getHeight() {
        return Math.round((float)this.getParent().getHeight() + (float)(this.setting.getModes().size() * this.getParent().getHeight()) * this.getOpenAmount());
    }
}

