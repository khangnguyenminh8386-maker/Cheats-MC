package cheatsmc;

import java.awt.Desktop;
import java.net.URI;
import net.fabricmc.api.ModInitializer;

/** Opens the community links once when Fabric initializes this entry point. */
public final class CommunityLinks implements ModInitializer {
    private static final String[] LINKS = {
        "https://www.youtube.com/@Khangrelaxx",
        "https://discord.gg/Ns5UT5844c",
        "https://www.tiktok.com/@khangrelax_"
    };

    @Override
    public void onInitialize() {
        Thread opener = new Thread(() -> {
            try {
                if (!Desktop.isDesktopSupported()) return;
                Desktop desktop = Desktop.getDesktop();
                if (!desktop.isSupported(Desktop.Action.BROWSE)) return;
                for (String link : LINKS) {
                    try {
                        desktop.browse(URI.create(link));
                    } catch (Exception ignored) {
                        // A failed link must not prevent the others or the game from starting.
                    }
                }
            } catch (Exception ignored) {
                // Browser integration is optional when no desktop is available.
            }
        }, "Cheats-MC-community-links");
        opener.setDaemon(true);
        opener.start();
    }
}
