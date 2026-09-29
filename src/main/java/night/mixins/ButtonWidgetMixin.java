/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractButton
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import night.Night;
import night.modules.impl.core.ClickGuiModule;
import night.utils.graphics.NeekeriFill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractButton.class})
public abstract class ButtonWidgetMixin {
    @Inject(method={"extractDefaultSprite"}, at={@At(value="TAIL")})
    private void night$neekeriFill(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGui.fillMode.getValue().equalsIgnoreCase("Default")) {
            return;
        }
        AbstractButton self = (AbstractButton)(Object)this;
        int alpha = Math.round(clickGui.neekeriOpacity.getValue().floatValue() / 100.0f * 255.0f);
        NeekeriFill.fill(graphics, self.getX(), self.getY(), self.getWidth(), self.getHeight(), alpha);
    }
}

