/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.client.gui.screens.options.OptionsScreen
 *  net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvents
 */
package night.gui.special;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.screens.account.AccountManagerScreen;
import night.modules.impl.core.ClickGuiModule;
import night.utils.graphics.MenuShader;
import night.utils.graphics.Renderer2D;

public class MainMenuScreen
extends Screen {
    private final List<Entry> entries = new ArrayList<Entry>();
    private static final long LERP_DURATION_MS = 4000L;
    private static final long BUTTON_FADE_DURATION_MS = 800L;
    private static long initialOpenTime = -1L;

    public MainMenuScreen() {
        super((Component)Component.literal((String)"Cheats MC"));
        if (initialOpenTime < 0L) {
            initialOpenTime = System.currentTimeMillis();
        }
    }

    protected void init() {
        if (initialOpenTime < 0L) {
            initialOpenTime = System.currentTimeMillis();
        }
        this.entries.clear();
        if (Night.FONT_MANAGER != null) {
            Night.FONT_MANAGER.getWidth("Singleplayer Multiplayer Account Manager Options Quit Cheats MC v26");
            Night.FONT_MANAGER.getHeight();
        }
        int buttonWidth = 230;
        int buttonHeight = 22;
        int spacing = 6;
        int x = this.width / 2 - buttonWidth / 2;
        int y = this.height / 2 - 18;
        this.entries.add(new Entry("Singleplayer", x, y, buttonWidth, buttonHeight, () -> this.minecraft.gui.setScreen((Screen)new SelectWorldScreen((Screen)this))));
        this.entries.add(new Entry("Multiplayer", x, y += buttonHeight + spacing, buttonWidth, buttonHeight, () -> this.minecraft.gui.setScreen((Screen)new JoinMultiplayerScreen((Screen)this))));
        this.entries.add(new Entry("Account Manager", x, y += buttonHeight + spacing, buttonWidth, buttonHeight, () -> this.minecraft.gui.setScreen((Screen)new AccountManagerScreen(this))));
        int half = (buttonWidth - spacing) / 2;
        this.entries.add(new Entry("Options", x, y += buttonHeight + spacing, half, buttonHeight, () -> this.minecraft.gui.setScreen((Screen)new OptionsScreen((Screen)this, this.minecraft.options, false))));
        this.entries.add(new Entry("Quit", x + half + spacing, y, buttonWidth - half - spacing, buttonHeight, () -> this.minecraft.stop()));
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        long elapsed = System.currentTimeMillis() - initialOpenTime;
        float lerpProgress = Math.min(1.0f, (float)elapsed / 4000.0f);
        float buttonEase = 0.0f;
        if (lerpProgress >= 1.0f) {
            long buttonElapsed = elapsed - 4000L;
            float buttonFade = Math.min(1.0f, (float)buttonElapsed / 800.0f);
            buttonEase = 1.0f - (float)Math.pow(1.0f - buttonFade, 3.0);
        }
        if (buttonEase > 0.001f) {
            this.renderLogo(context, buttonEase);
            for (Entry entry : this.entries) {
                entry.render(context, mouseX, mouseY, delta, buttonEase);
            }
            this.renderFooter(context, buttonEase);
        }
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        long elapsed = System.currentTimeMillis() - initialOpenTime;
        float lerpProgress = Math.min(1.0f, (float)elapsed / 4000.0f);
        MenuShader.render(context, this.width, this.height, 1.0f, lerpProgress);
    }

    private void renderLogo(GuiGraphicsExtractor context, float ease) {
        String title = "Cheats MC";
        int titleWidth = Night.FONT_MANAGER.getWidth(title);
        int titleX = this.width / 2 - titleWidth / 2;
        int titleY = this.height / 2 - 70;
        Color accent = MainMenuScreen.accentColor((int)(220.0f * ease));
        Renderer2D.renderQuad(context, (float)this.width / 2.0f - (float)titleWidth / 2.0f - 6.0f, titleY + Night.FONT_MANAGER.getHeight() + 3, (float)this.width / 2.0f + (float)titleWidth / 2.0f + 6.0f, titleY + Night.FONT_MANAGER.getHeight() + 4, accent);
        if (MainMenuScreen.isRainbow()) {
            Night.FONT_MANAGER.drawRainbowString(context, title, titleX, titleY, 6L);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(context, title, titleX, titleY, new Color(255, 255, 255, (int)(255.0f * ease)));
        }
        String subtitle = "v26 \u2022 MC 26.2";
        int subWidth = Night.FONT_MANAGER.getWidth(subtitle);
        Night.FONT_MANAGER.drawTextWithShadow(context, subtitle, this.width / 2 - subWidth / 2, titleY + Night.FONT_MANAGER.getHeight() + 8, new Color(170, 170, 180, (int)(180.0f * ease)));
    }

    private void renderFooter(GuiGraphicsExtractor context, float ease) {
        String accountInfo = "Logged in as: \u00a7f" + (Night.ACCOUNT_MANAGER != null ? Night.ACCOUNT_MANAGER.getCurrentName() : "");
        Night.FONT_MANAGER.drawTextWithShadow(context, accountInfo, 6, 6, new Color(200, 200, 200, (int)(200.0f * ease)));
        String left = "Cheats MC v26";
        Night.FONT_MANAGER.drawTextWithShadow(context, left, 4, this.height - Night.FONT_MANAGER.getHeight() - 4, new Color(160, 160, 160, (int)(160.0f * ease)));
        String right = "git-eeeb439b78";
        int rightWidth = Night.FONT_MANAGER.getWidth(right);
        Night.FONT_MANAGER.drawTextWithShadow(context, right, this.width - rightWidth - 4, this.height - Night.FONT_MANAGER.getHeight() - 4, new Color(160, 160, 160, (int)(160.0f * ease)));
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        long elapsed = System.currentTimeMillis() - initialOpenTime;
        if (elapsed < 4000L) {
            return false;
        }
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0) {
            for (Entry entry : this.entries) {
                if (!entry.isHovered(mouseX, mouseY)) continue;
                this.playClick();
                entry.action.run();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void playClick() {
        if (Night.MODULE_MANAGER.getModule(ClickGuiModule.class).sounds.getValue()) {
            this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        }
    }

    private static boolean isRainbow() {
        return Night.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow();
    }

    private static Color accentColor(int alpha) {
        return ClickGuiScreen.getButtonColor(0, alpha);
    }

    private static class Entry {
        private final String label;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final Runnable action;
        private float hover;

        private Entry(String label, int x, int y, int width, int height, Runnable action) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
        }

        private boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
        }

        private void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, float ease) {
            float underline;
            float target = this.isHovered(mouseX, mouseY) ? 1.0f : 0.0f;
            this.hover += (target - this.hover) * 0.2f;
            Color accent = MainMenuScreen.accentColor((int)(255.0f * ease));
            int alpha = (int)((this.hover * 35.0f + 35.0f) * ease);
            Color base = new Color(8, 8, 12, alpha);
            Renderer2D.renderQuad(context, this.x, this.y, this.x + this.width, this.y + this.height, base);
            int borderAlpha = (int)((this.hover * 70.0f + 25.0f) * ease);
            Color border = this.hover > 0.1f ? MainMenuScreen.accentColor((int)(this.hover * 180.0f * ease)) : new Color(255, 255, 255, borderAlpha);
            Renderer2D.renderOutline(context, this.x, this.y, this.x + this.width, this.y + this.height, border);
            float bar = 3.0f * this.hover;
            if (bar > 0.1f) {
                Renderer2D.renderQuad(context, this.x, this.y, (float)this.x + bar, this.y + this.height, accent);
            }
            if ((underline = (float)this.width / 2.0f * this.hover) > 0.5f) {
                Renderer2D.renderQuad(context, (float)this.x + (float)this.width / 2.0f - underline, this.y + this.height - 1, (float)this.x + (float)this.width / 2.0f + underline, this.y + this.height, accent);
            }
            Color textColor = Entry.lerp(new Color(210, 210, 215), Color.WHITE, this.hover);
            int textY = this.y + this.height / 2 - Night.FONT_MANAGER.getHeight() / 2;
            int labelWidth = Night.FONT_MANAGER.getWidth(this.label);
            int textX = this.x + this.width / 2 - labelWidth / 2;
            Night.FONT_MANAGER.drawTextWithShadow(context, this.label, textX, textY, new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(255.0f * ease)));
        }

        private static Color lerp(Color from, Color to, float t) {
            t = Math.max(0.0f, Math.min(1.0f, t));
            return new Color((int)((float)from.getRed() + (float)(to.getRed() - from.getRed()) * t), (int)((float)from.getGreen() + (float)(to.getGreen() - from.getGreen()) * t), (int)((float)from.getBlue() + (float)(to.getBlue() - from.getBlue()) * t));
        }
    }
}

