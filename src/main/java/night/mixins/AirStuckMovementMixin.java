package night.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import night.modules.impl.movement.AirStuckModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LocalPlayer.class, priority = 1000)
public abstract class AirStuckMovementMixin {
    @WrapOperation(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"),
            require = 1, expect = 1, allow = 1)
    private void night$freezeVanillaMovement(LocalPlayer player, MoverType movementType,
            Vec3 displacement, Operation<Void> original) {
        original.call(player, movementType, AirStuckModule.movementDisplacement(displacement));
    }

}
