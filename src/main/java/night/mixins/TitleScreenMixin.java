/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import night.Night;
import night.gui.components.AccountButton;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TitleScreen.class})
public abstract class TitleScreenMixin
extends Screen
implements IMinecraft {
    @Shadow
    private boolean fading;
    @Shadow
    private long fadeInStart;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void init(CallbackInfo ci) {
        this.addRenderableWidget(AccountButton.create(8, 8, 75, 20, this));
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void render$drawTextWithShadow(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        int i;
        float g;
        if (Night.UPDATE_STATUS.equalsIgnoreCase("none")) {
            return;
        }
        String primaryText = "";
        String secondaryText = "";
        Color color = Color.WHITE;
        if (Night.UPDATE_STATUS.equalsIgnoreCase("update-available")) {
            secondaryText = "An update is available for Cheats MC.";
            primaryText = "Please restart the game to apply changes.";
            color = Color.ORANGE;
        }
        if (Night.UPDATE_STATUS.equalsIgnoreCase("failed-connection")) {
            secondaryText = "Failed to connect to Cheats MC's servers.";
            primaryText = "Please make sure you have a working internet connection.";
            color = Color.RED;
        }
        if (Night.UPDATE_STATUS.equalsIgnoreCase("failed")) {
            secondaryText = "Failed to update Cheats MC.";
            primaryText = "Please make sure the auto-updater is working properly.";
            color = Color.RED;
        }
        if (Night.UPDATE_STATUS.equalsIgnoreCase("up-to-date")) {
            primaryText = "Cheats MC is on the latest version.";
        }
        if (primaryText.isEmpty()) {
            return;
        }
        float f = 1.0f;
        if (this.fading && (g = (float)(Util.getMillis() - this.fadeInStart) / 2000.0f) <= 1.0f) {
            g = Mth.clamp((float)g, (float)0.0f, (float)1.0f);
            f = Mth.clampedMap((float)g, (float)0.5f, (float)1.0f, (float)0.0f, (float)1.0f);
        }
        if (((i = Mth.ceil((float)(f * 255.0f)) << 24) & 0xFC000000) != 0) {
            if (!secondaryText.isEmpty()) {
                context.text(TitleScreenMixin.mc.font, secondaryText, this.width / 2 - TitleScreenMixin.mc.font.width(secondaryText) / 2, this.height - 30, color.getRGB() | i, true);
            }
            context.text(TitleScreenMixin.mc.font, primaryText, this.width / 2 - TitleScreenMixin.mc.font.width(primaryText) / 2, this.height - 20, color.getRGB() | i, true);
        }
    }
}

