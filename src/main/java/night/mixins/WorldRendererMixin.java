/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.ShaderManager
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Set;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import night.Night;
import night.modules.impl.visuals.AtmosphereModule;
import night.modules.impl.visuals.BlockHighlightModule;
import night.modules.impl.visuals.ShadersModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={LevelRenderer.class})
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private RenderTarget entityOutlineTarget;

    @ModifyReturnValue(method={"entityOutlineTarget"}, at={@At(value="RETURN")})
    private RenderTarget night$outlineTargetOutsideFrameGraph(RenderTarget original) {
        return original != null ? original : this.entityOutlineTarget;
    }

    @ModifyVariable(method={"render"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private boolean renderOutline(boolean renderOutline) {
        if (Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule(BlockHighlightModule.class).isToggled()) {
            return false;
        }
        return renderOutline;
    }

    @ModifyVariable(method={"render"}, at=@At(value="HEAD"), argsOnly=true, ordinal=1)
    private boolean night$forceShouldRenderSky(boolean shouldRenderSky) {
        AtmosphereModule atmosphere;
        AtmosphereModule atmosphereModule = atmosphere = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphere != null && atmosphere.isToggled() && atmosphere.modifySky.getValue()) {
            return true;
        }
        return shouldRenderSky;
    }

    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain night$interceptEntityOutlineChain(ShaderManager instance, Identifier id, Set<Identifier> allowed) {
        if (id.equals((Object)Identifier.withDefaultNamespace((String)"entity_outline")) && ShadersModule.pickActiveOutlineChain() != null) {
            return null;
        }
        return instance.getPostChain(id, allowed);
    }
}

