/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.AbstractPackResources
 *  net.minecraft.server.packs.PackLocationInfo
 *  net.minecraft.server.packs.PackResources$ResourceOutput
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.repository.PackSource
 *  net.minecraft.server.packs.resources.IoSupplier
 *  org.jspecify.annotations.Nullable
 */
package night.utils.graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import night.Night;
import night.managers.ImageManager;
import org.jspecify.annotations.Nullable;

public class NightPackResources
extends AbstractPackResources {
    public static final NightPackResources INSTANCE = new NightPackResources();
    private static final String NAMESPACE = "night";
    private static final Set<String> NAMESPACES = Set.of("night");
    private static final List<String> FALLBACK_ASSETS = List.of("icon.png", "post_effect/clickgui_bg.json", "post_effect/glow_blur.json", "post_effect/menu_bg.json", "post_effect/neekeri_ui.json", "post_effect/outline_bloom.json", "post_effect/outline_both_100.json", "post_effect/outline_both_20.json", "post_effect/outline_both_40.json", "post_effect/outline_both_60.json", "post_effect/outline_both_80.json", "post_effect/outline_fill_100.json", "post_effect/outline_fill_20.json", "post_effect/outline_fill_40.json", "post_effect/outline_fill_60.json", "post_effect/outline_fill_80.json", "post_effect/outline_glow.json", "post_effect/outline_outline.json", "post_effect/star_glow.json", "shaders/core/esp.fsh", "shaders/core/killeffect_meme.fsh", "shaders/core/menu_shader.fsh", "shaders/core/sky.fsh", "shaders/core/text_glow.fsh", "shaders/program/clickgui_bg.fsh", "shaders/program/glow_blur.fsh", "shaders/program/menu_bg.fsh", "shaders/program/neekeri_ui.fsh", "shaders/program/outline.fsh", "shaders/program/outline_bloom.fsh", "shaders/program/outline_glow.fsh", "shaders/program/outline_glow_blur.fsh", "shaders/program/sky/screen_blit.vsh", "shaders/program/sky/sky_shader.fsh", "shaders/program/sky/sky_shader.json", "shaders/program/sky/sky_shader.vsh", "shaders/program/smoke/smoke.fsh", "shaders/program/smoke/smoke.json", "shaders/program/smoke/smoke.vsh", "shaders/program/star_glow.fsh", "shaders/program/star_glow_blur.fsh", "sounds.json", "sounds/vine.ogg", "splash.txt", "textures/cape.png", "textures/chorus.png", "textures/crystal.png", "textures/custom_image.png", "textures/effect/custom_image.png", "textures/effect/meme_arrow.png", "textures/effect/meme_circle.png", "textures/gear.png", "textures/glint.png", "textures/gui/logo.png", "textures/gui/logo_mask.png", "textures/gui/moonlight_bg.png", "textures/pearl.png");

    public NightPackResources() {
        super(new PackLocationInfo(NAMESPACE, (Component)Component.literal((String)"Cheats MC Resources"), PackSource.BUILT_IN, Optional.empty()));
    }

    public @Nullable IoSupplier<InputStream> getRootResource(String ... path) {
        String fullPath = String.join((CharSequence)"/", path);
        if (NightPackResources.class.getClassLoader().getResource(fullPath) != null) {
            return () -> NightPackResources.class.getClassLoader().getResourceAsStream(fullPath);
        }
        return null;
    }

    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        File imgFile;
        String activeName;
        if (type != PackType.CLIENT_RESOURCES || !NAMESPACE.equals(location.getNamespace())) {
            return null;
        }
        if ((location.getPath().equals("textures/effect/custom_image.png") || location.getPath().equals("textures/custom_image.png")) && Night.IMAGE_MANAGER != null && (activeName = Night.IMAGE_MANAGER.getCurrentImageName()) != null && !activeName.isEmpty() && !activeName.equalsIgnoreCase("None") && (imgFile = ImageManager.resolveImageFile(activeName)) != null && imgFile.exists() && imgFile.isFile()) {
            return () -> new FileInputStream(imgFile);
        }
        String path = "assets/night/" + location.getPath();
        if (NightPackResources.class.getClassLoader().getResource(path) != null) {
            return () -> NightPackResources.class.getClassLoader().getResourceAsStream(path);
        }
        return null;
    }

    public void listResources(PackType type, String namespace, String directory, PackResources.ResourceOutput output) {
        String prefix;
        HashSet<String> visited;
        block16: {
            if (type != PackType.CLIENT_RESOURCES || !NAMESPACE.equals(namespace)) {
                return;
            }
            visited = new HashSet<String>();
            prefix = directory.isEmpty() ? "" : (directory.endsWith("/") ? directory : directory + "/");
            try {
                URL codeSource = NightPackResources.class.getProtectionDomain().getCodeSource().getLocation();
                if (codeSource == null) break block16;
                String jarPrefix = "assets/night/" + prefix;
                try (InputStream in = codeSource.openStream();
                     JarInputStream jar = new JarInputStream(in);){
                    JarEntry entry;
                    while ((entry = jar.getNextJarEntry()) != null) {
                        Identifier id;
                        String relPath;
                        String name = entry.getName();
                        if (entry.isDirectory() || !name.startsWith(jarPrefix) || !visited.add(relPath = name.substring("assets/night/".length())) || (id = Identifier.tryBuild((String)NAMESPACE, (String)relPath)) == null) continue;
                        output.accept(id, () -> NightPackResources.class.getClassLoader().getResourceAsStream(name));
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        for (String asset : FALLBACK_ASSETS) {
            Identifier id;
            if (!asset.startsWith(prefix) || !visited.add(asset)) continue;
            String fullPath = "assets/night/" + asset;
            if (NightPackResources.class.getClassLoader().getResource(fullPath) == null || (id = Identifier.tryBuild((String)NAMESPACE, (String)asset)) == null) continue;
            output.accept(id, () -> NightPackResources.class.getClassLoader().getResourceAsStream(fullPath));
        }
    }

    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? NAMESPACES : Set.of();
    }

    public void close() {
    }
}

