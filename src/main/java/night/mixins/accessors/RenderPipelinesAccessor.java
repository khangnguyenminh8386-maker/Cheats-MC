/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  net.minecraft.client.renderer.RenderPipelines
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package night.mixins.accessors;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderPipelines.class})
public interface RenderPipelinesAccessor {
    @Accessor(value="LINES_SNIPPET")
    public static RenderPipeline.Snippet getLinesSnippet() {
        throw new AssertionError();
    }

    @Accessor(value="DEBUG_FILLED_SNIPPET")
    public static RenderPipeline.Snippet getDebugFilledSnippet() {
        throw new AssertionError();
    }

    @Accessor(value="ITEM_SNIPPET")
    public static RenderPipeline.Snippet getItemSnippet() {
        throw new AssertionError();
    }

    @Accessor(value="ENTITY_SNIPPET")
    public static RenderPipeline.Snippet getEntitySnippet() {
        throw new AssertionError();
    }

    @Accessor(value="GLOBALS_SNIPPET")
    public static RenderPipeline.Snippet getGlobalsSnippet() {
        throw new AssertionError();
    }

    @Accessor(value="GUI_TEXTURED_SNIPPET")
    public static RenderPipeline.Snippet getGuiTexturedSnippet() {
        throw new AssertionError();
    }
}

