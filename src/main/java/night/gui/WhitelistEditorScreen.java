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
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 */
package night.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import night.Night;
import night.gui.ClickGuiScreen;
import night.settings.impl.WhitelistSetting;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.graphics.Renderer2D;
import night.utils.minecraft.IdentifierUtils;
import night.utils.system.Timer;

public class WhitelistEditorScreen
extends Screen {
    private static final int COL_WIDTH = 150;
    private static final int COL_GAP = 10;
    private static final int HEADER_H = 13;
    private static final int ROW_H = 18;
    private static final int ICON_SIZE = 16;
    private static final int SLIDE_DURATION_MS = 320;
    private static final float SLIDE_DISTANCE = 160.0f;
    private static final int SEARCH_STAGGER_MS = 40;
    private static final int SEARCH_ROW_ANIM_MS = 220;
    private static final float SEARCH_SLIDE_DISTANCE = 40.0f;
    private final WhitelistSetting setting;
    private final Screen parent;
    private final List<Entry> allEntries;
    private String query = "";
    private String lastQuery = "";
    private long queryChangeTime = System.currentTimeMillis();
    private float scrollAll = 0.0f;
    private float targetScrollAll = 0.0f;
    private float scrollWhite = 0.0f;
    private float targetScrollWhite = 0.0f;
    private final Animation slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
    private boolean closing = false;
    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private int colX1;
    private int colX2;
    private int colY;
    private int colHeight;
    private static final String GROUP_PREFIX = "group:";
    private static final String[] DYE_COLORS = new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final int MIN_FAMILY_SIZE = 8;

    public WhitelistEditorScreen(WhitelistSetting setting, Screen parent) {
        super((Component)Component.literal((String)"night-whitelist-editor"));
        this.setting = setting;
        this.parent = parent;
        this.allEntries = WhitelistEditorScreen.buildEntries(setting);
    }

    private static String familyOf(String path) {
        for (String color : DYE_COLORS) {
            if (path.equals(color)) {
                return "";
            }
            if (!path.startsWith(color + "_")) continue;
            return path.substring(color.length() + 1);
        }
        return null;
    }

   private static List<WhitelistEditorScreen.Entry> buildEntries(WhitelistSetting setting) {
      List<WhitelistEditorScreen.Entry> entries = new ArrayList<>();
      Map<String, List<WhitelistEditorScreen.Entry>> families = new LinkedHashMap<>();
      if (setting.getType() == WhitelistSetting.Type.BLOCKS) {
         BuiltInRegistries.BLOCK.entrySet().forEach(e -> {
            Block block = e.getValue();
            String id = e.getKey().identifier().toString();
            Item item = block.asItem();
            ItemStack icon = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
            WhitelistEditorScreen.Entry entry = new WhitelistEditorScreen.Entry(id, id.replace("minecraft:", ""), icon, block);
            entries.add(entry);
            String family = familyOf(id.substring("minecraft:".length()));
            if (family != null && !family.isEmpty()) {
               families.computeIfAbsent(family, k -> new ArrayList<>()).add(entry);
            }
         });
      } else if (setting.getType() == WhitelistSetting.Type.POTIONS) {
         BuiltInRegistries.POTION.entrySet().forEach(e -> {
            Potion potionType = e.getValue();
            String id = e.getKey().identifier().toString();
            Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potionType);
            ItemStack icon = new ItemStack(Items.SPLASH_POTION);
            icon.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
            String labelx = "Splash Potion of " + potionType.name().replace("_", " ");
            entries.add(new WhitelistEditorScreen.Entry(id, labelx, icon, potionType));
         });
      } else {
         BuiltInRegistries.ITEM.entrySet().forEach(e -> {
            Item item = e.getValue();
            String id = e.getKey().identifier().toString();
            WhitelistEditorScreen.Entry entry = new WhitelistEditorScreen.Entry(id, id.replace("minecraft:", ""), new ItemStack(item), item);
            entries.add(entry);
            String family = familyOf(id.substring("minecraft:".length()));
            if (family != null && !family.isEmpty()) {
               families.computeIfAbsent(family, k -> new ArrayList<>()).add(entry);
            }
         });
      }

      for (Map.Entry<String, List<WhitelistEditorScreen.Entry>> f : families.entrySet()) {
         if (f.getValue().size() >= 8) {
            WhitelistEditorScreen.Entry sample = f.getValue().get(0);
            String label = f.getKey().replace("_", " ");
            entries.add(new WhitelistEditorScreen.Entry("group:" + f.getKey(), label + " (all colors)", sample.icon, null));
         }
      }

      entries.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
      return entries;
   }

    public void requestClose() {
        this.closing = true;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Renderer2D.renderQuad(context, 0.0f, 0.0f, this.width, this.height, new Color(10, 8, 18, 150));
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
        int totalWidth = 310;
        this.colX1 = (this.width - totalWidth) / 2;
        this.colX2 = this.colX1 + 150 + 10;
        this.colY = 20;
        this.colHeight = this.height - 40;
        context.pose().pushMatrix();
        context.pose().translate(0.0f, (1.0f - progress) * -160.0f);
        List<String> whitelistIds = this.setting.getWhitelistIds();
        List<Entry> whitelistEntries = this.allEntries.stream().filter(e -> whitelistIds.contains(e.id)).toList();
        List<Entry> pool = this.allEntries.stream().filter(e -> !whitelistIds.contains(e.id)).toList();
        List<Entry> shown = this.query.isEmpty() ? pool : this.filter(pool, this.query);
        this.renderSearchColumn(context, mouseX, mouseY, this.colX1, shown, whitelistIds);
        this.renderColumn(context, mouseX, mouseY, this.colX2, "Whitelist (" + whitelistEntries.size() + ")", whitelistEntries, whitelistIds, this.scrollWhite, 2);
        context.pose().popMatrix();
    }

    private List<Entry> filter(List<Entry> source, String q) {
        String[] words = q.toLowerCase().split(" ");
        return source.stream().filter(e -> {
            String lower = e.name.toLowerCase();
            for (String w : words) {
                if (w.isEmpty() || lower.contains(w)) continue;
                return false;
            }
            return true;
        }).toList();
    }

    private void renderColumn(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, String title, List<Entry> entries, List<String> whitelistIds, float scroll, int colIndex) {
        Renderer2D.renderQuad(context, x, this.colY, x + 150, this.colY + 13, new Color(20, 20, 25, 200));
        Renderer2D.renderQuad(context, x, this.colY + 13 - 1, x + 150, this.colY + 13, ClickGuiScreen.getButtonColor(this.colY, 200));
        Night.FONT_MANAGER.drawTextWithShadow(context, title, x + 3, this.colY + 2, Color.WHITE);
        int bodyY = this.colY + 13;
        int bodyHeight = this.colHeight - 13;
        Renderer2D.renderQuad(context, x, bodyY, x + 150, bodyY + bodyHeight, new Color(15, 15, 20, 180));
        Renderer2D.renderOutline(context, x, this.colY, x + 150, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        context.enableScissor(x, bodyY, x + 150, bodyY + bodyHeight);
        int rowsVisible = bodyHeight / 18 + 2;
        int firstIndex = (int)scroll;
        for (int i = 0; i < rowsVisible; ++i) {
            int index = firstIndex + i;
            if (index < 0 || index >= entries.size()) continue;
            Entry entry = entries.get(index);
            int rowY = bodyY + Math.round((float)(i * 18) - (scroll - (float)firstIndex) * 18.0f);
            if (rowY + 18 < bodyY || rowY > bodyY + bodyHeight) continue;
            this.renderRow(context, mouseX, mouseY, x, rowY, entry, whitelistIds.contains(entry.id), colIndex);
        }
        context.disableScissor();
    }

    private void renderSearchColumn(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, List<Entry> results, List<String> whitelistIds) {
        Renderer2D.renderQuad(context, x, this.colY, x + 150, this.colY + 13, new Color(20, 20, 25, 200));
        Renderer2D.renderQuad(context, x, this.colY + 13 - 1, x + 150, this.colY + 13, ClickGuiScreen.getButtonColor(this.colY, 200));
        if (this.query.isEmpty()) {
            String placeholder = "Items (search...)";
            int cursorWidth = Night.FONT_MANAGER.getWidth(" ");
            if (this.showLine) {
                Night.FONT_MANAGER.drawTextWithShadow(context, "|", x + 3, this.colY + 2, Color.WHITE);
            }
            Night.FONT_MANAGER.drawTextWithShadow(context, placeholder, x + 3 + cursorWidth, this.colY + 2, Color.GRAY);
        } else {
            String display = this.query + (this.showLine ? "|" : " ");
            Night.FONT_MANAGER.drawTextWithShadow(context, display, x + 3, this.colY + 2, Color.WHITE);
        }
        int bodyY = this.colY + 13;
        int bodyHeight = this.colHeight - 13;
        Renderer2D.renderQuad(context, x, bodyY, x + 150, bodyY + bodyHeight, new Color(15, 15, 20, 180));
        Renderer2D.renderOutline(context, x, this.colY, x + 150, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        context.enableScissor(x, bodyY, x + 150, bodyY + bodyHeight);
        long now = System.currentTimeMillis();
        int rowsVisible = bodyHeight / 18 + 2;
        int firstIndex = (int)this.scrollAll;
        for (int i = 0; i < rowsVisible; ++i) {
            int index = firstIndex + i;
            if (index < 0 || index >= results.size()) continue;
            Entry entry = results.get(index);
            int rowY = bodyY + Math.round((float)(i * 18) - (this.scrollAll - (float)firstIndex) * 18.0f);
            if (rowY + 18 < bodyY || rowY > bodyY + bodyHeight) continue;
            float delayed = now - this.queryChangeTime - (long)(index * 40);
            float t = Easing.ease(Mth.clamp((float)(delayed / 220.0f), (float)0.0f, (float)1.0f), Easing.Method.EASE_OUT_CUBIC);
            int slideX = Math.round((1.0f - t) * 40.0f);
            context.pose().pushMatrix();
            context.pose().translate((float)(-slideX), 0.0f);
            this.renderRow(context, mouseX, mouseY, x, rowY, entry, whitelistIds.contains(entry.id), 1);
            context.pose().popMatrix();
        }
        context.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int rowY, Entry entry, boolean inWhitelist, int colIndex) {
        Color bg;
        boolean hovering;
        boolean bl = hovering = mouseX >= x && mouseX < x + 150 && mouseY >= rowY && mouseY < rowY + 18 - 1;
        bg = hovering ? new Color(255, 255, 255, 15) : (inWhitelist ? new Color(255, 255, 255, 8) : new Color(0, 0, 0, 0));
        if (bg.getAlpha() > 0) {
            Renderer2D.renderQuad(context, x, rowY, x + 150, rowY + 18 - 1, bg);
        }
        if (!entry.icon.isEmpty()) {
            context.item(entry.icon, x + 2, rowY + 0);
        }
        Color nameColor = inWhitelist ? Color.YELLOW : Color.WHITE;
        Night.FONT_MANAGER.drawTextWithShadow(context, entry.name, x + 16 + 6, rowY + 8 - Night.FONT_MANAGER.getHeight() / 2, nameColor);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }
        List<String> whitelistIds = this.setting.getWhitelistIds();
        int bodyY = this.colY + 13;
        int bodyHeight = this.colHeight - 13;
        if (mouseY < (double)bodyY || mouseY >= (double)(bodyY + bodyHeight)) {
            return super.mouseClicked(event, doubleClick);
        }
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 150)) {
            List<Entry> pool = this.allEntries.stream().filter(e -> !whitelistIds.contains(e.id)).toList();
            List<Entry> shown = this.query.isEmpty() ? pool : this.filter(pool, this.query);
            Entry clicked = this.pickRow(mouseX, mouseY, bodyY, this.scrollAll, shown);
            if (clicked != null) {
                this.addToWhitelist(clicked);
            }
            return true;
        }
        if (mouseX >= (double)this.colX2 && mouseX < (double)(this.colX2 + 150)) {
            List<Entry> whitelistEntries = this.allEntries.stream().filter(e -> whitelistIds.contains(e.id)).toList();
            Entry clicked = this.pickRow(mouseX, mouseY, bodyY, this.scrollWhite, whitelistEntries);
            if (clicked != null) {
                this.removeFromWhitelist(clicked);
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private Entry pickRow(double mouseX, double mouseY, int bodyY, float scroll, List<Entry> entries) {
        int relativeY = (int)(mouseY - (double)bodyY);
        int index = (int)scroll + relativeY / 18;
        if (index < 0 || index >= entries.size()) {
            return null;
        }
        return entries.get(index);
    }

    private void addToWhitelist(Entry entry) {
        if (entry.id.startsWith(GROUP_PREFIX)) {
            String family = entry.id.substring(GROUP_PREFIX.length());
            if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                BuiltInRegistries.BLOCK.entrySet().forEach(e -> {
                    String path = ((ResourceKey)e.getKey()).identifier().toString().substring("minecraft:".length());
                    if (family.equals(WhitelistEditorScreen.familyOf(path)) || path.equals(family)) {
                        this.setting.add(e.getValue());
                    }
                });
            } else {
                BuiltInRegistries.ITEM.entrySet().forEach(e -> {
                    String path = ((ResourceKey)e.getKey()).identifier().toString().substring("minecraft:".length());
                    if (family.equals(WhitelistEditorScreen.familyOf(path)) || path.equals(family)) {
                        this.setting.add(e.getValue());
                    }
                });
            }
            return;
        }
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.add(IdentifierUtils.getBlock(entry.id));
        } else if (this.setting.getType() == WhitelistSetting.Type.POTIONS) {
            this.setting.add(IdentifierUtils.getPotion(entry.id));
        } else {
            this.setting.add(IdentifierUtils.getItem(entry.id));
        }
    }

    private void removeFromWhitelist(Entry entry) {
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.remove(IdentifierUtils.getBlock(entry.id));
        } else if (this.setting.getType() == WhitelistSetting.Type.POTIONS) {
            this.setting.remove(IdentifierUtils.getPotion(entry.id));
        } else {
            this.setting.remove(IdentifierUtils.getItem(entry.id));
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 150)) {
            List<String> whitelistIds = this.setting.getWhitelistIds();
            List<Entry> pool = this.allEntries.stream().filter(e -> !whitelistIds.contains(e.id)).toList();
            List<Entry> shown = this.query.isEmpty() ? pool : this.filter(pool, this.query);
            this.targetScrollAll = this.clampScroll(this.targetScrollAll - (float)verticalAmount, shown.size());
        } else if (mouseX >= (double)this.colX2 && mouseX < (double)(this.colX2 + 150)) {
            this.targetScrollWhite = this.clampScroll(this.targetScrollWhite - (float)verticalAmount, this.setting.getWhitelistIds().size());
        }
        this.scrollAll += (this.targetScrollAll - this.scrollAll) * 0.5f;
        this.scrollWhite += (this.targetScrollWhite - this.scrollWhite) * 0.5f;
        return true;
    }

    private float clampScroll(float value, int entryCount) {
        int bodyHeight = this.colHeight - 13;
        float maxScroll = Math.max(0.0f, (float)entryCount - (float)bodyHeight / 18.0f);
        return Math.max(0.0f, Math.min(value, maxScroll));
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) {
            this.requestClose();
            return true;
        }
        if (keyCode == 259 && !this.query.isEmpty()) {
            this.setQuery(this.query.substring(0, this.query.length() - 1));
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        char chr = (char)event.codepoint();
        if (!Character.isISOControl(chr)) {
            this.setQuery(this.query + chr);
            return true;
        }
        return super.charTyped(event);
    }

    private void setQuery(String newQuery) {
        if (!newQuery.equals(this.lastQuery)) {
            this.queryChangeTime = System.currentTimeMillis();
            this.lastQuery = newQuery;
            this.scrollAll = 0.0f;
            this.targetScrollAll = 0.0f;
        }
        this.query = newQuery;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class Entry {
        final String id;
        final String name;
        final ItemStack icon;

        Entry(String id, String name, ItemStack icon, Object registryObject) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }
    }
}

