/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  lombok.Generated
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package night.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.WhitelistEditorScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.settings.impl.WhitelistSetting;
import night.utils.graphics.Renderer2D;
import night.utils.minecraft.IdentifierUtils;

public class WhitelistButton
extends Button {
    private final WhitelistSetting setting;
    private boolean open = false;
    private String searchQuery = "";
    private boolean searching = false;
    private boolean selecting = false;
    private int cursorPos = 0;
    private String oldSearchQuery = "";
    private boolean draggingScrollbar = false;
    private int dragStartY = 0;
    private float dragStartScroll = 0.0f;
    private final List<String> allElements;
    private List<String> visibleElements = new ArrayList<String>();
    private final int maxDisplayed = 6;
    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private final int searchBarHeight = 12;
    private final int elementHeight = 12;
    private final int offsetX = 2;
    private final int scrollbarWidth = 5;
    private final int symbolWidth = 15;

    public WhitelistButton(WhitelistSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
        this.allElements = this.getAllElements();
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        boolean queryChanged = !this.searchQuery.equals(this.oldSearchQuery);
        this.oldSearchQuery = this.searchQuery;
        float ratio = 0.0f;
        if (queryChanged) {
            ratio = this.getScrollRatio();
        }
        this.updateVisibleElements();
        this.clampScrollValues();
        if (queryChanged) {
            this.applyScrollRatio(ratio);
            this.clampScrollValues();
        }
        this.scrollOffset += (this.targetScrollOffset - this.scrollOffset) * 0.5f;
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + 13 - 1, ClickGuiScreen.getButtonColor(this.getY(), 100));
        Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getPadding() + 2, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 30));
        Night.FONT_MANAGER.drawTextWithShadow(context, this.setting.getTag(), this.getX() + this.getTextPadding() + 1, this.getY() + 2, Color.WHITE);
        Night.FONT_MANAGER.drawTextWithShadow(context, String.valueOf(ChatFormatting.GRAY) + this.setting.getWhitelist().size(), this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth("" + this.setting.getWhitelist().size()), this.getY() + 2, Color.WHITE);
        if (this.open) {
            String cursorChar;
            int contentY = this.getY() + this.getParent().getHeight();
            Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1 + 2, contentY, this.getX() + this.getWidth() - this.getPadding() - 1, contentY + 12, ClickGuiScreen.getButtonColor(this.getY(), 30));
            Object displayedSearch = this.searchQuery;
            if (this.searching) {
                String string = cursorChar = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                if (this.cursorPos <= ((String)displayedSearch).length()) {
                    displayedSearch = ((String)displayedSearch).substring(0, this.cursorPos) + cursorChar + ((String)displayedSearch).substring(this.cursorPos);
                }
            } else {
                displayedSearch = "Search...";
            }
            if (this.searching && this.searchQuery.isEmpty()) {
                cursorChar = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                Night.FONT_MANAGER.drawTextWithShadow(context, cursorChar, this.getX() + this.getTextPadding() + 2 + 2, contentY + 6 - Night.FONT_MANAGER.getHeight() / 2, Color.WHITE);
                Night.FONT_MANAGER.drawTextWithShadow(context, "Search...", this.getX() + this.getTextPadding() + 2 + 2 + Night.FONT_MANAGER.getWidth(" "), contentY + 6 - Night.FONT_MANAGER.getHeight() / 2, Color.GRAY);
            } else {
                Night.FONT_MANAGER.drawTextWithShadow(context, (String)displayedSearch, this.getX() + this.getTextPadding() + 2 + 2, contentY + 6 - Night.FONT_MANAGER.getHeight() / 2, this.searching ? (this.selecting ? ClickGuiScreen.getButtonColor(this.getY(), 255) : Color.WHITE) : Color.GRAY);
            }
            int listStartY = contentY + 12;
            int renderCount = Math.min(this.visibleElements.size(), 6);
            int scrollbarX = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
            int scrollbarFullHeight = 72;
            List<String> whitelistIds = this.setting.getWhitelistIds();
            String[] words = this.searchQuery.isEmpty() ? new String[]{} : this.searchQuery.toLowerCase().split(" ");
            for (int i = 0; i < renderCount; ++i) {
                boolean symbolHovered;
                int index = (int)((float)i + this.scrollOffset);
                if (index < 0 || index >= this.visibleElements.size()) continue;
                String registryId = this.visibleElements.get(index);
                boolean inWhitelist = whitelistIds.contains(registryId);
                String renderText = registryId.replace("minecraft:", "");
                Color normalColor = inWhitelist ? Color.WHITE : Color.GRAY;
                Color highlightColor = inWhitelist ? Color.YELLOW : Color.LIGHT_GRAY;
                int itemY = listStartY + i * 12;
                Renderer2D.renderQuad(context, this.getX() + this.getPadding() + 1 + 2, itemY, this.getX() + this.getWidth() - this.getPadding() - 1, itemY + 12, ClickGuiScreen.getButtonColor(this.getY(), 30));
                int textX = this.getX() + this.getTextPadding() + 2 + 2;
                int symbolX = scrollbarX - 15 + 5;
                boolean textHovered = mouseX >= textX && mouseX <= symbolX && mouseY >= itemY && mouseY < itemY + 12;
                List<ColoredSegment> segments = this.highlightMatches(renderText, words, normalColor, highlightColor);
                int textWidth = 0;
                for (ColoredSegment seg : segments) {
                    textWidth += Night.FONT_MANAGER.getWidth(seg.text);
                }
                int availableSpace = symbolX - textX;
                List<ColoredSegment> drawSegments = textHovered || textWidth <= availableSpace ? segments : this.truncateSegments(segments, availableSpace);
                int drawX = textX;
                for (ColoredSegment seg : drawSegments) {
                    Night.FONT_MANAGER.drawTextWithShadow(context, seg.text, drawX, itemY + 2, seg.color);
                    drawX += Night.FONT_MANAGER.getWidth(seg.text);
                }
                String symbol = !this.searching || inWhitelist ? "-" : "+";
                int symbolRenderX = scrollbarX - 15 + 5;
                boolean bl = symbolHovered = mouseX >= symbolRenderX && mouseX <= symbolRenderX + 15 && mouseY >= itemY && mouseY < itemY + 12;
                Color symbolColor = symbol.equals("+") ? (symbolHovered ? new Color(0, 255, 0) : Color.WHITE) : (symbolHovered ? new Color(255, 0, 0) : Color.WHITE);
                Night.FONT_MANAGER.drawTextWithShadow(context, symbol, symbolRenderX, itemY + 2, symbolColor);
            }
            if (this.visibleElements.size() > 6) {
                float maxScroll = Math.max(0, this.visibleElements.size() - 6);
                float scrollPercent = maxScroll == 0.0f ? 0.0f : this.scrollOffset / maxScroll;
                int barHeight = (int)((float)scrollbarFullHeight * (6.0f / (float)this.visibleElements.size()));
                if (barHeight < 20) {
                    barHeight = 20;
                }
                int scrollbarPos = (int)((float)(scrollbarFullHeight - barHeight) * scrollPercent);
                Renderer2D.renderQuad(context, scrollbarX, listStartY, scrollbarX + 5, listStartY + scrollbarFullHeight, ClickGuiScreen.getButtonColor(this.getY(), 40));
                Renderer2D.renderQuad(context, scrollbarX, listStartY + scrollbarPos, scrollbarX + 5, listStartY + scrollbarPos + barHeight, ClickGuiScreen.getButtonColor(this.getY(), 200));
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 1) {
            boolean bl = this.open = !this.open;
        }
        if (this.isHovering(mouseX, mouseY) && button == 0 && (!this.open || mouseY < (double)(this.getY() + this.getParent().getHeight()))) {
            WhitelistButton.mc.gui.setScreen((Screen)new WhitelistEditorScreen(this.setting, WhitelistButton.mc.gui.screen()));
            return;
        }
        if (!this.open) {
            return;
        }
        if (button == 0) {
            int relativeY;
            int index;
            int contentY = this.getY() + this.getParent().getHeight();
            if (mouseX >= (double)(this.getX() + this.getPadding() + 1 + 2) && mouseX <= (double)(this.getX() + this.getWidth() - this.getPadding() - 1) && mouseY >= (double)contentY && mouseY <= (double)(contentY + 12)) {
                this.searching = true;
                this.selecting = false;
                this.cursorPos = this.searchQuery.length();
                return;
            }
            if (!this.isWithinOpenArea(mouseX, mouseY)) {
                this.searching = false;
                this.selecting = false;
                this.searchQuery = "";
                this.cursorPos = 0;
            }
            if (this.visibleElements.size() > 6 && this.isHoveringScrollbar(mouseX, mouseY)) {
                this.draggingScrollbar = true;
                this.dragStartY = (int)mouseY;
                this.dragStartScroll = this.scrollOffset;
                return;
            }
            if (!this.isWithinOpenArea(mouseX, mouseY)) {
                return;
            }
            int contentAreaX1 = this.getX() + this.getPadding() + 1 + 2;
            int contentAreaX2 = this.getX() + this.getWidth() - this.getPadding() - 1;
            if (mouseX < (double)contentAreaX1 || mouseX > (double)contentAreaX2) {
                return;
            }
            int contentYArea = this.getY() + this.getParent().getHeight();
            int listStartY = contentYArea + 12;
            if (mouseY >= (double)listStartY && mouseY < (double)(listStartY + 72) && (index = (int)this.scrollOffset + (relativeY = (int)(mouseY - (double)listStartY)) / 12) >= 0 && index < this.visibleElements.size()) {
                String registryId = this.visibleElements.get(index);
                int scrollbarX = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
                int symbolX = scrollbarX - 15 + 5;
                int itemY = listStartY + relativeY / 12 * 12;
                if (mouseX >= (double)symbolX && mouseX <= (double)(symbolX + 15) && mouseY >= (double)itemY && mouseY < (double)(itemY + 12)) {
                    boolean inWhitelist = this.setting.getWhitelistIds().contains(registryId);
                    if (inWhitelist) {
                        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                            this.setting.remove(IdentifierUtils.getBlock(registryId));
                        } else {
                            this.setting.remove(IdentifierUtils.getItem(registryId));
                        }
                        this.setting.remove(registryId);
                    } else if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                        this.setting.add(IdentifierUtils.getBlock(registryId));
                    } else {
                        this.setting.add(IdentifierUtils.getItem(registryId));
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.draggingScrollbar && this.visibleElements.size() > 6) {
            int scrollbarFullHeight = 72;
            float maxScroll = Math.max(0, this.visibleElements.size() - 6);
            float dy = (float)(mouseY - (double)this.dragStartY);
            float ratio = dy / (float)scrollbarFullHeight;
            this.targetScrollOffset = this.dragStartScroll + maxScroll * ratio;
            this.clampScrollValues();
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.open && this.isWithinOpenArea(mouseX, mouseY)) {
            this.targetScrollOffset -= (float)verticalAmount;
            this.clampScrollValues();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.searching) {
            return;
        }
        Window handle = mc.getWindow();
        boolean ctrlPressed = InputConstants.isKeyDown((Window)handle, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (keyCode == 256) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
        } else if (keyCode == 257) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
        } else if (keyCode == 259) {
            if (this.selecting) {
                this.searchQuery = "";
                this.cursorPos = 0;
                this.selecting = false;
            } else if (this.cursorPos > 0) {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos - 1) + this.searchQuery.substring(this.cursorPos);
                --this.cursorPos;
            }
        } else if (keyCode == 261) {
            if (this.cursorPos < this.searchQuery.length()) {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + this.searchQuery.substring(this.cursorPos + 1);
            }
        } else if (keyCode == 263) {
            if (this.cursorPos > 0) {
                --this.cursorPos;
            }
        } else if (keyCode == 262) {
            if (this.cursorPos < this.searchQuery.length()) {
                ++this.cursorPos;
            }
        } else if (ctrlPressed) {
            if (keyCode == 86) {
                try {
                    String clip = WhitelistButton.mc.keyboardHandler.getClipboard();
                    if (clip != null) {
                        this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + clip + this.searchQuery.substring(this.cursorPos);
                        this.cursorPos += clip.length();
                    }
                }
                catch (Exception e) {
                    Night.LOGGER.error("{}: Failed to process clipboard paste", (Object)e.getClass().getName(), (Object)e);
                }
            } else if (keyCode == 67 && this.selecting) {
                try {
                    WhitelistButton.mc.keyboardHandler.setClipboard(this.searchQuery);
                }
                catch (Exception e) {
                    Night.LOGGER.error("{}: Failed to process clipboard change", (Object)e.getClass().getName(), (Object)e);
                }
            } else if (keyCode == 65) {
                this.selecting = true;
                this.cursorPos = this.searchQuery.length();
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.searching && !Character.isISOControl(chr)) {
            if (this.selecting) {
                this.searchQuery = String.valueOf(chr);
                this.selecting = false;
                this.cursorPos = 1;
            } else {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + chr + this.searchQuery.substring(this.cursorPos);
                ++this.cursorPos;
            }
        }
    }

    @Override
    public int getHeight() {
        int baseHeight = this.getParent().getHeight();
        if (!this.open) {
            return baseHeight;
        }
        return baseHeight + 12 + 72;
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return (double)(this.getX() + this.getPadding()) <= mouseX && (double)this.getY() <= mouseY && (double)(this.getX() + this.getWidth() - this.getPadding()) > mouseX && (double)(this.getY() + this.getParent().getHeight()) > mouseY;
    }

    public boolean isHandlingScroll(double mouseX, double mouseY) {
        return this.open && (this.isWithinOpenArea(mouseX, mouseY) || this.draggingScrollbar);
    }

    private boolean isWithinOpenArea(double mouseX, double mouseY) {
        int contentY = this.getY() + this.getParent().getHeight();
        int maxAreaHeight = 84;
        return mouseX >= (double)(this.getX() + this.getPadding() + 1 + 2) && mouseX <= (double)(this.getX() + this.getWidth() - this.getPadding() - 1) && mouseY >= (double)contentY && mouseY <= (double)(contentY + maxAreaHeight);
    }

    private boolean isHoveringScrollbar(double mouseX, double mouseY) {
        if (!this.open || this.visibleElements.size() <= 6) {
            return false;
        }
        int contentY = this.getY() + this.getParent().getHeight();
        int listStartY = contentY + 12;
        int scrollbarX = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
        int scrollbarHeight = 72;
        return mouseX >= (double)scrollbarX && mouseX <= (double)(scrollbarX + 5) && mouseY >= (double)listStartY && mouseY <= (double)(listStartY + scrollbarHeight);
    }

    private void updateVisibleElements() {
        List<String> baseList;
        if (this.searchQuery.isEmpty()) {
            if (this.searching) {
                baseList = new ArrayList<String>(this.allElements);
            } else {
                List<String> whitelistIds = this.setting.getWhitelistIds();
                baseList = this.allElements.stream().filter(whitelistIds::contains).collect(Collectors.toList());
            }
            baseList.sort((id1, id2) -> {
                int cmp = id1.compareToIgnoreCase((String)id2);
                if (cmp == 0) {
                    return Integer.compare(id1.length(), id2.length());
                }
                return cmp;
            });
        } else {
            String[] words = this.searchQuery.toLowerCase().split(" ");
            baseList = this.allElements.stream().filter(id -> {
                String lower = id.toLowerCase();
                for (String w : words) {
                    if (w.isEmpty() || lower.contains(w)) continue;
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            baseList.sort((id1, id2) -> {
                int compare = this.compareBySearchPriority((String)id1, (String)id2, words);
                if (compare != 0) {
                    return compare;
                }
                int cmp = id1.compareToIgnoreCase((String)id2);
                if (cmp == 0) {
                    return Integer.compare(id1.length(), id2.length());
                }
                return cmp;
            });
        }
        this.visibleElements = baseList;
    }

    private int compareBySearchPriority(String id1, String id2, String[] words) {
        int startCount1 = this.countStarts(id1, words);
        int startCount2 = this.countStarts(id2, words);
        return Integer.compare(startCount1, startCount2);
    }

    private int countStarts(String id, String[] words) {
        int count = id.length();
        String lower = id.toLowerCase();
        for (String w : words) {
            int c;
            if (w.isEmpty() || !lower.contains(w.toLowerCase()) || (c = lower.split(w.toLowerCase())[0].length()) >= count) continue;
            count = c;
        }
        return count;
    }

    private float getScrollRatio() {
        int maxSize = this.visibleElements.size();
        int maxScroll = Math.max(0, maxSize - 6);
        if (maxScroll == 0) {
            return 0.0f;
        }
        return this.scrollOffset / (float)maxScroll;
    }

    private void applyScrollRatio(float ratio) {
        float newOffset;
        int maxSize = this.visibleElements.size();
        int maxScroll = Math.max(0, maxSize - 6);
        this.scrollOffset = newOffset = ratio * (float)maxScroll;
        this.targetScrollOffset = newOffset;
    }

    private void clampScrollValues() {
        int maxSize = this.visibleElements.size();
        int maxScroll = Math.max(0, maxSize - 6);
        if (this.targetScrollOffset < 0.0f) {
            this.targetScrollOffset = 0.0f;
        }
        if (this.targetScrollOffset > (float)maxScroll) {
            this.targetScrollOffset = maxScroll;
        }
        if (this.scrollOffset < 0.0f) {
            this.scrollOffset = 0.0f;
        }
        if (this.scrollOffset > (float)maxScroll) {
            this.scrollOffset = maxScroll;
        }
    }

    private List<String> getAllElements() {
        ArrayList<String> temp = new ArrayList<String>();
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach(e -> temp.add(((ResourceKey)e.getKey()).identifier().toString()));
        } else {
            BuiltInRegistries.ITEM.entrySet().forEach(e -> temp.add(((ResourceKey)e.getKey()).identifier().toString()));
        }
        temp.sort((a, b) -> {
            int cmp = a.compareToIgnoreCase((String)b);
            if (cmp == 0) {
                return Integer.compare(a.length(), b.length());
            }
            return cmp;
        });
        return temp;
    }

    private List<ColoredSegment> truncateSegments(List<ColoredSegment> segments, int maxWidth) {
        ArrayList<ColoredSegment> truncated = new ArrayList<ColoredSegment>();
        int currentWidth = 0;
        for (ColoredSegment seg : segments) {
            int segWidth = Night.FONT_MANAGER.getWidth(seg.text);
            if (currentWidth + segWidth <= maxWidth) {
                truncated.add(seg);
                currentWidth += segWidth;
                continue;
            }
            int spaceLeft = maxWidth - currentWidth;
            if (spaceLeft > 0) {
                char c;
                int charWidth;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < seg.text.length() && (charWidth = Night.FONT_MANAGER.getWidth(String.valueOf(c = seg.text.charAt(i)))) <= spaceLeft - Night.FONT_MANAGER.getWidth("\u2026"); ++i) {
                    sb.append(c);
                    spaceLeft -= charWidth;
                }
                sb.append("\u2026");
                truncated.add(new ColoredSegment(sb.toString(), seg.color));
                break;
            }
            truncated.add(new ColoredSegment("\u2026", Color.GRAY));
            break;
        }
        return truncated;
    }

    private List<ColoredSegment> highlightMatches(String text, String[] words, Color normalColor, Color highlightColor) {
        if (words.length == 0) {
            ArrayList<ColoredSegment> segments = new ArrayList<ColoredSegment>();
            segments.add(new ColoredSegment(text, normalColor));
            return segments;
        }
        String lowerText = text.toLowerCase();
        List<Range> ranges = new ArrayList<Range>();
        for (String w : words) {
            int idx;
            if (w.isEmpty()) continue;
            int start = 0;
            while ((idx = lowerText.indexOf(w, start)) != -1) {
                ranges.add(new Range(idx, idx + w.length()));
                start = idx + w.length();
            }
        }
        ranges = this.mergeRanges(ranges);
        ArrayList<ColoredSegment> segments = new ArrayList<ColoredSegment>();
        int currentIndex = 0;
        for (Range r : ranges) {
            if (r.start > currentIndex) {
                segments.add(new ColoredSegment(text.substring(currentIndex, r.start), normalColor));
            }
            segments.add(new ColoredSegment(text.substring(r.start, r.end), highlightColor));
            currentIndex = r.end;
        }
        if (currentIndex < text.length()) {
            segments.add(new ColoredSegment(text.substring(currentIndex), normalColor));
        }
        return segments;
    }

    private List<Range> mergeRanges(List<Range> ranges) {
        if (ranges.isEmpty()) {
            return ranges;
        }
        ranges.sort(Comparator.comparingInt(r -> r.start));
        ArrayList<Range> merged = new ArrayList<Range>();
        Range current = ranges.getFirst();
        for (int i = 1; i < ranges.size(); ++i) {
            Range next = ranges.get(i);
            if (next.start <= current.end) {
                current.end = Math.max(current.end, next.end);
                continue;
            }
            merged.add(current);
            current = next;
        }
        merged.add(current);
        return merged;
    }

    private static class ColoredSegment {
        String text;
        Color color;

        @Generated
        public ColoredSegment(String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }

    private static class Range {
        int start;
        int end;

        @Generated
        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}

