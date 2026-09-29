/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package night.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.ExpandableRow;
import night.gui.api.Frame;
import night.gui.impl.BindButton;
import night.gui.impl.BooleanButton;
import night.gui.impl.CategoryButton;
import night.gui.impl.ColorButton;
import night.gui.impl.ImageButton;
import night.gui.impl.ModeButton;
import night.gui.impl.NumberButton;
import night.gui.impl.StringButton;
import night.gui.impl.WhitelistButton;
import night.modules.Module;
import night.modules.impl.core.ClickGuiModule;
import night.settings.Setting;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ImageSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;
import night.utils.graphics.NeekeriFill;
import night.utils.graphics.Renderer2D;
import night.utils.input.KeyboardUtils;

public class ModuleButton
extends Button
implements ExpandableRow {
    private final Module module;
    private boolean open = false;
    private final ArrayList<Button> buttons = new ArrayList();
    private String searchQuery = "";
    private final Animation openAnim = new Animation(220, Easing.Method.EASE_OUT_CUBIC);
    private int revealHeight = 0;
    private final Animation fillAnim;

    @Override
    public float getOpenAmount() {
        return this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Override
    public String getRowName() {
        return this.module.getName();
    }

    public ModuleButton(Module module, Frame parent, int height) {
        super(parent, height, module.getDescription());
        this.module = module;
        float start = module.isToggled() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(start, start, 200, Easing.Method.EASE_OUT_CUBIC);
        List<Setting> settings = module.getSettings();
        for (int i = 0; i < settings.size(); ++i) {
            Setting setting = settings.get(i);
            if (setting instanceof BooleanSetting) {
                Setting.Visibility visibility;
                BooleanSetting s = (BooleanSetting)setting;
                if (s.getTag().equals("Enabled") && (visibility = s.getVisibility()) instanceof CategorySetting.Visibility) {
                    CategorySetting.Visibility v = (CategorySetting.Visibility)visibility;
                    if (i > 0 && settings.get(i - 1) == v.getValue()) continue;
                }
                this.buttons.add(new BooleanButton(s, parent, height));
                continue;
            }
            if (setting instanceof NumberSetting) {
                NumberSetting s = (NumberSetting)setting;
                this.buttons.add(new NumberButton(s, parent, height));
                continue;
            }
            if (setting instanceof CategorySetting) {
                CategorySetting.Visibility v;
                BooleanSetting next;
                Object object;
                CategorySetting s = (CategorySetting)setting;
                BooleanSetting enableSetting = null;
                if (i + 1 < settings.size() && (object = settings.get(i + 1)) instanceof BooleanSetting && (next = (BooleanSetting)object).getTag().equals("Enabled") && (object = next.getVisibility()) instanceof CategorySetting.Visibility && (v = (CategorySetting.Visibility)object).getValue() == s) {
                    enableSetting = next;
                }
                this.buttons.add(new CategoryButton(s, enableSetting, parent, height));
                continue;
            }
            if (setting instanceof BindSetting) {
                BindSetting s = (BindSetting)setting;
                this.buttons.add(new BindButton(s, parent, height));
                continue;
            }
            if (setting instanceof ModeSetting) {
                ModeSetting s = (ModeSetting)setting;
                this.buttons.add(new ModeButton(s, parent, height));
                continue;
            }
            if (setting instanceof WhitelistSetting) {
                WhitelistSetting s = (WhitelistSetting)setting;
                this.buttons.add(new WhitelistButton(s, parent, height));
                continue;
            }
            if (setting instanceof StringSetting) {
                StringSetting s = (StringSetting)setting;
                this.buttons.add(new StringButton(s, parent, height));
                continue;
            }
            if (setting instanceof ImageSetting) {
                ImageSetting s = (ImageSetting)setting;
                this.buttons.add(new ImageButton(s, parent, height));
                continue;
            }
            if (!(setting instanceof ColorSetting)) continue;
            ColorSetting s = (ColorSetting)setting;
            this.buttons.add(new ColorButton(s, parent, height));
        }
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float fillProgress;
        Color bgColor;
        if (this.isHovering(mouseX, mouseY) && Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            Night.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());
        }
        Color color = bgColor = this.isHovering(mouseX, mouseY) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
        if (bgColor.getAlpha() > 0) {
            Renderer2D.renderQuad(context, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, bgColor);
        }
        ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (!this.module.isToggled()) {
            Color dimFill = ColorUtils.getColor(clickGui.color.getColor(), 45);
            Renderer2D.renderQuad(context, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, dimFill);
        }
        if ((fillProgress = this.fillAnim.get(this.module.isToggled() ? 1.0f : 0.0f)) > 0.001f) {
            boolean shaderFill = !clickGui.fillMode.getValue().equalsIgnoreCase("Default");
            int fillRight = this.getX() + this.getPadding() + Math.round((float)(this.getWidth() - this.getPadding() * 2) * fillProgress);
            if (shaderFill) {
                int alpha = Math.round(clickGui.neekeriOpacity.getValue().floatValue() / 100.0f * 255.0f);
                context.enableScissor(this.getX() + this.getPadding(), this.getY(), fillRight, this.getY() + this.getHeight() - 1);
                NeekeriFill.fill(context, this.getX() + this.getPadding(), this.getY(), this.getWidth() - this.getPadding() * 2, this.getHeight() - 1, alpha);
                context.disableScissor();
            } else {
                Color fillColor = ColorUtils.getColor(clickGui.color.getColor(), 90);
                Renderer2D.renderQuad(context, this.getX() + this.getPadding(), this.getY(), fillRight, this.getY() + this.getHeight() - 1, fillColor);
            }
        }
        Color separator = ClickGuiScreen.getButtonColor(this.getY(), 60);
        Renderer2D.renderQuad(context, this.getX() + this.getPadding(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight(), separator);
        String moduleName = this.module.getName();
        int textX = this.getX() + this.getTextPadding() + (this.module.isToggled() ? 1 : 0);
        int textY = this.getY() + 2;
        if (this.searchQuery != null && !this.searchQuery.isEmpty()) {
            String lowerQuery;
            String lowerName = moduleName.toLowerCase();
            int matchIndex = lowerName.indexOf(lowerQuery = this.searchQuery.toLowerCase());
            if (matchIndex != -1) {
                String before = moduleName.substring(0, matchIndex);
                String match = moduleName.substring(matchIndex, matchIndex + this.searchQuery.length());
                String after = moduleName.substring(matchIndex + this.searchQuery.length());
                if (!before.isEmpty()) {
                    String prefix = this.module.isToggled() ? "" : ChatFormatting.GRAY.toString();
                    Night.FONT_MANAGER.drawTextWithShadow(context, prefix + before, textX, textY, Color.WHITE);
                    textX += Night.FONT_MANAGER.getWidth(before);
                }
                Color highlightColor = ClickGuiScreen.getButtonColor(this.getY(), 200);
                int matchWidth = Night.FONT_MANAGER.getWidth(match);
                Renderer2D.renderQuad(context, textX - 1, textY - 1, textX + matchWidth + 1, textY + Night.FONT_MANAGER.getHeight(), highlightColor);
                Night.FONT_MANAGER.drawTextWithShadow(context, match, textX, textY, Color.WHITE);
                textX += matchWidth;
                if (!after.isEmpty()) {
                    String prefix = this.module.isToggled() ? "" : ChatFormatting.GRAY.toString();
                    Night.FONT_MANAGER.drawTextWithShadow(context, prefix + after, textX, textY, Color.WHITE);
                }
            } else {
                Night.FONT_MANAGER.drawTextWithShadow(context, String.valueOf(this.module.isToggled() ? "" : ChatFormatting.GRAY) + moduleName, textX, textY, Color.WHITE);
            }
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(context, String.valueOf(this.module.isToggled() ? "" : ChatFormatting.GRAY) + moduleName, textX, textY, Color.WHITE);
        }
        int rightEdge = this.getX() + this.getWidth() - this.getTextPadding() - 1;
        if (clickGui.triangles.getValue() && !this.buttons.isEmpty()) {
            String expandIcon = this.open ? "\u25b2" : "\u25bc";
            int iconWidth = Night.FONT_MANAGER.getWidth(expandIcon);
            int iconX = rightEdge - iconWidth;
            Color iconCol = this.open ? ClickGuiScreen.getButtonColor(this.getY(), 220) : new Color(255, 255, 255, 120);
            Night.FONT_MANAGER.drawTextWithShadow(context, expandIcon, iconX, this.getY() + 2, iconCol);
            rightEdge -= iconWidth + 4;
        }
        if (clickGui.switches.getValue()) {
            int trackW = 14;
            int trackH = 7;
            int trackX = rightEdge - trackW;
            int trackY = this.getY() + (this.getHeight() - trackH) / 2;
            Color trackBg = this.module.isToggled() ? ClickGuiScreen.getButtonColor(this.getY(), Math.round(180.0f * fillProgress)) : new Color(40, 40, 50, 160);
            Renderer2D.renderQuad(context, trackX, trackY, trackX + trackW, trackY + trackH, trackBg);
            Renderer2D.renderOutline(context, trackX, trackY, trackX + trackW, trackY + trackH, new Color(0, 0, 0, 80));
            int thumbW = 5;
            int thumbX = trackX + Math.round(fillProgress * (float)(trackW - thumbW));
            Color thumbColor = this.module.isToggled() ? Color.WHITE : new Color(180, 180, 190);
            Renderer2D.renderQuad(context, thumbX, trackY - 1, thumbX + thumbW, trackY + trackH + 1, thumbColor);
            rightEdge -= trackW + 4;
        }
        if (this.module.getBind() != 0) {
            String bindText = KeyboardUtils.getKeyName(this.module.getBind()).toUpperCase();
            int bindWidth = Night.FONT_MANAGER.getWidth(bindText);
            int nameWidth = Night.FONT_MANAGER.getWidth(moduleName);
            int nameEndX = this.getX() + this.getTextPadding() + (this.module.isToggled() ? 1 : 0) + nameWidth;
            int available = rightEdge - nameEndX - 4;
            if (bindWidth <= available || available <= 4) {
                int bindX = rightEdge - bindWidth;
                Night.FONT_MANAGER.drawTextWithShadow(context, bindText, bindX, this.getY() + 2, new Color(220, 220, 230, 160));
            } else {
                float scale = Math.max(0.55f, (float)available / (float)bindWidth);
                int scaledBindX = rightEdge - Math.round((float)bindWidth * scale);
                context.pose().pushMatrix();
                context.pose().scale(scale, scale);
                Night.FONT_MANAGER.drawTextWithShadow(context, bindText, Math.round((float)scaledBindX / scale), Math.round((float)(this.getY() + 2) / scale), new Color(220, 220, 230, 160));
                context.pose().popMatrix();
            }
        }
        if (this.revealHeight > 0) {
            int clipTop = this.getY() + this.getHeight();
            context.enableScissor(this.getX(), clipTop, this.getX() + this.getWidth(), clipTop + this.revealHeight);
            for (Button button : this.buttons) {
                if (!button.isVisible()) {
                    if (button instanceof BooleanButton) {
                        BooleanButton bb = (BooleanButton)button;
                        bb.setPrevVisible(false);
                        continue;
                    }
                    if (!(button instanceof NumberButton)) continue;
                    NumberButton nb = (NumberButton)button;
                    nb.setPrevVisible(false);
                    continue;
                }
                button.render(context, mouseX, mouseY, delta);
                if (!button.isHovering(mouseX, mouseY) || !Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) continue;
                Night.CLICK_GUI.getDescriptionFrame().setDescription(button.getDescription());
            }
            context.disableScissor();
        } else {
            for (Button button : this.buttons) {
                if (button instanceof BooleanButton) {
                    BooleanButton bb = (BooleanButton)button;
                    bb.setPrevVisible(false);
                    continue;
                }
                if (!(button instanceof NumberButton)) continue;
                NumberButton nb = (NumberButton)button;
                nb.setPrevVisible(false);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.module.setToggled(!this.module.isToggled());
                this.playClickSound();
            } else if (button == 1) {
                this.open = !this.open;
                this.openAnim.setEasing(this.open ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD);
                if (this.open) {
                    for (Button b : this.buttons) {
                        if (b instanceof BooleanButton) {
                            BooleanButton bb = (BooleanButton)b;
                            bb.resetSlide();
                            continue;
                        }
                        if (!(b instanceof NumberButton)) continue;
                        NumberButton nb = (NumberButton)b;
                        nb.resetSlide();
                    }
                }
                this.playClickSound();
            }
        }
        if (this.open) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (Button b : this.buttons) {
            b.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Button b : this.buttons) {
            b.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.open) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.open) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.charTyped(chr, modifiers);
            }
        }
    }

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Override
    @Generated
    public boolean isOpen() {
        return this.open;
    }

    @Generated
    public ArrayList<Button> getButtons() {
        return this.buttons;
    }

    @Generated
    public String getSearchQuery() {
        return this.searchQuery;
    }

    @Generated
    public Animation getOpenAnim() {
        return this.openAnim;
    }

    @Generated
    public int getRevealHeight() {
        return this.revealHeight;
    }

    @Generated
    public Animation getFillAnim() {
        return this.fillAnim;
    }

    @Generated
    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    @Generated
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    @Generated
    public void setRevealHeight(int revealHeight) {
        this.revealHeight = revealHeight;
    }
}

