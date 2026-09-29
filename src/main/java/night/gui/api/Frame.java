/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 */
package night.gui.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.ExpandableRow;
import night.gui.impl.ModuleButton;
import night.gui.impl.WhitelistButton;
import night.modules.Module;
import night.modules.impl.core.ClickGuiModule;
import night.modules.impl.core.PingBypassModule;
import night.settings.Setting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;

public class Frame {
    private final Module.Category category;
    private final String headerLabel;
    private int x;
    private int y;
    private int width;
    private int height;
    private int totalHeight;
    private int baseContentHeight;
    private int dragX = 0;
    private int dragY = 0;
    private int textPadding = 3;
    public boolean open = true;
    public boolean dragging = false;
    private final ArrayList<Button> buttons = new ArrayList();
    private float scrollOffset = 0.0f;
    private float targetScroll = 0.0f;
    private long lastScrollTime = System.currentTimeMillis();
    private final Animation openAnim = new Animation(1.0f, 1.0f, 220, Easing.Method.EASE_OUT_CUBIC);

    public Frame(Module.Category category, int x, int y, int width, int height) {
        this.category = category;
        this.headerLabel = null;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        for (Module module : Night.MODULE_MANAGER.getModules(category)) {
            if (module instanceof PingBypassModule) continue;
            this.buttons.add(new ModuleButton(module, this, height));
        }
    }

    public Frame(String headerLabel, List<Button> prebuiltButtons, int x, int y, int width, int height) {
        this.category = null;
        this.headerLabel = headerLabel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.buttons.addAll(prebuiltButtons);
    }

