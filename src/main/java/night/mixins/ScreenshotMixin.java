/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Screenshot
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package night.mixins;

import java.util.function.Consumer;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import night.Night;
import night.modules.impl.miscellaneous.ScreenshotModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={Screenshot.class})
public class ScreenshotMixin {
    @ModifyVariable(method={"grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private static Consumer<Component> night$wrapScreenshotConsumer(Consumer<Component> original) {
        return component -> {
            boolean handled;
            ScreenshotModule module = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ScreenshotModule.class) : null;
            boolean bl = handled = module != null && module.isToggled();
            if (!handled) {
                original.accept((Component)component);
            }
            try {
                if (handled) {
                    module.onScreenshotCaptured((Component)component);
                }
            }
            catch (Throwable t) {
                Night.LOGGER.error("Failed to process screenshot callback in ScreenshotModule", t);
            }
        };
    }
}

