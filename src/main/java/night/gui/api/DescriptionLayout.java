package night.gui.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.ToIntFunction;

/** Geometry in the same logical coordinates used by the scaled GUI pose. */
public final class DescriptionLayout {
    private DescriptionLayout() {}
    public record Rect(int x, int y, int width, int height) {
        public boolean intersects(Rect other) {
            return x < other.x + other.width && x + width > other.x
                && y < other.y + other.height && y + height > other.y;
        }
    }
    public record Panel(Rect bounds, int headerHeight, List<String> english, List<String> vietnamese) {
        public int totalHeight() { return bounds.height(); }
    }

    public static List<String> wrap(String text, int limit, ToIntFunction<String> measure) {
        if (text.isEmpty()) return List.of();
        if (limit < 1) throw new IllegalArgumentException("No text width");
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\R", -1)) {
            String line = "";
            for (String word : paragraph.strip().split("\\s+")) {
                if (word.isEmpty()) continue;
                String joined = line.isEmpty() ? word : line + " " + word;
                if (measure.applyAsInt(joined) <= limit) { line = joined; continue; }
                if (!line.isEmpty()) { lines.add(line); line = ""; }
                for (int offset = 0; offset < word.length();) {
                    int cp = word.codePointAt(offset);
                    String glyph = new String(Character.toChars(cp));
                    if (measure.applyAsInt(glyph) > limit) throw new IllegalArgumentException("Glyph exceeds text width");
                    if (measure.applyAsInt(line + glyph) > limit) { lines.add(line); line = ""; }
                    line += glyph;
                    offset += Character.charCount(cp);
                }
            }
            lines.add(line);
        }
        return List.copyOf(lines);
    }

    public static Panel create(String english, String vietnamese, String title, String hint,
            int padding, int lineHeight, int originalHeaderHeight, int preferredWidth,
            int preferredX, int preferredY, int viewportWidth, int viewportHeight,
            List<Rect> occupied, ToIntFunction<String> measure) {
        int header = Math.max(originalHeaderHeight, lineHeight + 4);
        int minimumWidth = measure.applyAsInt(title) + measure.applyAsInt(hint) + padding * 3;
        if (viewportWidth < minimumWidth || viewportHeight < header + padding * 2) return null;
        LinkedHashSet<Integer> widths = new LinkedHashSet<>();
        widths.add(Math.min(viewportWidth, Math.max(minimumWidth, preferredWidth)));
        for (int w = widths.iterator().next() + 24; w < viewportWidth; w += 24) widths.add(w);
        widths.add(viewportWidth);
        for (Rect r : occupied) {
            if (r.x >= minimumWidth && r.x <= viewportWidth) widths.add(r.x);
            int rightGap = viewportWidth - r.x - r.width;
            if (rightGap >= minimumWidth && rightGap <= viewportWidth) widths.add(rightGap);
            for (Rect other : occupied) {
                int gap = other.x - r.x - r.width;
                if (gap >= minimumWidth && gap <= viewportWidth) widths.add(gap);
            }
        }
        for (int width : widths) {
            List<String> en, vi;
            try {
                en = wrap(english, width - padding * 2, measure);
                vi = wrap(vietnamese, width - padding * 2, measure);
            } catch (IllegalArgumentException tooNarrow) { continue; }
            int height = header + padding * 2 + (en.size() + vi.size()) * lineHeight;
            if (height > viewportHeight) continue;
            LinkedHashSet<Integer> xs = new LinkedHashSet<>(), ys = new LinkedHashSet<>();
            xs.add(Math.clamp(preferredX, 0, viewportWidth - width));
            ys.add(Math.clamp(preferredY, 0, viewportHeight - height));
            xs.add(0); xs.add(viewportWidth - width);
            ys.add(0); ys.add(viewportHeight - height);
            for (Rect r : occupied) {
                xs.add(r.x - width); xs.add(r.x + r.width);
                ys.add(r.y - height); ys.add(r.y + r.height);
            }
            for (int y : ys) for (int x : xs) {
                if (x < 0 || y < 0 || x + width > viewportWidth || y + height > viewportHeight) continue;
                Rect bounds = new Rect(x, y, width, height);
                if (occupied.stream().noneMatch(bounds::intersects)) return new Panel(bounds, header, en, vi);
            }
        }
        return null;
    }
}
