/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.GpuFormat
 *  com.mojang.blaze3d.pipeline.TextureTarget
 */
package night.utils.graphics;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.TextureTarget;
import night.utils.IMinecraft;

public class StarCapture
implements IMinecraft {
    private static TextureTarget target;

    public static TextureTarget ensure() {
        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();
        if (target == null || StarCapture.target.width != w || StarCapture.target.height != h) {
            if (target != null) {
                target.destroyBuffers();
            }
            target = new TextureTarget("night_star_capture", w, h, true, GpuFormat.RGBA8_UNORM);
        }
        return target;
    }

    public static TextureTarget get() {
        return target;
    }
}

