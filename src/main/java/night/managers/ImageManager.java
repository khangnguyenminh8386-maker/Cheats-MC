/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.platform.NativeImage$Format
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package night.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import night.Night;
import night.utils.graphics.CustomImageTexture;

public class ImageManager {
    public static final Path IMAGES_DIR = Paths.get("Cheats MC", "images");
    public static final Identifier CUSTOM_IMAGE_ID = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/effect/custom_image.png");
    public static final Identifier CUSTOM_IMAGE_ID_LEGACY = Identifier.fromNamespaceAndPath((String)"night", (String)"textures/custom_image.png");
    private final Map<String, ImageEntry> cache = new ConcurrentHashMap<String, ImageEntry>();
    private String currentImageName = "None";

    public ImageManager() {
        this.ensureDirectoryExists();
        try {
            Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, (AbstractTexture)CustomImageTexture.INSTANCE);
            Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID_LEGACY, (AbstractTexture)CustomImageTexture.INSTANCE);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        String saved = ImageManager.findSavedActiveImage();
        if (saved != null && !saved.equalsIgnoreCase("None") && !saved.isEmpty()) {
            this.setCurrentActiveImage(saved);
        } else {
            this.initDefaultTexture();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String findSavedActiveImage() {
        try {
            Path configPath;
            String currentCfg = "default";
            Path generalPath = Paths.get("Cheats MC", "General.json");
            if (Files.exists(generalPath, new LinkOption[0])) {
                try (InputStream stream2 = Files.newInputStream(generalPath, new OpenOption[0]);){
                    JsonObject obj = JsonParser.parseReader((Reader)new InputStreamReader(stream2)).getAsJsonObject();
                    if (obj.has("Config")) {
                        currentCfg = obj.get("Config").getAsString();
                    }
                }
            }
            if (!Files.exists(configPath = Paths.get("Cheats MC", "Configs", currentCfg + ".json"), new LinkOption[0])) {
                configPath = Paths.get("Cheats MC", "Configs", "default.json");
            }
            if (!Files.exists(configPath, new LinkOption[0])) return "None";
            try (InputStream stream = Files.newInputStream(configPath, new OpenOption[0]);){
                JsonObject obj = JsonParser.parseReader((Reader)new InputStreamReader(stream)).getAsJsonObject();
                if (!obj.has("Shaders")) return "None";
                JsonObject shaders = obj.getAsJsonObject("Shaders");
                if (!shaders.has("Settings")) return "None";
                JsonObject settings = shaders.getAsJsonObject("Settings");
                if (!settings.has("Image")) return "None";
                String imgName = settings.get("Image").getAsString();
                if (imgName == null) return "None";
                if (imgName.isEmpty()) return "None";
                if (imgName.equalsIgnoreCase("None")) return "None";
                String string = imgName;
                return string;
            }
        }
        catch (Throwable t) {
            Night.LOGGER.debug("Could not pre-read saved image config: {}", (Object)t.getMessage());
        }
        return "None";
    }

    public void ensureDirectoryExists() {
        try {
            if (!Files.exists(IMAGES_DIR, new LinkOption[0])) {
                Files.createDirectories(IMAGES_DIR, new FileAttribute[0]);
            }
        }
        catch (Exception e) {
            Night.LOGGER.error("Failed to create images directory", (Throwable)e);
        }
    }

    public static NativeImage loadNativeImage(File file) throws Exception {
        BufferedImage bImg = ImageIO.read(file);
        if (bImg == null) {
            throw new Exception("ImageIO failed to read image file: " + file.getName());
        }
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bImg.getWidth(), bImg.getHeight(), false);
        for (int y = 0; y < bImg.getHeight(); ++y) {
            for (int x = 0; x < bImg.getWidth(); ++x) {
                nativeImage.setPixel(x, y, bImg.getRGB(x, y));
            }
        }
        return nativeImage;
    }

