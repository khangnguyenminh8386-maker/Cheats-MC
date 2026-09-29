/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.network.chat.Component
 */
package night.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import night.Night;
import night.gui.ClickGuiScreen;
import night.managers.ImageManager;
import night.settings.impl.ImageSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;
import night.utils.system.Timer;

public class ImageSelectScreen
extends Screen {
    private static final int COL_WIDTH = 175;
    private static final int COL_GAP = 14;
    private static final int HEADER_H = 15;
    private static final int ROW_H = 18;
    private static final int SLIDE_DURATION_MS = 320;
    private static final float SLIDE_DISTANCE = 160.0f;
    private final ImageSetting setting;
    private final Screen parent;
    private List<File> allFiles = new ArrayList<File>();
    private String query = "";
    private String lastQuery = "";
    private long queryChangeTime = System.currentTimeMillis();
    private float scrollList = 0.0f;
    private float targetScrollList = 0.0f;
    private String selectedPreviewName = null;
    private ImageManager.ImageEntry currentPreviewEntry = null;
    private final Animation slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
    private boolean closing = false;
    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private int colX1;
    private int colX2;
    private int colY;
    private int colHeight;
    private long lastClickTime = 0L;
    private String lastClickedItem = null;

    public ImageSelectScreen(ImageSetting setting, Screen parent) {
        super((Component)Component.literal((String)"night-image-select"));
        this.setting = setting;
        this.parent = parent;
        this.selectedPreviewName = setting.getValue();
        this.refreshFiles();
    }

    public void refreshFiles() {
        if (Night.IMAGE_MANAGER != null) {
            this.allFiles = Night.IMAGE_MANAGER.getImageFiles();
            if ((this.selectedPreviewName == null || this.selectedPreviewName.equals("None")) && !this.allFiles.isEmpty()) {
                this.selectedPreviewName = this.allFiles.get(0).getName();
            }
            this.updatePreview();
        }
    }

    private void updatePreview() {
        this.currentPreviewEntry = this.selectedPreviewName != null && !this.selectedPreviewName.equalsIgnoreCase("None") && Night.IMAGE_MANAGER != null ? Night.IMAGE_MANAGER.getImageEntry(this.selectedPreviewName) : null;
    }

    public void requestClose() {
        this.closing = true;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Renderer2D.renderQuad(context, 0.0f, 0.0f, this.width, this.height, new Color(10, 8, 18, 160));
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float progress = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        if (this.closing && progress <= 0.001f) {
            Minecraft.getInstance().gui.setScreen(this.parent);
            return;
        }
        int totalWidth = 364;
        this.colX1 = (this.width - totalWidth) / 2;
        this.colX2 = this.colX1 + 175 + 14;
        this.colY = Math.max(15, (this.height - 240) / 2);
        this.colHeight = Math.min(260, this.height - 30);
        context.pose().pushMatrix();
        context.pose().translate(0.0f, (1.0f - progress) * -160.0f);
        List<File> shown = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
        this.renderListColumn(context, mouseX, mouseY, this.colX1, shown);
        this.renderPreviewColumn(context, mouseX, mouseY, this.colX2);
        context.pose().popMatrix();
    }

    private List<File> filterFiles(List<File> source, String q) {
        String lower = q.toLowerCase().trim();
        return source.stream().filter(f -> f.getName().toLowerCase().contains(lower)).toList();
    }

    private void renderListColumn(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, List<File> files) {
        Renderer2D.renderQuad(context, x, this.colY, x + 175, this.colY + 15, new Color(20, 20, 25, 220));
        Renderer2D.renderQuad(context, x, this.colY + 15 - 1, x + 175, this.colY + 15, ClickGuiScreen.getButtonColor(this.colY, 200));
        if (this.query.isEmpty()) {
            String placeholder = "Images (search...)";
            int cursorWidth = Night.FONT_MANAGER.getWidth(" ");
            if (this.showLine) {
                Night.FONT_MANAGER.drawTextWithShadow(context, "|", x + 3, this.colY + 3, Color.WHITE);
            }
            Night.FONT_MANAGER.drawTextWithShadow(context, placeholder, x + 3 + cursorWidth, this.colY + 3, Color.GRAY);
        } else {
            String display = this.query + (this.showLine ? "|" : " ");
            Night.FONT_MANAGER.drawTextWithShadow(context, display, x + 3, this.colY + 3, Color.WHITE);
        }
        int bodyY = this.colY + 15;
        int bodyHeight = this.colHeight - 15;
        Renderer2D.renderQuad(context, x, bodyY, x + 175, bodyY + bodyHeight, new Color(15, 15, 20, 190));
        Renderer2D.renderOutline(context, x, this.colY, x + 175, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        if (files.isEmpty()) {
            Night.FONT_MANAGER.drawTextWithShadow(context, "No images found in:", x + 10, bodyY + 20, Color.GRAY);
            Night.FONT_MANAGER.drawTextWithShadow(context, "Night/images", x + 10, bodyY + 32, ClickGuiScreen.getButtonColor(bodyY, 220));
            int btnY = bodyY + 55;
            int btnH = 16;
            boolean btnHover = mouseX >= x + 10 && mouseX <= x + 175 - 10 && mouseY >= btnY && mouseY <= btnY + btnH;
            Color btnColor = btnHover ? ClickGuiScreen.getButtonColor(btnY, 200) : new Color(30, 30, 40, 200);
            Renderer2D.renderQuad(context, x + 10, btnY, x + 175 - 10, btnY + btnH, btnColor);
            Renderer2D.renderOutline(context, x + 10, btnY, x + 175 - 10, btnY + btnH, ClickGuiScreen.getButtonColor(btnY, 150));
            int textW = Night.FONT_MANAGER.getWidth("Open Folder");
            Night.FONT_MANAGER.drawTextWithShadow(context, "Open Folder", x + (175 - textW) / 2, btnY + 4, Color.WHITE);
            return;
        }
        context.enableScissor(x, bodyY, x + 175, bodyY + bodyHeight);
        int rowsVisible = bodyHeight / 18 + 2;
        int firstIndex = (int)this.scrollList;
        for (int i = 0; i < rowsVisible; ++i) {
            int index = firstIndex + i;
            if (index < 0 || index >= files.size()) continue;
            File file = files.get(index);
            int rowY = bodyY + Math.round((float)(i * 18) - (this.scrollList - (float)firstIndex) * 18.0f);
            if (rowY + 18 < bodyY || rowY > bodyY + bodyHeight) continue;
            this.renderRow(context, mouseX, mouseY, x, rowY, file);
        }
        context.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int rowY, File file) {
        Color bg;
        boolean hovering;
        String fileName = file.getName();
        boolean isSelected = fileName.equals(this.selectedPreviewName);
        boolean isCurrentActive = fileName.equals(this.setting.getValue());
        boolean bl = hovering = mouseX >= x && mouseX < x + 175 && mouseY >= rowY && mouseY < rowY + 18 - 1;
        bg = isSelected ? new Color(255, 255, 255, 30) : (hovering ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0));
        if (bg.getAlpha() > 0) {
            Renderer2D.renderQuad(context, x, rowY, x + 175, rowY + 18 - 1, bg);
        }
        if (isCurrentActive) {
            Renderer2D.renderQuad(context, x, rowY, x + 3, rowY + 18 - 1, ClickGuiScreen.getButtonColor(rowY, 255));
        }
        Color nameColor = isCurrentActive ? Color.YELLOW : (isSelected ? ClickGuiScreen.getButtonColor(rowY, 255) : Color.WHITE);
        Object renderText = fileName;
        if (Night.FONT_MANAGER.getWidth((String)renderText) > 155) {
            while (Night.FONT_MANAGER.getWidth((String)renderText + "...") > 155 && ((String)renderText).length() > 3) {
                renderText = ((String)renderText).substring(0, ((String)renderText).length() - 1);
            }
            renderText = (String)renderText + "...";
        }
        Night.FONT_MANAGER.drawTextWithShadow(context, (String)renderText, x + 8, rowY + (18 - Night.FONT_MANAGER.getHeight()) / 2, nameColor);
    }

    private void renderPreviewColumn(GuiGraphicsExtractor context, int mouseX, int mouseY, int x) {
        Renderer2D.renderQuad(context, x, this.colY, x + 175, this.colY + 15, new Color(20, 20, 25, 220));
        Renderer2D.renderQuad(context, x, this.colY + 15 - 1, x + 175, this.colY + 15, ClickGuiScreen.getButtonColor(this.colY, 200));
        Object title = "Preview" + (String)(this.selectedPreviewName != null ? ": " + this.selectedPreviewName : "");
        if (Night.FONT_MANAGER.getWidth((String)title) > 165) {
            title = "Preview";
        }
        Night.FONT_MANAGER.drawTextWithShadow(context, (String)title, x + 5, this.colY + 3, Color.WHITE);
        int bodyY = this.colY + 15;
        int bodyHeight = this.colHeight - 15;
        Renderer2D.renderQuad(context, x, bodyY, x + 175, bodyY + bodyHeight, new Color(15, 15, 20, 190));
        Renderer2D.renderOutline(context, x, this.colY, x + 175, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        if (this.currentPreviewEntry != null && this.currentPreviewEntry.texture != null && this.currentPreviewEntry.identifier != null) {
            int drawH;
            int drawW;
            int previewBoxPad = 12;
            int boxX = x + previewBoxPad;
            int boxY = bodyY + previewBoxPad;
            int boxW = 175 - previewBoxPad * 2;
            int boxH = bodyHeight - previewBoxPad * 2 - 28;
            Renderer2D.renderQuad(context, boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, new Color(5, 5, 10, 220));
            Renderer2D.renderOutline(context, boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, new Color(40, 40, 50, 150));
            float imgAspect = (float)this.currentPreviewEntry.width / (float)Math.max(1, this.currentPreviewEntry.height);
            float boxAspect = (float)boxW / (float)boxH;
            if (imgAspect > boxAspect) {
                drawW = boxW;
                drawH = Math.max(1, Math.round((float)boxW / imgAspect));
            } else {
                drawH = boxH;
                drawW = Math.max(1, Math.round((float)boxH * imgAspect));
            }
            int drawX = boxX + (boxW - drawW) / 2;
            int drawY = boxY + (boxH - drawH) / 2;
            context.blit(RenderPipelines.GUI_TEXTURED, this.currentPreviewEntry.identifier, drawX, drawY, 0.0f, 0.0f, drawW, drawH, drawW, drawH, drawW, drawH, -1);
            String dimText = this.currentPreviewEntry.width + " x " + this.currentPreviewEntry.height + " px";
            int dimW = Night.FONT_MANAGER.getWidth(dimText);
            Night.FONT_MANAGER.drawTextWithShadow(context, dimText, x + (175 - dimW) / 2, boxY + boxH + 4, Color.LIGHT_GRAY);
            String tipText = "Double-click item to select";
            int tipW = Night.FONT_MANAGER.getWidth(tipText);
            Night.FONT_MANAGER.drawTextWithShadow(context, tipText, x + (175 - tipW) / 2, boxY + boxH + 15, Color.GRAY);
        } else {
            String msg = "Select an image on the left";
            int msgW = Night.FONT_MANAGER.getWidth(msg);
            Night.FONT_MANAGER.drawTextWithShadow(context, msg, x + (175 - msgW) / 2, bodyY + bodyHeight / 2 - 6, Color.GRAY);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }
        int bodyY = this.colY + 15;
        int bodyHeight = this.colHeight - 15;
        if (this.allFiles.isEmpty() && mouseX >= (double)(this.colX1 + 10) && mouseX <= (double)(this.colX1 + 175 - 10) && mouseY >= (double)(bodyY + 55) && mouseY <= (double)(bodyY + 71)) {
            try {
                Desktop.getDesktop().open(ImageManager.IMAGES_DIR.toFile());
            }
            catch (Exception exception) {
                // empty catch block
            }
            return true;
        }
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 175) && mouseY >= (double)bodyY && mouseY < (double)(bodyY + bodyHeight)) {
            List<File> shown = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
            int relativeY = (int)(mouseY - (double)bodyY);
            int index = (int)this.scrollList + relativeY / 18;
            if (index >= 0 && index < shown.size()) {
                File clickedFile = shown.get(index);
                String fileName = clickedFile.getName();
                long now = System.currentTimeMillis();
                if (doubleClick || fileName.equals(this.lastClickedItem) && now - this.lastClickTime < 350L) {
                    this.setting.setValue(fileName);
                    if (Night.IMAGE_MANAGER != null) {
                        Night.IMAGE_MANAGER.setCurrentActiveImage(fileName);
                    }
                    this.requestClose();
                    return true;
                }
                this.selectedPreviewName = fileName;
                this.lastClickedItem = fileName;
                this.lastClickTime = now;
                this.setting.setValue(fileName);
                if (Night.IMAGE_MANAGER != null) {
                    Night.IMAGE_MANAGER.setCurrentActiveImage(fileName);
                }
                this.updatePreview();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 175)) {
            List<File> shown = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
            int bodyHeight = this.colHeight - 15;
            float maxScroll = Math.max(0.0f, (float)shown.size() - (float)bodyHeight / 18.0f);
            this.targetScrollList = Math.max(0.0f, Math.min(this.targetScrollList - (float)verticalAmount, maxScroll));
            this.scrollList += (this.targetScrollList - this.scrollList) * 0.5f;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) {
            this.requestClose();
            return true;
        }
        if (keyCode == 259 && !this.query.isEmpty()) {
            this.query = this.query.substring(0, this.query.length() - 1);
            this.scrollList = 0.0f;
            this.targetScrollList = 0.0f;
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        char chr = (char)event.codepoint();
        if (!Character.isISOControl(chr)) {
            this.query = this.query + chr;
            this.scrollList = 0.0f;
            this.targetScrollList = 0.0f;
            return true;
        }
        return super.charTyped(event);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

