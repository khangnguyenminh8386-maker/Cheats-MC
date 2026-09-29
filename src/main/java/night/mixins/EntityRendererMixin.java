/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Display
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.vehicle.boat.AbstractBoat
 *  net.minecraft.world.entity.vehicle.minecart.AbstractMinecart
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import night.Night;
import night.modules.impl.visuals.ChamsModule;
import night.modules.impl.visuals.LogoutSpotModule;
import night.modules.impl.visuals.NameTagsModule;
import night.modules.impl.visuals.NoRenderModule;
import night.modules.impl.visuals.PopChamsModule;
import night.modules.impl.visuals.ShadersModule;
import night.utils.mixins.IChamsCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityRenderer.class})
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method={"getNameTag"}, at={@At(value="HEAD")}, cancellable=true)
    private void getDisplayName(T entity, CallbackInfoReturnable<Component> info) {
        if (entity instanceof RemotePlayer) {
            RemotePlayer ghost = (RemotePlayer)entity;
            PopChamsModule popChams = Night.MODULE_MANAGER.getModule(PopChamsModule.class);
            if (popChams != null && popChams.isToggled() && popChams.isGhost((Entity)ghost)) {
                info.setReturnValue(null);
                return;
            }
            LogoutSpotModule logoutSpot = Night.MODULE_MANAGER.getModule(LogoutSpotModule.class);
            if (logoutSpot != null && logoutSpot.isToggled() && logoutSpot.isGhost((Entity)ghost)) {
                info.setReturnValue(null);
                return;
            }
        }
        if (entity instanceof Player && Night.MODULE_MANAGER.getModule(NameTagsModule.class).isToggled()) {
            info.setReturnValue(null);
        }
    }

    @Inject(method={"shouldRender"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$limitItemRendering(T entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule noRender;
        if (entity instanceof RemotePlayer) {
            PopChamsModule popChams;
            RemotePlayer ghost = (RemotePlayer)entity;
            PopChamsModule popChamsModule = popChams = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
            if (popChams != null && popChams.isToggled() && popChams.isGhost((Entity)ghost)) {
                cir.setReturnValue(true);
                return;
            }
        }
        NoRenderModule noRenderModule = noRender = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
        if (noRender != null && noRender.isToggled()) {
            if (noRender.items.getValue() && entity instanceof ItemEntity && !noRender.shouldRenderItem()) {
                cir.setReturnValue(false);
            }
            if (noRender.displays.getValue() && entity instanceof Display) {
                cir.setReturnValue(false);
            }
            if (noRender.armorStand.getValue() && entity instanceof ArmorStand) {
                cir.setReturnValue(false);
            }
            if (noRender.minecart.getValue() && entity instanceof AbstractMinecart) {
                cir.setReturnValue(false);
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
   private void night$chams(T entity, S state, float partialTicks, CallbackInfo info) {
      IChamsCapture capture = (IChamsCapture)state;
      capture.night$setChams(false, 0, false, 0, false);
      capture.night$setChamsYOffset(0.0F);
      PopChamsModule popChams = Night.MODULE_MANAGER.getModule(PopChamsModule.class);
      LogoutSpotModule logoutSpot = Night.MODULE_MANAGER.getModule(LogoutSpotModule.class);
      boolean isPopGhost = entity instanceof RemotePlayer ghost && popChams != null && popChams.isGhost(ghost);
      boolean isLogoutGhost = entity instanceof RemotePlayer ghostx && logoutSpot != null && logoutSpot.isGhost(ghostx);
      boolean isGhost = isPopGhost || isLogoutGhost;
      if (!isGhost) {
         ChamsModule chams = Night.MODULE_MANAGER.getModule(ChamsModule.class);
         if (chams.isToggled()) {
            if (entity instanceof LivingEntity livingEntity && chams.isValidEntity(livingEntity)) {
               chams.applyEntityChams(livingEntity, capture);
            } else if (chams.crystals.getValue() && entity instanceof EndCrystal && chams.inRange(entity)) {
               chams.applyCrystalChams(capture);
            } else if (chams.vehicles.getValue() && chams.inRange(entity) && (entity instanceof AbstractBoat || entity instanceof AbstractMinecart)) {
               chams.applyVehicleChams(capture);
            }
         }

         ShadersModule shaders = Night.MODULE_MANAGER.getModule(ShadersModule.class);
         if (shaders.isToggled() && shaders.isValidEntity(entity)) {
            state.outlineColor = shaders.getFillColor(entity);
         }
      }

      if (popChams != null && popChams.isToggled() && isPopGhost) {
         RemotePlayer ghostxx = (RemotePlayer)entity;
         capture.night$setChams(
            popChams.shouldFill(), popChams.getFillColor(ghostxx).getRGB(), popChams.shouldOutline(), popChams.getOutlineColor(ghostxx).getRGB(), false, true
         );
         capture.night$setChamsYOffset(popChams.getYOffset(ghostxx));
      }

      if (logoutSpot != null && logoutSpot.isToggled() && isLogoutGhost) {
         RemotePlayer ghostxx = (RemotePlayer)entity;
         capture.night$setChams(
            logoutSpot.shouldFill(),
            logoutSpot.getFillColor(ghostxx).getRGB(),
            logoutSpot.shouldOutline(),
            logoutSpot.getOutlineColor(ghostxx).getRGB(),
            false,
            true
         );
      }
   }
}

