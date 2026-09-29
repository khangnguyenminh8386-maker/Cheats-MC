/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.llamalad7.mixinextras.sugar.ref.LocalRef
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.StringDecomposer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import night.utils.IMinecraft;
import night.utils.text.CustomFormatting;
import night.utils.text.FormattingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={StringDecomposer.class})
public class TextVisitFactoryMixin
implements IMinecraft {
    @WrapOperation(method={"iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z"}, at={@At(value="INVOKE", target="Ljava/lang/String;charAt(I)C", ordinal=1)})
    private static char visitFormatted(String instance, int index, Operation<Character> original, @Local(ordinal=2) LocalRef<Style> style) {
        CustomFormatting customFormatting = CustomFormatting.byCode(instance.charAt(index));
        if (customFormatting != null) {
            style.set(FormattingUtils.withExclusiveFormatting((Style)style.get(), customFormatting));
        }
        return ((Character)original.call(new Object[]{instance, index})).charValue();
    }
}

