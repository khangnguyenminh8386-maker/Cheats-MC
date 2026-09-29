/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package night.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import night.Night;
import night.gui.api.Button;
import night.gui.api.DescriptionFrame;
import night.gui.api.Frame;
import night.gui.api.PingBypassFrame;
import night.gui.api.SearchFrame;
import night.gui.impl.BindButton;
import night.modules.Module;
import night.modules.impl.core.ClickGuiModule;
import night.utils.animations.Animation;
import night.utils.animations.Easing;
import night.utils.color.ColorUtils;
import night.utils.graphics.ClickGuiBackground;
import night.utils.graphics.Renderer2D;
import night.utils.system.Timer;

public class ClickGuiScreen
extends Screen {
    private final ArrayList<Frame> frames = new ArrayList();
    private final ArrayList<Button> buttons = new ArrayList();
    private final DescriptionFrame descriptionFrame;
    private final SearchFrame searchFrame;
    private final PingBypassFrame pingBypassFrame;
    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private Color colorClipboard = null;
    private float appliedScale = 1.0f;
    private static final int SLIDE_DURATION_MS = 320;
    private final Animation slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
    private boolean closing = false;
    private static final int CATEGORY_STAGGER_MS = 35;
    private boolean readyToRemove = false;

    public boolean isReadyToRemove() {
        return this.readyToRemove;
    }

    public void requestClose() {
        this.closing = true;
        this.readyToRemove = false;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void cancelClose() {
        this.closing = false;
        this.readyToRemove = false;
        this.slideAnim.setEasing(Easing.Method.EASE_OUT_CUBIC);
    }

    public ClickGuiScreen() {
        super((Component)Component.literal((String)"night-click-gui"));
        int x = 6;
        for (Module.Category category : Module.Category.values()) {
            this.frames.add(new Frame(category, x, 3, 100, 13));
            x += 104;
        }
        this.pingBypassFrame = new PingBypassFrame(x, 3, 100, 13);
        this.descriptionFrame = new DescriptionFrame(x, 3, 200, 13);
        this.searchFrame = new SearchFrame();
    }

    private float getSlideDistance() {
        if (Minecraft.getInstance().getWindow() != null) {
            return (float)Minecraft.getInstance().getWindow().getGuiScaledHeight() + 30.0f;
        }
        return 300.0f;
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float lastFrameProgress;
        super.extractRenderState(context, mouseX, mouseY, delta);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float progress = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        float f = lastFrameProgress = this.frames.isEmpty() ? 0.0f : this.categoryFrameProgress(this.frames.size() - 1);
        if (this.closing && progress <= 0.005f && lastFrameProgress <= 0.005f) {
            this.readyToRemove = true;
            Minecraft.getInstance().gui.setScreen(null);
            return;
        }
        this.descriptionFrame.setDescription("");
        String query = this.searchFrame.getQuery();
        float baseSlideDist = this.getSlideDistance();
        int sx = Math.round((float)mouseX / this.appliedScale);
        int sy = Math.round((float)mouseY / this.appliedScale);
        context.pose().pushMatrix();
        context.pose().scale(this.appliedScale, this.appliedScale);
        for (int i = 0; i < this.frames.size(); ++i) {
            Frame frame = this.frames.get(i);
            float frameProgress = this.categoryFrameProgress(i);
            float frameSlideDist = Math.max((float)(frame.getY() + frame.getTotalHeight()) + 30.0f, baseSlideDist);
            float yOffset = Mth.lerp((float)frameProgress, (float)(-frameSlideDist), (float)0.0f);
            context.pose().pushMatrix();
            context.pose().translate(0.0f, yOffset);
            frame.render(context, sx, sy, delta, query);
            context.pose().popMatrix();
        }
        float globalSlideDist = baseSlideDist;
        float globalYOffset = Mth.lerp((float)progress, (float)(-globalSlideDist), (float)0.0f);
        context.pose().pushMatrix();
        context.pose().translate(0.0f, globalYOffset);
        this.descriptionFrame.render(context, sx, sy, delta);
        this.searchFrame.render(context, sx, sy, delta);
        context.pose().popMatrix();
        context.pose().popMatrix();
    }

    private float categoryFrameProgress(int index) {
        long elapsed = System.currentTimeMillis() - this.slideAnim.getStartTime() - (long)index * 35L;
        float t = Mth.clamp((float)((float)elapsed / 320.0f), (float)0.0f, (float)1.0f);
        float eased = Easing.ease(t, this.closing ? Easing.Method.EASE_IN_CUBIC : Easing.Method.EASE_OUT_CUBIC);
        return this.closing ? 1.0f - eased : eased;
    }

    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.x() / (double)this.appliedScale;
        double mouseY = event.y() / (double)this.appliedScale;
        int button = event.button();
        for (Frame frame : this.frames) {
            frame.mouseDragged(mouseX, mouseY, button, deltaX / (double)this.appliedScale, deltaY / (double)this.appliedScale);
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button;
        double mouseY;
        double mouseX = event.x() / (double)this.appliedScale;
        if (this.searchFrame.mouseClicked(mouseX, mouseY = event.y() / (double)this.appliedScale, button = event.button())) {
            return true;
        }
        for (Frame frame : this.frames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }
        this.descriptionFrame.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(event, doubleClick);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x() / (double)this.appliedScale;
        double mouseY = event.y() / (double)this.appliedScale;
        int button = event.button();
        for (Frame frame : this.frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        this.descriptionFrame.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(event);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double sx = mouseX / (double)this.appliedScale;
        double sy = mouseY / (double)this.appliedScale;
        for (Frame frame : this.frames) {
            frame.mouseScrolled(sx, sy, horizontalAmount, verticalAmount);
        }
        return this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        if (keyCode == 256 && !BindButton.isAnyListening()) {
            this.requestClose();
            return true;
        }
        if (keyCode == Night.MODULE_MANAGER.getModule(ClickGuiModule.class).getBind()) {
            if (this.closing) {
                this.cancelClose();
            }
            return true;
        }
        boolean ctrl = InputConstants.isKeyDown((Window)this.minecraft.getWindow(), (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (ctrl && keyCode == 70) {
            this.searchFrame.toggle();
            return true;
        }
        if (this.searchFrame.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (Frame frame : this.frames) {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 256) {
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        int modifiers;
        char chr = (char)event.codepoint();
        if (this.searchFrame.charTyped(chr, modifiers = 0)) {
            return true;
        }
        for (Frame frame : this.frames) {
            frame.charTyped(chr, modifiers);
        }
        return super.charTyped(event);
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGui.blur.getValue()) {
            context.blurBeforeThisStratum();
        }
        Renderer2D.renderQuad(context, 0.0f, 0.0f, this.width, this.height, new Color(8, 7, 14, 140));
        String bgMode = clickGui.background.getValue();
        int modeIdx = Arrays.asList(ClickGuiModule.BG_MODES).indexOf(bgMode);
        if (modeIdx >= 0 && modeIdx < ClickGuiModule.BG_MODES.length - 1) {
            float speed = clickGui.bgSpeed.getValue().floatValue();
            float opacity = clickGui.bgOpacity.getValue().floatValue() / 100.0f;
            Color themeColor = clickGui.color.getColor();
            boolean rainbow = clickGui.isRainbow();
            ClickGuiBackground.renderBackground(context, this.width, this.height, modeIdx, speed, opacity, themeColor, rainbow);
        }
    }

    public void removed() {
        this.searchFrame.setQuery("");
        this.searchFrame.setCursorIndex(0);
        this.searchFrame.setFocused(false);
        this.searchFrame.setVisible(false);
        super.removed();
        Night.MODULE_MANAGER.getModule(ClickGuiModule.class).setToggled(false);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public static Color getButtonColor(int index, int alpha) {
        Color color = Night.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow() ? ColorUtils.getOffsetRainbow((long)index * 2L) : Night.MODULE_MANAGER.getModule(ClickGuiModule.class).color.getColor();
        return ColorUtils.getColor(color, alpha);
    }

    @Generated
    public ArrayList<Frame> getFrames() {
        return this.frames;
    }

    @Generated
    public ArrayList<Button> getButtons() {
        return this.buttons;
    }

    @Generated
    public DescriptionFrame getDescriptionFrame() {
        return this.descriptionFrame;
    }

    @Generated
    public SearchFrame getSearchFrame() {
        return this.searchFrame;
    }

    @Generated
    public PingBypassFrame getPingBypassFrame() {
        return this.pingBypassFrame;
    }

    @Generated
    public Timer getLineTimer() {
        return this.lineTimer;
    }

    @Generated
    public boolean isShowLine() {
        return this.showLine;
    }

    @Generated
    public Color getColorClipboard() {
        return this.colorClipboard;
    }

    @Generated
    public float getAppliedScale() {
        return this.appliedScale;
    }

    @Generated
    public Animation getSlideAnim() {
        return this.slideAnim;
    }

    @Generated
    public boolean isClosing() {
        return this.closing;
    }

    @Generated
    public void setShowLine(boolean showLine) {
        this.showLine = showLine;
    }

    @Generated
    public void setColorClipboard(Color colorClipboard) {
        this.colorClipboard = colorClipboard;
    }

    @Generated
    public void setAppliedScale(float appliedScale) {
        this.appliedScale = appliedScale;
    }

    @Generated
    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    @Generated
    public void setReadyToRemove(boolean readyToRemove) {
        this.readyToRemove = readyToRemove;
    }
}

