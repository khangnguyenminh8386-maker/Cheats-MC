package night.gui.api;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import night.Night;

/** Exact English keys preserve the existing module and setting hover pipeline. */
public final class GuiDescriptionTranslations {
    private GuiDescriptionTranslations() {}
    private record Entry(String module, String english, String vi) {}
    private static final class Holder { private static final Map<String, String> VALUES = load(); }
    public static String translate(String english) { return Holder.VALUES.getOrDefault(english, ""); }
    private static Map<String, String> load() {
        try (InputStream stream = GuiDescriptionTranslations.class.getResourceAsStream("/assets/night/gui/module-descriptions-vi.json")) {
            if (stream == null) throw new IllegalStateException("Missing Vietnamese description resource");
            Entry[] entries = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Entry[].class);
            if (entries == null) throw new IllegalStateException("Empty Vietnamese description resource");
            Map<String, String> values = new HashMap<>();
            for (Entry entry : entries) {
                if (entry == null || entry.english == null || entry.vi == null || entry.vi.isBlank())
                    throw new IllegalStateException("Invalid Vietnamese description entry");
                String previous = values.putIfAbsent(entry.english, entry.vi);
                if (previous != null && !previous.equals(entry.vi)) throw new IllegalStateException("Conflicting Vietnamese descriptions");
            }
            return Map.copyOf(values);
        } catch (Exception error) {
            Night.LOGGER.warn("Could not load Vietnamese GUI descriptions", error);
            return Map.of();
        }
    }
}
