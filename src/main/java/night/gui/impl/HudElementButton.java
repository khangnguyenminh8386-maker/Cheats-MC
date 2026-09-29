/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package night.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
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
import night.gui.impl.ModeButton;
import night.gui.impl.NumberButton;
import night.gui.impl.StringButton;
import night.gui.impl.WhitelistButton;
import night.managers.HudElementRegistry;
import night.modules.impl.core.ClickGuiModule;
import night.settings.Setting;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;
import night.utils.graphics.NeekeriFill;
import night.utils.graphics.Renderer2D;

public class HudElementButton
extends Button
implements ExpandableRow {
    private final HudElementRegistry.Element element;
    private boolean open = false;
    private final ArrayList<Button> buttons = new ArrayList();
    private String searchQuery = "";
    private final Animation openAnim = new Animation(180, Easing.Method.EASE_OUT_QUAD);
    private int revealHeight = 0;
    private final Animation fillAnim;

    public HudElementRegistry.Element getElement() {
        return this.element;
    }

    @Override
    public boolean isOpen() {
        return this.element.category() != null ? this.element.category().isOpen() : this.open;
    }

    @Override
    public float getOpenAmount() {
        return this.element.category() != null ? this.element.category().getOpenAmount() : this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Override
    public String getRowName() {
        return this.element.name();
    }

    @Override
    public List<Button> getButtons() {
        return this.buttons;
    }

    @Override
    public void setRevealHeight(int height) {
        this.revealHeight = height;
    }

    @Override
    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public int getRevealHeight() {
        return this.revealHeight;
    }

    public HudElementButton(HudElementRegistry.Element element, List<Setting> settings, Frame parent, int height) {
        super(parent, height, element.name());
        this.element = element;
        float start = element.enabled().getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(start, start, 180, Easing.Method.EASE_OUT_QUAD);
        for (Setting setting : settings) {
            if (setting instanceof BooleanSetting) {
                BooleanSetting s = (BooleanSetting)setting;
                this.buttons.add(new BooleanButton(s, parent, height));
                continue;
            }
            if (setting instanceof NumberSetting) {
                NumberSetting s = (NumberSetting)setting;
                this.buttons.add(new NumberButton(s, parent, height));
                continue;
            }
            if (setting instanceof CategorySetting) {
                CategorySetting s = (CategorySetting)setting;
                this.buttons.add(new CategoryButton(s, null, parent, height));
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
            if (!(setting instanceof ColorSetting)) continue;
            ColorSetting s = (ColorSetting)setting;
            this.buttons.add(new ColorButton(s, parent, height));
        }
    }

    private void updateChildBounds() {
        int currentY = this.getY() + this.getHeight();
        for (Button b : this.buttons) {
            b.setX(this.getX());
            b.setY(currentY);
            if (!b.isVisible()) continue;
            currentY += b.getHeight();
        }
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float fillProgress;
        Color bgColor;
        this.updateChildBounds();
        if (this.isHovering(mouseX, mouseY) && Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            Night.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());
        }
        Color color = bgColor = this.isHovering(mouseX, mouseY) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
        if (bgColor.getAlpha() > 0) {
            Renderer2D.renderQuad(context, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, bgColor);
        }
        if ((fillProgress = this.fillAnim.get(this.element.enabled().getValue() ? 1.0f : 0.0f)) > 0.001f) {
            ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
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
        int textX = this.getX() + this.getTextPadding() + (this.element.enabled().getValue() ? 1 : 0);
        int textY = this.getY() + 2;
        String prefix = this.element.enabled().getValue() ? "" : ChatFormatting.GRAY.toString();
        Night.FONT_MANAGER.drawTextWithShadow(context, prefix + this.element.name(), textX, textY, Color.WHITE);
        if (!this.buttons.isEmpty()) {
            String indicator = this.isOpen() ? "..." : "+";
            int indX = this.getX() + this.getWidth() - this.getTextPadding() - Night.FONT_MANAGER.getWidth(indicator) - 1;
            Night.FONT_MANAGER.drawTextWithShadow(context, indicator, indX, this.getY() + 2, Color.WHITE);
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
        this.updateChildBounds();
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.element.enabled().setValue(!this.element.enabled().getValue());
                this.playClickSound();
            } else if (button == 1) {
                if (this.element.category() != null) {
                    this.element.category().setOpen(!this.element.category().isOpen());
                } else {
                    this.open = !this.open;
                    this.openAnim.setEasing(this.open ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD);
                }
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
                this.playClickSound();
            }
        }
        if (this.isOpen()) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.updateChildBounds();
        if (this.isOpen()) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.updateChildBounds();
        if (this.isOpen()) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isOpen()) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.isOpen()) {
            for (Button b : this.buttons) {
                if (!b.isVisible()) continue;
                b.charTyped(chr, modifiers);
            }
        }
    }
}

