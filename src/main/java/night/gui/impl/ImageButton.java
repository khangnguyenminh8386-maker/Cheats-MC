/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 */
package night.gui.impl;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import night.Night;
import night.gui.ImageSelectScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.settings.impl.ImageSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;

public class ImageButton
extends Button {
    private final ImageSetting setting;
    private final Animation hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private final Animation textAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);

    public ImageButton(ImageSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
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
        String val = this.setting.getValue();
        if (val == null || val.isEmpty() || val.equalsIgnoreCase("None")) {
            val = "[Select...]";
        }
        Object displayVal = val;
        int maxValWidth = this.getWidth() - Night.FONT_MANAGER.getWidth(this.setting.getTag()) - this.getTextPadding() * 2 - 15;
        if (Night.FONT_MANAGER.getWidth((String)displayVal) > maxValWidth) {
            while (Night.FONT_MANAGER.getWidth((String)displayVal + "...") > maxValWidth && ((String)displayVal).length() > 3) {
                displayVal = ((String)displayVal).substring(0, ((String)displayVal).length() - 1);
            }
            displayVal = (String)displayVal + "...";
        }
        String inText = String.valueOf(ChatFormatting.GRAY) + (String)displayVal;
        int valueRight = this.getX() + this.getWidth() - this.getTextPadding() - 1;
        Night.FONT_MANAGER.drawTextWithShadow(context, inText, valueRight - Night.FONT_MANAGER.getWidth(inText), this.getY() + 2, Color.LIGHT_GRAY);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && (button == 0 || button == 1)) {
            Minecraft mc = Minecraft.getInstance();
            mc.gui.setScreen((Screen)new ImageSelectScreen(this.setting, mc.gui.screen()));
            this.playClickSound();
        }
    }
}

