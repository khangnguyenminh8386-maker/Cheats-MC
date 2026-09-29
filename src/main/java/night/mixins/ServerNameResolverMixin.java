/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.resolver.ResolvedServerAddress
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.client.multiplayer.resolver.ServerNameResolver
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package night.mixins;

import java.net.InetSocketAddress;
import java.util.Optional;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ServerNameResolver.class})
public class ServerNameResolverMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Night/DnsFix");

    @Inject(method={"resolveAddress"}, at={@At(value="RETURN")}, cancellable=true)
    private void resolveAddress(ServerAddress address, CallbackInfoReturnable<Optional<ResolvedServerAddress>> cir) {
        if (((Optional)cir.getReturnValue()).isPresent()) {
            return;
        }
        try {
            InetSocketAddress resolved = new InetSocketAddress(address.getHost(), address.getPort());
            if (resolved.isUnresolved()) {
                LOGGER.warn("[PB] java.net fallback resolve of {} also unresolved", (Object)address.getHost());
                return;
            }
            LOGGER.info("[PB] Resolved {} via java.net fallback: {}", (Object)address.getHost(), (Object)resolved);
            cir.setReturnValue(Optional.of(ResolvedServerAddress.from((InetSocketAddress)resolved)));
        }
        catch (Exception e) {
            LOGGER.warn("[PB] java.net fallback resolve of {} failed", (Object)address.getHost(), (Object)e);
        }
    }
}