    public static NativeImage loadNativeImage(InputStream stream) throws Exception {
        BufferedImage bImg = ImageIO.read(stream);
        if (bImg == null) {
            throw new Exception("ImageIO failed to read image stream");
        }
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bImg.getWidth(), bImg.getHeight(), false);
        for (int y = 0; y < bImg.getHeight(); ++y) {
            for (int x = 0; x < bImg.getWidth(); ++x) {
                nativeImage.setPixel(x, y, bImg.getRGB(x, y));
            }
        }
        return nativeImage;
    }

    private void initDefaultTexture() {
        try {
            NativeImage image = new NativeImage(1, 1, false);
            image.setPixel(0, 0, 0);
            CustomImageTexture.INSTANCE.update(image);
        }
        catch (Throwable t) {
            Night.LOGGER.warn("Failed to load default custom_image fallback", t);
        }
    }

    public static File resolveImageFile(String name) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("None")) {
            return null;
        }
        ArrayList<Path> dirs = new ArrayList<Path>();
        dirs.add(IMAGES_DIR);
        dirs.add(Paths.get("Cheats MC", "Images"));
        dirs.add(Paths.get("Cheats MC", "images"));
        dirs.add(Paths.get("Cheats MC", "Images"));
        dirs.add(Paths.get("night", "images"));
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            dirs.add(gameDir.resolve("Cheats MC").resolve("images"));
            dirs.add(gameDir.resolve("Cheats MC").resolve("Images"));
            dirs.add(gameDir.resolve("Cheats MC").resolve("images"));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (Path d : dirs) {
            File f = d.resolve(name).toFile();
            if (!f.exists() || !f.isFile()) continue;
            return f;
        }
        return IMAGES_DIR.resolve(name).toFile();
    }

    public List<File> getImageFiles() {
        this.ensureDirectoryExists();
        LinkedHashMap<String, File> unique = new LinkedHashMap<String, File>();
        ArrayList<Path> dirs = new ArrayList<Path>();
        dirs.add(IMAGES_DIR);
        dirs.add(Paths.get("Cheats MC", "Images"));
        dirs.add(Paths.get("Cheats MC", "images"));
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            dirs.add(gameDir.resolve("Cheats MC").resolve("images"));
            dirs.add(gameDir.resolve("Cheats MC").resolve("Images"));
        }
        catch (Throwable gameDir) {
            // empty catch block
        }
        for (Path d : dirs) {
            File[] files;
            File dir = d.toFile();
            if (!dir.exists() || !dir.isDirectory() || (files = dir.listFiles((dirFile, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp") || lower.endsWith(".webp");
            })) == null) continue;
            for (File f : files) {
                unique.putIfAbsent(f.getName(), f);
            }
        }
        ArrayList<File> result = new ArrayList<File>(unique.values());
        result.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }

    public ImageEntry getImageEntry(String name) {
        File file;
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("None")) {
            return null;
        }
        if (this.cache.containsKey(name)) {
            ImageEntry cached = this.cache.get(name);
            if (cached.file.exists()) {
                return cached;
            }
            cached.close();
            this.cache.remove(name);
        }
        if ((file = ImageManager.resolveImageFile(name)) == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try {
            NativeImage nativeImage = ImageManager.loadNativeImage(file);
            Identifier id = Identifier.fromNamespaceAndPath((String)"night", (String)("preview_" + Math.abs(name.hashCode()) + "_" + System.currentTimeMillis()));
            DynamicTexture texture = new DynamicTexture(() -> "night/" + name, nativeImage);
            texture.upload();
            Minecraft.getInstance().getTextureManager().register(id, (AbstractTexture)texture);
            ImageEntry entry = new ImageEntry(name, file, id, texture, nativeImage.getWidth(), nativeImage.getHeight());
            this.cache.put(name, entry);
            return entry;
        }
        catch (Throwable t) {
            Night.LOGGER.error("Failed to load image: " + name, t);
            return null;
        }
    }

    public void setCurrentActiveImage(String name) {
        String string = this.currentImageName = name != null ? name : "None";
        if (this.currentImageName.equalsIgnoreCase("None") || this.currentImageName.isEmpty()) {
            this.initDefaultTexture();
            return;
        }
        File file = ImageManager.resolveImageFile(this.currentImageName);
        if (file == null || !file.exists()) {
            this.initDefaultTexture();
            return;
        }
        try {
            NativeImage nativeImage = ImageManager.loadNativeImage(file);
            CustomImageTexture.INSTANCE.update(nativeImage);
            try {
                Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, (AbstractTexture)CustomImageTexture.INSTANCE);
                Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID_LEGACY, (AbstractTexture)CustomImageTexture.INSTANCE);
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable t) {
            Night.LOGGER.error("Failed to set active custom image: " + name, t);
            this.initDefaultTexture();
        }
    }

    public String getCurrentImageName() {
        return this.currentImageName;
    }

    public static class ImageEntry {
        public final String name;
        public final File file;
        public final Identifier identifier;
        public final DynamicTexture texture;
        public final int width;
        public final int height;

        public ImageEntry(String name, File file, Identifier identifier, DynamicTexture texture, int width, int height) {
            this.name = name;
            this.file = file;
            this.identifier = identifier;
            this.texture = texture;
            this.width = width;
            this.height = height;
        }

        public void close() {
            try {
                if (this.texture != null) {
                    this.texture.close();
                }
                if (this.identifier != null) {
                    Minecraft.getInstance().getTextureManager().release(this.identifier);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}

