/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package night.gui.api;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import night.Night;
import night.gui.ClickGuiScreen;
import night.gui.api.Button;
import night.gui.api.Frame;
import night.gui.impl.ProxyModuleButton;
import night.modules.impl.core.PingBypassModule;
import night.utils.graphics.Renderer2D;

public class PingBypassFrame
extends Frame {
    private static final String CATEGORY_NAME = "PingBypass";
    private Set<String> lastKnownModules = Collections.emptySet();

    public PingBypassFrame(int x, int y, int width, int height) {
        super(null, x, y, width, height);
    }

    private boolean isActive() {
        PingBypassModule pbModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        return pbModule != null && pbModule.isToggled() && !pbModule.getProxyModuleStates().isEmpty();
    }

    private void refreshButtons() {
        PingBypassModule pbModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        if (pbModule == null) {
            return;
        }
        TreeSet<String> currentModules = new TreeSet<String>(pbModule.getProxyModuleStates().keySet());
        if (currentModules.equals(this.lastKnownModules)) {
            return;
        }
        this.lastKnownModules = currentModules;
        this.getButtons().clear();
        for (String moduleName : currentModules) {
            this.getButtons().add(new ProxyModuleButton(moduleName, this, this.getHeight()));
        }
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, String searchQuery) {
        if (!this.isActive()) {
            return;
        }
        this.refreshButtons();
        if (this.isDragging()) {
            this.setX(mouseX - this.getDragX());
            this.setY(mouseY - this.getDragY());
        }
        boolean searching = searchQuery != null && !searchQuery.isEmpty();
        int totalH = this.getHeight();
        if (this.isOpen()) {
            ++totalH;
            for (Button button : this.getButtons()) {
                if (searching && button instanceof ProxyModuleButton) {
                    ProxyModuleButton pmb = (ProxyModuleButton)button;
                    if (!pmb.getModuleName().toLowerCase().contains(searchQuery.toLowerCase())) {
                        button.setVisible(false);
                        continue;
                    }
                    button.setVisible(true);
                } else {
                    button.setVisible(true);
                }
                if (!button.isVisible()) continue;
                button.setX(this.getX());
                button.setY(this.getY() + totalH);
                totalH += button.getHeight();
            }
        }
        this.setTotalHeight(totalH);
        Renderer2D.renderQuad(context, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), new Color(20, 20, 25, 200));
        Color accentColor = ClickGuiScreen.getButtonColor(this.getY(), 200);
        Renderer2D.renderQuad(context, this.getX(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth(), this.getY() + this.getHeight(), accentColor);
        Night.FONT_MANAGER.drawTextWithShadow(context, CATEGORY_NAME, this.getX() + this.getTextPadding(), this.getY() + 2, Color.WHITE);
        if (this.isOpen()) {
            Renderer2D.renderQuad(context, this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), this.getY() + totalH + 1, new Color(15, 15, 20, 180));
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.render(context, mouseX, mouseY, delta, "");
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isActive()) {
            return;
        }
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.setDragging(true);
                this.setDragX((int)(mouseX - (double)this.getX()));
                this.setDragY((int)(mouseY - (double)this.getY()));
            } else if (button == 1) {
                this.setOpen(!this.isOpen());
            }
        }
        if (this.isOpen()) {
            for (Button b : this.getButtons()) {
                if (!b.isVisible()) continue;
                b.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.isActive()) {
            return;
        }
        if (button == 0) {
            this.setDragging(false);
        }
        for (Button b : this.getButtons()) {
            if (!b.isVisible()) continue;
            b.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.isActive()) {
            return;
        }
        super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isActive()) {
            return;
        }
        if (this.isOpen()) {
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (!this.isActive()) {
            return;
        }
        if (this.isOpen()) {
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.charTyped(chr, modifiers);
            }
        }
    }
}

