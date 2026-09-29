/*
 * Decompiled with CFR 0.152.
 */
package night.managers;

import java.util.LinkedHashMap;
import java.util.Map;
import night.settings.impl.BooleanSetting;
import night.settings.impl.CategorySetting;
import night.settings.impl.PositionSetting;

public class HudElementRegistry {
    private static final Map<String, Element> ELEMENTS = new LinkedHashMap<String, Element>();
    private static final Map<String, float[]> RAW_BOUNDS = new LinkedHashMap<String, float[]>();

    public static void register(String name, BooleanSetting enabled, PositionSetting offset) {
        HudElementRegistry.register(name, enabled, offset, null);
    }

    public static void register(String name, BooleanSetting enabled, PositionSetting offset, CategorySetting category) {
        ELEMENTS.put(name, new Element(name, enabled, offset, category));
    }

    public static void reportBounds(String name, float x0, float y0, float x1, float y1) {
        RAW_BOUNDS.put(name, new float[]{x0, y0, x1, y1});
    }

    public static Map<String, Element> getElements() {
        return ELEMENTS;
    }

    public static float[] getBounds(String name) {
        float[] raw = RAW_BOUNDS.get(name);
        Element element = ELEMENTS.get(name);
        if (raw == null || element == null) {
            return raw;
        }
        float ox = element.offset().getX();
        float oy = element.offset().getY();
        return new float[]{raw[0] + ox, raw[1] + oy, raw[2] + ox, raw[3] + oy};
    }

    public static void clamp(String name, int screenWidth, int screenHeight) {
        Element element = ELEMENTS.get(name);
        float[] raw = RAW_BOUNDS.get(name);
        if (element == null || raw == null) {
            return;
        }
        float width = raw[2] - raw[0];
        float height = raw[3] - raw[1];
        float minOffsetX = -raw[0];
        float maxOffsetX = Math.max(minOffsetX, (float)screenWidth - width - raw[0]);
        float minOffsetY = -raw[1];
        float maxOffsetY = Math.max(minOffsetY, (float)screenHeight - height - raw[1]);
        element.offset().set(Math.clamp(element.offset().getX(), minOffsetX, maxOffsetX), Math.clamp(element.offset().getY(), minOffsetY, maxOffsetY));
    }

    public record Element(String name, BooleanSetting enabled, PositionSetting offset, CategorySetting category) {
    }
}

