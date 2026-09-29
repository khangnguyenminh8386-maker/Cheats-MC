package night.modules.impl.movement;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.PacketSendEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.utils.EarlyTickHooks;

@RegisterModule(name = "AirStuck", description = "Suspends movement in the air by freezing movement packets.",
        category = Module.Category.MOVEMENT)
public class AirStuckModule extends Module {
    public final BooleanSetting onlyWhenFalling = new BooleanSetting(
            "Only When Falling", "Only captures velocity while falling.", false);
    private Vec3 savedVelocity = Vec3.ZERO;
    private final Runnable earlyTickCallback = this::onEarlyTick;

    @Override
    public void onEnable() {
        EarlyTickHooks.register(this.earlyTickCallback);
    }

    @Override
    public void onDisable() {
        EarlyTickHooks.unregister(this.earlyTickCallback);
        if (!this.getNull() && !this.savedVelocity.equals(Vec3.ZERO)) {
            mc.player.setDeltaMovement(this.savedVelocity);
        }
        // Missing-world/player disable must not leave a stale capture for a new player.
        this.savedVelocity = Vec3.ZERO;
    }

    private void onEarlyTick() {
        if (!this.isToggled() || this.getNull()) {
            return;
        }
        if (!this.onlyWhenFalling.getValue() || !(mc.player.fallDistance <= 0.0D)) {
            if (this.savedVelocity.equals(Vec3.ZERO) && !mc.player.isSprinting()) {
                this.savedVelocity = mc.player.getDeltaMovement();
            }
        }
    }

    private boolean isActive() {
        return this.isToggled() && !this.getNull() && !this.savedVelocity.equals(Vec3.ZERO);
    }

    @SubscribeEvent(priority = Integer.MAX_VALUE)
    public void onPacketSend(PacketSendEvent event) {
        if (!this.isActive()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundClientTickEndPacket
                || packet instanceof ServerboundPlayerCommandPacket
                || packet instanceof ServerboundMovePlayerPacket) {
            event.setCancelled(true);
        }
    }

    private static AirStuckModule instance() {
        return Night.MODULE_MANAGER == null ? null : Night.MODULE_MANAGER.getModule(AirStuckModule.class);
    }

    public static Vec3 movementDisplacement(Vec3 original) {
        AirStuckModule module = instance();
        return module != null && module.isActive() ? new Vec3(0.0D, -1.0E-10D, 0.0D) : original;
    }

    public static void localPlayerConstructed() {
        AirStuckModule module = instance();
        if (module != null && module.isToggled()) {
            module.setToggled(false, false);
        }
    }
}
