/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Util
 */
package night.gui.screens.account;

import java.awt.Color;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import night.Night;
import night.accounts.Account;
import night.accounts.AccountType;
import night.accounts.MicrosoftAuth;
import night.utils.graphics.Renderer2D;

public class MicrosoftLoginScreen
extends Screen {
    private final Screen parent;
    private volatile MicrosoftAuth.BrowserLoginSession session;
    private volatile String status = "Opening browser for Microsoft login...";
    private volatile boolean failed = false;

    public MicrosoftLoginScreen(Screen parent) {
        super((Component)Component.literal((String)"Microsoft Login"));
        this.parent = parent;
    }

    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int dialogH = 160;
        int dialogY = centerY - dialogH / 2;
        if (this.session == null && !this.failed) {
            this.startAuth();
        }
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Open Browser Again"), button -> {
            if (this.session != null && this.session.loginUrl() != null) {
                try {
                    Util.getPlatform().openUri(URI.create(this.session.loginUrl()));
                }
                catch (Exception e) {
                    this.status = "\u00a7cFailed to open browser: " + e.getMessage();
                }
            }
        }).bounds(centerX - 105, dialogY + 85, 210, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Cancel"), button -> this.onClose()).bounds(centerX - 105, dialogY + 112, 210, 20).build());
    }

    private void startAuth() {
        try {
            this.session = MicrosoftAuth.startBrowserLogin(newStatus -> {
                this.status = newStatus;
            });
            this.session.future().thenAccept(result -> {
                if (result.success()) {
                    Account account = new Account(result.username(), result.uuid(), AccountType.MICROSOFT, result.refreshToken(), result.accessToken());
                    Night.ACCOUNT_MANAGER.addAccount(account);
                    Night.ACCOUNT_MANAGER.login(account);
                    Minecraft.getInstance().execute(this::onClose);
                } else {
                    this.status = "\u00a7c" + result.errorMessage();
                    this.failed = true;
                }
            });
        }
        catch (Exception e) {
            this.status = "\u00a7cFailed to start login: " + e.getMessage();
            this.failed = true;
        }
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Renderer2D.renderGradient(context, 0.0f, 0.0f, this.width, this.height, new Color(18, 14, 30), new Color(8, 6, 14));
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int dialogW = 290;
        int dialogH = 160;
        int dialogX = centerX - dialogW / 2;
        int dialogY = centerY - dialogH / 2;
        Renderer2D.renderQuad(context, dialogX, dialogY, dialogX + dialogW, dialogY + dialogH, new Color(28, 28, 28, 245));
        Renderer2D.renderOutline(context, dialogX, dialogY, dialogX + dialogW, dialogY + dialogH, new Color(70, 70, 70, 200));
        String title = "Microsoft Account Login";
        Night.FONT_MANAGER.drawTextWithShadow(context, title, centerX - Night.FONT_MANAGER.getWidth(title) / 2, dialogY + 12, Color.WHITE);
        String line1 = "A browser tab has been opened.";
        Night.FONT_MANAGER.drawTextWithShadow(context, line1, centerX - Night.FONT_MANAGER.getWidth(line1) / 2, dialogY + 32, new Color(180, 180, 180));
        String line2 = "Log in to your account and approve to continue.";
        Night.FONT_MANAGER.drawTextWithShadow(context, line2, centerX - Night.FONT_MANAGER.getWidth(line2) / 2, dialogY + 45, new Color(180, 180, 180));
        Night.FONT_MANAGER.drawTextWithShadow(context, this.status, centerX - Night.FONT_MANAGER.getWidth(this.status) / 2, dialogY + 66, new Color(85, 255, 255));
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    public void onClose() {
        if (this.session != null && this.session.cancelAction() != null) {
            this.session.cancelAction().run();
        }
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}

