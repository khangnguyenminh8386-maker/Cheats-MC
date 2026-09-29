/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 */
package night.gui.screens.account;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import night.Night;
import night.accounts.Account;
import night.accounts.AccountType;
import night.gui.screens.account.AddOfflineAccountScreen;
import night.gui.screens.account.MicrosoftLoginScreen;
import night.modules.impl.core.ClickGuiModule;
import night.utils.graphics.Renderer2D;

public class AccountManagerScreen
extends Screen {
    private final Screen parent;
    private Account selectedAccount = null;
    private float scrollOffset = 0.0f;
    private float targetScroll = 0.0f;
    private String statusMessage = "";
    private long statusMessageTime = 0L;
    private long lastClickTime = 0L;
    private Account lastClickedAccount = null;
    private final Map<Account, Float> hoverAnimations = new HashMap<Account, Float>();
    private Button loginButton;
    private Button deleteButton;

    public AccountManagerScreen(Screen parent) {
        super((Component)Component.literal((String)"Account Manager"));
        this.parent = parent;
    }

    protected void init() {
        int centerX = this.width / 2;
        int listBottom = this.height - 55;
        this.loginButton = Button.builder((Component)Component.literal((String)"Login"), button -> this.loginSelected()).bounds(centerX - 160, listBottom, 102, 20).build();
        this.addRenderableWidget(this.loginButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add Microsoft"), button -> Minecraft.getInstance().gui.setScreen((Screen)new MicrosoftLoginScreen(this))).bounds(centerX - 52, listBottom, 104, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add Offline"), button -> Minecraft.getInstance().gui.setScreen((Screen)new AddOfflineAccountScreen(this))).bounds(centerX + 58, listBottom, 102, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Random Offline"), button -> {
            Account acc;
            String[] prefixes = new String[]{"Shadow", "Cheats MC", "Ghost", "Viper", "Phantom", "Raven", "Frost", "Nova", "Dark", "Storm"};
            String randomName = prefixes[new Random().nextInt(prefixes.length)] + "_" + (100 + new Random().nextInt(900));
            this.selectedAccount = acc = Night.ACCOUNT_MANAGER.loginOffline(randomName);
            this.setStatus("Logged in as " + randomName);
        }).bounds(centerX - 160, listBottom + 25, 102, 20).build());
        this.deleteButton = Button.builder((Component)Component.literal((String)"Delete"), button -> {
            if (this.selectedAccount != null) {
                Night.ACCOUNT_MANAGER.removeAccount(this.selectedAccount);
                this.selectedAccount = null;
                this.setStatus("Account deleted");
            }
        }).bounds(centerX - 52, listBottom + 25, 104, 20).build();
        this.addRenderableWidget(this.deleteButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Back"), button -> this.onClose()).bounds(centerX + 58, listBottom + 25, 102, 20).build());
        this.updateButtonStates();
    }

    private void loginSelected() {
        if (this.selectedAccount != null) {
            this.setStatus("Logging in to " + this.selectedAccount.getName() + "...");
            CompletableFuture.runAsync(() -> {
                boolean success = Night.ACCOUNT_MANAGER.login(this.selectedAccount);
                if (success) {
                    this.setStatus("Logged in as " + this.selectedAccount.getName());
                } else {
                    this.setStatus("\u00a7cFailed to login to " + this.selectedAccount.getName());
                }
            });
        }
    }

    private void updateButtonStates() {
        if (this.loginButton != null) {
            boolean bl = this.loginButton.active = this.selectedAccount != null;
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = this.selectedAccount != null;
        }
    }

    private void setStatus(String message) {
        this.statusMessage = message;
        this.statusMessageTime = System.currentTimeMillis();
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Renderer2D.renderGradient(context, 0.0f, 0.0f, this.width, this.height, new Color(24, 24, 24), new Color(12, 12, 12));
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        String title = "Account Manager";
        Night.FONT_MANAGER.drawTextWithShadow(context, title, centerX - Night.FONT_MANAGER.getWidth(title) / 2, 12, Color.WHITE);
        String currentName = Night.ACCOUNT_MANAGER.getCurrentName();
        if (System.currentTimeMillis() - this.statusMessageTime < 5000L && !this.statusMessage.isEmpty()) {
            Color statusColor = this.statusMessage.startsWith("\u00a7c") ? new Color(255, 100, 100) : (this.statusMessage.startsWith("Logging") ? new Color(200, 200, 200) : new Color(255, 255, 100));
            Night.FONT_MANAGER.drawTextWithShadow(context, this.statusMessage, centerX - Night.FONT_MANAGER.getWidth(this.statusMessage) / 2, 28, statusColor);
        } else {
            String currentAccountStr = "Logged in as: \u00a7f" + currentName;
            Night.FONT_MANAGER.drawTextWithShadow(context, currentAccountStr, centerX - Night.FONT_MANAGER.getWidth(currentAccountStr) / 2, 28, new Color(180, 180, 180));
        }
        int listX = centerX - 160;
        int listY = 55;
        int listW = 320;
        int listH = this.height - 120;
        Renderer2D.renderQuad(context, listX, listY, listX + listW, listY + listH, new Color(24, 24, 24, 230));
        Renderer2D.renderOutline(context, listX, listY, listX + listW, listY + listH, new Color(60, 60, 60, 160));
        this.scrollOffset += (this.targetScroll - this.scrollOffset) * 0.3f;
        List<Account> accounts = Night.ACCOUNT_MANAGER.getAccounts();
        int entryH = 32;
        int maxScroll = Math.max(0, accounts.size() * entryH - listH);
        this.targetScroll = Mth.clamp((float)this.targetScroll, (float)(-maxScroll), (float)0.0f);
        context.enableScissor(listX, listY, listX + listW, listY + listH);
        float yPos = (float)listY + this.scrollOffset;
        for (Account account : accounts) {
            if (yPos + (float)entryH >= (float)listY && yPos <= (float)(listY + listH)) {
                float pillAlpha;
                boolean isSelected = account == this.selectedAccount;
                boolean isCurrent = account.getName().equalsIgnoreCase(currentName);
                boolean isHovered = mouseX >= listX + 4 && mouseX <= listX + listW - 4 && (float)mouseY >= yPos + 2.0f && (float)mouseY <= yPos + (float)entryH - 2.0f && mouseY >= listY && mouseY <= listY + listH;
                float targetHover = isHovered ? 1.0f : 0.0f;
                float currentHover = this.hoverAnimations.getOrDefault(account, Float.valueOf(0.0f)).floatValue();
                if (Math.abs(targetHover - (currentHover += (targetHover - currentHover) * 0.22f)) < 0.005f) {
                    currentHover = targetHover;
                }
                this.hoverAnimations.put(account, Float.valueOf(currentHover));
                Color bg = isSelected ? new Color(50, 50, 50, 220) : (isCurrent ? new Color(40, 40, 40, 180) : new Color(30, 30, 30, 150));
                Renderer2D.renderQuad(context, listX + 4, yPos + 2.0f, listX + listW - 4, yPos + (float)entryH - 2.0f, bg);
                Color accent = this.getAccentColor();
                if (currentHover > 0.001f) {
                    float fillWidth = (float)(listW - 8) * currentHover;
                    Color slideStart = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(90.0f * currentHover));
                    Color slideEnd = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(20.0f * currentHover));
                    Renderer2D.renderSidewaysGradient(context, listX + 4, yPos + 2.0f, (float)(listX + 4) + fillWidth, yPos + (float)entryH - 2.0f, slideStart, slideEnd);
                    Color leadingEdge = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(200.0f * currentHover));
                    Renderer2D.renderQuad(context, (float)(listX + 3) + fillWidth, yPos + 2.0f, (float)(listX + 5) + fillWidth, yPos + (float)entryH - 2.0f, leadingEdge);
                }
                float f = pillAlpha = isSelected ? 1.0f : currentHover;
                if (pillAlpha > 0.001f) {
                    float pillWidth = isSelected ? 3.5f : 3.0f * currentHover;
                    Color pillColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(255.0f * pillAlpha));
                    Renderer2D.renderQuad(context, listX + 4, yPos + 2.0f, (float)(listX + 4) + pillWidth, yPos + (float)entryH - 2.0f, pillColor);
                }
                float textSlideOffset = 3.0f * currentHover;
                String nameStr = (isCurrent ? "\u00a7a\u2713 " : "") + account.getName();
                Night.FONT_MANAGER.drawTextWithShadow(context, nameStr, (int)((float)(listX + 14) + textSlideOffset), (int)(yPos + 6.0f), isCurrent ? Color.GREEN : Color.WHITE);
                String typeStr = "[" + account.getType().getDisplayName() + "]";
                Color typeColor = switch (account.getType()) {
                    default -> throw new MatchException(null, null);
                    case AccountType.MICROSOFT -> new Color(0, 220, 255);
                    case AccountType.OFFLINE -> new Color(255, 200, 80);
                    case AccountType.SESSION -> new Color(220, 120, 255);
                };
                Night.FONT_MANAGER.drawTextWithShadow(context, typeStr, (int)((float)(listX + 14) + textSlideOffset), (int)(yPos + 18.0f), typeColor);
                if (isCurrent) {
                    String activeStr = "\u00a7aACTIVE";
                    Night.FONT_MANAGER.drawTextWithShadow(context, activeStr, listX + listW - Night.FONT_MANAGER.getWidth(activeStr) - 12, (int)(yPos + 11.0f), Color.GREEN);
                }
            }
            yPos += (float)entryH;
        }
        context.disableScissor();
        if (accounts.isEmpty()) {
            String emptyStr = "No saved accounts. Click 'Add Offline' or 'Add Microsoft' below.";
            Night.FONT_MANAGER.drawTextWithShadow(context, emptyStr, centerX - Night.FONT_MANAGER.getWidth(emptyStr) / 2, listY + listH / 2 - 5, new Color(140, 140, 160));
        }
        this.updateButtonStates();
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        int centerX = this.width / 2;
        int listX = centerX - 160;
        int listY = 55;
        int listW = 320;
        int listH = this.height - 120;
        if (button == 0 && mouseX >= (double)listX && mouseX <= (double)(listX + listW) && mouseY >= (double)listY && mouseY <= (double)(listY + listH)) {
            List<Account> accounts = Night.ACCOUNT_MANAGER.getAccounts();
            float relativeY = (float)(mouseY - (double)listY - (double)this.scrollOffset);
            int entryH = 32;
            int index = (int)(relativeY / (float)entryH);
            if (index >= 0 && index < accounts.size()) {
                Account clicked = accounts.get(index);
                long now = System.currentTimeMillis();
                if (clicked == this.lastClickedAccount && (now - this.lastClickTime < 350L || doubleClick)) {
                    this.selectedAccount = clicked;
                    this.loginSelected();
                } else {
                    this.selectedAccount = clicked;
                }
                this.lastClickedAccount = clicked;
                this.lastClickTime = now;
                this.updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<Account> accounts = Night.ACCOUNT_MANAGER.getAccounts();
        int listH = this.height - 120;
        int entryH = 32;
        int maxScroll = Math.max(0, accounts.size() * entryH - listH);
        this.targetScroll += (float)(verticalAmount * 24.0);
        this.targetScroll = Mth.clamp((float)this.targetScroll, (float)(-maxScroll), (float)0.0f);
        return true;
    }

    private Color getAccentColor() {
        ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGui != null) {
            return clickGui.color.getColor();
        }
        return new Color(130, 90, 240);
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}

