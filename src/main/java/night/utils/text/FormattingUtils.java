/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 */
package night.utils.text;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import night.Night;
import night.mixins.accessors.StyleAccessor;
import night.mixins.accessors.TextColorAccessor;
import night.utils.color.ColorUtils;
import night.utils.text.CustomFormatting;

public class FormattingUtils {
    public static String[] FORMATS = new String[]{"White", "Black", "Blue", "Dark Blue", "Green", "Dark Green", "Aqua", "Dark Aqua", "Red", "Dark Red", "Light Purple", "Dark Purple", "Yellow", "Gold", "Gray", "Dark Gray", "Client", "Rainbow"};

    public static Style withExclusiveFormatting(Style style, CustomFormatting formatting) {
        TextColor textColor = style.getColor();
        if (formatting == CustomFormatting.CLIENT) {
            textColor = TextColorAccessor.create(ColorUtils.getGlobalColor().getRGB(), "CLIENT");
        } else if (formatting == CustomFormatting.RAINBOW) {
            textColor = TextColorAccessor.create(ColorUtils.getGlobalColor().getRGB(), "RAINBOW");
        }
        return StyleAccessor.create(textColor, null, false, false, false, false, false, style.getClickEvent(), style.getHoverEvent(), style.getInsertion(), style.getFont());
    }

    public static Object getFormatting(String str) {
        return switch (str.toLowerCase()) {
            case "black" -> ChatFormatting.BLACK;
            case "blue" -> ChatFormatting.BLUE;
            case "dark blue" -> ChatFormatting.DARK_BLUE;
            case "green" -> ChatFormatting.GREEN;
            case "dark green" -> ChatFormatting.DARK_GREEN;
            case "aqua" -> ChatFormatting.AQUA;
            case "dark aqua" -> ChatFormatting.DARK_AQUA;
            case "red" -> ChatFormatting.RED;
            case "dark red" -> ChatFormatting.DARK_RED;
            case "light purple" -> ChatFormatting.LIGHT_PURPLE;
            case "dark purple" -> ChatFormatting.DARK_PURPLE;
            case "yellow" -> ChatFormatting.YELLOW;
            case "gold" -> ChatFormatting.GOLD;
            case "gray" -> ChatFormatting.GRAY;
            case "dark gray" -> ChatFormatting.DARK_GRAY;
            case "client" -> CustomFormatting.CLIENT;
            case "rainbow" -> CustomFormatting.RAINBOW;
            default -> ChatFormatting.WHITE;
        };
    }

    public static List<String> wrapText(String text, int width) {
        ArrayList<String> wrappedText = new ArrayList<String>();
        String[] words = text.split(" ");
        Object current = "";
        for (String word : words) {
            if (Night.FONT_MANAGER.getWidth((String)current) + Night.FONT_MANAGER.getWidth(word) <= width) {
                current = (String)current + word + " ";
                continue;
            }
            wrappedText.add((String)current);
            current = word + " ";
        }
        if (Night.FONT_MANAGER.getWidth((String)current) > 0) {
            wrappedText.add((String)current);
        }
        return wrappedText;
    }

    public static String[] formatSeconds(long seconds) {
        String h = String.format("%02d", seconds / 3600L);
        String m = String.format("%02d", seconds % 3600L / 60L);
        String s = String.format("%02d", seconds % 60L);
        return new String[]{h, m, s};
    }
}

