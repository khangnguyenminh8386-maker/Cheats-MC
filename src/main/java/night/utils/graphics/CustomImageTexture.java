/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.GpuFormat
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 */
package night.utils.graphics;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import night.Night;

public class CustomImageTexture
extends AbstractTexture {
    public static final CustomImageTexture INSTANCE = new CustomImageTexture();
    private NativeImage currentPixels;

    public void update(NativeImage image) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && !mc.isSameThread()) {
            mc.execute(() -> this.updateDirect(image));
        } else {
            this.updateDirect(image);
        }
    }

    private synchronized void updateDirect(NativeImage image) {
        if (this.currentPixels != null && this.currentPixels != image) {
            try {
                this.currentPixels.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        this.currentPixels = image;
        if (this.texture != null) {
            try {
                this.texture.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.texture = null;
        }
        if (image == null) {
            this.textureView = null;
            return;
        }
        try {
            GpuDevice device = RenderSystem.getDevice();
            if (device == null) {
                return;
            }
            this.texture = device.createTexture(() -> "night/custom_image", 5, GpuFormat.RGBA8_UNORM, image.getWidth(), image.getHeight(), 1, 1);
            this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
            this.textureView = device.createTextureView(this.texture);
            device.createCommandEncoder().writeToTexture(this.texture, image);
        }
        catch (Throwable t) {
            Night.LOGGER.error("Failed to upload custom image texture to GPU", t);
            if (this.texture != null) {
                try {
                    this.texture.close();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                this.texture = null;
            }
            this.textureView = null;
        }
    }

    public GpuTextureView getTextureView() {
        if (this.textureView == null) {
            String active;
            if (Night.IMAGE_MANAGER != null && (active = Night.IMAGE_MANAGER.getCurrentImageName()) != null && !active.equalsIgnoreCase("None") && !active.isEmpty()) {
                Night.IMAGE_MANAGER.setCurrentActiveImage(active);
            }
            if (this.textureView == null) {
                try {
                    NativeImage fallback = new NativeImage(1, 1, false);
                    fallback.setPixel(0, 0, 0);
                    this.updateDirect(fallback);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return super.getTextureView();
    }

    public synchronized void close() {
        if (this.currentPixels != null) {
            try {
                this.currentPixels.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.currentPixels = null;
        }
        if (this.texture != null) {
            try {
                this.texture.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.texture = null;
        }
        this.textureView = null;
    }
}

