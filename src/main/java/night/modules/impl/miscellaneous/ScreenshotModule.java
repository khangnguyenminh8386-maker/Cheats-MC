/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$OpenFile
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.Util
 */
package night.modules.impl.miscellaneous;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Util;
import night.Night;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.utils.chat.ChatUtils;
import night.utils.system.ImageTransferable;

@RegisterModule(name="Screenshot", description="Automatically copies screenshots to clipboard when pressing F2.", category=Module.Category.MISCELLANEOUS, toggled=true)
public class ScreenshotModule
extends Module {
    public BooleanSetting clipboard = new BooleanSetting("Clipboard", "Copies screenshot image directly to your clipboard.", true);
    public BooleanSetting copyFile = new BooleanSetting("CopyFile", "Also includes file flavor for pasting into file explorers.", true);
    public BooleanSetting notify = new BooleanSetting("Notify", "Sends a confirmation message in chat when copied.", true);
    public BooleanSetting openFolder = new BooleanSetting("OpenFolder", "Automatically opens the screenshots folder.", false);

    public void onScreenshotCaptured(Component component) {
        if (!this.isToggled()) {
            return;
        }
        File file = this.extractScreenshotFile(component);
        if (file == null || !file.exists()) {
            file = this.findLatestScreenshotFile();
        }
        if (file == null || !file.exists()) {
            return;
        }
        File targetFile = file;
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage image;
                boolean copied = false;
                if (this.clipboard.getValue() && (image = ImageIO.read(targetFile)) != null) {
                    ImageTransferable transferable = new ImageTransferable(image, targetFile, this.copyFile.getValue());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
                    copied = true;
                }
                if (!copied && this.openFolder.getValue()) {
                    Util.getPlatform().openFile(targetFile.getParentFile());
                }
                if (this.notify.getValue()) {
                    Night.CHAT_MANAGER.message("Screenshot copied to clipboard: " + String.valueOf(ChatUtils.getPrimary()) + targetFile.getName(), "screenshot-copied");
                }
            }
            catch (Throwable t) {
                Night.LOGGER.error("Failed to copy screenshot to clipboard", t);
            }
        });
    }

    private File extractScreenshotFile(Component component) {
        ClickEvent clickEvent;
        if (component == null) {
            return null;
        }
        Style style = component.getStyle();
        if (style != null && (clickEvent = style.getClickEvent()) instanceof ClickEvent.OpenFile) {
            ClickEvent.OpenFile openFile = (ClickEvent.OpenFile)clickEvent;
            return openFile.file();
        }
        for (Component sibling : component.getSiblings()) {
            File res = this.extractScreenshotFile(sibling);
            if (res == null) continue;
            return res;
        }
        return null;
    }

    private File findLatestScreenshotFile() {
        try {
            File dir = new File(ScreenshotModule.mc.gameDirectory, "screenshots");
            if (!dir.exists() || !dir.isDirectory()) {
                return null;
            }
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            if (files == null || files.length == 0) {
                return null;
            }
            return Arrays.stream(files).max(Comparator.comparingLong(File::lastModified)).orElse(null);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
}

