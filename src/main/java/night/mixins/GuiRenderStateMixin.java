/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.client.renderer.state.gui.GuiTextRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import night.Night;
import night.mixins.accessors.GuiTextRenderStateAccessor;
import night.modules.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiRenderState.class})
public class GuiRenderStateMixin {
    @Shadow
    private int firstStratumAfterBlur;

    @Inject(method={"blurBeforeThisStratum"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$noopIfAlreadyBlurredThisFrame(CallbackInfo ci) {
        if (this.firstStratumAfterBlur != Integer.MAX_VALUE) {
            ci.cancel();
        }
    }

    @Inject(method={"addText"}, at={@At(value="HEAD")}, cancellable=true)
   private void night$globalFont(GuiTextRenderState state, CallbackInfo ci) {
      if (Night.MODULE_MANAGER != null) {
         FontModule module = Night.MODULE_MANAGER.getModule(FontModule.class);
         if (module != null && module.isToggled() && module.customFont.getValue() && module.global.getValue()) {
            if (Night.FONT_MANAGER != null && Night.FONT_MANAGER.getFontRenderer() != null) {
               GuiTextRenderStateAccessor acc = (GuiTextRenderStateAccessor)(Object)state;
               GuiRenderState self = (GuiRenderState)(Object)this;
               if (acc.isDropShadow()) {
                  float offset = 1.0F;
                  Night.FONT_MANAGER
                     .getFontRenderer()
                     .submitGlyphs(self, acc.getPose(), acc.getScissor(), acc.getText(), acc.getX() + offset, acc.getY() + offset, acc.getColor(), true);
               }

               Night.FONT_MANAGER
                  .getFontRenderer()
                  .submitGlyphs(self, acc.getPose(), acc.getScissor(), acc.getText(), acc.getX(), acc.getY(), acc.getColor(), false);
               ci.cancel();
            }
         }
      }
   }
}

