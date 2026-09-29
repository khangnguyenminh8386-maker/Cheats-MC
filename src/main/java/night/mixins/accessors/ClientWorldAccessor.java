/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ClientLevel.class})
public interface ClientWorldAccessor {
    @Invoker(value="getBlockStatePredictionHandler")
    public BlockStatePredictionHandler invokeGetPendingUpdateManager();
}

