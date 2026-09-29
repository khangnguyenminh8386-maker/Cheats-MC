/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import javax.annotation.Nullable;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Style.class})
public interface StyleAccessor {
    @Invoker(value="<init>")
    public static Style create(@Nullable TextColor color, @Nullable Integer shadowColor, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough, @Nullable Boolean obfuscated, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String insertion, @Nullable FontDescription font) {
        return null;
    }
}

