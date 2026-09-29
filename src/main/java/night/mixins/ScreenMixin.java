/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.world.inventory.Slot
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import night.Night;
import night.mixins.accessors.AbstractContainerScreenAccessor;
import night.modules.impl.combat.MainhandModule;
import night.modules.impl.core.MenuModule;
import night.modules.impl.miscellaneous.ShulkerInfoModule;
import night.modules.impl.visuals.NoRenderModule;
import night.utils.IMinecraft;
import night.utils.color.ColorUtils;
import night.utils.graphics.Renderer2D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Screen.class})
public class ScreenMixin
implements IMinecraft {
    @Shadow
    public int width;
    @Shadow
    public int height;

    @Inject(method={"extractBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Night.MODULE_MANAGER.getModule(MenuModule.class).isToggled() && Night.MODULE_MANAGER.getModule(MenuModule.class).mainMenu.getValue() && ScreenMixin.mc.level == null) {
            Renderer2D.renderQuad(context, 0.0f, 0.0f, this.width, this.height, new Color(25, 25, 25, 255));
            for (int i = 0; i < this.width; ++i) {
                Color color = ColorUtils.getRainbow(2L, 0.7f, 1.0f, 255, (long)i * 5L);
                Renderer2D.renderQuad(context, i, 0.0f, i + 1, 1.0f, color);
            }
            ci.cancel();
            return;
        }
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).background.getValue() && ((Screen)(Object)this).isInGameUi()) {
            ci.cancel();
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
   private void night$hideLegitInventory(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if (MainhandModule.hidingLegitInventory && (Object)this instanceof InventoryScreen) {
         ci.cancel();
      }
   }

    @Inject(method={"extractRenderStateWithTooltipAndSubtitles"}, at={@At(value="TAIL")})
   private void night$shulkerInfoOnTop(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if ((Object)this instanceof AbstractContainerScreen<?> containerScreen) {
         ShulkerInfoModule shulkerInfoModule = Night.MODULE_MANAGER.getModule(ShulkerInfoModule.class);
         if (shulkerInfoModule != null && shulkerInfoModule.isToggled()) {
            Slot hoveredSlot = ((AbstractContainerScreenAccessor)containerScreen).getHoveredSlot();
            if (hoveredSlot != null
               && !hoveredSlot.getItem().isEmpty()
               && containerScreen.getMenu().getCarried().isEmpty()
               && shulkerInfoModule.hasItems(hoveredSlot.getItem())) {
               shulkerInfoModule.renderInfo(context, mouseX, mouseY, hoveredSlot.getItem());
            }
         }
      }
   }
}

