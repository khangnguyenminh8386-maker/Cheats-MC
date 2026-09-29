/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.BlendFactor
 *  com.mojang.blaze3d.platform.CompareOp
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.resources.Identifier
 */
package night.utils.graphics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import night.mixins.accessors.RenderPipelinesAccessor;

public class TextGlowShader {
    private static final Identifier TEXT_GLOW_FRAGMENT = Identifier.fromNamespaceAndPath((String)"night", (String)"core/text_glow");
    public static final RenderPipeline TEXT_GLOW_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()}).withLocation("night/text_glow").withFragmentShader(TEXT_GLOW_FRAGMENT).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build();
    private static final Map<Identifier, RenderType> WORLD_GLOW_TYPES = new ConcurrentHashMap<Identifier, RenderType>();

    public static RenderType getWorldGlowType(Identifier textureId) {
        return WORLD_GLOW_TYPES.computeIfAbsent(textureId, id -> RenderType.create((String)("night_text_glow_" + id.getNamespace() + "_" + id.getPath().replace('/', '_')), (RenderSetup)RenderSetup.builder((RenderPipeline)TEXT_GLOW_PIPELINE).withTexture("Sampler0", id).createRenderSetup()));
    }
}

