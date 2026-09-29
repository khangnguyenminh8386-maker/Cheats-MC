/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.LevelLoadingScreen
 *  net.minecraft.client.gui.screens.LevelLoadingScreen$Reason
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import night.Night;
import night.modules.impl.core.MenuModule;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer2D;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelLoadingScreen.class})
public class DownloadingTerrainScreenMixin
extends Screen {
    @Shadow
    @Final
    private LevelLoadingScreen.Reason reason;

    protected DownloadingTerrainScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"extractBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.reason.equals((Object)LevelLoadingScreen.Reason.OTHER) && Night.MODULE_MANAGER.getModule(MenuModule.class).isToggled() && Night.MODULE_MANAGER.getModule(MenuModule.class).mainMenu.getValue()) {
            Renderer2D.renderQuad(context, 0.0f, 0.0f, this.width, this.height, new Color(25, 25, 25, 255));
            for (int i = 0; i < this.width; ++i) {
                Color color = ColorUtils.getRainbow(2L, 0.7f, 1.0f, 255, (long)i * 5L);
                Renderer2D.renderQuad(context, i, 0.0f, i + 1, 1.0f, color);
            }
            ci.cancel();
        }
    }
}

