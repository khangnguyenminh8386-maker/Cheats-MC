/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.packs.PackResources
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.resources.FallbackResourceManager
 *  net.minecraft.server.packs.resources.MultiPackResourceManager
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import java.util.List;
import java.util.Map;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import night.utils.graphics.NightPackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MultiPackResourceManager.class})
public class MultiPackResourceManagerMixin {
    @Shadow
    @Final
    private Map<String, FallbackResourceManager> namespacedManagers;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void night$injectClientPack(PackType type, List<PackResources> packs, CallbackInfo ci) {
        if (type == PackType.CLIENT_RESOURCES) {
            FallbackResourceManager manager = this.namespacedManagers.computeIfAbsent("night", k -> new FallbackResourceManager(type, "night"));
            manager.push((PackResources)NightPackResources.INSTANCE);
        }
    }
}

