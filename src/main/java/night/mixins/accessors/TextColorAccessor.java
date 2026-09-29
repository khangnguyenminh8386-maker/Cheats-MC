/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.TextColor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={TextColor.class})
public interface TextColorAccessor {
    @Invoker(value="<init>")
    public static TextColor create(int rgb, String name) {
        throw new AssertionError();
    }
}