    private String getHeaderText() {
        return this.category != null ? this.category.getName() : this.headerLabel;
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.render(context, mouseX, mouseY, delta, "");
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, String searchQuery) {
        int shift;
        if (this.dragging) {
            this.setX(mouseX - this.dragX);
            this.setY(mouseY - this.dragY);
        }
        boolean searching = searchQuery != null && !searchQuery.isEmpty();
        this.totalHeight = this.height;
        this.baseContentHeight = 0;
        ++this.totalHeight;
        for (Button button : this.buttons) {
            ExpandableRow row;
            if (searching && button instanceof ExpandableRow) {
                row = (ExpandableRow)((Object)button);
                if (!row.getRowName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    button.setVisible(false);
                    continue;
                }
                button.setVisible(true);
                row.setSearchQuery(searchQuery);
            } else if (!searching && button instanceof ExpandableRow) {
                ExpandableRow row2 = (ExpandableRow)((Object)button);
                button.setVisible(true);
                row2.setSearchQuery("");
            }
            if (!button.isVisible()) continue;
            this.baseContentHeight += button.getHeight();
            button.setX(this.x);
            button.setY(this.y + this.totalHeight);
            this.totalHeight += button.getHeight();
            if (!(button instanceof ExpandableRow)) continue;
            row = (ExpandableRow)((Object)button);
            float openAmount = row.getOpenAmount();
            List<Button> settingButtons = row.getButtons();
            float childOffset = 0.0f;
            float fullHeight = 0.0f;
            for (Button b : settingButtons) {
                b.getSetting().getVisibility().update();
                boolean visible = b.getSetting().getVisibility().isVisible();
                b.setVisible(visible);
                if (!visible) continue;
                float categoryScale = 1.0f;
                Setting.Visibility visibility = b.getSetting().getVisibility();
                if (visibility instanceof CategorySetting.Visibility) {
                    CategorySetting.Visibility categoryVisibility = (CategorySetting.Visibility)visibility;
                    categoryScale = categoryVisibility.getValue().getOpenAmount();
                } else {
                    visibility = b.getSetting().getVisibility();
                    if (visibility instanceof BooleanSetting.Visibility) {
                        BooleanSetting.Visibility booleanVisibility = (BooleanSetting.Visibility)visibility;
                        categoryScale = booleanVisibility.getOpenAmount();
                    }
                }
                float rowHeight = (float)b.getHeight() * categoryScale;
                b.setX(this.x);
                b.setY(this.y + this.totalHeight + Math.round(childOffset));
                childOffset += rowHeight;
                fullHeight += rowHeight;
            }
            int revealHeight = Math.round(fullHeight * openAmount);
            row.setRevealHeight(revealHeight);
            this.totalHeight += revealHeight;
            if (!searching) continue;
            this.baseContentHeight += revealHeight;
        }
        float contentHeight = this.totalHeight - this.height - 1;
        float viewHeight = this.baseContentHeight;
        float maxScroll = 0.0f;
        float minScroll = -Math.max(0.0f, contentHeight - viewHeight);
        long now = System.currentTimeMillis();
        float dt = Math.min((float)(now - this.lastScrollTime) / 1000.0f, 0.05f);
        this.lastScrollTime = now;
        if (dt <= 1.0E-4f) {
            dt = 0.016f;
        }
        if (this.targetScroll > maxScroll) {
            this.targetScroll += (maxScroll - this.targetScroll) * Math.min(1.0f, 8.0f * dt);
        } else if (this.targetScroll < minScroll) {
            this.targetScroll += (minScroll - this.targetScroll) * Math.min(1.0f, 8.0f * dt);
        }
        float lerpFactor = 1.0f - (float)Math.exp(-9.0 * (double)dt);
        this.scrollOffset = Mth.lerp((float)lerpFactor, (float)this.scrollOffset, (float)this.targetScroll);
        if (Math.abs(this.scrollOffset - this.targetScroll) < 0.01f && this.targetScroll >= minScroll && this.targetScroll <= maxScroll) {
            this.scrollOffset = this.targetScroll;
        }
        if ((shift = Math.round(this.scrollOffset)) != 0) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.setY(button.getY() + shift);
                if (!(button instanceof ExpandableRow)) continue;
                ExpandableRow row = (ExpandableRow)((Object)button);
                for (Button b : row.getButtons()) {
                    if (!b.isVisible()) continue;
                    b.setY(b.getY() + shift);
                }
            }
        }
        ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        boolean showIcons = clickGui.categoryIcons.getValue();
        boolean glow = clickGui.glowAccent.getValue();
        Renderer2D.renderQuad(context, this.x, this.y, this.x + this.width, this.y + this.height, ClickGuiScreen.getButtonColor(this.y, 55));
        if (glow) {
            Renderer2D.renderQuad(context, this.x, this.y, this.x + this.width, this.y + 1, ClickGuiScreen.getButtonColor(this.y, 220));
        }
        Color accentColor = ClickGuiScreen.getButtonColor(this.y, 220);
        Renderer2D.renderQuad(context, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, accentColor);
        String icon = showIcons ? this.getCategoryIcon() + " " : "";
        Night.FONT_MANAGER.drawTextWithShadow(context, icon + this.getHeaderText(), this.x + this.textPadding, this.y + 2, Color.WHITE);
        if (clickGui.triangles.getValue()) {
            String chevron = this.open ? "\u25b2" : "\u25bc";
            int chevronX = this.x + this.width - this.textPadding - Night.FONT_MANAGER.getWidth(chevron);
            Night.FONT_MANAGER.drawTextWithShadow(context, chevron, chevronX, this.y + 2, new Color(255, 255, 255, 180));
        }
        float frameOpenAmount = this.openAnim.get(this.open ? 1.0f : 0.0f);
        int fullContentHeight = this.baseContentHeight;
        int revealHeight = Math.round((float)fullContentHeight * frameOpenAmount);
        int bottomMargin = 4;
        if (revealHeight > 0) {
            int clipTop = this.y + this.height;
            int clipBottom = clipTop + revealHeight + bottomMargin;
            context.enableScissor(this.x, clipTop, this.x + this.width, clipBottom);
            Renderer2D.renderQuad(context, this.x, this.y + this.height, this.x + this.width, clipBottom, new Color(14, 13, 20, 195));
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.render(context, mouseX, mouseY, delta);
            }
            if (contentHeight > viewHeight && viewHeight > 0.0f && revealHeight > 10) {
                float scrollRange = contentHeight - viewHeight;
                float scrollRatio = Mth.clamp((float)(-this.scrollOffset / scrollRange), (float)0.0f, (float)1.0f);
                int thumbH = Math.max(10, Math.round((float)revealHeight * (viewHeight / contentHeight)));
                int thumbY = clipTop + Math.round(scrollRatio * (float)(revealHeight - thumbH));
                Renderer2D.renderQuad(context, this.x + this.width - 3, thumbY, this.x + this.width - 1, thumbY + thumbH, ClickGuiScreen.getButtonColor(this.y, 160));
            }
            context.disableScissor();
        }
        Color borderColor = ClickGuiScreen.getButtonColor(this.y, 130);
        int borderBottom = this.open ? this.y + this.height + revealHeight + bottomMargin : this.y + this.height;
        Renderer2D.renderOutline(context, this.x, this.y, this.x + this.width, borderBottom, borderColor);
    }

    private String getCategoryIcon() {
        if (this.category == null) {
            return "\u2726";
        }
        return switch (this.category) {
            default -> throw new MatchException(null, null);
            case Module.Category.COMBAT -> "\u2694";
            case Module.Category.MOVEMENT -> "\u26a1";
            case Module.Category.VISUALS -> "\u25c9";
            case Module.Category.PLAYER -> "\u265f";
            case Module.Category.MISCELLANEOUS -> "\u2699";
            case Module.Category.CORE -> "\u25c6";
        };
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.dragging = true;
                this.dragX = (int)(mouseX - (double)this.getX());
                this.dragY = (int)(mouseY - (double)this.getY());
            } else if (button == 1) {
                boolean bl = this.open = !this.open;
            }
        }
        if (this.open) {
            int clipTop = this.y + this.height;
            int clipBottom = clipTop + Math.round((float)this.baseContentHeight * this.openAnim.get(this.open ? 1.0f : 0.0f)) + 4;
            if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)clipTop && mouseY <= (double)clipBottom) {
                for (Button b : this.buttons) {
                    if (!b.isVisible()) continue;
                    b.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragging = false;
        }
        for (Button b : this.buttons) {
            if (!b.isVisible()) continue;
            b.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int clipTop = this.y;
        int clipBottom = this.y + this.height + (this.open ? Math.round((float)this.baseContentHeight * this.openAnim.get(this.open ? 1.0f : 0.0f)) + 4 : 0);
        if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)clipTop && mouseY <= (double)clipBottom) {
            boolean whitelistHandling = false;
            block0: for (Button b : this.buttons) {
                ExpandableRow row;
                if (!(b instanceof ExpandableRow) || !(row = (ExpandableRow)((Object)b)).isOpen()) continue;
                List<Button> wbButtons = row.getButtons().stream().filter(button -> button instanceof WhitelistButton).toList();
                for (Button whitelistButton : wbButtons) {
                    WhitelistButton wb;
                    if (!(whitelistButton instanceof WhitelistButton) || !(wb = (WhitelistButton)whitelistButton).isHandlingScroll(mouseX, mouseY)) continue;
                    wb.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                    whitelistHandling = true;
                    continue block0;
                }
            }
            if (!whitelistHandling && verticalAmount != 0.0) {
                float scrollSpeed = Night.MODULE_MANAGER.getModule(ClickGuiModule.class).scrollSpeed.getValue().floatValue();
                float contentHeight = this.totalHeight - this.height - 1;
                float viewHeight = this.baseContentHeight;
                float maxScroll = 0.0f;
                float minScroll = -Math.max(0.0f, contentHeight - viewHeight);
                float scrollStep = (float)(verticalAmount * (double)scrollSpeed * 12.0);
                float resistance = 1.0f;
                if (this.targetScroll > maxScroll) {
                    resistance = 1.0f / (1.0f + (this.targetScroll - maxScroll) * 0.05f);
                } else if (this.targetScroll < minScroll) {
                    resistance = 1.0f / (1.0f + (minScroll - this.targetScroll) * 0.05f);
                }
                this.targetScroll += scrollStep * resistance;
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Button b : this.buttons) {
            b.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.open) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (this.open) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.charTyped(chr, modifiers);
            }
        }
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return (double)this.x <= mouseX && (double)this.y <= mouseY && (double)(this.x + this.width) > mouseX && (double)(this.y + this.height) > mouseY;
    }

    @Generated
    public Module.Category getCategory() {
        return this.category;
    }

    @Generated
    public String getHeaderLabel() {
        return this.headerLabel;
    }

    @Generated
    public int getX() {
        return this.x;
    }

    @Generated
    public int getY() {
        return this.y;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public int getTotalHeight() {
        return this.totalHeight;
    }

    @Generated
    public int getBaseContentHeight() {
        return this.baseContentHeight;
    }

    @Generated
    public int getDragX() {
        return this.dragX;
    }

    @Generated
    public int getDragY() {
        return this.dragY;
    }

    @Generated
    public int getTextPadding() {
        return this.textPadding;
    }

    @Generated
    public boolean isOpen() {
        return this.open;
    }

    @Generated
    public boolean isDragging() {
        return this.dragging;
    }

    @Generated
    public ArrayList<Button> getButtons() {
        return this.buttons;
    }

    @Generated
    public float getScrollOffset() {
        return this.scrollOffset;
    }

    @Generated
    public float getTargetScroll() {
        return this.targetScroll;
    }

    @Generated
    public long getLastScrollTime() {
        return this.lastScrollTime;
    }

    @Generated
    public Animation getOpenAnim() {
        return this.openAnim;
    }

    @Generated
    public void setX(int x) {
        this.x = x;
    }

    @Generated
    public void setY(int y) {
        this.y = y;
    }

    @Generated
    public void setWidth(int width) {
        this.width = width;
    }

    @Generated
    public void setHeight(int height) {
        this.height = height;
    }

    @Generated
    public void setTotalHeight(int totalHeight) {
        this.totalHeight = totalHeight;
    }

    @Generated
    public void setBaseContentHeight(int baseContentHeight) {
        this.baseContentHeight = baseContentHeight;
    }

    @Generated
    public void setDragX(int dragX) {
        this.dragX = dragX;
    }

    @Generated
    public void setDragY(int dragY) {
        this.dragY = dragY;
    }

    @Generated
    public void setTextPadding(int textPadding) {
        this.textPadding = textPadding;
    }

    @Generated
    public void setOpen(boolean open) {
        this.open = open;
    }

    @Generated
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Generated
    public void setScrollOffset(float scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    @Generated
    public void setTargetScroll(float targetScroll) {
        this.targetScroll = targetScroll;
    }

    @Generated
    public void setLastScrollTime(long lastScrollTime) {
        this.lastScrollTime = lastScrollTime;
    }
}

