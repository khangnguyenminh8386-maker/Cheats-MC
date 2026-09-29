/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.StringRepresentable
 */
package night.utils.text;

import lombok.Generated;
import net.minecraft.util.StringRepresentable;

public enum CustomFormatting implements StringRepresentable
{
    CLIENT('z'),
    RAINBOW('y');

    private final char code;

    private CustomFormatting(char code) {
        this.code = code;
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    public static CustomFormatting byCode(char code) {
        char c = Character.toLowerCase(code);
        for (CustomFormatting formatting : CustomFormatting.values()) {
            if (formatting.code != c) continue;
            return formatting;
        }
        return null;
    }

    public String toString() {
        return "\u00a7" + this.code;
    }

    public String getSerializedName() {
        return null;
    }

    @Generated
    public char getCode() {
        return this.code;
    }
}

