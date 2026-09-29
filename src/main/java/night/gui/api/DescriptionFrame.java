/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package night.gui.api;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.modules.impl.core.ClickGuiModule;
import night.utils.graphics.Renderer2D;


public class DescriptionFrame {
    private String description = "";
    private int x;
    private int y;
    private int width;
    private int preferredWidth;
    private int preferredX;
    private int preferredY;
    private int originalHeaderHeight;
    private boolean rendered;
    private int height;
    private int dragX = 0;
    private int dragY = 0;
    private int textPadding = 3;
    private boolean dragging = false;

    public DescriptionFrame(int x, int y, int width, int height) {
        this.x = x;
        this.preferredX = x;
        this.y = y;
        this.preferredY = y;
        this.width = width;
        this.preferredWidth = width;
        this.height = height;
        this.originalHeaderHeight = height;
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (this.dragging) {
            this.setX(mouseX - this.dragX);
            this.setY(mouseY - this.dragY);
        }
        this.rendered = false;
        ClickGuiScreen screen = Night.CLICK_GUI;
        // Categories and this panel use different slide offsets. Wait until those settle.
        if (screen.isClosing() || System.currentTimeMillis() - screen.getSlideAnim().getStartTime()
                < 320L + Math.max(0, screen.getFrames().size() - 1) * 35L) return;
        float scale = screen.getAppliedScale();
        if (!(scale > 0) || !Float.isFinite(scale)) return;
        int rawWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int rawHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int viewportWidth = (int)Math.floor(rawWidth / scale);
        int viewportHeight = (int)Math.floor(rawHeight / scale);
        List<DescriptionLayout.Rect> occupied = new ArrayList<>();
        for (Frame frame : screen.getFrames()) {
            int body = Math.round(frame.getBaseContentHeight() * frame.getOpenAnim().get(frame.isOpen() ? 1.0f : 0.0f));
            occupied.add(new DescriptionLayout.Rect(frame.getX(), frame.getY(), frame.getWidth(),
                    frame.getHeight() + (body > 0 ? body + 4 : 0)));
        }
        SearchFrame search = screen.getSearchFrame();
        if (search.isVisible()) {
            // SearchFrame computes these raw GUI coordinates inside the scaled pose.
            occupied.add(new DescriptionLayout.Rect((rawWidth - search.getWidth()) / 2,
                    rawHeight - search.getHeight() - 8, search.getWidth(), search.getHeight()));
        }
        String english = this.description.isEmpty()
                ? "Hover over a module or setting to view details.\nCtrl+F to search" : this.description;
        int fontHeight = Math.max(1, Night.FONT_MANAGER.getHeight());
        DescriptionLayout.Panel panel = DescriptionLayout.create(english,
                this.description.isEmpty() ? "" : GuiDescriptionTranslations.translate(this.description),
                "\u2139 Description", "[Ctrl+F]", this.textPadding, fontHeight, this.originalHeaderHeight,
                this.preferredWidth, this.preferredX, this.preferredY, viewportWidth, viewportHeight,
                occupied, Night.FONT_MANAGER::getWidth);
        // No free rectangle: leave other controls usable instead of clipping this panel.
        if (panel == null) return;
        this.x = panel.bounds().x();
        this.y = panel.bounds().y();
        this.width = panel.bounds().width();
        this.height = panel.headerHeight();
        if (this.dragging) {
            this.dragX = mouseX - this.x;
            this.dragY = mouseY - this.y;
        }
        this.rendered = true;
        ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        boolean glow = clickGui.glowAccent.getValue();
        Renderer2D.renderQuad(context, this.x, this.y, this.x + this.width, this.y + this.height, ClickGuiScreen.getButtonColor(this.y, 55));
        if (glow) {
            Renderer2D.renderQuad(context, this.x, this.y, this.x + this.width, this.y + 1, ClickGuiScreen.getButtonColor(this.y, 220));
        }
        Color accentColor = ClickGuiScreen.getButtonColor(this.y, 220);
        Renderer2D.renderQuad(context, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, accentColor);
        Night.FONT_MANAGER.drawTextWithShadow(context, "\u2139 Description", this.x + this.textPadding, this.y + 2, Color.WHITE);
        String searchHint = "[Ctrl+F]";
        Night.FONT_MANAGER.drawTextWithShadow(context, searchHint, this.x + this.width - this.textPadding - Night.FONT_MANAGER.getWidth(searchHint), this.y + 2, new Color(130, 130, 140));
        int contentHeight = panel.totalHeight() - this.height;
        Renderer2D.renderQuad(context, this.x, this.y + this.height, this.x + this.width, this.y + panel.totalHeight(), new Color(14, 13, 20, 195));
        int i = 0;
        for (String line : panel.english()) {
            Color textCol = this.description.isEmpty() ? (line.equals("Ctrl+F to search") ? new Color(125, 125, 135) : new Color(140, 140, 150)) : Color.WHITE;
            Night.FONT_MANAGER.drawTextWithShadow(context, line, this.x + this.textPadding, this.y + this.height + this.textPadding + fontHeight * i++, textCol);
        }
        for (String line : panel.vietnamese()) {
            Night.FONT_MANAGER.drawTextWithShadow(context, line, this.x + this.textPadding, this.y + this.height + this.textPadding + fontHeight * i++, new Color(190, 190, 200));
        }
        Color borderColor = ClickGuiScreen.getButtonColor(this.y, 130);
        Renderer2D.renderOutline(context, this.x, this.y, this.x + this.width, this.y + this.height + contentHeight, borderColor);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            this.dragging = true;
            this.dragX = (int)(mouseX - (double)this.getX());
            this.dragY = (int)(mouseY - (double)this.getY());
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragging = false;
        }
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return this.rendered && (double)this.x <= mouseX && (double)this.y <= mouseY && (double)(this.x + this.width) > mouseX && (double)(this.y + this.height) > mouseY;
    }

    @Generated
    public String getDescription() {
        return this.description;
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
    public boolean isDragging() {
        return this.dragging;
    }

    @Generated
    public void setDescription(String description) {
        this.description = description;
    }

    @Generated
    public void setX(int x) {
        this.x = x;
        this.preferredX = x;
    }

    @Generated
    public void setY(int y) {
        this.y = y;
        this.preferredY = y;
    }

    @Generated
    public void setWidth(int width) {
        this.width = width;
        this.preferredWidth = width;
    }

    @Generated
    public void setHeight(int height) {
        this.height = height;
        this.originalHeaderHeight = height;
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
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }
}

