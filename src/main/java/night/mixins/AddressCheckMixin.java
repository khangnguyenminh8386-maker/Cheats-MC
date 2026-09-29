/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.resolver.AddressCheck
 *  net.minecraft.client.multiplayer.resolver.ResolvedServerAddress
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import net.minecraft.client.multiplayer.resolver.AddressCheck;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AddressCheck.class})
public interface AddressCheckMixin {
    @Inject(method={"createFromService"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onCreateFromService(CallbackInfoReturnable<AddressCheck> cir) {
        cir.setReturnValue(new AddressCheck(){

            public boolean isAllowed(ResolvedServerAddress address) {
                return true;
            }

            public boolean isAllowed(ServerAddress address) {
                return true;
            }
        });
    }
}

