/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package night.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import night.Night;
import night.managers.ChatManager;
import night.modules.impl.core.HUDModule;
import night.modules.impl.movement.ElytraFlyModule;
import night.modules.impl.visuals.NoRenderModule;
import night.utils.graphics.HotbarCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Hud.class})
public class HudMixin {
    private static final Identifier POWDER_SNOW_OUTLINE_LOCATION = Identifier.withDefaultNamespace((String)"textures/misc/powder_snow_outline.png");

    @Inject(method={"extractEffects"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderStatusEffectOverlay(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo info) {
        if (Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule(HUDModule.class).isToggled() && Night.MODULE_MANAGER.getModule(HUDModule.class).vanillaPotions.getValue().equalsIgnoreCase("Hide")) {
            info.cancel();
        }
    }

    @Inject(method={"extractPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderPortalOverlay(GuiGraphicsExtractor context, float portalIntensity, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).portalOverlay.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"extractVignette"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderVignetteOverlay(GuiGraphicsExtractor context, Entity entity, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).vignette.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"extractSelectedItemName"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderSelectedItemName(GuiGraphicsExtractor context, CallbackInfo info) {
        if (Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).itemName.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"extractScoreboardSidebar"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderScoreboardSidebar(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo info) {
        if (Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoRenderModule.class).scoreboard.getValue()) {
            info.cancel();
        }
    }

    @WrapWithCondition(method={"extractCameraOverlays"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/Hud;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V")})
    private boolean renderTextureOverlay(Hud instance, GuiGraphicsExtractor context, Identifier texture, float alpha) {
        if (texture.equals((Object)POWDER_SNOW_OUTLINE_LOCATION)) {
            return !Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() || !Night.MODULE_MANAGER.getModule(NoRenderModule.class).snowOverlay.getValue();
        }
        if (texture.getPath().contains("pumpkin")) {
            return !Night.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() || !Night.MODULE_MANAGER.getModule(NoRenderModule.class).pumpkinOverlay.getValue();
        }
        return true;
    }

    @Inject(method={"extractChat"}, at={@At(value="HEAD")})
    private void night$onExtractChatHead(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo info) {
        ChatManager.isInsideHudExtractChat = true;
    }

    @Inject(method={"extractChat"}, at={@At(value="RETURN")})
    private void night$onExtractChatReturn(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo info) {
        ChatManager.isInsideHudExtractChat = false;
    }

    @Redirect(method={"extractItemHotbar"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack night$grimHotbarVisual(Inventory inventory, int slot) {
        ItemStack real = inventory.getItem(slot);
        if (Night.MODULE_MANAGER == null) {
            return real;
        }
        ElytraFlyModule elytraFly = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytraFly.getGrimParkedSlot() == slot) {
            return elytraFly.getGrimParkedDisplaced();
        }
        return real;
    }

    @Redirect(method={"extractSlot"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void night$extractSlotCached(GuiGraphicsExtractor context, LivingEntity entity, ItemStack stack, int x, int y, int seed) {
        HotbarCache.extractItem(context, entity, stack, x, y, seed);
    }

    @Inject(method={"clearCache"}, at={@At(value="HEAD")})
    private void night$onClearCache(CallbackInfo info) {
        HotbarCache.clear();
    }

    @Inject(method={"onDisconnected"}, at={@At(value="HEAD")})
    private void night$onDisconnected(CallbackInfo info) {
        HotbarCache.clear();
    }
}

