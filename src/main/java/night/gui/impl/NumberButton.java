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
import night.settings.impl.NumberSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.chat.ChatUtils;
import night.utils.graphics.Renderer2D;
import night.utils.system.MathUtils;

public class NumberButton
extends Button {
    private final NumberSetting setting;
    private boolean dragging = false;
    private String currentString = "";
    private boolean listening = false;
    private boolean selecting = false;
    private final Animation slideAnim;
    private final Animation sliderAnim;
    private boolean prevVisible = false;

    public NumberButton(NumberSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
        this.sliderAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean prevVisible) {
        this.prevVisible = prevVisible;
    }

    public void resetSlide() {
        this.slideAnim.setCurrent(0.0f);
        this.slideAnim.setPrev(0.0f);
        this.slideAnim.setActiveDuration(220);
        this.slideAnim.setStartTime(System.currentTimeMillis());
        this.sliderAnim.setCurrent(0.0f);
        this.sliderAnim.setPrev(0.0f);
        this.sliderAnim.setActiveDuration(220);
        this.sliderAnim.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        boolean showCursor;
        float currentSlider;
        double slider;
        if (!this.prevVisible) {
            this.resetSlide();
            this.prevVisible = true;
        }
        double sliderMax = this.getWidth() - 2 - this.getPadding() * 2;
        double drag = Math.min(sliderMax, (double)Math.max(0, mouseX - this.getX() - 1 - this.getPadding()));
        if (this.setting.getType() == NumberSetting.Type.INTEGER) {
            slider = sliderMax * (double)(this.setting.getValue().intValue() - this.setting.getMinimum().intValue()) / (double)(this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue());
            if (this.dragging) {
                int raw = (int)MathUtils.round(drag / sliderMax * (double)(this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue()) + (double)this.setting.getMinimum().intValue(), 0);
                int step = this.setting.getStep().intValue();
                this.setting.setValue(step > 0 ? Math.round((float)raw / (float)step) * step : raw);
            }
        } else if (this.setting.getType() == NumberSetting.Type.FLOAT) {
            slider = sliderMax * (double)(this.setting.getValue().floatValue() - this.setting.getMinimum().floatValue()) / (double)(this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue());
            if (this.dragging) {
                float raw = (float)MathUtils.round(drag / sliderMax * (double)(this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue()) + (double)this.setting.getMinimum().floatValue(), 2);
                float step = this.setting.getStep().floatValue();
                float value = step > 0.0f ? (float)MathUtils.round((float)Math.round(raw / step) * step, 2) : raw;
                this.setting.setValue(Float.valueOf(value));
            }
        } else if (this.setting.getType() == NumberSetting.Type.DOUBLE) {
            slider = sliderMax * (this.setting.getValue().doubleValue() - this.setting.getMinimum().doubleValue()) / (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue());
            if (this.dragging) {
                double raw = MathUtils.round(drag / sliderMax * (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue()) + this.setting.getMinimum().doubleValue(), 2);
                double step = this.setting.getStep().doubleValue();
                double value = step > 0.0 ? MathUtils.round((double)Math.round(raw / step) * step, 2) : raw;
                this.setting.setValue(value);
            }
        } else {
            slider = sliderMax * (double)(this.setting.getValue().longValue() - this.setting.getMinimum().longValue()) / (double)(this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue());
            if (this.dragging) {
                long value = (long)MathUtils.round(drag / sliderMax * (double)(this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue()) + (double)this.setting.getMinimum().longValue(), 0);
                this.setting.setValue(value);
            }
        }
        float sFactor = this.slideAnim.get(1.0f);
        int slideOffset = Math.round((1.0f - sFactor) * -20.0f);
        int left = this.getX() + this.getPadding() + 1 + slideOffset;
        int right = this.getX() + this.getWidth() - this.getPadding() - 1 + slideOffset;
        Renderer2D.renderQuad(context, left, this.getY(), right, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 45));
        if (this.isHovering(mouseX, mouseY)) {
            Renderer2D.renderQuad(context, left, this.getY(), right, this.getY() + this.getHeight() - 1, new Color(255, 255, 255, 12));
        }
        float targetSlider = (float)Math.max(0.0, Math.min(sliderMax, slider));
        if (this.dragging) {
            this.sliderAnim.setCurrent(targetSlider);
            this.sliderAnim.setPrev(targetSlider);
            currentSlider = targetSlider;
        } else {
            currentSlider = this.sliderAnim.get(targetSlider);
        }
        if (currentSlider > 0.5f) {
            Renderer2D.renderQuad(context, left, this.getY(), (float)left + currentSlider, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 95));
            Renderer2D.renderQuad(context, (float)left + currentSlider - 1.0f, this.getY(), (float)left + currentSlider + 1.0f, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 230));
        }
        boolean bl = showCursor = System.currentTimeMillis() / 500L % 2L == 0L;
        String cursor = this.selecting ? "" : (showCursor ? "|" : "");
        Night.FONT_MANAGER.drawTextWithShadow(context, (String)(this.listening ? this.currentString + cursor : this.setting.getTag()), left + this.getTextPadding() - this.getPadding() + 2, this.getY() + 2, Color.WHITE);
        if (!this.listening) {
            String valueText = this.setting.isZeroIsIgnore() && this.setting.getValue().doubleValue() == 0.0 ? "Ignore" : String.valueOf(this.setting.getValue());
            Night.FONT_MANAGER.drawTextWithShadow(context, String.valueOf(ChatFormatting.GRAY) + valueText, right - this.getTextPadding() + this.getPadding() - Night.FONT_MANAGER.getWidth(valueText), this.getY() + 2, Color.WHITE);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            this.dragging = true;
        }
        if (button == 1) {
            if (this.isHovering(mouseX, mouseY) && !this.listening) {
                this.listening = true;
                this.currentString = "";
            } else {
                this.listening = false;
                this.selecting = false;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.dragging) {
                this.playClickSound();
            }
            this.dragging = false;
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.listening) {
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)256)) {
            this.selecting = false;
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)257)) {
            try {
                switch (this.setting.getType()) {
                    case LONG: {
                        this.setting.setValue(Long.parseLong(this.currentString));
                        break;
                    }
                    case DOUBLE: {
                        this.setting.setValue(Double.parseDouble(this.currentString));
                        break;
                    }
                    case FLOAT: {
                        this.setting.setValue(Float.valueOf(Float.parseFloat(this.currentString)));
                        break;
                    }
                    default: {
                        this.setting.setValue(Integer.parseInt(this.currentString));
                        break;
                    }
                }
            }
            catch (NumberFormatException exception) {
                Night.CHAT_MANAGER.warn("Please input a valid " + String.valueOf(ChatUtils.getPrimary()) + this.setting.getType().name().toLowerCase() + String.valueOf(ChatUtils.getSecondary()) + " number.");
            }
            this.selecting = false;
            this.listening = false;
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)259)) {
            this.currentString = this.selecting ? "" : (!this.currentString.isEmpty() ? this.currentString.substring(0, this.currentString.length() - 1) : this.currentString);
            this.selecting = false;
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341)) && InputConstants.isKeyDown((Window)mc.getWindow(), (int)65)) {
            this.selecting = true;
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.listening) {
            String string = this.currentString = this.selecting ? String.valueOf(chr) : this.currentString + chr;
            if (this.selecting) {
                this.selecting = false;
            }
        }
    }
}

