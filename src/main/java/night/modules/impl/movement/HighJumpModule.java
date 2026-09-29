package night.modules.impl.movement;

import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import night.mixins.accessors.ShulkerBoxProgressAccessor;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.ModeSetting;
import night.utils.BlockEntityInstanceTracker;
import night.utils.EarlyTickHooks;

@RegisterModule(name = "HighJump", description = "Alek Grim Shulker HighJump.", category = Module.Category.MOVEMENT)
public class HighJumpModule extends Module {
    private long timestamp = System.currentTimeMillis();
    public final ModeSetting mode = new ModeSetting("Mode", "Selects the type of mode", "Grim Shulker", new String[]{"Grim Shulker"});
    private final Runnable earlyTickCallback = this::onEarlyTick;

    @Override
    public void onEnable() {
        EarlyTickHooks.register(this.earlyTickCallback);
    }

    @Override
    public void onDisable() {
        EarlyTickHooks.unregister(this.earlyTickCallback);
    }

    private boolean passed(int threshold) {
        return (System.currentTimeMillis() - threshold) >= this.timestamp;
    }

    private void onEarlyTick() {
        if (this.getNull()) {
            return;
        }
        if (this.passed(1000)) {
            BlockEntityInstanceTracker.snapshot().stream().filter(HighJumpModule::qualifies).findFirst()
                    .ifPresent(instance -> this.timestamp = System.currentTimeMillis());
        }
        if (!this.passed(199)) {
            Vec3 velocity = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(velocity.x, velocity.y + 0.899D, velocity.z);
        }
    }

    private static boolean qualifies(BlockEntity instance) {
        if (!(instance instanceof ShulkerBoxBlockEntity shulker)) {
            return false;
        }
        if (shulker.getBlockState().getValue(ShulkerBoxBlock.FACING).getStepY() == 0) {
            return false;
        }
        float progress = ((ShulkerBoxProgressAccessor) shulker).night$getCurrentProgress();
        // Alek FCMPL + IFLE rejects zero/negative values, but accepts NaN.
        if (progress <= 0.0F) {
            return false;
        }
        return mc.player.getBoundingBox().inflate(0.7D, 1.0D, 0.7D)
                .intersects(new AABB(shulker.getBlockPos()));
    }
}
