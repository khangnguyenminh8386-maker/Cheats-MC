/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.core.ClientAsset$ResourceTexture
 *  net.minecraft.core.ClientAsset$Texture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.PlayerSkin
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import night.Night;
import night.modules.impl.core.CapesModule;
import night.utils.IMinecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerInfo.class})
public class PlayerListEntryMixin
implements IMinecraft {
    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method={"getSkin"}, at={@At(value="TAIL")}, cancellable=true)
    private void getSkinTextures(CallbackInfoReturnable<PlayerSkin> info) {
        if (this.profile.name().equals(PlayerListEntryMixin.mc.player.getGameProfile().name()) && this.profile.id().equals(PlayerListEntryMixin.mc.player.getGameProfile().id()) && Night.MODULE_MANAGER.getModule(CapesModule.class).isToggled() && Night.MODULE_MANAGER.getModule(CapesModule.class).getCapeTexture() != null) {
            Identifier identifier = Night.MODULE_MANAGER.getModule(CapesModule.class).getCapeTexture();
            PlayerSkin skin = (PlayerSkin)info.getReturnValue();
            info.setReturnValue(new PlayerSkin(skin.body(), (ClientAsset.Texture)new ClientAsset.ResourceTexture(identifier), skin.elytra(), skin.model(), skin.secure()));
        }
    }
}

