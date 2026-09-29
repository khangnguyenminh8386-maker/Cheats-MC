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
 *  net.minecraft.network.chat.Component
 */
package night.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import night.Night;
import night.gui.api.Button;
import night.gui.api.ExpandableRow;
import night.gui.api.Frame;
import night.gui.impl.BindButton;
import night.gui.impl.BooleanButton;
import night.gui.impl.CategoryButton;
import night.gui.impl.ColorButton;
import night.gui.impl.HudElementButton;
import night.gui.impl.ModeButton;
import night.gui.impl.NumberButton;
import night.gui.impl.StringButton;
import night.gui.impl.WhitelistButton;
import night.managers.FontManager;
import night.managers.HudElementRegistry;
import night.modules.impl.core.HUDEditorModule;
import night.modules.impl.core.HUDModule;
import night.settings.Setting;
import night.settings.impl.BindSetting;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.ColorSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.PositionSetting;
import night.settings.impl.StringSetting;
import night.settings.impl.WhitelistSetting;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer2D;

public class HUDEditorScreen
extends Screen {
    private String draggingElement = null;
    private float dragOffsetX;
    private float dragOffsetY;
    private final Frame elementsFrame;

    public HUDEditorScreen() {
        super((Component)Component.literal((String)"night-hud-editor"));
        int guiWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int guiHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int frameX = Math.round((float)guiWidth * 0.23f);
        int frameY = Math.round((float)guiHeight * 0.14f);
        this.elementsFrame = new Frame("HUD", List.of(), frameX, frameY, 110, 13);
        HUDModule hudModule = Night.MODULE_MANAGER.getModule(HUDModule.class);
        List<Setting> hudSettings = hudModule.getSettings();
        HashSet<CategorySetting> boundCategories = new HashSet<CategorySetting>();
        HashSet<BooleanSetting> elementEnabledSettings = new HashSet<BooleanSetting>();
        for (HudElementRegistry.Element element : HudElementRegistry.getElements().values()) {
            if (element.category() != null) {
                boundCategories.add(element.category());
            }
            if (element.enabled() != null) {
                elementEnabledSettings.add(element.enabled());
            }
            ArrayList<Setting> ownSettings = new ArrayList<Setting>();
            if (element.category() != null) {
                ownSettings.addAll(hudSettings.stream().filter(s -> {
                    CategorySetting.Visibility v;
                    Setting.Visibility patt0$temp = s.getVisibility();
                    return patt0$temp instanceof CategorySetting.Visibility && (v = (CategorySetting.Visibility)patt0$temp).getValue() == element.category();
                }).filter(s -> !(s instanceof BooleanSetting) || !s.getTag().equals("Enabled")).toList());
            }
            if (element.enabled() != null) {
                ownSettings.addAll(hudSettings.stream().filter(s -> {
                    BooleanSetting.Visibility v;
                    Setting.Visibility patt0$temp = s.getVisibility();
                    return patt0$temp instanceof BooleanSetting.Visibility && (v = (BooleanSetting.Visibility)patt0$temp).getSetting() == element.enabled();
                }).toList());
            }
            this.elementsFrame.getButtons().add(new HudElementButton(element, ownSettings, this.elementsFrame, 13));
        }
        for (Setting setting : hudSettings) {
            List<Setting> catSettings;
            CategorySetting cat;
            if (!(setting instanceof CategorySetting) || boundCategories.contains(cat = (CategorySetting)setting) || (catSettings = hudSettings.stream().filter(s -> {
                CategorySetting.Visibility v;
                Setting.Visibility patt0$temp = s.getVisibility();
                return patt0$temp instanceof CategorySetting.Visibility && (v = (CategorySetting.Visibility)patt0$temp).getValue() == cat;
            }).filter(s -> !elementEnabledSettings.contains(s)).toList()).isEmpty()) continue;
            this.elementsFrame.getButtons().add(new CustomCategoryButton(cat, catSettings, this.elementsFrame, 13));
        }
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        FontManager.setHudRendering(true);
        try {
            for (Map.Entry<String, HudElementRegistry.Element> entry : HudElementRegistry.getElements().entrySet()) {
                Color accent;
                String name = entry.getKey();
                HudElementRegistry.Element element = entry.getValue();
                float[] bounds = HudElementRegistry.getBounds(name);
                if (bounds == null) {
                    float ox = element.offset().getX();
                    float oy = element.offset().getY();
                    bounds = new float[]{ox, oy, ox + 60.0f, oy + 15.0f};
                }
                boolean hovering = (float)mouseX >= bounds[0] && (float)mouseX <= bounds[2] && (float)mouseY >= bounds[1] && (float)mouseY <= bounds[3];
                boolean enabled = element.enabled().getValue();
                boolean dragging = name.equals(this.draggingElement);
                Color color = accent = enabled ? ColorUtils.getGlobalColor() : new Color(150, 150, 150);
                int fill = dragging ? 70 : (hovering ? 45 : 25);
                Renderer2D.renderQuad(context, bounds[0] - 1.0f, bounds[1] - 1.0f, bounds[2] + 1.0f, bounds[3] + 1.0f, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), fill));
                Renderer2D.renderOutline(context, bounds[0] - 1.0f, bounds[1] - 1.0f, bounds[2] + 1.0f, bounds[3] + 1.0f, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), enabled ? 220 : 120));
                if (!hovering && !dragging) continue;
                String label = name + (enabled ? "" : " (disabled)");
                float labelX = bounds[0];
                float labelY = bounds[1] - (float)Night.FONT_MANAGER.getHeight() - 2.0f;
                Renderer2D.renderQuad(context, labelX - 1.0f, labelY - 1.0f, labelX + (float)Night.FONT_MANAGER.getWidth(label) + 1.0f, labelY + (float)Night.FONT_MANAGER.getHeight(), new Color(0, 0, 0, 180));
                Night.FONT_MANAGER.drawTextWithShadow(context, label, (int)labelX, (int)labelY, accent);
            }
            this.elementsFrame.render(context, mouseX, mouseY, delta);
        }
        finally {
            FontManager.setHudRendering(false);
        }
    }

    private boolean insideElementsFrame(double mouseX, double mouseY) {
        return mouseX >= (double)this.elementsFrame.getX() && mouseX <= (double)(this.elementsFrame.getX() + this.elementsFrame.getWidth()) && mouseY >= (double)this.elementsFrame.getY() && mouseY <= (double)(this.elementsFrame.getY() + this.elementsFrame.getTotalHeight() + 4);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseY;
        double mouseX = event.x();
        if (this.insideElementsFrame(mouseX, mouseY = event.y())) {
            this.elementsFrame.mouseClicked(mouseX, mouseY, event.button());
            return true;
        }
        for (Map.Entry<String, HudElementRegistry.Element> entry : HudElementRegistry.getElements().entrySet()) {
            String name = entry.getKey();
            HudElementRegistry.Element element = entry.getValue();
            float[] bounds = HudElementRegistry.getBounds(name);
            if (bounds == null) {
                float ox = element.offset().getX();
                float oy = element.offset().getY();
                bounds = new float[]{ox, oy, ox + 60.0f, oy + 15.0f};
            }
            if (mouseX < (double)bounds[0] || mouseX > (double)bounds[2] || mouseY < (double)bounds[1] || mouseY > (double)bounds[3]) continue;
            if (event.button() == 1) {
                element.enabled().setValue(!element.enabled().getValue());
            } else if (event.button() == 0) {
                this.draggingElement = name;
                this.dragOffsetX = (float)mouseX - element.offset().getX();
                this.dragOffsetY = (float)mouseY - element.offset().getY();
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.draggingElement != null) {
            HudElementRegistry.Element element = HudElementRegistry.getElements().get(this.draggingElement);
            if (element != null) {
                PositionSetting offset = element.offset();
                offset.set((float)event.x() - this.dragOffsetX, (float)event.y() - this.dragOffsetY);
                HudElementRegistry.clamp(this.draggingElement, this.width, this.height);
            }
            return true;
        }
        this.elementsFrame.mouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY);
        return super.mouseDragged(event, deltaX, deltaY);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingElement = null;
        this.elementsFrame.mouseReleased(event.x(), event.y(), event.button());
        return super.mouseReleased(event);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.insideElementsFrame(mouseX, mouseY)) {
            this.elementsFrame.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        this.elementsFrame.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 256 && !BindButton.isAnyListening()) {
            this.onClose();
            return true;
        }
        if (keyCode == 256) {
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        char chr = (char)event.codepoint();
        int modifiers = 0;
        this.elementsFrame.charTyped(chr, modifiers);
        return super.charTyped(event);
    }

    public void onClose() {
        super.onClose();
        Night.MODULE_MANAGER.getModule(HUDEditorModule.class).setToggled(false);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class CustomCategoryButton
    extends CategoryButton
    implements ExpandableRow {
        private final List<Button> childButtons = new ArrayList<Button>();
        private final CategorySetting category;
        private int revealHeight = 0;

        public CustomCategoryButton(CategorySetting category, List<Setting> settings, Frame parent, int height) {
            super(category, null, parent, height);
            this.category = category;
            for (Setting setting : settings) {
                if (setting instanceof BooleanSetting) {
                    BooleanSetting s = (BooleanSetting)setting;
                    this.childButtons.add(new BooleanButton(s, parent, height));
                    continue;
                }
                if (setting instanceof NumberSetting) {
                    NumberSetting s = (NumberSetting)setting;
                    this.childButtons.add(new NumberButton(s, parent, height));
                    continue;
                }
                if (setting instanceof CategorySetting) {
                    CategorySetting s = (CategorySetting)setting;
                    this.childButtons.add(new CategoryButton(s, null, parent, height));
                    continue;
                }
                if (setting instanceof BindSetting) {
                    BindSetting s = (BindSetting)setting;
                    this.childButtons.add(new BindButton(s, parent, height));
                    continue;
                }
                if (setting instanceof ModeSetting) {
                    ModeSetting s = (ModeSetting)setting;
                    this.childButtons.add(new ModeButton(s, parent, height));
                    continue;
                }
                if (setting instanceof WhitelistSetting) {
                    WhitelistSetting s = (WhitelistSetting)setting;
                    this.childButtons.add(new WhitelistButton(s, parent, height));
                    continue;
                }
                if (setting instanceof StringSetting) {
                    StringSetting s = (StringSetting)setting;
                    this.childButtons.add(new StringButton(s, parent, height));
                    continue;
                }
                if (!(setting instanceof ColorSetting)) continue;
                ColorSetting s = (ColorSetting)setting;
                this.childButtons.add(new ColorButton(s, parent, height));
            }
        }

        @Override
        public String getRowName() {
            return this.category.getTag();
        }

        @Override
        public boolean isOpen() {
            return this.category.isOpen();
        }

        @Override
        public float getOpenAmount() {
            return this.category.getOpenAmount();
        }

        @Override
        public List<Button> getButtons() {
            return this.childButtons;
        }

        @Override
        public void setRevealHeight(int height) {
            this.revealHeight = height;
        }

        @Override
        public void setSearchQuery(String query) {
        }

        @Override
        public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            if (this.revealHeight > 0) {
                int clipTop = this.getY() + this.getHeight();
                context.enableScissor(this.getX(), clipTop, this.getX() + this.getWidth(), clipTop + this.revealHeight);
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.render(context, mouseX, mouseY, delta);
                }
                context.disableScissor();
            }
        }

        @Override
        public void mouseClicked(double mouseX, double mouseY, int button) {
            super.mouseClicked(mouseX, mouseY, button);
            if (this.category.isOpen()) {
                for (Button b : this.childButtons) {
                    if (!b.isVisible()) continue;
                    b.mouseClicked(mouseX, mouseY, button);
                }
            }
        }

        @Override
        public void mouseReleased(double mouseX, double mouseY, int button) {
            super.mouseReleased(mouseX, mouseY, button);
            if (this.category.isOpen()) {
                for (Button b : this.childButtons) {
                    if (!b.isVisible()) continue;
                    b.mouseReleased(mouseX, mouseY, button);
                }
            }
        }

        @Override
        public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            if (this.category.isOpen()) {
                for (Button b : this.childButtons) {
                    if (!b.isVisible()) continue;
                    b.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
            }
        }

        @Override
        public void keyPressed(int keyCode, int scanCode, int modifiers) {
            super.keyPressed(keyCode, scanCode, modifiers);
            if (this.category.isOpen()) {
                for (Button b : this.childButtons) {
                    if (!b.isVisible()) continue;
                    b.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }

        @Override
        public void charTyped(char chr, int modifiers) {
            super.charTyped(chr, modifiers);
            if (this.category.isOpen()) {
                for (Button b : this.childButtons) {
                    if (!b.isVisible()) continue;
                    b.charTyped(chr, modifiers);
                }
            }
        }
    }
}

